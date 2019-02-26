package nokori.clear.windows.event;

import nokori.clear.windows.Joystick;
import nokori.clear.windows.pool.Pool;

public class JoystickAxisEvent implements Event {
	
	private static final Pool<JoystickAxisEvent> POOL = new Pool<JoystickAxisEvent>() {
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
	
	public static JoystickAxisEvent fire(Joystick joystick, long timestamp, int axis, float newValue) {
		
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

	@Override
	public void reset() {
		joystick = null;
	}

	public Joystick getJoystick() {
		return joystick;
	}

	public int getAxis() {
		return axis;
	}

	public float getNewValue() {
		return newValue;
	}
}