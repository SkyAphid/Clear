package nokori.clear.windows.event;

import nokori.clear.windows.pool.Poolable;

public interface Event extends Poolable{
	
	public long getTimestamp();
	
}
