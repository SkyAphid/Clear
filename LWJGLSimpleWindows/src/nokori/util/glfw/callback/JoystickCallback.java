package nokori.util.glfw.callback;

import nokori.util.glfw.Joystick;

public interface JoystickCallback  extends InputCallback {
	public void axisMoved(Joystick joystick, long timestamp, int axis, float newValue);
	public void buttonStateChanged(Joystick joystick, long timestamp, int button, boolean pressed);
}