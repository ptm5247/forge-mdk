package cmd5247.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;

import cmd5247.task.TaskManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class TaskReportOverlay implements IGuiOverlay {
	
	static TaskReportOverlay TASK_REPORT_ELEMENT;
	
	private TaskReportOverlay() {}

	public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "task_report", new TaskReportOverlay());
	}

	/** Adapted from {@link ForgeIngameGui#renderHUDText} */
	@Override
	@SuppressWarnings("resource")
	public void render(ForgeGui gui, GuiGraphics graphics,
			float partialTick, int width, int height) {
		var task = TaskManager.getActiveTask();
		if (task.isEmpty()) return;
		
		TaskManager.push("report overlay");
		RenderSystem.defaultBlendFunc();
		
		int y1 = 1;
		var font = Minecraft.getInstance().font;
		
		for (var line : task.get().reporter().generate()) {
			if (!line.isEmpty()) {
				int x2 = width - 1;
				int x1 = x2 - font.width(line) - 1;
				int y2 = y1 + font.lineHeight;

				graphics.fill(x1, y1, x2, y2, 0x90505050);
				graphics.drawString(Minecraft.getInstance().font, line, x1 + 1, y1 + 1, 0xE0E0E0);
			}
			
			y1 += font.lineHeight;
		}
		
		TaskManager.pop();
	}

}
