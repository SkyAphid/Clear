package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;

public class WidgetClip extends Widget {
	
	public enum Alignment {
		TOP_LEFT, TOP_CENTER, TOP_RIGHT,
		CENTER_LEFT, CENTER, CENTER_RIGHT,
		BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
	};
	
	private Alignment alignment;
	private float xPadding, yPadding;
	
	private boolean xModdingEnabled;
	private boolean yModdingEnabled;
	
	public WidgetClip(Alignment alignment) {
		this(alignment, 0f, 0f);
	}
	
	public WidgetClip(Alignment alignment, float xPadding, float yPadding) {
		this(alignment, xPadding, yPadding, true, true);
	}
	
	public WidgetClip(Alignment alignment, float xPadding, float yPadding, boolean xModdingEnabled, boolean yModdingEnabled) {
		this.alignment = alignment;
		this.xPadding = xPadding;
		this.yPadding = yPadding;
		this.xModdingEnabled = xModdingEnabled;
		this.yModdingEnabled = yModdingEnabled;
	}

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		if (parent == null || parent.parent == null) {
			System.err.println("WARNING: WidgetClip isn't attached to another Widget, or that Widget isn't attached to another Widget!"
					+ "\nBoth must be present for a WidgetClip to work correctly! (Heirarchy: Widget -> Child Widget -> Widget Clip)");
			return;
		}

		float containerWidth = parent.parent.getWidth();
		float containerHeight = parent.parent.getHeight();
		
		float centerX = containerWidth/2 - parent.getWidth()/2;
		float centerY = containerHeight/2 - parent.getHeight()/2;
		
		float leftX = 0f;
		float rightX = containerWidth - parent.getWidth();
		
		float topY = 0f;
		float bottomY = containerHeight - parent.getHeight();
		
		float clipX = parent.getX();
		float clipY = parent.getY();
		
		switch(alignment) {
		case BOTTOM_CENTER:
			clipX = centerX;
			clipY = bottomY;
			break;
		case BOTTOM_LEFT:
			clipX = leftX;
			clipY = bottomY;
			break;
		case BOTTOM_RIGHT:
			clipX = rightX;
			clipY = bottomY;
			break;
		case CENTER:
			clipX = centerX;
			clipY = centerY;
			break;
		case CENTER_LEFT:
			clipX = leftX;
			clipY = centerY;
			break;
		case CENTER_RIGHT:
			clipX = rightX;
			clipY = centerY;
			break;
		case TOP_CENTER:
			clipX = centerX;
			clipY = topY;
			break;
		case TOP_LEFT:
			clipX = leftX;
			clipY = topY;
			break;
		case TOP_RIGHT:
			clipX = rightX;
			clipY = topY;
			break;
		default:
			break;
		}
		
		clipX += xPadding;
		clipY += yPadding;

		if (xModdingEnabled) {
			parent.setX(clipX);
		}

		if (yModdingEnabled) {
			parent.setY(clipY);
		}
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	public float getxPadding() {
		return xPadding;
	}

	public void setxPadding(float xPadding) {
		this.xPadding = xPadding;
	}

	public float getyPadding() {
		return yPadding;
	}

	public void setyPadding(float yPadding) {
		this.yPadding = yPadding;
	}

	public boolean isxModdingEnabled() {
		return xModdingEnabled;
	}

	public void setxModdingEnabled(boolean xModdingEnabled) {
		this.xModdingEnabled = xModdingEnabled;
	}

	public boolean isyModdingEnabled() {
		return yModdingEnabled;
	}

	public void setyModdingEnabled(boolean yModdingEnabled) {
		this.yModdingEnabled = yModdingEnabled;
	}

	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {
		
	}

}
