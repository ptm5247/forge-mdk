package cmdai.commands;

import static cmdai.task.Task.LABEL;
import static cmdai.task.Task.STOP;
import static cmdai.task.Task.compile;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import cmdai.ModException;

public class FishCommand extends Command {
	
	public static final ModException ERROR_NO_ROD = new ModException("You must have a fishing rod "
			+ "in your inventory to use this command!");
	
	/** Registers the $fish command. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("fish").executes(new FishCommand()));
	}
	
	@Override
	public void performChecks(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		var inv = Command.getLocalPlayerOrException(context).getInventory();
		
		if (!inv.contains(new ItemStack(Items.FISHING_ROD))) throw ERROR_NO_ROD.cmd();
	}
	
	private FishCommand() {
		super(compile("Fish",
			LABEL("fish"),
			STOP()
		));
	}
	
	/**
	 * Gives access to player.fishing.biting without access transformers. Data 0-7 are defined in
	 * Entity, and 8 is FishingHook.hookedIn, which is an entity hooked while not fishing.
	 */
	private static final EntityDataAccessor<Boolean> DATA_BITING =
			EntityDataSerializers.BOOLEAN.createAccessor(9);
	
}
