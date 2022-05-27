package cmdai.task;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class Inputs {
	
	public static final KeyMapping
		USE = Minecraft.getInstance().options.keyUse,
		UP = Minecraft.getInstance().options.keyUp,
		DOWN = Minecraft.getInstance().options.keyDown;
	
	public static Runnable click(KeyMapping key) {
		return () -> KeyMapping.click(key.getKey());
	}
	
	public static Runnable press(KeyMapping key) {
		return () -> key.setDown(true);
	}
	
	public static Runnable release(KeyMapping key) {
		return () -> key.setDown(false);
	}

}
