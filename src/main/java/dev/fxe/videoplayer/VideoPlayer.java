package dev.fxe.videoplayer;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jcodec.api.JCodecException;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
@Mod(modid = VideoPlayer.MODID, version = VideoPlayer.VERSION)
public class VideoPlayer {
	public static final String MODID = "video-player";
	public static final String VERSION = "1.0";
	public static Video video;

	static {
		try {
			VideoPlayer.video = new Video(new File("/home/f1fxe/Downloads/coloured-redstone/sample.mp4"));
		} catch (IOException | JCodecException e) {
			e.printStackTrace();
		}
	}

	public static boolean running = false;

	private int frame = -1;
	private int ticks = 0;


	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		ClientCommandHandler.instance.registerCommand(new VideoCommand());
	}

	@SubscribeEvent
	public void render(RenderGameOverlayEvent.Post event) {
		if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) {
			return;
		}
		if (!VideoPlayer.running) {
			return;
		}
		this.ticks++;
		if (this.frame == -1 || this.frame > VideoPlayer.video.frameCount - 1) {
			this.frame = 0;
		}
		VideoPlayer.video.update(this.frame);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, VideoPlayer.video.textureId);
		Gui.drawModalRectWithCustomSizedTexture(20, 20, 0, 0, 480, 270, 480, 270);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		if (this.ticks % (int) VideoPlayer.video.frameRate == 0) {
			this.frame++;
			this.ticks = 0;
		}
	}
}
