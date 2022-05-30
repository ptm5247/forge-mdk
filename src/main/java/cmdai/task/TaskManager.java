package cmdai.task;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

import cmdai.Main;
import cmdai.ModException;

public class TaskManager {
	
	private static final String BUSY_MSG = "Task \"%s\" is already running! Stop it with $stop";
	private static ArrayList<Consumer<PlayerTickEvent>> listeners = new ArrayList<>();
	private static Optional<Task> activeTask = Optional.empty();
	private static long activeStart;
//	private static int activeTicks = 0;
//	private static ContinuousProfiler profiler =
//			new ContinuousProfiler(Util.timeSource, () -> activeTicks);
//	private static ProfileResults profileResults = EmptyProfileResults.EMPTY;
	
	/** To be called during FMLClientSetupEvent. */
	public static void clientSetup() {
		MinecraftForge.EVENT_BUS.addListener(TaskManager::forwardTickEvent);
		//MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TaskManager::profilerStart);
		//MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TaskManager::profilerEnd);
	}
	
	public static void push(String str) {
		Minecraft.getInstance().getProfiler().push(Main.MODID);
		Minecraft.getInstance().getProfiler().push(str);
	}
	
	public static void pop() {
		Minecraft.getInstance().getProfiler().pop();
		Minecraft.getInstance().getProfiler().pop();
	}
	
	/** Register a TickInstruction to receive PlayerTickEvents. */
	static void register(Consumer<PlayerTickEvent> listener) {
		listeners.add(listener);
	}
	
//	public static void profilerStart(ClientTickEvent event) {
//		if (event.phase != Phase.START) return;
//
//		if (!Options.renderTaskReportOverlay) {
//			if (profiler.isEnabled())
//				profiler.disable();
//		} else if (!profiler.isEnabled()) {
//			activeTicks = 0;
//			profiler.enable();
//		} else {
//			activeTicks += 1;
//		}
//		
//		//getProfiler().startTick();
//	}
//	
//	public static void profilerEnd(RenderTickEvent event) {
//		if (event.phase != Phase.END) return;
//		
//		//getProfiler().endTick();
//		
//		profileResults = profiler.getResults();
//	}
	
	/** Forwards the hooked PlayerTickEvent to all of the registered listeners. */
	public static void forwardTickEvent(PlayerTickEvent event) {
		if (activeTask.isEmpty()) return;
		
		if (event.side != LogicalSide.CLIENT || event.phase != Phase.END) return;
		
		push("task instructions");
		
		int i = 0;
		while (i < listeners.size()) {
			int prevSize = listeners.size();
			listeners.get(i).accept(event);
			if (prevSize == listeners.size()) i += 1;
		}
		
		pop();
	}
	
	/** Unregister a TickInstruction from receiving PlayerTickEvents. */
	static void unregister(Consumer<PlayerTickEvent> listener) {
		listeners.remove(listener);
	}
	
	/** Starts the given task to active and throws a CommandSyntaxException if busy. */
	public static void start(Task task) throws CommandSyntaxException {
		if (activeTask.isEmpty()) {
			activeTask = Optional.of(task);
			activeStart = System.currentTimeMillis();
			task.start();
		} else throw new ModException(String.format(BUSY_MSG, activeTask.get().name())).cmd();
	}
	
	/** Stops the currently active task. This will cause an error if no tasks are active. */
	public static void stopActiveTask() {
		activeTask.get().stop();
		activeTask = Optional.empty();
	}
	
	public static Optional<Task> getActiveTask() {
		return activeTask;
	}
	
	public static String getActiveElapsed() {
		long millis = System.currentTimeMillis() - activeStart;
		
		int h = (int) (millis / 3600e3);
		millis %= 3600e3;
		int m = (int) (millis / 60e3);
		millis %= 60e3;
		int s = (int) (millis / 1e3);
		millis %= 1e3;
		int t = (int) (millis / 100);
		
		if (h == 0 && m == 0) return String.format("%02d.%d", s, t);
		if (h == 0)	return String.format("%02d:%02d.%d", m, s, t);
		else return String.format("%02d:%02d:%02d.%d", h, m, s, t);
			
	}
	
//	public static ProfilerFiller getProefiler() {
//		return profiler.getFiller();
//	}
//	
//	public static ProfileResults getProfileResults() {
//		return profileResults;
//	}
	
}
