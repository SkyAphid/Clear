package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

/**
 * This is a callback for the window's internal rendering pixel width and height (not necessarily the actual window dimensions - that's handled by <code>WindowSizeCallback</code>)
 */
public interface WindowFramebufferSizeCallback extends InputCallback {
    public void windowSizeEvent(Window window, long timestamp, int width, int height);
}
