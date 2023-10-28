package cmd5247;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class Util {
	
	@SuppressWarnings("resource")
	public static void msg(Component component) {
		Minecraft.getInstance().player.sendSystemMessage(component);
	}
	
	public static void msg(String message) {
		msg(Component.literal(message));
	}

	@SuppressWarnings("resource")
	public static BlockPos pbpos() {
		return Minecraft.getInstance().player.blockPosition();
	}
	
	/** Adapted from the KeyboardHandler. */
	@Deprecated
	@SuppressWarnings("resource")
	public static void debugFeedback(Component component) {
		var msg = Component.empty()
				.append(Component.literal("[CMD AI]:")
						.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
				.append(" ").append(component);
		Minecraft.getInstance().player.sendSystemMessage(msg);
	}

}
