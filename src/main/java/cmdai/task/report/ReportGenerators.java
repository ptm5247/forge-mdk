package cmdai.task.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;

import cmdai.Main;

import it.unimi.dsi.fastutil.doubles.DoubleObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;

public class ReportGenerators {
	
	public static class TaskProfiler implements IReportGenerator {
		
		private static char sep = ProfileResults.PATH_SEPARATOR;
		
		@Override public void start() {}
		
		@Override public void stop() {}

		@Override
		@SuppressWarnings("resource")
		public List<String> generate() {
			var profileResults = Minecraft.getInstance().fpsPieResults;
			if (profileResults == null) return Collections.emptyList();
			
			// TODO VERY TIME CONSUMING (10-15% of execution time per tick)
			var results = new ArrayList<ResultField>();
			recurse(profileResults, "root", results, false);
			
			// TODO quesetionable code (not too slow in the grand scheme of things)
			var cumulative = new Object2DoubleAVLTreeMap<String>();
			results.forEach(r -> cumulative.addTo(r.name, r.globalPercentage));
			var reverse = new ArrayList<DoubleObjectImmutablePair<String>>();
			cumulative.forEach((str, d) -> reverse.add(new DoubleObjectImmutablePair<>(d, str)));
			reverse.sort((p1, p2) -> Double.compare(p2.leftDouble(), p1.leftDouble()));
			
			Minecraft.getInstance().fpsPieResults = null;
			return Lists.transform(reverse, p ->
					String.format("%s - %.2f", p.right(), p.leftDouble()));
		}
		
		private static void recurse(ProfileResults results, String path,
				List<ResultField> collection, boolean collect) {
			var lst = results.getTimes(path);
			var iter = lst.listIterator();
			
			iter.next();
			
			while (iter.hasNext()) {
				var r = iter.next();
				if (r.name.equals("unspecified")) continue;
				if (collect) collection.add(r);
				recurse(results, path + sep + r.name, collection,
						r.name.equals(Main.MODID) ? true : collect);
			}
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
			map.object2IntEntrySet().forEach(e -> report.add(e.getKey() + " - " + e.getIntValue()));
			
			return report;
		}
		
	}

}
