package cmdai;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class Options {
	
	public static final KeyMapping keyCommand =
			new KeyMapping("key.modcommand", GLFW.GLFW_KEY_RIGHT_ALT, KeyMapping.CATEGORY_MULTIPLAYER);
	public static final KeyMapping keyToggleRenderOverlay = 
			new KeyMapping("key.toggleoverlay", GLFW.GLFW_KEY_O, KeyMapping.CATEGORY_INTERFACE);
	
	public static boolean renderOverlay = false;

}
