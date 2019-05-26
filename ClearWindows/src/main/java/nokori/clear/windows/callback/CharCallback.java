package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface CharCallback extends InputCallback {
    public void charEvent(Window window, long timestamp, int codepoint, String c, int mods);
}
