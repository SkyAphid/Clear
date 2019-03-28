package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * WidgetSynch's will synchronize the parent Widget's dimensions to its parent's dimensions. This Widget is very useful for WidgetAssemblies that 
 * are frequently resized.
 * <br><br>
 * ***NOTE: If the parent Widget has no parent, it'll instead synchronize with the entire program window instead. This is useful for keeping WidgetAssemblies 
 * synchronized with the GLFW window.
 */
public class WidgetSynch extends Widget {
	
	private boolean synchX, synchY, synchWidth, synchHeight;
	private float xOffset, yOffset, wOffset, hOffset;
	
	/**
	 * This constructor configures the WidgetSynch to sync the assigned Widget to its parent completely (x, y, width, height).
	 */
	public WidgetSynch() {
		this(true, true, true, true, 0f, 0f, 0f, 0f);
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
		this(synchX, synchY, synchWidth, synchHeight, 0f, 0f, 0f, 0f);
	}
	
	/**
	 * This constructor allows the configuring of which aspects to synchronize between the assigned Widget and its parent. 
	 * Offsets can also be inputted to tweak the values used for synchronization.
	 * 
	 * @param synchX - synchronize the x-position with the parent (sets it to zero)
	 * @param synchY - synchronize the y-position with the parent (sets it to zero)
	 * @param synchWidth - synchronize the width with the parent (sets it to the parents width)
	 * @param synchHeight - synchronize the height with the parent (sets it to the parents height)
	 * @param xOffset - this value is added to the synchronized x-value
	 * @param yOffset - this value is added to the synchronized y-value
	 * @param wOffset - this value is added to the synchronized width-value
	 * @param hOffset - this value is added to the synchronized height-value
	 */
	public WidgetSynch(boolean synchX, boolean synchY, boolean synchWidth, boolean synchHeight, float xOffset, float yOffset, float wOffset, float hOffest) {
		this.synchX = synchX;
		this.synchY = synchY;
		this.synchWidth = synchWidth;
		this.synchHeight = synchHeight;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context,	WidgetAssembly rootWidgetAssembly) {
		if (parent == null) {
			System.err.println("WARNING: WidgetSynch isn't attached to another Widget! It has nothing to synchronize!");
			return;
		}
		
		synch(window);
	}

	/**
	 * This checks if each synchonization type is enabled and calls the corresponding functions. This is called every frame from tick() by default.
	 */
	protected void synch(Window window) {
		if (synchX) {
			synchX(window);
		}
		
		if (synchY) {
			synchY(window);
		}
		
		if (synchWidth) {
			synchWidth(window);
		}
		
		if (synchHeight) {
			synchHeight(window);
		}
	}
	
	/**
	 * If X synchronization is enabled, this will be called, this will be called to synchronize the parent's X with its parent's X.
	 */
	protected void synchX(Window window) {
		parent.setX(0f + xOffset);
	}
	
	/**
	 * If Y synchronization is enabled, this will be called, this will be called to synchronize the parent's Y with its parent's Y.
	 */
	protected void synchY(Window window) {
		parent.setY(0f + yOffset);
	}
	
	/**
	 * If width synchronization is enabled, this will be called, this will be called to synchronize the parent's height with its parent's height.
	 */
	protected void synchWidth(Window window) {
		if (parent.parent != null) {
			parent.setWidth(parent.parent.getWidth() + wOffset);
		} else {
			parent.setWidth(window.getFramebufferWidth() + wOffset);
		}
	}
	
	/**
	 * If height synchronization is enabled, this will be called to synchronize the parent's width with its parent's width.
	 */
	protected void synchHeight(Window window) {
		if (parent.parent != null) {
			parent.setHeight(parent.parent.getHeight() + hOffset);
		} else {
			parent.setHeight(window.getFramebufferHeight() + hOffset);
		}
	}
	
	public float getXOffset() {
		return xOffset;
	}

	public void setXOffset(float xOffset) {
		this.xOffset = xOffset;
	}

	public float getYOffset() {
		return yOffset;
	}

	public void setYOffset(float yOffset) {
		this.yOffset = yOffset;
	}

	public float getWOffset() {
		return wOffset;
	}

	public void setWOffset(float wOffset) {
		this.wOffset = wOffset;
	}

	public float getHOffset() {
		return hOffset;
	}

	public void setHOffset(float hOffset) {
		this.hOffset = hOffset;
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {}

}
