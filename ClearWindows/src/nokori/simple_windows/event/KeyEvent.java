package nokori.simple_windows.event;

import nokori.simple_windows.Window;
import nokori.simple_windows.callback.KeyCallback;
import nokori.simple_windows.util.pool.Pool;

public class KeyEvent implements Event{
	
	private static final Pool<KeyEvent> POOL = new Pool<KeyEvent>() {
		@Override
		protected KeyEvent create() {
			return new KeyEvent();
		}
	};
	
	private Window window;
	private long timestamp;
	private int key;
	private int scanCode;
	private boolean pressed;
	private boolean repeat;
	private int mods;

	private KeyEvent() {}
	
	public static KeyEvent get(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods) {

		KeyEvent e = POOL.get();
		
		e.window = window;
		e.timestamp = timestamp;
		e.key = key;
		e.scanCode = scanCode;
		e.pressed = pressed;
		e.repeat = repeat;
		e.mods = mods;
		
		return e;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(KeyCallback callback){
		callback.keyEvent(window, timestamp, key, scanCode, pressed, repeat, mods);
	}

	@Override
	public void reset() {
		window = null;
	}
}