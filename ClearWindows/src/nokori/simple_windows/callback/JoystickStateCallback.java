package nokori.simple_windows.callback;

import nokori.simple_windows.Joystick;

public interface JoystickStateCallback extends InputCallback {
	public void joystickStateChanged(Joystick joystick, long timestamp, boolean connected);
}
