package nokori.util.glfw.callback;

import nokori.util.glfw.Joystick;

public interface JoystickStateCallback extends InputCallback {
	public void joystickStateChanged(Joystick joystick, long timestamp, boolean connected);
}
