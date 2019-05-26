package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;

/**
 * WidgetSynch's will synchronize the parent Widget's dimensions to the designated Widget's dimensions. This Widget is very useful for WidgetAssemblies that 
 * are frequently resized.
 */
public class WidgetSynch extends Widget {
	
	private Widget synchWith = null;
	
	private boolean synchXEnabled, synchYEnabled, synchWidthEnabled, synchHeightEnabled;
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
		 * Synchronize with frambuffer: the width/height will be synchronized with the framebuffer settings in NanoVGContext.
		 */
		WITH_FRAMEBUFFER
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
	
	public WidgetSynch(Widget synchWith, boolean synchXEnabled, boolean synchYEnabled, boolean synchWidthEnabled, boolean synchHeightEnabled) {
		this(Mode.MANUAL, synchWith, synchXEnabled, synchYEnabled, synchWidthEnabled, synchHeightEnabled, 0f, 0f, 0f, 0f);
	}
	
	public WidgetSynch(Mode mode, float xOffset, float yOffset, float wOffset, float hOffset) {
		this(mode, null, true, true, true, true, xOffset, yOffset, wOffset, hOffset);
	}
	
	public WidgetSynch(Mode mode, boolean synchXEnabled, boolean synchYEnabled, boolean synchWidthEnabled, boolean synchHeightEnabled) {
		this(mode, null, synchXEnabled, synchYEnabled, synchWidthEnabled, synchHeightEnabled, 0f, 0f, 0f, 0f);
	}

	public WidgetSynch(Mode mode, boolean synchXEnabled, boolean synchYEnabled, boolean synchWidthEnabled, boolean synchHeightEnabled, float xOffset, float yOffset, float wOffset, float hOffset) {
		this(mode, null, synchXEnabled, synchYEnabled, synchWidthEnabled, synchHeightEnabled, xOffset, yOffset, wOffset, hOffset);
	}

	/**
	 * This constructor allows the configuring of which aspects to synchronize between the assigned Widget and its parent. 
	 * Offsets can also be inputted to tweak the values used for synchronization.
	 * 
	 * @param synchWith - the widget to synchronize with. This cannot be null if the Mode is set to MANUAL.
	 * @param synchXEnabled - synchronize the x-position with the parent (sets it to zero)
	 * @param synchYEnabled - synchronize the y-position with the parent (sets it to zero)
	 * @param synchWidthEnabled - synchronize the width with the parent (sets it to the parents width)
	 * @param synchHeightEnabled - synchronize the height with the parent (sets it to the parents height)
	 * @param xOffset - this value is added to the synchronized x-value
	 * @param yOffset - this value is added to the synchronized y-value
	 * @param wOffset - this value is added to the synchronized width-value
	 * @param hOffset - this value is added to the synchronized height-value
	 */
	public WidgetSynch(Mode mode, Widget synchWith, boolean synchXEnabled, boolean synchYEnabled, boolean synchWidthEnabled, boolean synchHeightEnabled, float xOffset, float yOffset, float wOffset, float hOffset) {
		this.mode = mode;
		this.synchWith = synchWith;
		
		this.synchXEnabled = synchXEnabled;
		this.synchYEnabled = synchYEnabled;
		this.synchWidthEnabled = synchWidthEnabled;
		this.synchHeightEnabled = synchHeightEnabled;
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.wOffset = wOffset;
		this.hOffset = hOffset;
	}

	@Override
	public void tick(NanoVGContext context,	WidgetAssembly rootWidgetAssembly) {
		synch(context);
	}

	/**
	 * This checks if each synchonization type is enabled and calls the corresponding functions. This is called every frame from tick() by default.
	 */
	public void synch(NanoVGContext context) {
		Mode activeMode = mode;
		
		if (activeMode == Mode.WITH_PARENT && parent.parent == null) {
			activeMode = Mode.WITH_FRAMEBUFFER;
		}
		
		if (synchXEnabled) {
			synchX(context);
		}
		
		if (synchYEnabled) {
			synchY(context);
		}
		
		if (synchWidthEnabled) {
			synchWidth(context, activeMode);
		}
		
		if (synchHeightEnabled) {
			synchHeight(context, activeMode);
		}
	}
	
	/**
	 * If X synchronization is enabled, this will be called, this will be called to synchronize the parent's X with its parent's X.
	 */
	protected void synchX(NanoVGContext context) {
		float internalOffsetX = 0f;
		
		if (mode == Mode.WITH_FRAMEBUFFER) {
			internalOffsetX = -parent.getClippedX();
		}
		
		parent.setX(internalOffsetX + xOffset);
	}
	
	/**
	 * If Y synchronization is enabled, this will be called, this will be called to synchronize the parent's Y with its parent's Y.
	 */
	protected void synchY(NanoVGContext context) {
		float internalOffsetY = 0f;
		
		if (mode == Mode.WITH_FRAMEBUFFER) {
			internalOffsetY = -parent.getClippedY();
		}
		
		parent.setY(internalOffsetY + yOffset);
	}
	
	/**
	 * If width synchronization is enabled, this will be called, this will be called to synchronize the parent's height with its parent's height.
	 */
	protected void synchWidth(NanoVGContext context, Mode mode) {
		switch(mode) {
		case WITH_PARENT:
			parent.setWidth(parent.parent.getWidth() + wOffset);
			break;
		case WITH_FRAMEBUFFER:
			parent.setWidth(context.getFramebufferWidth() + wOffset);
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
	protected void synchHeight(NanoVGContext context, Mode mode) {
		switch(mode) {
		case WITH_PARENT:
			parent.setHeight(parent.parent.getHeight() + hOffset);
			break;
		case WITH_FRAMEBUFFER:
			parent.setHeight(context.getFramebufferHeight() + hOffset);
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

	public boolean isSynchXEnabled() {
		return synchXEnabled;
	}

	public void setSynchXEnabled(boolean synchXEnabled) {
		this.synchXEnabled = synchXEnabled;
	}

	public boolean isSynchYEnabled() {
		return synchYEnabled;
	}

	public void setSynchYEnabled(boolean synchYEnabled) {
		this.synchYEnabled = synchYEnabled;
	}

	public boolean isSynchWidthEnabled() {
		return synchWidthEnabled;
	}

	public void setSynchWidthEnabled(boolean synchWidthEnabled) {
		this.synchWidthEnabled = synchWidthEnabled;
	}

	public boolean isSynchHeightEnabled() {
		return synchHeightEnabled;
	}

	public void setSynchHeightEnabled(boolean synchHeightEnabled) {
		this.synchHeightEnabled = synchHeightEnabled;
	}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {}

}
