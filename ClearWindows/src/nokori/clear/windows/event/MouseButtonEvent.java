package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Pool;

public class MouseButtonEvent implements Event{
	
	private static final Pool<MouseButtonEvent> POOL = new Pool<MouseButtonEvent>() {
		@Override
		protected MouseButtonEvent create() {
			return new MouseButtonEvent();
		}
	};
	
	private Window window;
	private long timestamp;
	private double mouseX;
	private double mouseY;
	private int button;
	private boolean pressed;
	private int mods;
	
	private MouseButtonEvent() {}
	
	public static MouseButtonEvent fire(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
		
		MouseButtonEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.mouseX = mouseX;
		e.mouseY = mouseY;
		e.button = button;
		e.pressed = pressed;
		e.mods = mods;
		
		return e;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
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

	public int getButton() {
		return button;
	}

	public boolean isPressed() {
		return pressed;
	}

	public int getMods() {
		return mods;
	}
}