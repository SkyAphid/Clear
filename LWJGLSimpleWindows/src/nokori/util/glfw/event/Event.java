package nokori.util.glfw.event;

import nokori.util.pool.Poolable;

public interface Event extends Poolable{
	
	public long getTimestamp();
}
