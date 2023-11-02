package cmd5247.task;

import java.util.function.Supplier;

import cmd5247.CMD;

public class TickInstruction extends SimpleInstruction {
	
	protected final Supplier<Boolean> condition;
	
	protected TickInstruction(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	@Override
	protected void process() {
		if (condition instanceof Util.RequiresReset rr) rr.reset();
		CMD.getInstance().taskManager.register(this);
	}
	
	protected void tick() {
		if (condition.get()) {
			CMD.getInstance().taskManager.unregister(this);
			super.process();
		}
	}
	
	@Override
	protected void cancel() {
		CMD.getInstance().taskManager.unregister(this);
		super.cancel();
	}
	
	@Override
	public String toString() {
		if (condition instanceof Util.RequiresReset)
			return super.toString() + " <-- " + condition;
		else return super.toString();
	}

  private static abstract class AbstractBranch extends TickInstruction {
		
		final String target;
		final boolean dir;
		
		/** dir = true for forwards, false for backwards */
		private AbstractBranch(String target, boolean dir, Supplier<Boolean> condition) {
			super(condition);
			this.target = target;
			this.dir = dir;
			this.comment(getClass().getSimpleName().toUpperCase() + " " + target);
		}
		
    @Override
		protected void tick() {
			CMD.getInstance().taskManager.unregister(this);
			complete = true;
			var ptr = next;
			if (condition.get())
				for (ptr = this; !(ptr instanceof Label lbl && lbl.label.equals(target)); ptr = dir ? ptr.next : ptr.prev)
					ptr.complete = dir;
			ptr.process();
		}
		
	}

  /**
	 * Jump forwards in the code to the specified label. 
	 * Use NOW as the trigger for a simple jump,
	 * or specify a condition to get IF-like behavior.
	 */
	static class Goto extends AbstractBranch {
		Goto(String label, Supplier<Boolean> condition) { super(label, true, condition); }
	}

  /**
	 * Jump backwards in the code to the specified label.
	 * Use NOW as the trigger for LOOP-like behavior,
	 * specify a condition for DO-WHILE-like behavior,
	 * or use a Counter for a fixed number of executions.
	 */
	static class Loop extends AbstractBranch {
		Loop(String label, Supplier<Boolean> condition) { super(label, false, condition); }
	}

  /** 
	 * Tries an instruction for a specified max number of ticks,
	 * and executes a separate instruction in the event of a timeout.
	 */
	static class Try extends TickInstruction {
		
		private TickInstruction action;
		private SimpleInstruction timeoutAction;
		
		Try(int ticks, TickInstruction action, SimpleInstruction timeoutAction) {
			super(new Util.Counter(ticks));
			this.action = action;
			this.timeoutAction = timeoutAction;
			this.comment("TRY " + action);
		}
		
		@Override
		protected void process() {
			super.process();
			timeoutAction.next = action.next = next;
			timeoutAction.prev = action.prev = prev;
			action.complete = false;
			if (action.condition instanceof Util.RequiresReset rr) rr.reset();
		}
		
		@Override
		protected void tick() {
      action.tick();
			
			if (action.complete || condition.get()) {
				CMD.getInstance().taskManager.unregister(this);
				this.complete = true;
				if (!action.complete) timeoutAction.process();
			}
		}
		
	}
	
}
