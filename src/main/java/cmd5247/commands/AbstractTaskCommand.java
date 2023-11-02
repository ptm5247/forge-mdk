package cmd5247.commands;

import java.util.ArrayList;
import java.util.function.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cmd5247.CMD;
import cmd5247.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

abstract class AbstractTaskCommand implements Command<CommandSourceStack> {
	
	private Task task;
	protected Minecraft minecraft;
	protected Player player;
	
	protected void setTask(Task task) {
		this.task = task;
	}
	
	abstract void performChecks(CommandContext<CommandSourceStack> context) throws CommandRuntimeException;
	
	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException, CommandRuntimeException {
		this.minecraft = Minecraft.getInstance();
		try {
			// cannot use getPlayerOrException because it looks for a ServerPlayer
			this.player = (Player) context.getSource().getEntityOrException();
		} catch (ClassCastException e) {
			throw CommandSourceStack.ERROR_NOT_PLAYER.create();
		}
		performChecks(context);

		CMD.getInstance().taskManager.start(task);
		
		return AbstractTaskCommand.SINGLE_SUCCESS;
	}
	
	/** 
	 * Equips the best version of the specified tool based on the specified enchantment precedence.
	 * Ties are broken arbitrarily.
	 */
  @SuppressWarnings("deprecation")
	protected boolean equipBestTool(Item tool, Enchantment[] precedence) {
		var inv = player.getInventory();
		var tools = new ArrayList<ItemStack>();
		
		for (int i = 0; i <= Inventory.SLOT_OFFHAND; i++) {
			var item = inv.getItem(i);
			if (item.is(tool)) tools.add(item);
		}
		
		tools.removeIf(t -> t.getMaxDamage() - t.getDamageValue() < 2);
		sortItemsByAttribute(tools, t -> t.getMaxDamage() - t.getDamageValue());
		for (var ench : precedence)
			sortItemsByAttribute(tools, t -> EnchantmentHelper.getItemEnchantmentLevel(ench, t));
		
		if (tools.isEmpty()) return false;
		
		int ind = 0;
		while (inv.getItem(ind) != tools.get(0)) ind += 1;
		if (Inventory.isHotbarSlot(ind))
			inv.selected = ind;
		else if (ind < Inventory.INVENTORY_SIZE)
			minecraft.gameMode.handlePickItem(ind);
		else 
			minecraft.getConnection().send(new ServerboundPlayerActionPacket(
					Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
		
		return true;
	}
	
	private static void sortItemsByAttribute(
			ArrayList<ItemStack> items, Function<ItemStack, Integer> evaluator) {
		long onehot = 0L;
		int i = 0, maxLevel = 0;
		
		for (var item : items) {
			int level = evaluator.apply(item);
			
			if (level > maxLevel) {
				maxLevel = level;
				onehot = 0b1L << i;
			} else if (level == maxLevel) {
				onehot |= 0b1L << i;
			}
			
			i += 1;
		}
		
		while (i-- > 0)	if ((onehot & (0b1L << i)) == 0) items.remove(i);
	}
	
	/** 
	 * Stops at durability 2 since the check may occur before the last durability reduction has
	 * been applied, depending on the structure of the task. This also assumes that the next action
	 * will not use more than 1 durability.
	 */
	protected boolean toolWillBreak() {
		var tool = player.getMainHandItem();
		return tool.getDamageValue() + 2 >= tool.getMaxDamage();
	}
	
}
