package nokori.simple_windows.event;

import nokori.simple_windows.Window;
import nokori.simple_windows.callback.MouseCallback;
import nokori.simple_windows.util.pool.Pool;

public class MouseEvent implements Event{
	
	private static final Pool<MouseEvent> POOL = new Pool<MouseEvent>() {
		@Override
		protected MouseEvent create() {
			return new MouseEvent();
		}
	};
	
	private Window window;
	private long timestamp;
	private double mouseX;
	private double mouseY;
	private int button;
	private boolean pressed;
	private int mods;
	
	private MouseEvent() {}
	
	public static MouseEvent get(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
		
		MouseEvent e = POOL.get();
		
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
	
	public void callback(MouseCallback callback){
		callback.mouseEvent(window, timestamp, mouseX, mouseY, button, pressed, mods);
	}

	@Override
	public void reset() {
		window = null;
	}
}