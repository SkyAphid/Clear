package nokori.clear.vg;

import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.windows.Cursor;
import nokori.clear.windows.Cursor.Type;;

/**
 * This class contains some static utility variables and functions that can be used easily around the system.
 */
public class ClearStaticResources {
	private static Widget focusedWidget = null;
	
	private static Cursor[] loadedCursors;
	
	public static Widget getFocusedWidget() {
		return focusedWidget;
	}

	public static void setFocusedWidget(Widget focusedWidget) {
		ClearStaticResources.focusedWidget = focusedWidget;
	}
	
	/**
	 * @return true if there is no currently focused widget, or the currently focused widget is the one that is passed in.
	 */
	public static boolean canFocus(Widget widget) {
		return (focusedWidget == null || focusedWidget == widget);
	}
	
	/**
	 * Loads all of the available Cursors and stores them statically for use around the program. Make sure to call disposeAllCursors() at the end of the program's lifecycle.
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
	
	public static void destroyAllCursors() {
		if (loadedCursors != null) {
			for (int i = 0; i < loadedCursors.length; i++) {
				loadedCursors[i].destroy();
			}
		}
	}
}
