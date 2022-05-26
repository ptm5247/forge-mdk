package cmdai.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;

import cmdai.task.Task;
import cmdai.task.TaskManager;

abstract class Command implements com.mojang.brigadier.Command<CommandSourceStack> {
	
	private Task task;
	
	protected Command(Task task) {
		this.task = task;
	}
	
	abstract void performChecks(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException;
	
	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		performChecks(context);
		TaskManager.start(task);
		
		return Command.SINGLE_SUCCESS;
	}
	
	/** Retrieves the LocalPlayer from the calling context. */
	static LocalPlayer getLocalPlayerOrException(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		try {
			return (LocalPlayer) context.getSource().getEntityOrException();
		} catch (ClassCastException e) {
			throw CommandSourceStack.ERROR_NOT_PLAYER.create();
		}
	}
	
}
