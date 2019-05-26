package nokori.clear.windows.event;

public abstract class MouseEventImpl extends EventImpl {
    protected double mouseX;
    protected double mouseY;

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getScaledMouseX(double scale) {
        return getScaledMouseCoordinate(mouseX, scale);
    }

    public double getScaledMouseY(double scale) {
        return getScaledMouseCoordinate(mouseY, scale);
    }

    public static double getScaledMouseCoordinate(double coordinate, double scale) {
        return (coordinate / scale);
    }
}
