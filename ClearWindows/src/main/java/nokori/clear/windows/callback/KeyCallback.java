package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface KeyCallback extends InputCallback {
    public void keyEvent(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods);
}
