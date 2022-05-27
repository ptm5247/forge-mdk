package cmdai.task;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;

import cmdai.task.Predicates.RequiresReset;

public class TickInstruction extends BaseInstruction implements Consumer<PlayerTickEvent> {
	
	protected Predicate<PlayerTickEvent> trigger;
	
	protected TickInstruction(Predicate<PlayerTickEvent> trigger) {
		this.trigger = trigger;
	}
	
	@Override
	public TickInstruction comment(String comment) {
		return (TickInstruction) super.comment(comment);
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
	protected void cancel() {
		TaskManager.unregister(this);
		super.cancel();
	}
	
	@Override
	public String toString() {
		if (trigger instanceof RequiresReset)
			return super.toString() + " <-- " + trigger;
		else return super.toString();
	}

}
