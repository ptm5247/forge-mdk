package cmdai.task;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * This class provides an environment in which tasks can set up variables, and is based on
 * {@link net.minecraft.network.syncher.SynchedEntityData SynchedEntityData}. This class is far
 * simpler than SynchedEntityData, since synchronization, serialization, attachment to an Entity,
 * and size restrictions are not necessary here and would complicate code and add overhead.
 */
class TaskEnvironment {
	
	static record DataAccessor<T>(TaskEnvironment env, int id) {
		
		void set(T value) {
			env.items.put(id, value);
		}
		
		@SuppressWarnings("unchecked")
		T get() {
			return (T) env.items.get(id);
		}
		
	}
	
	private Int2ObjectMap<Object> items = new Int2ObjectOpenHashMap<>();
	private int nextId = 0;
	
	<T> DataAccessor<T> define(Class<T> type, T value) {
		items.put(nextId, value);
		return new DataAccessor<T>(this, nextId++);
	}
	
}
