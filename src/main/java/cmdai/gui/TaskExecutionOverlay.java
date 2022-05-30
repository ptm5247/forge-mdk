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

public class TaskExecutionOverlay implements IIngameOverlay {
	
	static TaskExecutionOverlay TASK_EXECUTION_ELEMENT;
	
	private TaskExecutionOverlay() {}
	
	/** To be called during FMLClientSetupEvent. */
	public static void clientSetup() {
		TASK_EXECUTION_ELEMENT = (TaskExecutionOverlay) OverlayRegistry.registerOverlayAbove(
				ForgeIngameGui.HUD_TEXT_ELEMENT, "Task Execution", new TaskExecutionOverlay());
		OverlayRegistry.enableOverlay(TASK_EXECUTION_ELEMENT, true);
		ClientRegistry.registerKeyBinding(Options.keyToggleRenderTaskExecutionOverlay);
	}
	
	/**
	 * Renders the Task Manager overlay, which is now separate from the normal debug overlay.
	 * Adapted from {@link ForgeIngameGui#renderHUDText}
	 */
	@Override
	@SuppressWarnings("resource")
	public void render(ForgeIngameGui gui, PoseStack poseStack,
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
				
				ForgeIngameGui.fill(poseStack, x1, y1, x2, y2, 0x90505050);
				font.draw(poseStack, msg.line(), x1 + 1, y1 + 1, msg.active() ? 0xE0E0E0 : 0xB0B0B0);
			}
			
			y1 += font.lineHeight;
		}
		
		TaskManager.pop();
	}

}
