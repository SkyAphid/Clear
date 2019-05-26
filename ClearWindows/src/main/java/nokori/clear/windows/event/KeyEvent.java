package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Pool;

public class KeyEvent extends EventImpl {

    private static final Pool<KeyEvent> POOL = new Pool<KeyEvent>() {
        @Override
        protected KeyEvent create() {
            return new KeyEvent();
        }
    };

    private int key;
    private int scanCode;
    private boolean pressed;
    private boolean repeat;
    private int mods;

    private KeyEvent() {
    }

    public static KeyEvent fire(Window window, long timestamp, int key, int scanCode, boolean pressed, boolean repeat, int mods) {

        KeyEvent e = POOL.get();

        e.window = window;
        e.timestamp = timestamp;
        e.key = key;
        e.scanCode = scanCode;
        e.pressed = pressed;
        e.repeat = repeat;
        e.mods = mods;

        return e;
    }

    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public int getMods() {
        return mods;
    }
}