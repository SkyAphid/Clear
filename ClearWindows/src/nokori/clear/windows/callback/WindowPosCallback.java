package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface WindowPosCallback extends InputCallback {
	public void windowPositionEvent(Window window, long timestamp, int x, int y);
}
