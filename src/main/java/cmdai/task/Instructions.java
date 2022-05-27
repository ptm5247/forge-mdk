package cmdai.task;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.client.KeyMapping;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

import cmdai.task.Predicates.Counter;
import cmdai.task.Predicates.RequiresReset;
import cmdai.task.TaskEnvironment.DataAccessor;

class Instructions {
	
	static interface Action extends Runnable {}
	
	/** Just for indentation when debugging (not displayed as a step). */
	static class Indentation extends BaseInstruction {
		
		final int distance;
		
		Indentation(int distance) {
			this.distance = distance;
		}
		
	}
	
	/** Cancels the whole task and relinquishes task manager lock. */
	static class Stop extends BaseInstruction {
		
		private Task parentTask;
		
		Stop(Task parentTask) {
			this.parentTask = parentTask;
			this.comment("STOP");
		}
		
		@Override
		protected void process() {
			parentTask.stop();
			KeyMapping.releaseAll();
			// TODO unlock mouse handler?
			TaskManager.deactivateTask();
		}
		
		@Override protected void cancel() {}
		
	}
	
	/** A label within the task (for loops, goto, fork, etc.). */
	static class Label extends BaseInstruction {
		
		Label(String label) {
			this.comment("LABEL " + label);
		}
		
	}
	
	private static class BaseJump extends TickInstruction {
		
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
	static class Try extends TickInstruction {
		
		private TickInstruction action;
		private BaseInstruction timeoutAction;
		
		Try(int ticks, TickInstruction action, BaseInstruction timeoutAction) {
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
	static class TickAction extends TickInstruction implements Action {
		
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
