package cmdai.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
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

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

import cmdai.task.Task;
import cmdai.task.TaskManager;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;

abstract class Command implements com.mojang.brigadier.Command<CommandSourceStack> {
	
	private Task task;
	protected Minecraft minecraft;
	protected Player player;
	
	protected void setTask(Task task) {
		this.task = task;
	}
	
	abstract void performChecks(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException;
	
	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		this.minecraft = Minecraft.getInstance();
		try {
			// cannot use getPlayerOrException because it looks for a ServerPlayer
			this.player = (Player) context.getSource().getEntityOrException();
		} catch (ClassCastException e) {
			throw CommandSourceStack.ERROR_NOT_PLAYER.create();
		}
		performChecks(context);
		TaskManager.start(task);
		
		return Command.SINGLE_SUCCESS;
	}
	
	protected void equipBestTool(Item tool, Enchantment[] precedence) {
		var inv = player.getInventory();
		var tools = new Int2ObjectLinkedOpenHashMap<ItemStack>();
		
		for (int i = 0; i < Inventory.SLOT_OFFHAND + 1; i++) {
			var item = inv.getItem(i);
			if (item.is(tool)) tools.put(i, item);
		}
		
		for (var ench : precedence) {
			if (tools.size() == 1) break;
			
			long onehot = (0b1L << tools.size()) - 0b1L;
			int i = 0, maxLevel = 0;
			
			for (var entry : tools.int2ObjectEntrySet()) {
				int level = EnchantmentHelper.getItemEnchantmentLevel(ench, entry.getValue());
				
				if (level > maxLevel) {
					maxLevel = level;
					onehot = 0b1L << i;
				} else if (level == maxLevel) {
					onehot |= 0b0L << i;
				}
				
				i += 1;
			}
			
			var iter = tools.int2ObjectEntrySet().fastIterator();
			
			for (iter.previous(); i-- > 0; iter.previous())
				if ((onehot & (0b1L << i)) == 0)
					iter.remove();
		}
		
		int ind = tools.firstIntKey();
		
		if (Inventory.isHotbarSlot(ind))
			inv.selected = ind;
		else if (ind < Inventory.INVENTORY_SIZE)
			minecraft.gameMode.handlePickItem(ind);
		else 
			minecraft.getConnection().send(new ServerboundPlayerActionPacket(
					Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
	}
	
	/** 
	 * Stops at durability 2 since the check may occur before the last durability reduction has
	 * been applied, depending on the structure of the task. This also assumes that the next action
	 * will not use more than 1 durability.
	 */
	protected boolean toolWillBreak(PlayerTickEvent event) {
		var tool = event.player.getMainHandItem();
		return tool.getDamageValue() + 2 >= tool.getMaxDamage();
	}
	
}
