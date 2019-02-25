package nokori.util.glfw.event;

import nokori.util.glfw.Joystick;
import nokori.util.glfw.callback.JoystickCallback;
import nokori.util.pool.SynchronizedPool;

public class JoystickAxisEvent implements Event {
	
	private static final SynchronizedPool<JoystickAxisEvent> POOL = new SynchronizedPool<JoystickAxisEvent>() {
		@Override
		protected JoystickAxisEvent create() {
			return new JoystickAxisEvent();
		}
	};
	
	private Joystick joystick;
	private long timestamp;
	private int axis;
	private float newValue;
	
	private JoystickAxisEvent() {}
	
	public static JoystickAxisEvent get(Joystick joystick, long timestamp, int axis, float newValue) {
		
		JoystickAxisEvent e = POOL.get();
		
		e.joystick = joystick;
		e.timestamp = timestamp;
		e.axis = axis;
		e.newValue = newValue;
		
		return e;
	}

	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(JoystickCallback callback){
		callback.axisMoved(joystick, timestamp, axis, newValue);
	}
	
	@Override
	public void reset() {
		joystick = null;
	}
}