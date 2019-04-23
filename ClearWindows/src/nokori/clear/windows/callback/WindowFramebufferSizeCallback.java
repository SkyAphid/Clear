package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface WindowFramebufferSizeCallback extends InputCallback {
	public void windowSizeEvent(Window window, long timestamp, int width, int height);
}
