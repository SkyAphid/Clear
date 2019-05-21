package nokori.clear.windows.event.vg;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.EventImpl;
import nokori.clear.windows.pool.Pool;

public class MouseEnteredEvent extends EventImpl {
	
	private static final Pool<MouseEnteredEvent> POOL = new Pool<MouseEnteredEvent>() {
		@Override
		protected MouseEnteredEvent create() {
			return new MouseEnteredEvent();
		}
	};
	
	private Window window;
	private double mouseX;
	private double mouseY;
	
	private MouseEnteredEvent() {}
	
	public static MouseEnteredEvent fire(Window window, long timestamp, double mouseX, double mouseY) {

		MouseEnteredEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.mouseX = mouseX;
		e.mouseY = mouseY;
		
		return e;
	}
	
	@Override
	public void reset() {
		window = null;
	}

	public Window getWindow() {
		return window;
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}
}
