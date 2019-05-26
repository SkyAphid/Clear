package nokori.clear.windows.callback;

import nokori.clear.windows.Joystick;

public interface JoystickStateCallback extends InputCallback {
    public void joystickStateChanged(Joystick joystick, long timestamp, boolean connected);
}
