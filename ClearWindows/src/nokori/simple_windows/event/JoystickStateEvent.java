package nokori.simple_windows.event;

import nokori.simple_windows.Joystick;
import nokori.simple_windows.callback.JoystickStateCallback;
import nokori.simple_windows.util.pool.Pool;

public class JoystickStateEvent implements Event{
	
	private static final Pool<JoystickStateEvent> POOL = new Pool<JoystickStateEvent>() {
		@Override
		protected JoystickStateEvent create() {
			return new JoystickStateEvent();
		}
	};
	
	private Joystick joystick;
	private long timestamp;
	private boolean connected;

	private JoystickStateEvent() {}
	
	public static JoystickStateEvent get(Joystick joystick, long timestamp, boolean connected){
		
		JoystickStateEvent e = POOL.get();
		
		e.joystick = joystick;
		e.timestamp = timestamp;
		e.connected = connected;
		
		return e;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void callback(JoystickStateCallback callback){
		callback.joystickStateChanged(joystick, timestamp, connected);
	}

	@Override
	public void reset() {
		joystick = null;
	}
}