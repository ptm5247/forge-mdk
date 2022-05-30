package cmdai.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import cmdai.task.TaskManager;

public class StopCommand implements Command<CommandSourceStack> {
	
	/** Registers the $stop command. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("stop").executes(new StopCommand()));
	}
	
	/** Cancels the currently active task, if any. */
	@Override
	@SuppressWarnings("resource")
	public int run(CommandContext<CommandSourceStack> context) {
		String msg;
		
		if (TaskManager.getActiveTask().isPresent()) {
			msg = "Stopped task \"" + TaskManager.getActiveTask().get().name() + "\"";
			TaskManager.stopActiveTask();
		} else msg = "No tasks to stop!";
		
		Minecraft.getInstance().player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}
	
}
