package cmd5247.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import cmd5247.Util;
import cmd5247.task.TaskManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class StopCommand implements Command<CommandSourceStack> {
	
	/** Registers the $stop command. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("stop").executes(new StopCommand()));
	}
	
	/** Cancels the currently active task, if any. */
	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		if (TaskManager.getActiveTask().isPresent()) {
			Util.msg("Stopped task \"" + TaskManager.getActiveTask().get().name() + "\"");
			TaskManager.stopActiveTask();
		} else Util.msg("No tasks to stop!");
		
		return Command.SINGLE_SUCCESS;
	}
	
}
