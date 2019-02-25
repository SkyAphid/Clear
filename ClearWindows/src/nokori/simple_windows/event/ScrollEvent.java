package nokori.simple_windows.event;

import nokori.simple_windows.Window;
import nokori.simple_windows.callback.ScrollCallback;
import nokori.simple_windows.util.pool.Pool;

public class ScrollEvent implements Event{
	
	private static final Pool<ScrollEvent> POOL = new Pool<ScrollEvent>() {
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
