package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface WindowSizeCallback extends InputCallback {
	public void windowSizeEvent(Window window, long timestamp, int width, int height);
}
