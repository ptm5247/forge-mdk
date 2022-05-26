package cmdai.task;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

import cmdai.task.Predicates.RequiresReset;

class TickInstruction extends BaseInstruction implements Consumer<PlayerTickEvent> {
	
	static final Predicate<PlayerTickEvent> NOW = e -> true;
	static final Predicate<PlayerTickEvent> NEVER = e -> false;
	
	protected Predicate<PlayerTickEvent> trigger;
	
	protected TickInstruction(Predicate<PlayerTickEvent> trigger) {
		this.trigger = trigger;
	}
	
	@Override
	protected void process() {
		if (trigger instanceof RequiresReset rr) rr.reset();
		TaskManager.register(this);
	}
	
	@Override
	public void accept(PlayerTickEvent event) {
		if (trigger.test(event)) {
			TaskManager.unregister(this);
			super.process();
		}
	}
	
	@Override
	protected void cancel(Boolean dir) {
		TaskManager.unregister(this);
		super.cancel(dir);
	}
	
	@Override
	public String toString() {
		if (trigger instanceof RequiresReset)
			return super.toString() + " <-- " + trigger;
		else return super.toString();
	}

}
