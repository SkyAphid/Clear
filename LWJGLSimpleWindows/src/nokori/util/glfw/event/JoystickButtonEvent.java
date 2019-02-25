package nokori.util.glfw.event;

import nokori.util.glfw.Joystick;
import nokori.util.glfw.callback.JoystickCallback;
import nokori.util.pool.SynchronizedPool;

public class JoystickButtonEvent implements Event {
	
	private static final SynchronizedPool<JoystickButtonEvent> POOL = new SynchronizedPool<JoystickButtonEvent>() {
		@Override
		protected JoystickButtonEvent create() {
			return new JoystickButtonEvent();
		}
	};
	
	private Joystick joystick;
	private long timestamp;
	private int button;
	private boolean pressed;
	
	private JoystickButtonEvent() {}
	
	public static JoystickButtonEvent get(Joystick joystick, long timestamp, int button, boolean pressed) {

		JoystickButtonEvent e = POOL.get();
		
		e.joystick = joystick;
		e.timestamp = timestamp;
		e.button = button;
		e.pressed = pressed;
		
		return e;
	}

	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(JoystickCallback callback){
		callback.buttonStateChanged(joystick, timestamp, button, pressed);
	}
	
	@Override
	public void reset() {
		joystick = null;
	}
}