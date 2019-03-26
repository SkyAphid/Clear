package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * WidgetSynch's will synchronize the parent Widget's dimensions to its parent's dimensions. This Widget is very useful for WidgetAssemblies that 
 * are frequently resized.
 */
public class WidgetSynch extends Widget {
	
	private boolean synchX, synchY, synchWidth, synchHeight;
	
	/**
	 * This constructor configures the WidgetSynch to sync the assigned Widget to its parent completely (x, y, width, height).
	 */
	public WidgetSynch() {
		this(true, true, true, true);
	}
	
	/**
	 * This constructor allows the configuring of which aspects to synchronize between the assigned Widget and its parent.
	 * 
	 * @param synchX - synchronize the x-position with the parent (sets it to zero)
	 * @param synchY - synchronize the y-position with the parent (sets it to zero)
	 * @param synchWidth - synchronize the width with the parent (sets it to the parents width)
	 * @param synchHeight - synchronize the height with the parent (sets it to the parents height)
	 */
	public WidgetSynch(boolean synchX, boolean synchY, boolean synchWidth, boolean synchHeight) {
		this.synchX = synchX;
		this.synchY = synchY;
		this.synchWidth = synchWidth;
		this.synchHeight = synchHeight;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context,	WidgetAssembly rootWidgetAssembly) {
		if (parent == null || parent.parent == null) {
			System.err.println("WARNING: WidgetSynch isn't attached to another Widget, or that Widget isn't attached to another Widget!"
					+ "\nBoth must be present for a WidgetSynch to work correctly! (Heirarchy: Widget -> Child Widget -> Widget Clip)");
			return;
		}
		
		if (synchX) {
			parent.setX(0f);
		}
		
		if (synchY) {
			parent.setY(0f);
		}
		
		if (synchWidth) {
			parent.setWidth(parent.parent.getWidth());
		}
		
		if (synchHeight) {
			parent.setHeight(parent.parent.getHeight());
		}
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {}

}
