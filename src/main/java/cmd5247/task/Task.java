package cmd5247.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

import cmd5247.task.report.ReportManager;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

public record Task(String name, TaskEnvironment env, ReportManager reporter, BaseInstruction head) {
	
	public static Task compile(String name, BaseInstruction...instructions) {
		Task task = new Task(name, new TaskEnvironment(), new ReportManager(), instructions[0]);
		instructions = ArrayUtils.addAll(instructions, L(), LABEL("stop"), T(1), STOP());
		
		for (int i = 0; i < instructions.length; i++) {
			if (i > 0)
				instructions[i].prev = instructions[i - 1];
			if (i < instructions.length - 1)
				instructions[i].next = instructions[i + 1];
		}
		
		return task;
	}
	
	void start() {
		for (var ptr = head; ptr != null; ptr = ptr.next)
			ptr.complete = false;
		reporter.start();
		head.process();
	}
	
	void stop() {
		head.cancel();
		reporter.stop();
	}
	
	public static record DebugLine(String line, int indentation, boolean active) {}
	
	public List<DebugLine> debug() {
		var overlay = new ArrayList<DebugLine>();
		int indentation = 0;
		boolean prev = true;
		var header = String.format("Task: %s - %s", name, TaskManager.getActiveElapsed());
		
		overlay.add(new DebugLine(header, 0, true));
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
	
	public static BaseTickInstruction $(Predicate<PlayerTickEvent> trigger, Runnable action) {
		return new Instructions.TickAction(trigger, action);
	}
	
	public static BaseTickInstruction AFTER(int ticks, Runnable action) {
		return $(new Predicates.Counter(ticks), action);
	}
	
	public static BaseInstruction FORK(String label) {
		return new Instructions.Fork(label);
	}
	
	public static BaseInstruction GOTO(String label, Predicate<PlayerTickEvent> condition) {
		return new Instructions.Goto(label, condition);
	}
	
	public static BaseInstruction L() {
		return new Instructions.LineBreak();
	}
	
	public static BaseInstruction LABEL(String label) {
		return new Instructions.Label(label);
	}
	
	public static BaseInstruction LOOP(String label) {
		return LOOP(label, Predicates.NOW);
	}
	
	public static BaseInstruction LOOP(String label, Predicate<PlayerTickEvent> condition) {
		return new Instructions.Loop(label, condition);
	}
	
	public static BaseInstruction NOOP() {
		return $(() -> {});
	}
	
	private static BaseInstruction STOP() {
		return new Instructions.Stop();
	}
	
	public static BaseInstruction T(int distance) {
		return new Instructions.Indentation(distance);
	}
	
	public static BaseInstruction TRY(int ticks, BaseTickInstruction tryInstruction) {
		return new Instructions.Try(ticks, tryInstruction, NOOP());
	}
	
	public static BaseInstruction WATCH(Supplier<Object> target, Runnable action) {
		return $(new Predicates.Observer(target), action); 
	}
	
}
