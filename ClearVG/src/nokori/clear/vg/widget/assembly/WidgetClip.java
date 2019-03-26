package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class WidgetClip extends Widget {
	
	public enum Alignment {
		UPPER_LEFT, UPPER_CENTER, UPPER_RIGHT,
		CENTER_LEFT, CENTER, CENTER_RIGHT,
		BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
	};
	
	private Alignment alignment;
	private float xPadding, yPadding;
	
	public WidgetClip(Alignment alignment) {
		this(alignment, 0f, 0f);
	}
	
	public WidgetClip(Alignment alignment, float xPadding, float yPadding) {
		this.alignment = alignment;
		this.xPadding = xPadding;
		this.yPadding = yPadding;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
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
		case UPPER_CENTER:
			clipX = centerX;
			clipY = topY;
			break;
		case UPPER_LEFT:
			clipX = leftX;
			clipY = topY;
			break;
		case UPPER_RIGHT:
			clipX = rightX;
			clipY = topY;
			break;
		default:
			break;
		}
		
		clipX += xPadding;
		clipY += yPadding;

		parent.setX(clipX);
		parent.setY(clipY);
	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {
		
	}

}
