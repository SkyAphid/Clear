package nokori.clear.vg.transition;

import nokori.clear.vg.widget.assembly.Widget;

/**
 * This transition will move a Widget to the target position. Good for fancy transitions in when a new widget is created.
 */
public class WidgetPositionTransition extends Transition {
	
	private Widget widget;
	private float startX, startY, targetX, targetY;

	public WidgetPositionTransition(Widget widget, long durationInMillis, float targetX, float targetY) {
		this(widget, durationInMillis, widget.getX(), widget.getY(), targetX, targetY);
	}
	
	public WidgetPositionTransition(Widget widget, long durationInMillis, float startX, float startY, float targetX, float targetY) {
		super(durationInMillis);
		this.widget = widget;
		this.startX = startX;
		this.startY = startY;
		this.targetX = targetX;
		this.targetY = targetY;
	}

	@Override
	public void tick(float progress) {
		float endX = startX + ((targetX - startX) * progress);
		float endY = startY + ((targetY - startY) * progress);
		
		widget.setX(endX);
		widget.setY(endY);
	}

}
