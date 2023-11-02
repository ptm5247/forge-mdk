package cmd5247.task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cmd5247.gui.components.TaskManagerOverlay.DebugLine;
import net.minecraft.network.chat.Component;

public interface ITaskSummaries {

  public List<DebugLine> summarize();
  public Component report();
  public void reset();
  
  public static class Tally implements ITaskSummaries {

    protected static class Entry implements Comparable<Entry> {

      public final String key;
      public final int value;

      private Entry(String key, int value) {
        this.key = key;
        this.value = value;
      }

		  @Override
		  public int compareTo(Entry o) {
        return Integer.compare(value, o.value);
		  }

    }

    protected LinkedList<Entry> list = new LinkedList<>();
    protected final String title;

    public Tally(String title) {
      this.title = title;
    }

    @Override
    public void reset() {
      list.clear();
    }

    public void add(String key, int value) {
      if (list.isEmpty()) {
        list.add(new Entry(key, value));
        return;
      }

      var iter = list.listIterator();
      Entry ptr;
      do {
        ptr = iter.next();
        if (ptr.key.equals(key)) {
          iter.remove();
          break;
        }
      } while (iter.hasNext());

      Entry current = new Entry(key, ptr.key.equals(key) ? (ptr.value + value) : value);
      while (iter.hasPrevious()) {
        ptr = iter.previous();
        if (ptr.value > current.value) {
          iter.next();
          break;
        }
      }
      iter.add(current);
    }

    @Override
    public List<DebugLine> summarize() {
      var report = new ArrayList<DebugLine>(list.size() + 1);

      report.add(DebugLine.simple(title));
      list.forEach(e -> report.add(DebugLine.simple(e.key + " - " + e.value)));
      
      return report;
    }

    @Override
    public Component report() {
      return Component.empty();
    }

  }
  
}
