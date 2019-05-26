package nokori.clear.windows.event;

import nokori.clear.windows.Window;

public abstract class EventImpl implements Event {

    protected long timestamp;
    protected Window window = null;
    protected boolean consumed = false;

    @Override
    public void reset() {
        window = null;
        consumed = false;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

}
