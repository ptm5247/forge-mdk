package cmd5247.task;

import java.util.Optional;

import cmd5247.CMD;
import net.minecraft.client.KeyMapping;

public class SimpleInstruction {
	
	protected SimpleInstruction prev, next;
	protected boolean complete;
	protected Optional<String> comment = Optional.empty();
  protected Optional<Runnable> action = Optional.empty();
	
	public SimpleInstruction comment(String comment) {
		this.comment = Optional.of(comment);
		return this;
	}

  SimpleInstruction withAction(Runnable other) {
    this.action = Optional.of(other);
    return this;
  }
	
	protected void process() {
		if (action.isPresent()) action.get().run();
		complete = true;
		next.process();
	}
	
	protected void cancel() {
		complete = true;
		next.cancel();
	}

	@Override
	public String toString() {
		return comment.isPresent() ? comment.get() : super.toString();
	}

  /** Just for indentation when debugging (not displayed as a step). */
	static class Indentation extends SimpleInstruction {
		final int distance;
		Indentation(int distance) { this.distance = distance; }
	}

  /** Just for indentation when debugging (not displayed as a step). */
	static class LineBreak extends SimpleInstruction {
		LineBreak() { this.comment(""); }
  }

  /** A label within the task (for loops, goto, fork, etc.). */
	static class Label extends SimpleInstruction {
    final String label;
		Label(String label) {	this.label = label; this.comment("LABEL " + label);	}
	}

  /** Cancels the whole task and relinquishes task manager lock. */
	static class Stop extends SimpleInstruction {

		Stop() {
      this.comment("STOP");
    }
		
		@Override protected void process() {
      CMD.getInstance().taskManager.stopActiveTask();
			KeyMapping.releaseAll();
		}
		
		@Override protected void cancel() {}
		
	}

  /** Continue execution concurrently at next and label. */
	static class Fork extends SimpleInstruction implements Runnable {
		
		final String dest;
		
		Fork(String label) {
			this.dest = label;
			this.comment("FORK " + label);
      this.withAction(this);
		}
		
		@Override
		public void run() {
			var ptr = next;
			for (; !(ptr instanceof Label lbl && lbl.label.equals(dest)); ptr = ptr.next) ;
			ptr.process();
		}
		
	}
	
}
