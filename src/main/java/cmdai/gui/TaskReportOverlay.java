package cmdai.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

import cmdai.Options;
import cmdai.task.TaskManager;

public class TaskReportOverlay implements IIngameOverlay {
	
	static TaskReportOverlay TASK_REPORT_ELEMENT;
	
	private TaskReportOverlay() {}
	
	/** To be called during FMLClientSetupEvent. */
	public static void clientSetup() {
		TASK_REPORT_ELEMENT = (TaskReportOverlay) OverlayRegistry.registerOverlayAbove(
				ForgeIngameGui.HUD_TEXT_ELEMENT, "Task Report", new TaskReportOverlay());
		OverlayRegistry.enableOverlay(TASK_REPORT_ELEMENT, true);
		ClientRegistry.registerKeyBinding(Options.keyToggleRenderTaskReportOverlay);
	}
	
	/** Adapted from {@link ForgeIngameGui#renderHUDText} */
	@Override
	@SuppressWarnings("resource")
	public void render(ForgeIngameGui gui, PoseStack poseStack,
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
				
				ForgeIngameGui.fill(poseStack, x1, y1, x2, y2, 0x90505050);
				font.draw(poseStack, line, x1 + 1, y1 + 1, 0xE0E0E0);
			}
			
			y1 += font.lineHeight;
		}
		
		TaskManager.pop();
	}

}
