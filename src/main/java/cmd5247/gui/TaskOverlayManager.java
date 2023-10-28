package cmd5247.gui;

import org.lwjgl.glfw.GLFW;

import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;

public class TaskOverlayManager {
	
	private static boolean renderDebugPrev = false;
	private static boolean renderDebugChartsPrev = false;
	private static boolean renderFpsChartPrev = false;
	
	public static void clientSetup() {
		MinecraftForge.EVENT_BUS.addListener(TaskOverlayManager::keyPress);
	}
	
	/** 
	 * Toggles the rendering of the Task Execution overlay. The KeyboardManager checks for the
	 * regular debug toggle key (F3) on every key event. This is also where KeyInputEvents are
	 * fired, so this is as close as we can get to interfering with the F3 action (since F3 is
	 * not a registered KeyMapping and there are no Forge events associated with it).
	 */
	public static void keyPress(Key event) {
		// var game = Minecraft.getInstance();
		// if (game.screen != null && !game.screen.passEvents) return;
		
		// TaskManager.push("inputs");
		
		// boolean vanillaOptionsChanged = updateVanillaOptionsAndDetectChanges();
		
		// if (Options.keyToggleRenderTaskExecutionOverlay.consumeClick()) {
		// 	toggle(TaskExecutionOverlay.TASK_EXECUTION_ELEMENT);
		// } else if (Options.keyToggleRenderTaskReportOverlay.consumeClick()) {
		// 	boolean rendering =  toggle(TaskReportOverlay.TASK_REPORT_ELEMENT);
		// 	/* The vanilla options renderDebug and renderDebugCharts need to be on in order for the
		// 	 * profiler to be active (see Minecraft.shouldRenderFpsPie). */
		// 	game.options.renderDebug = game.options.renderDebugCharts =
		// 			Options.profileDuringTaskReport = rendering && Screen.hasShiftDown();
		// } else if (event.getKey() == GLFW.GLFW_KEY_F3 && event.getAction() == GLFW.GLFW_RELEASE &&
		// 		anyTaskOverlayIsActive() && vanillaOptionsChanged) {
		// 	/* If a Task overlay is open when the player opens the vanilla debug screen, it should
		// 	 * be closed and potentially broken vanilla options need to be repaired. */
		// 	OverlayRegistry.enableOverlay(TaskExecutionOverlay.TASK_EXECUTION_ELEMENT, false);
		// 	OverlayRegistry.enableOverlay(TaskReportOverlay.TASK_REPORT_ELEMENT, false);
		// 	OverlayRegistry.enableOverlay(ForgeIngameGui.HUD_TEXT_ELEMENT, true);
		// 	OverlayRegistry.enableOverlay(ForgeIngameGui.FPS_GRAPH_ELEMENT, true);
		// 	renderDebugPrev = game.options.renderDebug = true;
        //     renderDebugChartsPrev = game.options.renderDebugCharts = Screen.hasShiftDown();
        //     renderFpsChartPrev = game.options.renderFpsChart = Screen.hasAltDown();
		// }
		
		// TaskManager.pop();
	}
	
	// /* TODO:
	//  * The pie will flash when turning off the Report overlay
	//  * The pie will become visible when the Report overlay is on and no task is active
	//  *     (but this is a unique feature and might be cool to leave in)
	//  */
	// @SuppressWarnings("resource")
	// private static boolean toggle(IGuiOverlay overlay) {
	// 	boolean rendering = OverlayRegistry.getEntry(overlay).isEnabled();
	// 	OverlayRegistry.enableOverlay(overlay, !rendering);
		
	// 	/* The vanilla debug overlays will visually conflict with the Task overlays. The vanilla
	// 	 * overlays are automatically rendered when the profiler is active, so they are disabled
	// 	 * whenever any of the Task overlays are enabled to avoid conflict.
	// 	 * 
	// 	 * Note: unlike the text columns and FPS graph, the profiler pie is not a registered
	// 	 * overlay and is outside the influence of Forge events. Its rendering happens at the end
	// 	 * of the tick and is triggered by Minecraft.fpsPieResults being non-null. Therefore,
	// 	 * overlays using the profiler results are responsible for setting fpsPieResults to null
	// 	 * in order to prevent pie rendering.
	// 	 *     fpsPieResults is generated before the tick starts, so if options are set to enable
	// 	 * the profiler, the results will not be available until the next tick. As a result,
	// 	 * fpsPieResults may be null for one tick while overlays which use them are active. Forge
	// 	 * will handle RuntimeExceptions during the rendering of registered overlays gracefully,
	// 	 * but it would be better to add a check in the render method. */
	// 	boolean anyRendering = anyTaskOverlayIsActive();
	// 	OverlayRegistry.enableOverlay(ForgeIngameGui.HUD_TEXT_ELEMENT, !anyRendering);
	// 	OverlayRegistry.enableOverlay(ForgeIngameGui.FPS_GRAPH_ELEMENT, !anyRendering);
	// 	// turn off all of the vanilla debug options by default
	// 	if (anyRendering) {
	// 		if (!OverlayRegistry.getEntry(TaskReportOverlay.TASK_REPORT_ELEMENT).isEnabled()) {
	// 			Minecraft.getInstance().options.renderDebug = false;
	// 			Minecraft.getInstance().options.renderDebugCharts = false;
	// 		}
	// 		Minecraft.getInstance().options.renderFpsChart = false;
	// 	}
		
	// 	return !rendering;
	// }
	
	// private static boolean anyTaskOverlayIsActive() {
	// 	return OverlayRegistry.getEntry(TaskExecutionOverlay.TASK_EXECUTION_ELEMENT).isEnabled()
	// 		|| OverlayRegistry.getEntry(TaskReportOverlay.TASK_REPORT_ELEMENT).isEnabled();
	// }
	
	// @SuppressWarnings("resource")
	// private static boolean updateVanillaOptionsAndDetectChanges() {
	// 	var options = Minecraft.getInstance().options;
		
	// 	boolean changed = (renderDebugPrev != options.renderDebug) ||
	// 			(renderDebugChartsPrev != options.renderDebugCharts) ||
	// 			(renderFpsChartPrev != options.renderFpsChart);
		
	// 	renderDebugPrev = options.renderDebug;
	// 	renderDebugChartsPrev = options.renderDebugCharts;
	// 	renderFpsChartPrev = options.renderFpsChart;
		
	// 	return changed;
	// }
	
}
