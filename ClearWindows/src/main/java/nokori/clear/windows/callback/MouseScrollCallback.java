package nokori.clear.windows.callback;

import nokori.clear.windows.Window;

public interface MouseScrollCallback extends InputCallback {
    public void scrollEvent(Window window, long timestamp, double mouseX, double mouseY, double xoffset, double yoffset);
}
