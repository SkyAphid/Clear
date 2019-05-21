package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class WidgetUtils {
	
	/**
	 * Shorthand way to render rectangles with NanoVG.
	 * 
	 * @param context
	 * @param fill
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void nvgRect(NanoVGContext context, NVGColor fill, float x, float y, float width, float height) {
		nvgRect(context.get(), fill, x, y, width, height);
	}
	
	public static void nvgRect(long vg, float x, float y, float width, float height) {
		nvgRect(vg, (NVGColor) null, x, y, width, height);
	}
	
	public static void nvgRect(long vg, ClearColor fill, float x, float y, float width, float height) {
		fill.tallocNVG(f -> {
			nvgRect(vg, f, x, y, width, height);
		});
	}

	/**
	 * Shorthand way to render rectangles with NanoVG.
	 * 
	 * @param vg - handle for NanoVG context
	 * @param fill
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void nvgRect(long vg, NVGColor fill, float x, float y, float width, float height) {
		nvgBeginPath(vg);
		
		if (fill != null) {
			nvgFillColor(vg, fill);
		}
		
		NanoVG.nvgRect(vg, x, y, width, height);
		nvgFill(vg);
		nvgClosePath(vg);
	}
	
	public static void nvgShape(long vg, NVGColor fill, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		nvgBeginPath(vg);
		
		if (fill != null) {
			nvgFillColor(vg, fill);
		}
		
		nvgMoveTo(vg, x1, y1);
		nvgLineTo(vg, x2, y2);
		nvgLineTo(vg, x3, y3);
		nvgLineTo(vg, x4, y4);
		nvgFill(vg);
		nvgClosePath(vg);
	}
	
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
	
	public static boolean mouseWithinRectangle(Widget widget, Window window, double x, double y, double w, double h) {
		return pointWithinRectangle(window.getScaledMouseX(widget.getScaler().getScale()), window.getScaledMouseY(widget.getScaler().getScale()), x, y, w, h);
	}
	
	/*
	 * CLAMP functions from NOKORI ENGINE
	 */
	
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
	
	/*
	 * MIX functions from NOKORI ENGINE
	 */

	public static float mix(float x, float y, float a) {
		return x + (y - x) * a;
	}

	public static double mix(double x, double y, double a) {
		return x + (y - x) * a;
	}

	/*
	 * SMOOTHMIX functions from NOKORI ENGINE
	 */
	
	public static float smoothmix(float x, float y, float a) {
		a = a * a * (3.0f - 2.0f * a);
		return x + (y - x) * a;
	}
	
	public static float smoothermix(float x, float y, float a) {
		a = a * a * a * (a * (a * 6 - 15) + 10);
		return x + (y - x) * a;
	}
}
