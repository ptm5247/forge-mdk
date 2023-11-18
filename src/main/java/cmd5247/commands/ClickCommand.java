package cmd5247.commands;

import static cmd5247.task.Task.AFTER;
import static cmd5247.task.Task.L;
import static cmd5247.task.Task.LABEL;
import static cmd5247.task.Task.LOOP;
import static cmd5247.task.Task.T;
import static cmd5247.task.Task.compile;
import static cmd5247.task.Util.ATTACK;
import static cmd5247.task.Util.click;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

class ClickCommand extends AbstractTaskCommand {
	
	/** Registers the $click command. */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("click").then(Commands.literal("left").then(Commands.argument("delay", IntegerArgumentType.integer(1)).executes(new ClickCommand()))));
	}
	
	@Override
	void performChecks(CommandContext<CommandSourceStack> context) throws CommandRuntimeException {
    int delay = IntegerArgumentType.getInteger(context, "delay") - 1;
    setTask(compile("Click", null,
			L(),
			LABEL("click"),
	    T(1),   AFTER(delay, click(ATTACK)).comment("click"),
	    T(-1),	LOOP("click")
		));
	}
	
}
