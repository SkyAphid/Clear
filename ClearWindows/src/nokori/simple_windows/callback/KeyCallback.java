package nokori.simple_windows.callback;

import nokori.simple_windows.Window;

public interface KeyCallback  extends InputCallback {
	public void keyEvent(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods);
}
