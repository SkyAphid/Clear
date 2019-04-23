package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Pool;

public class MouseMotionEvent  extends EventImpl {
	
	private static final Pool<MouseMotionEvent> POOL = new Pool<MouseMotionEvent>() {
		@Override
		protected MouseMotionEvent create() {
			return new MouseMotionEvent();
		}
	};
	
	private double mouseX;
	private double mouseY;
	private double dx;
	private double dy;
	
	private MouseMotionEvent() {}
	
	public static MouseMotionEvent fire(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy) {

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
	public void reset() {
		window = null;
	}
	
	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

	public double getDX() {
		return dx;
	}

	public double getDY() {
		return dy;
	}
}
