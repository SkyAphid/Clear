package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

/**
 * This is a callback for the window's actual pixel width and height (not the internal rendering dimensions - that's handled by <code>WindowFramebufferSizeCallback</code>)
 */
public interface WindowSizeCallback extends InputCallback {
    public void windowSizeEvent(Window window, long timestamp, int width, int height);
}
