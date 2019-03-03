package nokori.clear.vg.widget.assembly;

import nokori.clear.windows.Window;

public class WidgetUtil {

	/**
	 * Checks if the two given rectangles are intersecting.
	 * <br><br>
	 * Adapted from the Java AWT Rectangle implementation.
	 */
	public static boolean rectanglesIntersect(double x1, double y1, double w1, double h1, double x2, double y2, double w2, double h2){
		double tw = w1;
		double th = h1;
		
		double rw = w2;
		double rh = h2;

		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}

		double tx = x1;
		double ty = y1;
		tw += tx;
		th += ty;
		
		double rx = x2;
		double ry = y2;
		rw += rx;
		rh += ry;

		// overflow || intersect
		return ((rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry));
	}
	
	/**
	 * @return true if the given point is inside the given rectangle.
	 */
	public static boolean pointWithinRectangle(double px, double py, double x, double y, double w, double h){
		if (px >= x && px <= x + w){
			if (py >= y && py <= y + h){
				return true;
			}
		}

		return false;
	}
	
	public static boolean mouseWithinRectangle(Window window, double x, double y, double w, double h) {
		return pointWithinRectangle(window.getMouseX(), window.getMouseY(), x, y, w, h);
	}
	
	public static float clamp(float f, float min, float max){
		return f > min ? (f < max ? f : max) : min;
	}
	
	public static double clamp(double f, double min, double max){
		return f > min ? (f < max ? f : max) : min;
	}
	
	public static int clamp(int i, int min, int max){
		return i > min ? (i < max ? i : max) : min;
	}
	
	public static long clamp(long i, long min, long max){
		return i > min ? (i < max ? i : max) : min;
	}
}
