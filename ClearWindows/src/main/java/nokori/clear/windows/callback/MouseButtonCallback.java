package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface MouseButtonCallback extends InputCallback {
    public void mouseButtonEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods);
}
