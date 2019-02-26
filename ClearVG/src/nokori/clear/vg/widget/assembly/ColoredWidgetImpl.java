package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.widget.attachments.FillAttachment;

public abstract class ColoredWidgetImpl extends Widget implements FillAttachment {

	protected ClearColor fill, strokeFill;
	
	public ColoredWidgetImpl(float x, float y, float width, float height, ClearColor fill, ClearColor strokeFill) {
		pos = new Vector2f(x, y);
		size = new Vector2f(width, height);
		this.fill = fill;
		this.strokeFill = strokeFill;
	}
	
	@Override
	public ClearColor getFill() {
		return fill;
	}
	
	@Override
	public ClearColor getStrokeFill() {
		return strokeFill;
	}
}
