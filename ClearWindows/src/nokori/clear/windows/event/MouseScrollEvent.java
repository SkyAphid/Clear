package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Pool;

public class MouseScrollEvent  extends EventImpl {
	
	private static final Pool<MouseScrollEvent> POOL = new Pool<MouseScrollEvent>() {
		@Override
		protected MouseScrollEvent create() {
			return new MouseScrollEvent();
		}
	};
	
	private double mouseX;
	private double mouseY;
	private double xoffset;
	private double yoffset;

	private MouseScrollEvent() {}
	
	public static MouseScrollEvent fire(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset) {
		
		MouseScrollEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.mouseX = mouseX;
		e.mouseY = mouseY;
		e.xoffset = xoffset;
		e.yoffset = yoffset;
		
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

	public double getXoffset() {
		return xoffset;
	}

	public double getYoffset() {
		return yoffset;
	}
}
