package cmdai.task;

import java.util.Optional;

import cmdai.task.Instructions.Action;

public abstract class BaseInstruction {
	
	protected BaseInstruction prev, next;
	protected boolean complete;
	protected Optional<String> comment = Optional.empty();
	
	void comment(String comment) {
		this.comment = Optional.of(comment);
	}
	
	protected void process() {
		if (this instanceof Action action) action.run();
		complete = true;
		next.process();
	}
	
	protected void cancel(Boolean dir) {
		complete = true;
		
		if (dir != null) {
			var n = dir ? next : prev;
			if (n != null) n.cancel(dir);
		} else {
			if (prev != null) prev.cancel(false);
			if (next != null) next.cancel(true);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof BaseInstruction bi && toString().equals(bi.toString());
	}
	
	@Override
	public String toString() {
		return comment.isPresent() ? comment.get() : super.toString();
	}
	
}
