package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Pool;

public class CharEvent extends EventImpl {

    private static final Pool<CharEvent> POOL = new Pool<CharEvent>() {
        @Override
        protected CharEvent create() {
            return new CharEvent();
        }
    };

    private int codepoint;
    private String c;
    private int mods;

    public static CharEvent fire(Window window, long timestamp, int codepoint, String c, int mods) {

        CharEvent e = POOL.get();

        e.window = window;
        e.timestamp = timestamp;
        e.codepoint = codepoint;
        e.c = c;
        e.mods = mods;

        return e;
    }

    public int getCodepoint() {
        return codepoint;
    }

    public String getCharString() {
        return c;
    }

    public int getMods() {
        return mods;
    }

}
