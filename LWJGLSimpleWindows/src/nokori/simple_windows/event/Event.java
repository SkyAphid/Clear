package nokori.simple_windows.event;

import nokori.simple_windows.util.pool.Poolable;

public interface Event extends Poolable{
	
	public long getTimestamp();
}
