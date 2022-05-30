package cmdai.task.report;

import java.util.ArrayList;
import java.util.List;

public class ReportManager implements IReportGenerator {
	
	private List<IReportGenerator> generators = new ArrayList<>();
	
	public void registerGenerator(IReportGenerator generator) {
		generators.add(generator);
	}
	
	@Override
	public void start() {
		for (var generator : generators) generator.start();
	}
	
	@Override
	public void stop() {
		for (var generator : generators) generator.stop();
	}
	
	@Override
	public List<String> generate() {
		var report = new ArrayList<String>();
		
		for (var generator : generators) {
			report.addAll(generator.generate());
			report.add("");
		}
		
		return report;
	}
	
}
