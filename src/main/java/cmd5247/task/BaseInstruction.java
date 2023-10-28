package cmd5247.task;

import java.util.Optional;

import cmd5247.task.Instructions.Action;

public abstract class BaseInstruction {
	
	protected BaseInstruction prev, next;
	protected boolean complete;
	protected Optional<String> comment = Optional.empty();
	
	public BaseInstruction comment(String comment) {
		this.comment = Optional.of(comment);
		return this;
	}
	
	protected void process() {
		if (this instanceof Action action) action.run();
		complete = true;
		next.process();
	}
	
	protected void cancel() {
		complete = true;
		next.cancel();
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
