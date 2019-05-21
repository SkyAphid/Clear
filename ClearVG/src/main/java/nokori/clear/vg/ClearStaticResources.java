package nokori.clear.vg;

import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.windows.Cursor;
import nokori.clear.windows.Cursor.Type;

;

/**
 * This class contains some static utility variables and functions that can be used easily around the system. It will be initialized automatically if you're using a ClearApplication, 
 * otherwise, you can initialize it yourself with the various resource initialization functions (allowing you to choose what you load and what you don't load). Don't forget to dispose
 * what you end up loading if applicable!
 */
public class ClearStaticResources {
	private static Widget focusedWidget = null;

	private static Cursor[] loadedCursors;
	
	public static Widget getFocusedWidget() {
		return focusedWidget;
	}

	public static void setFocusedWidget(Widget focusedWidget) {
		ClearStaticResources.focusedWidget = focusedWidget;
		//System.err.println(focusedWidget);
		//Thread.dumpStack();
	}

	public static boolean isFocusedOrCanFocus(Widget widget) {
		return (focusedWidget == null || focusedWidget == widget);
	}
	
	public static boolean isFocused(Widget widget) {
		return (focusedWidget == widget);
	}
	
	public static boolean isFocused() {
		return (focusedWidget != null);
	}
	
	public static boolean clearFocusIfApplicable(Widget widget) {
		if (isFocused(widget)) {
			setFocusedWidget(null);
			return true;
		}
		
		return false;
	}

	/**
	 * Loads all of the available Cursors and stores them statically for use around the program. This only needs to be called once. 
	 * <br><br>
	 * IMPORTANT: Make sure to call disposeAllCursors() at the end of the program's lifecycle.
	 */
	public static void loadAllCursors() {
		loadedCursors = new Cursor[Type.values().length];
		
		for (int i = 0; i < loadedCursors.length; i++) {
			loadedCursors[i] = new Cursor(Type.values()[i]);
		}
	}
	
	/**
	 * @return Gets the loaded Cursor object for the given default system type.
	 */
	public static Cursor getCursor(Type type) {
		for (int i = 0; i < loadedCursors.length; i++) {
			if (loadedCursors[i].getType() == type) {
				return loadedCursors[i];
			}
		}
		
		return null;
	}
	
	/**
	 * Destroys all of the loaded static cursors. To be used at the end of a program's lifecycle if loadAllCursors() was called at any point during runtime.
	 */
	public static void destroyAllCursors() {
		if (loadedCursors != null) {
			for (int i = 0; i < loadedCursors.length; i++) {
				loadedCursors[i].destroy();
			}
		}
	}
}
