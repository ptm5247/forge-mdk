package cmd5247.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class TaskExecutionOverlay implements IGuiOverlay {
	
	static TaskExecutionOverlay TASK_EXECUTION_ELEMENT;
	
	private TaskExecutionOverlay() {}

	public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "task_execution", new TaskExecutionOverlay());
	}

	/**
	 * Renders the Task Manager overlay, which is now separate from the normal debug overlay.
	 * Adapted from {@link ForgeIngameGui#renderHUDText}
	 */
	@Override
	@SuppressWarnings("resource")
	public void render(ForgeGui gui, GuiGraphics graphics,
			float partialTick, int width, int height) {
		var task = TaskManager.getActiveTask();
		if (task.isEmpty()) return;
		
		TaskManager.push("execution overlay");
		RenderSystem.defaultBlendFunc();
		
		int y1 = 1;
		var font = Minecraft.getInstance().font;
		int indw = font.width("--");
		
		for (var msg : task.get().debug()) {
			if (!msg.line().isEmpty()) {
				int x1 = 1 + msg.indentation() * indw;
				int x2 = font.width(msg.line()) + x1 + 2;
				int y2 = y1 + font.lineHeight;
				
				graphics.fill(x1, y1, x2, y2, 0x90505050);
				graphics.drawString(Minecraft.getInstance().font, msg.line(), x1 + 1, y1 + 1, msg.active() ? 0xE0E0E0 : 0xB0B0B0);
			}
			
			y1 += font.lineHeight;
		}
		
		TaskManager.pop();
	}

}
