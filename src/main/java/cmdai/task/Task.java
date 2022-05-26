package cmdai.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Task(String name, Map<String, Object> env, BaseInstruction head) {
	
	static record DebugLine(String line, int indentation, boolean active) {}
	
	public static Task compile(String name, BaseInstruction...instructions) {
		for (int i = 0; i < instructions.length; i++) {
			if (i > 0)
				instructions[i].prev = instructions[i - 1];
			if (i < instructions.length - 1)
				instructions[i].next = instructions[i + 1];
		}
		
		return new Task(name, new HashMap<>(), instructions[0]);
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
	
	public static BaseInstruction LABEL(String label) {
		return new Instructions.Label(label);
	}
	
	public static BaseInstruction STOP() {
		return new Instructions.Stop();
	}
	
//	List<String> debug() {
//		var overlay = new ArrayList<String>();
//		var builder = new StringBuilder(256);
//		int indentation = 0;
//		boolean prev = true;
//		
//		overlay.add(name);
//		for (var ptr = head; ptr != null; prev = ptr.complete, ptr = ptr.next) {
//			if (ptr instanceof Instructions.Indentation indent) {
//				indentation += indent.distance;
//				continue;
//			}
//			
//			builder.append(prev && !ptr.complete ? "-->" : "    ");
//			for (int i = 0; i < indentation; i++) builder.append("    ");
//			
//			ptr.debug(builder);
//			overlay.add(builder.toString());
//			builder.delete(0, 256);
//		}
//		
//		return overlay;
//	}
	
}
