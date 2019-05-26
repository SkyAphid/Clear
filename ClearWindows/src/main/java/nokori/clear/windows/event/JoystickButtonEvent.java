package nokori.clear.windows.event;

import nokori.clear.windows.Joystick;
import nokori.clear.windows.pool.Pool;

public class JoystickButtonEvent extends EventImpl {

    private static final Pool<JoystickButtonEvent> POOL = new Pool<JoystickButtonEvent>() {
        @Override
        protected JoystickButtonEvent create() {
            return new JoystickButtonEvent();
        }
    };

    private Joystick joystick;
    private int button;
    private boolean pressed;

    private JoystickButtonEvent() {
    }

    public static JoystickButtonEvent fire(Joystick joystick, long timestamp, int button, boolean pressed) {

        JoystickButtonEvent e = POOL.get();

        e.joystick = joystick;
        e.timestamp = timestamp;
        e.button = button;
        e.pressed = pressed;

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

    public int getButton() {
        return button;
    }

    public boolean isPressed() {
        return pressed;
    }
}