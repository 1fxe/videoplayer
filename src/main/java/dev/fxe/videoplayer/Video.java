package dev.fxe.videoplayer;

import com.google.common.primitives.Bytes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Video {
	private final List<BufferedImage> images = new ArrayList<>();
	private final HashMap<BufferedImage, byte[]> pixelCache = new HashMap<>();
	public double frameRate;
	public int width, height, frameCount;
	public int textureId;

	private final int[] pbo = new int[2];
	private int index;

	public Video(File file) throws IOException, JCodecException {
		this.index = 0;
		FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
		DemuxerTrack vt = grab.getVideoTrack();
		this.frameCount = vt.getMeta().getTotalFrames();
		this.frameRate = this.frameCount / vt.getMeta().getTotalDuration();
		Picture picture;
		while (null != (picture = grab.getNativeFrame())) {
			this.images.add(AWTUtil.toBufferedImage(picture));
		}
		this.width = this.images.get(0).getWidth();
		this.height = this.images.get(0).getHeight();

		for (BufferedImage image : this.images) {
			List<Byte> bytes = new ArrayList<>(image.getHeight() * image.getWidth() * 4);
			int[] pixels = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = pixels[y * image.getWidth() + x];
					bytes.add((byte) ((pixel >> 16) & 0xFF));
					bytes.add((byte) ((pixel >> 8) & 0xFF));
					bytes.add((byte) (pixel & 0xFF));
					bytes.add((byte) ((pixel >> 24) & 0xFF));
				}
			}
			this.pixelCache.put(image, Bytes.toArray(bytes));
		}

		Minecraft.getMinecraft().addScheduledTask(this::load);
	}

	public void delete() {
		VideoPlayer.running = false;
		GL11.glDeleteTextures(this.textureId);
		GL15.glDeleteBuffers(this.pbo[0]);
		GL15.glDeleteBuffers(this.pbo[1]);
	}

	private void load() {
		// Generate texure
		this.textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		int size = this.width * this.height * 4;

		int pbo1 = GL15.glGenBuffers();
		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pbo1);
		GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, size, GL15.GL_STREAM_DRAW);
		int pbo2 = GL15.glGenBuffers();
		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pbo2);
		GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, size, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

		this.pbo[0] = pbo1;
		this.pbo[1] = pbo2;

		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
			Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Uploaded to opengl"));
			VideoPlayer.running = true;
		}
	}

	public void update(int frame) {
		int size = this.width * this.height * 4;
		int nextIndex;

		this.index = (this.index + 1) % 2;
		nextIndex = (this.index + 1) % 2;

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, this.pbo[this.index]);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);

		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, this.pbo[nextIndex]);
		GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, size, GL15.GL_STREAM_DRAW);

		ByteBuffer buffer = GL15.glMapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_WRITE_ONLY, null);

		if (buffer != null) {
			this.updatePixels(buffer, frame);
			GL15.glUnmapBuffer(GL21.GL_PIXEL_UNPACK_BUFFER);
		}


		GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
	}

	private void updatePixels(ByteBuffer buffer, int frame) {
		BufferedImage image = this.images.get(frame);
		byte[] pixels = this.pixelCache.get(image);
		buffer.clear();
		buffer.put(pixels);
		buffer.flip();
	}
}
