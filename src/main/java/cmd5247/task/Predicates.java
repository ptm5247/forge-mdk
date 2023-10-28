package cmd5247.task;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

class Predicates {
	
	static interface RequiresReset {
		
		void reset();
		
	}
	
	static final Predicate<PlayerTickEvent> NOW = e -> true;
	static final Predicate<PlayerTickEvent> NEVER = e -> false;
	
	/** Accepts a tick event after a certain number of tests. */
	static class Counter implements Predicate<PlayerTickEvent>, RequiresReset {
		
		private final int reset;
		private int count;
		private String fmt;
		
		/** When ticks = 1, the first call will return true. */
		Counter(int ticks) {
			this.count = this.reset = ticks;
			this.fmt = "[%0" + Integer.toString(ticks).length() + "d]";
		}
		
		@Override
		public void reset() {
			count = reset;
		}
		
		@Override
		public boolean test(PlayerTickEvent event) {
			count = Integer.max(0, count - 1);
			return count == 0;
		}
		
		@Override
		public String toString() {
			return String.format(fmt, count);
		}
		
	}
	
	/** 
	 * Stores the first value supplied by the supplier,
	 * and accepts a tick event once a subsequent call supplies a different value.
	 */
	static class Observer implements Predicate<PlayerTickEvent>, RequiresReset {
		
		private Optional<?> initial = Optional.empty();
		private Supplier<?> target;
		private String observedType = "Unknown";
		
		Observer(Supplier<?> target) {
			this.target = target;
		}
		
		@Override
		public void reset() {
			this.initial = Optional.empty();
		}
		
		@Override
		public boolean test(PlayerTickEvent event) {
			if (initial.isEmpty()) {
				initial = Optional.of(target.get());
				observedType = initial.get().getClass().getSimpleName();
				return false;
			} else return !initial.get().equals(target.get());
		}
		
		@Override
		public String toString() {
			return "OBSERVE " + observedType;
		}
		
	}

}
