package nokori.util.glfw.event;

import nokori.util.glfw.Window;
import nokori.util.glfw.callback.ScrollCallback;
import nokori.util.pool.SynchronizedPool;

public class ScrollEvent implements Event{
	
	private static final SynchronizedPool<ScrollEvent> POOL = new SynchronizedPool<ScrollEvent>() {
		@Override
		protected ScrollEvent create() {
			return new ScrollEvent();
		}
	};
	
	private Window window;
	private long timestamp;
	private double mouseX;
	private double mouseY;
	private double xoffset;
	private double yoffset;

	private ScrollEvent() {}
	
	public static ScrollEvent get(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset) {
		
		ScrollEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.mouseX = mouseX;
		e.mouseY = mouseY;
		e.xoffset = xoffset;
		e.yoffset = yoffset;
		
		return e;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(ScrollCallback callback){
		callback.scrollEvent(window, timestamp, mouseX, mouseY, xoffset, yoffset);
	}

	@Override
	public void reset() {
		window = null;
	}
}
