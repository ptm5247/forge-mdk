package cmdai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

public record Task(String name, TaskEnvironment env, BaseInstruction head) {
	
	static record DebugLine(String line, int indentation, boolean active) {}
	
	public static Task compile(String name, BaseInstruction...instructions) {
		for (int i = 0; i < instructions.length; i++) {
			if (i > 0)
				instructions[i].prev = instructions[i - 1];
			if (i < instructions.length - 1)
				instructions[i].next = instructions[i + 1];
		}
		
		return new Task(name, new TaskEnvironment(), instructions[0]);
	}
	
	void start() {
		for (var ptr = head; ptr != null; ptr = ptr.next)
			ptr.complete = false;
		head.process();
	}
	
	void stop() {
		head.cancel(null);
	}
	
	List<DebugLine> debug() {
		var overlay = new ArrayList<DebugLine>();
		int indentation = 0;
		boolean prev = true;
		
		overlay.add(new DebugLine("Task :" + name, 0, true));
		for (var ptr = head; ptr != null; prev = ptr.complete, ptr = ptr.next)
			if (ptr instanceof Instructions.Indentation indent)
				indentation += indent.distance;
			else
				overlay.add(new DebugLine(ptr.toString(), indentation, prev && !ptr.complete));
		
		return overlay;
	}
	
	public static BaseInstruction $(Runnable action) {
		return new Instructions.BaseAction(action);
	}
	
	public static TickInstruction $(Predicate<PlayerTickEvent> trigger, Runnable action) {
		return new Instructions.TickAction(trigger, action);
	}
	
	public static TickInstruction AFTER(int ticks, Runnable action) {
		return $(new Predicates.Counter(ticks), action);
	}
	
	public static BaseInstruction FORK(String label) {
		return new Instructions.Fork(label);
	}
	
	public static BaseInstruction GOTO(String label, Predicate<PlayerTickEvent> condition) {
		return new Instructions.Goto(label, condition);
	}
	
	public static BaseInstruction LABEL(String label) {
		return new Instructions.Label(label);
	}
	
	public static BaseInstruction LOOP(String label) {
		return new Instructions.Loop(label, Predicates.NOW);
	}
	
	public static BaseInstruction NOOP() {
		return $(() -> {});
	}
	
	public static BaseInstruction STOP() {
		return new Instructions.Stop();
	}
	
	public static BaseInstruction T(int distance) {
		return new Instructions.Indentation(distance);
	}
	
	public static BaseInstruction TRY(int ticks, TickInstruction tryInstruction) {
		return new Instructions.Try(ticks, tryInstruction, NOOP());
	}
	
	public static BaseInstruction WATCH(Supplier<Object> target, Runnable action) {
		return $(new Predicates.Observer(target), action); 
	}
	
}
