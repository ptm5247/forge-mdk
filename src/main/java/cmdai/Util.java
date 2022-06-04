package cmdai;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class Util {
	
	@SuppressWarnings("resource")
	public static void msg(Component component) {
		Minecraft.getInstance().player.sendMessage(component, net.minecraft.Util.NIL_UUID);
	}
	
	public static void msg(String message) {
		msg(new TextComponent(message));
	}

	@SuppressWarnings("resource")
	public static BlockPos pbpos() {
		return Minecraft.getInstance().player.blockPosition();
	}
	
	{
		
		/*
		 * 
		 * Up next:
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
