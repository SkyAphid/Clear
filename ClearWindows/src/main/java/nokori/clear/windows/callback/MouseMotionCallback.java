package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface MouseMotionCallback extends InputCallback {
    public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy);
}
