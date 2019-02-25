package nokori.simple_windows.event;

import nokori.simple_windows.Window;
import nokori.simple_windows.callback.MouseMotionCallback;
import nokori.simple_windows.util.pool.Pool;

public class MouseMotionEvent implements Event{
	
	private static final Pool<MouseMotionEvent> POOL = new Pool<MouseMotionEvent>() {
		@Override
		protected MouseMotionEvent create() {
			return new MouseMotionEvent();
		}
	};
	
	private Window window;
	private long timestamp;
	private double mouseX;
	private double mouseY;
	private double dx;
	private double dy;
	
	private MouseMotionEvent() {}
	
	public static MouseMotionEvent get(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy) {

		MouseMotionEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.mouseX = mouseX;
		e.mouseY = mouseY;
		e.dx = dx;
		e.dy = dy;
		
		return e;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(MouseMotionCallback callback){
		callback.mouseMotionEvent(window, timestamp, mouseX, mouseY, dx, dy);
	}

	@Override
	public void reset() {
		window = null;
	}
}
