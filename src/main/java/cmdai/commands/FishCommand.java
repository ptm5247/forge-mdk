package cmdai.commands;

import static cmdai.task.Task.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

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
		if (!player.getInventory().contains(new ItemStack(Items.FISHING_ROD)))
			throw ERROR_NO_ROD.cmd();
	}
	
	/**
	 * Gives access to player.fishing.biting without access transformers. Data 0-7 are defined in
	 * Entity, and 8 is FishingHook.hookedIn, which is an entity hooked while not fishing.
	 */
	private static final EntityDataAccessor<Boolean> DATA_BITING =
			EntityDataSerializers.BOOLEAN.createAccessor(9);
	
	private boolean isBiting(PlayerTickEvent event) {
		var hook = event.player.fishing;
		return hook == null ? false : hook.getEntityData().get(DATA_BITING);
	}
	
	private void equipBestFishingRod() {
		equipBestTool(Items.FISHING_ROD, new Enchantment[] {
				Enchantments.MENDING, Enchantments.FISHING_LUCK,
				Enchantments.UNBREAKING, Enchantments.FISHING_SPEED
		});
	}
	
	private FishCommand() {
		setTask(compile("Fish",
			$(this::equipBestFishingRod),
			FORK("move"),
			
			LABEL("fish"),
			
			LABEL("stop"),
			STOP()
		));
	}
	
}
