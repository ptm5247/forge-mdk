package cmdai.task.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;

public class ReportGenerators {
	
	public static class TaskProfiler implements IReportGenerator {
		
		@Override public void start() {}
		
		@Override public void stop() {}

		@Override
		@SuppressWarnings("resource")
		public List<String> generate() {
			var profileResults = Minecraft.getInstance().fpsPieResults;
			if (profileResults == null) return Collections.emptyList();
			
			var results = profileResults.getTimes("root");
			var report = new ArrayList<String>(results.size());
			
			for (var r : results)
				report.add(String.format("%s: %04.2f, %04.2f - %d",
						r.name, r.percentage, r.globalPercentage, r.count));
			
			Minecraft.getInstance().fpsPieResults = null;
			return report;
		}
		
	}
	
	public static class Tally implements IReportGenerator {
		
		private Object2IntAVLTreeMap<String> map = new Object2IntAVLTreeMap<>();
		private String title;
		
		protected Tally(String title) {
			this.title = title;
		}
		
		public void add(String key, int value) {
			map.addTo(key, value);
		}
		
		@Override
		public List<String> generate() {
			var report = new ArrayList<String>(map.size() + 1);
			
			report.add(title);
			for (var entry : map.object2IntEntrySet())
				report.add(entry.getKey() + " - " + entry.getIntValue());
			
			return report;
		}
		
	}

}
