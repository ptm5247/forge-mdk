package cmd5247.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import cmd5247.CMD;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

class StopCommand implements Command<CommandSourceStack> {
	
	/** Registers the $stop command. */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("stop").executes(new StopCommand()));
	}
	
	/** Cancels the currently active task, if any. */
	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		CMD.getInstance().taskManager.stopActiveTask();
		return Command.SINGLE_SUCCESS;
	}
	
}
