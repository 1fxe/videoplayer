package dev.fxe.videoplayer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.jcodec.api.JCodecException;
import scala.actors.threadpool.Executors;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class VideoCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "vp";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + this.getCommandName();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 1) {
			String arg = args[0].toLowerCase(Locale.ROOT);
			switch (arg) {
				case "delete":
					VideoPlayer.video.delete();
					break;
				case "play":
					VideoPlayer.running = true;
					break;
				case "pause":
					VideoPlayer.running = false;
					break;
			}
		} else if (args.length == 2) {
			String arg = args[0].toLowerCase(Locale.ROOT);
			String file = args[1].toLowerCase(Locale.ROOT);
			if (arg.equals("load")) {
				VideoPlayer.video.delete(); // free up resources
				// ok
				new Thread(() -> {
					try {
						VideoPlayer.video = new Video(new File("/home/f1fxe/Downloads/videoplayer/" + file));
					} catch (IOException | JCodecException e) {
						e.printStackTrace();
					}
				}).start();

			}
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return -1;
	}
}
