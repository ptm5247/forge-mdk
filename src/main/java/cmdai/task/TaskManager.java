package cmdai.task;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

import cmdai.ModException;
import cmdai.Options;

public class TaskManager {
	
	private static ArrayList<Consumer<PlayerTickEvent>> listeners = new ArrayList<>();
	private static Optional<Task> activeTask = Optional.empty();
	private static final String ERROR_BUSY = "Task \"%s\" is already running! Stop it with $stop";
	
	/** Register a TickInstruction to receive PlayerTickEvents. */
	static void register(Consumer<PlayerTickEvent> listener) {
		listeners.add(listener);
	}
	
	/** Forwards the hooked PlayerTickEvent to all of the registered listeners. */
	public static void forwardTickEvent(PlayerTickEvent event) {
		if (event.side == LogicalSide.CLIENT && event.phase == Phase.END) {
			int i = 0;
			
			while (i < listeners.size()) {
				int prevSize = listeners.size();
				listeners.get(i).accept(event);
				if (prevSize == listeners.size()) i += 1;
			}
		}
	}
	
	/** Unregister a TickInstruction from receiving PlayerTickEvents. */
	static void unregister(Consumer<PlayerTickEvent> listener) {
		listeners.remove(listener);
	}
	
	/** Starts the given task to active and throws a CommandSyntaxException if busy. */
	public static void start(Task task) throws CommandSyntaxException {
		if (activeTask.isEmpty()) {
			activeTask = Optional.of(task);
			task.start();
		} else throw new ModException(String.format(ERROR_BUSY, activeTask.get().name())).cmd();
	}
	
	static void deactivateTask() {
		/* 
		 * TODO make non-static, generate report through additions to an environment variable
		 * or by a Task.report method which tracks information to be compiled by the STOP
		 * instruction and displayed as a message to the player. If so, ensure that canceling
		 * a task with $stop will display a partially generated report. May require activate.
		 */
		activeTask = Optional.empty();
	}
	
	/** Registers the $stop command. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("stop").executes(TaskManager::stop));
	}
	
	/** The $stop command: cancels the currently active task, if any. */
	@SuppressWarnings("resource")
	private static int stop(CommandContext<CommandSourceStack> context) {
		String msg;
		
		if (activeTask.isPresent()) {
			activeTask.get().stop();
			msg = "Stopped task \"" + activeTask.get().name() + "\"";
		} else msg = "No tasks to stop!";
		
		Minecraft.getInstance().player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}
	
	/** 
	 * Toggles the rendering of the Task Manager overlay.
	 * The KeyboardManager checks for the regular debug toggle key (F3) explicitly on every key
	 * event. This is also where KeyInputEvents are fired, so this check is done here.
	 */
	public static void toggleRenderOverlay(KeyInputEvent event) {
		var game = Minecraft.getInstance();
		
		if (game.screen == null && game.getOverlay() == null) {
			while (Options.keyToggleRenderOverlay.consumeClick()) {
				Options.renderOverlay = !Options.renderOverlay;
				OverlayRegistry.enableOverlay(TaskManager::renderOverlay, Options.renderOverlay);
			}
		}
	}
	
	/**
	 * Implements {@link net.minecraftforge.client.gui.IIngameOverlay}.
	 * Renders the Task Manager overlay, which is now separate from the normal debug overlay.
	 * Adapted from {@link ForgeIngameGui#renderHUDText}
	 */
	@SuppressWarnings("resource")
	public static void renderOverlay(ForgeIngameGui gui, PoseStack pStack, float p, int w, int h) {
		if (activeTask.isEmpty()) return;
		RenderSystem.defaultBlendFunc();
		
		int y1 = 1;
		var font = Minecraft.getInstance().font;
		int indw = font.width("--");
		
		for (var msg : activeTask.get().debug()) {
			int x1 = 1 + msg.indentation() * indw;
			int x2 = font.width(msg.line()) + x1 + 2;
			int y2 = y1 + font.lineHeight;
			ForgeIngameGui.fill(pStack, x1, y1, x2, y2, 0x90505050);
			font.draw(pStack, msg.line(), x1 + 1, y1 + 1, msg.active() ? 0xE0E0E0 : 0xB0B0B0);
			y1 += font.lineHeight;
		}
	}

}
