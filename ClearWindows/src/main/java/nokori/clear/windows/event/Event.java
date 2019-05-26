package nokori.clear.windows.event;

import nokori.clear.windows.Window;
import nokori.clear.windows.pool.Poolable;

public interface Event extends Poolable {

    public long getTimestamp();

    public Window getWindow();

    public void setConsumed(boolean consumed);

    public boolean isConsumed();

}
