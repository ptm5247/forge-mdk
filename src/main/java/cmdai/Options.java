package cmdai;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class Options {
	
	public static final KeyMapping keyCommand =
			new KeyMapping("Open Mod Command Terminal", GLFW.GLFW_KEY_RIGHT_ALT, "CMD AI");
	public static final KeyMapping keyToggleRenderOverlay = 
			new KeyMapping("Toggle Task Manager Overlay", GLFW.GLFW_KEY_O, "CMD AI");
	
	public static boolean renderOverlay = false;

}
