package cmdai;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class Util {

	@SuppressWarnings("resource")
	public static BlockPos pbpos() {
		return Minecraft.getInstance().player.blockPosition();
	}
	
	{
		
		/*
		 * 
		 * Up next:
		 * - change task profiler to actually show task profile instead of everything
		 * - remove all of the old custom profiler stuff if the above step works
		 * - should the fishing tally be working?
		 * - check through all files, then ready to move on to mining
		 * 
		 */
		
	}
	
	/** Adapted from the KeyboardHandler. */
	@Deprecated
	@SuppressWarnings("resource")
	public static void debugFeedback(Component component) {
		var msg = new TextComponent("")
				.append(new TextComponent("[CMD AI]:")
						.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
				.append(" ").append(component);
		Minecraft.getInstance().player.sendMessage(msg, net.minecraft.Util.NIL_UUID);
	}

}
