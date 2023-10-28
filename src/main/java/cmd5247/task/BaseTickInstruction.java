package cmd5247.task;

import java.util.function.Consumer;
import java.util.function.Predicate;

import cmd5247.task.Predicates.RequiresReset;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

public class BaseTickInstruction extends BaseInstruction implements Consumer<PlayerTickEvent> {
	
	protected Predicate<PlayerTickEvent> trigger;
	
	protected BaseTickInstruction(Predicate<PlayerTickEvent> trigger) {
		this.trigger = trigger;
	}
	
	@Override
	public BaseTickInstruction comment(String comment) {
		return (BaseTickInstruction) super.comment(comment);
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
