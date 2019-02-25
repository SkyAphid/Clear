package nokori.util.glfw.event;

import nokori.util.glfw.Joystick;
import nokori.util.glfw.callback.JoystickStateCallback;
import nokori.util.pool.SynchronizedPool;

public class JoystickStateEvent implements Event{
	
	private static final SynchronizedPool<JoystickStateEvent> POOL = new SynchronizedPool<JoystickStateEvent>() {
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