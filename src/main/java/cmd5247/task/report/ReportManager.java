package cmd5247.task.report;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import cmd5247.Options;
import cmd5247.Util;
import net.minecraft.network.chat.TextComponent;

public class ReportManager implements IReportGenerator {
	
	private List<IReportGenerator> generators =
			Lists.newArrayList(new ReportGenerators.TaskProfiler());
	
	public void registerGenerator(IReportGenerator generator) {
		generators.add(generator);
	}
	
	@Override
	public void start() {
		generators.forEach(g -> g.start());
	}
	
	@Override
	public void stop() {
		generators.forEach(g -> g.stop());
		Options.profileDuringTaskReport = false;
		var report = generate();
		if (!report.isEmpty()) Util.msg(new TextComponent(String.join("\n", report)));
	}
	
	@Override
	public List<String> generate() {
		var report = new ArrayList<String>();
		
		int i = Options.profileDuringTaskReport ? 0 : 1;
		for (; i < generators.size(); i++) {
			report.addAll(generators.get(i).generate());
			report.add("");
		}
		
		return report;
	}
	
}
