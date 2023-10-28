package cmd5247.task;

import java.util.function.Predicate;
import java.util.function.Supplier;

import cmd5247.task.Predicates.Counter;
import cmd5247.task.Predicates.RequiresReset;
import cmd5247.task.TaskEnvironment.DataAccessor;
import net.minecraft.client.KeyMapping;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

class Instructions {
	
	static interface Action extends Runnable {}
	
	/** Just for indentation when debugging (not displayed as a step). */
	static class Indentation extends BaseInstruction {
		
		final int distance;
		
		Indentation(int distance) {
			this.distance = distance;
		}
		
	}
	
	/** Just for indentation when debugging (not displayed as a step). */
	static class LineBreak extends BaseInstruction {
		
		LineBreak() {
			this.comment("");
		}
		
	}
	
	/** Cancels the whole task and relinquishes task manager lock. */
	static class Stop extends BaseInstruction {
		
		Stop() {
			this.comment("STOP");
		}
		
		@Override
		protected void process() {
			TaskManager.stopActiveTask();
			KeyMapping.releaseAll();
			// TODO unlock mouse handler?
		}
		
		@Override protected void cancel() {}
		
	}
	
	/** A label within the task (for loops, goto, fork, etc.). */
	static class Label extends BaseInstruction {
		
		Label(String label) {
			this.comment("LABEL " + label);
		}
		
	}
	
	private static class BaseJump extends BaseTickInstruction {
		
		final Label dest;
		final boolean dir;
		
		/** dir = true for forwards, false for backwards */
		private BaseJump(String label, boolean dir, Predicate<PlayerTickEvent> condition) {
			super(condition);
			this.dest = new Label(label);
			this.dir = dir;
			this.comment(getClass().getSimpleName().toUpperCase() + " " + label);
		}
		
		@Override
		public void accept(PlayerTickEvent event) {
			TaskManager.unregister(this);
			complete = true;
			var ptr = next;
			
			if (trigger.test(event))
				for (ptr = this; !ptr.equals(dest); ptr = dir ? ptr.next : ptr.prev)
					ptr.complete = dir;
			ptr.process();
		}
		
	}
	
	/**
	 * Jump forwards in the code to the specified label. 
	 * Use NOW as the trigger for a simple jump,
	 * or specify a condition to get IF-like behavior.
	 */
	static class Goto extends BaseJump {
		
		Goto(String label, Predicate<PlayerTickEvent> condition) {
			super(label, true, condition);
		}
		
	}
	
	/**
	 * Jump backwards in the code to the specified label.
	 * Use NOW as the trigger for LOOP-like behavior,
	 * specify a condition for DO-WHILE-like behavior,
	 * or use a Counter for a fixed number of executions.
	 */
	static class Loop extends BaseJump {
		
		Loop(String label, Predicate<PlayerTickEvent> condition) {
			super(label, false, condition);
		}
		
	}
	
	/** Continue execution concurrently at next and label. */
	static class Fork extends BaseInstruction implements Action {
		
		private Label dest;
		
		Fork(String label) {
			this.dest = new Label(label);
			this.comment("FORK " + label);
		}
		
		@Override
		public void run() {
			var ptr = next;
			for (; !ptr.equals(dest); ptr = ptr.next) ;
			ptr.process();
		}
		
	}
	
	/** 
	 * Tries an instruction for a specified max number of ticks,
	 * and executes a separate instruction in the event of a timeout.
	 */
	static class Try extends BaseTickInstruction {
		
		private BaseTickInstruction action;
		private BaseInstruction timeoutAction;
		
		Try(int ticks, BaseTickInstruction action, BaseInstruction timeoutAction) {
			super(new Counter(ticks));
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
			if (action.trigger instanceof RequiresReset rr) rr.reset();
		}
		
		@Override
		public void accept(PlayerTickEvent event) {
			action.accept(event);
			
			if (action.complete || trigger.test(event)) {
				TaskManager.unregister(this);
				this.complete = true;
				if (!action.complete) timeoutAction.process();
			}
		}
		
	}
	
	/** A BaseAction with an associated Runnable, run during process. */
	static class BaseAction extends BaseInstruction implements Action {
		
		private Runnable action;
		
		BaseAction(Runnable action) {
			this.action = action;
		}

		@Override
		public void run() {
			action.run();
		}
		
	}
	
	/** A TickAction with an associated Runnable, run when the trigger fires. */
	static class TickAction extends BaseTickInstruction implements Action {
		
		private Runnable action;
		
		TickAction(Predicate<PlayerTickEvent> trigger, Runnable action) {
			super(trigger);
			this.action = action;
		}

		@Override
		public void run() {
			action.run();
		}
		
	}
	
	/** Sets the given variable with a value attained at runtime */
	static class Set<T> extends BaseInstruction implements Action {
		
		private DataAccessor<T> variable;
		private Supplier<T> runtimeGetter;

		Set(DataAccessor<T> variable, Supplier<T> runtimeGetter) {
			this.variable = variable;
			this.runtimeGetter = runtimeGetter;
		}
		
		@Override
		public void run() {
			variable.set(runtimeGetter.get());
		}
		
	}

}