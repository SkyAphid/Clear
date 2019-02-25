package nokori.simple_windows.callback;

import nokori.simple_windows.Window;

public interface CharCallback extends InputCallback {
	public void charEvent(Window window, long timestamp, int codepoint, String c, int mods);
}
