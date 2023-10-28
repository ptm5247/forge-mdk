package cmd5247;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class Options {
	
	public static final KeyMapping keyCommand =
			new KeyMapping("Open Mod Command Terminal", GLFW.GLFW_KEY_RIGHT_ALT, "CMD");
	public static final KeyMapping keyToggleRenderTaskExecutionOverlay = 
			new KeyMapping("Toggle Task Execution Overlay", GLFW.GLFW_KEY_O, "CMD");
	public static final KeyMapping keyToggleRenderTaskReportOverlay = 
			new KeyMapping("Toggle Task Report Overlay", GLFW.GLFW_KEY_P, "CMD");
	
	// This must be false. To set it to true, vanilla settings also need attention here.
	public static boolean profileDuringTaskReport = false;
	
}
