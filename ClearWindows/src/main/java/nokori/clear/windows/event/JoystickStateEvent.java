package nokori.clear.windows.event;

import nokori.clear.windows.Joystick;
import nokori.clear.windows.pool.Pool;

public class JoystickStateEvent extends EventImpl {

    private static final Pool<JoystickStateEvent> POOL = new Pool<JoystickStateEvent>() {
        @Override
        protected JoystickStateEvent create() {
            return new JoystickStateEvent();
        }
    };

    private Joystick joystick;
    private boolean connected;

    private JoystickStateEvent() {
    }

    public static JoystickStateEvent fire(Joystick joystick, long timestamp, boolean connected) {

        JoystickStateEvent e = POOL.get();

        e.joystick = joystick;
        e.timestamp = timestamp;
        e.connected = connected;

        return e;
    }

    @Override
    public void reset() {
        super.reset();
        joystick = null;
    }

    public Joystick getJoystick() {
        return joystick;
    }

    public boolean isConnected() {
        return connected;
    }
}