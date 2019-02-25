package nokori.simple_windows.callback;

import nokori.simple_windows.Window;

public interface MouseCallback extends InputCallback {
	public void mouseEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods);
}
