package nokori.simple_windows.callback;

import nokori.simple_windows.Joystick;

public interface JoystickCallback  extends InputCallback {
	public void axisMoved(Joystick joystick, long timestamp, int axis, float newValue);
	public void buttonStateChanged(Joystick joystick, long timestamp, int button, boolean pressed);
}