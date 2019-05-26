package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

/**
 * This is a callback for the Window's pixel position on the desktop.
 */
public interface WindowPosCallback extends InputCallback {
    public void windowPositionEvent(Window window, long timestamp, int x, int y);
}
