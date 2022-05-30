package cmdai.task.report;

import java.util.List;

import net.minecraftforge.common.MinecraftForge;

public interface IReportGenerator {
	
	default void start() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	default void stop() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	List<String> generate();

}
