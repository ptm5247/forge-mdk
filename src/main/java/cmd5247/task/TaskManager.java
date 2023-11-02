package cmd5247.task;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import cmd5247.gui.components.TaskManagerOverlay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;

public class TaskManager {
	
	private Optional<Task> activeTask = Optional.empty();
	private long activeStart;
  private List<TickInstruction> listeners = new ArrayList<>();

  public void onRegisterGuiOverlaysEvent(RegisterGuiOverlaysEvent event) {
    event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "task_manager", new TaskManagerOverlay());
  }

  public boolean isBusy() {
    return activeTask.isPresent();
  }

  public Task getAactiveTask() throws NoSuchElementException {
    return activeTask.get();
  }

  public void register(TickInstruction listener) {
    listeners.add(listener);
  }

  public void unregister(TickInstruction listener) {
    listeners.remove(listener);
  }

  public void tick() {
    for (TickInstruction instruction : listeners) instruction.tick();
  }
	
	/** Starts the given task to active and throws a CommandRuntimeException if busy. */
	public void start(Task task) throws CommandRuntimeException {
		if (activeTask.isEmpty()) {
			activeTask = Optional.of(task);
			activeStart = System.currentTimeMillis();
      task.reporter().reset();
      MinecraftForge.EVENT_BUS.register(task.reporter());
			task.start();
		} else {
      var message = String.format("Task \"%s\" is already running! Stop it with $stop", activeTask.get().name());
      throw new CommandRuntimeException(Component.literal(message).withStyle(ChatFormatting.RED));
    }
	}
	
	/** Stops the currently active task, if any. */
  @SuppressWarnings("resource")
	public void stopActiveTask() throws CommandRuntimeException {
    if (activeTask.isEmpty()) {
      var message = "No task to stop.";
      throw new CommandRuntimeException(Component.literal(message).withStyle(ChatFormatting.RED));
    } else {
      var message = String.format("Stopping task \"%s\"", activeTask.get().name());
      Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
      activeTask.get().stop();
      MinecraftForge.EVENT_BUS.unregister(activeTask.get().reporter());
      Minecraft.getInstance().player.sendSystemMessage(activeTask.get().reporter().report());
		  activeTask = Optional.empty();
      listeners.clear();
    }
	}
	
	public String getActiveElapsed() {
		long millis = System.currentTimeMillis() - activeStart;
		
		int h = (int) (millis / 3600e3);
		millis %= 3600e3;
		int m = (int) (millis / 60e3);
		millis %= 60e3;
		int s = (int) (millis / 1e3);
		millis %= 1e3;
		int t = (int) (millis / 100);
		
		if (h == 0 && m == 0) return String.format("%d.%d", s, t);
		if (h == 0)	return String.format("%d:%02d.%d", m, s, t);
		else return String.format("%d:%02d:%02d.%d", h, m, s, t);
	}
	
}
