package cmdai;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class Options {
	
	public static final KeyMapping keyCommand =
			new KeyMapping("Open Mod Command Terminal", GLFW.GLFW_KEY_RIGHT_ALT, "CMD AI");
	public static final KeyMapping keyToggleRenderTaskExecutionOverlay = 
			new KeyMapping("Toggle Task Execution Overlay", GLFW.GLFW_KEY_O, "CMD AI");
	public static final KeyMapping keyToggleRenderTaskReportOverlay = 
			new KeyMapping("Toggle Task Report Overlay", GLFW.GLFW_KEY_P, "CMD AI");
	
}
