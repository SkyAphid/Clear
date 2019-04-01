package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

/**
 * WidgetSynch's will synchronize the parent Widget's dimensions to the designated Widget's dimensions. This Widget is very useful for WidgetAssemblies that 
 * are frequently resized.
 */
public class WidgetSynch extends Widget {
	
	private Widget synchWith = null;
	
	private boolean synchX, synchY, synchWidth, synchHeight;
	private float xOffset, yOffset, wOffset, hOffset;
	
	/**
	 * This enum contains various synchronization modes for the WidgetSynch to use. 
	 */
	public enum Mode {
		/**
		 * Manual mode: all settings are manually configured.
		 */
		MANUAL,
		
		/**
		 * Synchronize with parent: If a parent widget is available, synchronize with its width/height. 
		 * Otherwise, WITH_WINDOW mode will be used instead (but the setting will remain the same).
		 */
		WITH_PARENT,
		
		/**
		 * Synchronize with Window: the width/height will be synchronized with the Window.
		 */
		WITH_WINDOW
	};
	
	private Mode mode = Mode.MANUAL;
	
	/*public WidgetSynch() {
		this(null, true, true, true, true, 0f, 0f, 0f, 0f);
	}*/
	
	public WidgetSynch(Mode mode) {
		this((Widget) null);
		this.mode = mode;
	}
	
	public WidgetSynch(Widget synchWith) {
		this(Mode.MANUAL, synchWith, true, true, true, true, 0f, 0f, 0f, 0f);
	}

	public WidgetSynch(Widget synchWith, float xOffset, float yOffset, float wOffset, float hOffset) {
		this(Mode.MANUAL, synchWith, true, true, true, true, xOffset, yOffset, wOffset, hOffset);
	}
	
	public WidgetSynch(Widget synchWith, boolean synchX, boolean synchY, boolean synchWidth, boolean synchHeight) {
		this(Mode.MANUAL, synchWith, synchX, synchY, synchWidth, synchHeight, 0f, 0f, 0f, 0f);
	}
	
	public WidgetSynch(Mode mode, float xOffset, float yOffset, float wOffset, float hOffset) {
		this(mode, null, true, true, true, true, xOffset, yOffset, wOffset, hOffset);
	}
	
	public WidgetSynch(Mode mode, boolean synchX, boolean synchY, boolean synchWidth, boolean synchHeight) {
		this(mode, null, synchX, synchY, synchWidth, synchHeight, 0f, 0f, 0f, 0f);
	}
	
	/**
	 * This constructor allows the configuring of which aspects to synchronize between the assigned Widget and its parent. 
	 * Offsets can also be inputted to tweak the values used for synchronization.
	 * 
	 * @param synchWith - the widget to synchronize with. This cannot be null if the Mode is set to MANUAL.
	 * @param synchX - synchronize the x-position with the parent (sets it to zero)
	 * @param synchY - synchronize the y-position with the parent (sets it to zero)
	 * @param synchWidth - synchronize the width with the parent (sets it to the parents width)
	 * @param synchHeight - synchronize the height with the parent (sets it to the parents height)
	 * @param xOffset - this value is added to the synchronized x-value
	 * @param yOffset - this value is added to the synchronized y-value
	 * @param wOffset - this value is added to the synchronized width-value
	 * @param hOffset - this value is added to the synchronized height-value
	 */
	public WidgetSynch(Mode mode, Widget synchWith, boolean synchX, boolean synchY, boolean synchWidth, boolean synchHeight, float xOffset, float yOffset, float wOffset, float hOffset) {
		this.mode = mode;
		this.synchWith = synchWith;
		
		this.synchX = synchX;
		this.synchY = synchY;
		this.synchWidth = synchWidth;
		this.synchHeight = synchHeight;
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.wOffset = wOffset;
		this.hOffset = hOffset;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context,	WidgetAssembly rootWidgetAssembly) {
		synch(window);
	}

	/**
	 * This checks if each synchonization type is enabled and calls the corresponding functions. This is called every frame from tick() by default.
	 */
	public void synch(Window window) {
		Mode activeMode = mode;
		
		if (activeMode == Mode.WITH_PARENT && parent.parent == null) {
			activeMode = Mode.WITH_WINDOW;
		}
		
		if (synchX) {
			synchX(window);
		}
		
		if (synchY) {
			synchY(window);
		}
		
		if (synchWidth) {
			synchWidth(window, activeMode);
		}
		
		if (synchHeight) {
			synchHeight(window, activeMode);
		}
	}
	
	/**
	 * If X synchronization is enabled, this will be called, this will be called to synchronize the parent's X with its parent's X.
	 */
	protected void synchX(Window window) {
		float internalOffsetX = 0f;
		
		if (mode == Mode.WITH_WINDOW) {
			internalOffsetX = -parent.getClippedX();
		}
		
		parent.setX(internalOffsetX + xOffset);
	}
	
	/**
	 * If Y synchronization is enabled, this will be called, this will be called to synchronize the parent's Y with its parent's Y.
	 */
	protected void synchY(Window window) {
		float internalOffsetY = 0f;
		
		if (mode == Mode.WITH_WINDOW) {
			internalOffsetY = -parent.getClippedY();
		}
		
		parent.setY(internalOffsetY + yOffset);
	}
	
	/**
	 * If width synchronization is enabled, this will be called, this will be called to synchronize the parent's height with its parent's height.
	 */
	protected void synchWidth(Window window, Mode mode) {
		switch(mode) {
		case WITH_PARENT:
			parent.setWidth(parent.parent.getWidth() + wOffset);
			break;
		case WITH_WINDOW:
			parent.setWidth(window.getFramebufferWidth() + wOffset);
			break;
		case MANUAL:
		default:
			parent.setWidth(synchWith.getWidth() + wOffset);
			break;
		
		}
	}
	
	/**
	 * If height synchronization is enabled, this will be called to synchronize the parent's width with its parent's width.
	 */
	protected void synchHeight(Window window, Mode mode) {
		switch(mode) {
		case WITH_PARENT:
			parent.setHeight(parent.parent.getHeight() + hOffset);
			break;
		case WITH_WINDOW:
			parent.setHeight(window.getFramebufferHeight() + hOffset);
			break;
		case MANUAL:
		default:
			parent.setHeight(synchWith.getHeight() + hOffset);
			break;
		
		}
	}
	
	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Widget getSynchWith() {
		return synchWith;
	}

	public void setSynchWith(Widget synchWith) {
		this.synchWith = synchWith;
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
