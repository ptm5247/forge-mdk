package cmd5247.commands;

import static cmd5247.task.Inputs.DOWN;
import static cmd5247.task.Inputs.UP;
import static cmd5247.task.Inputs.USE;
import static cmd5247.task.Inputs.click;
import static cmd5247.task.Inputs.press;
import static cmd5247.task.Inputs.release;
import static cmd5247.task.Task.$;
import static cmd5247.task.Task.AFTER;
import static cmd5247.task.Task.FORK;
import static cmd5247.task.Task.GOTO;
import static cmd5247.task.Task.L;
import static cmd5247.task.Task.LABEL;
import static cmd5247.task.Task.LOOP;
import static cmd5247.task.Task.T;
import static cmd5247.task.Task.TRY;
import static cmd5247.task.Task.WATCH;
import static cmd5247.task.Task.compile;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cmd5247.ModException;
import cmd5247.Util;
import cmd5247.task.report.ReportGenerators;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FishCommand extends AbstractTaskCommand {
	
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
	
	/** returns true upon failure. */
	private boolean equipBestFishingRod(PlayerTickEvent event) {
		return !equipBestTool(Items.FISHING_ROD, new Enchantment[] {
				Enchantments.MENDING, Enchantments.FISHING_LUCK,
				Enchantments.UNBREAKING, Enchantments.FISHING_SPEED
		});
	}
	
	private FishCommand() {
		setTask(compile("Fish",
			
			FORK("move"),
			
	L(),	LABEL("equip"),
			GOTO("stop", this::equipBestFishingRod)			.comment("equip best fishing rod"),
			LABEL("fish"),
	T(1),		LOOP("equip", this::toolWillBreak)			.comment("LOOP equip IF rod will break"),
				AFTER(20, click(USE))						.comment("cast line"),
				TRY(700, $(this::isBiting, click(USE))		.comment("reel IF biting")),
	T(-1),	LOOP("fish"),
	L(),
			LABEL("move"),
	T(1),		AFTER(12000, press(DOWN))					.comment("press DOWN"),
				WATCH(Util::pbpos, release(DOWN))			.comment("release DOWN"),
				$(press(UP))								.comment("press UP"),
				WATCH(Util::pbpos, release(UP))				.comment("release UP"),
	T(-1),	LOOP("move")
	
		));
		
		registerReportGenerator(new FishingTally());
	}
	
	private class FishingTally extends ReportGenerators.Tally {
		
		private FishingTally() {
			super("Items Fished:");
		}
		
		@SubscribeEvent
		public void on(ItemFishedEvent event) {
			if (!event.getPlayer().equals(player)) return;
			
			for (var stack : event.getDrops()) {
				var name = stack.getDisplayName().getString();
				add(name.substring(1, name.length() - 1), stack.getCount());
			}
		}
		
	}
	
}