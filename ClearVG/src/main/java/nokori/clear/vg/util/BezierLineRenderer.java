package nokori.clear.vg.util;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;

import static org.lwjgl.nanovg.NanoVG.*;

public class BezierLineRenderer {
	
	private float sx, sy, ex, ey, c1x, c1y, c2x, c2y;
	private float strokeThickness = 1f;
	
	private ClearColor strokeFill = null;
	
	public BezierLineRenderer(ClearColor strokeFill) {
		this.strokeFill = strokeFill;
	}

	public void render(NanoVGContext context) {
		if (strokeFill != null) {
			long vg = context.get();
			
			float c1x = (float) (sx + this.c1x);
			float c1y = (float) (sy + this.c1y);
			
			float c2x = (float) (sx + this.c2x);
			float c2y = (float) (sy + this.c2y);
			
			nvgBeginPath(vg);
			nvgMoveTo(vg, sx, sy);
			nvgBezierTo(vg, c1x, c1y, c2x, c2y, ex, ey);

			strokeFill.tallocNVG(strokeFill -> {
				nvgStrokeColor(vg, strokeFill);
				nvgStrokeWidth(vg, strokeThickness);
				nvgStroke(vg);
			});

			nvgClosePath(vg);
		}
	}
	
	public void setStartAndControl1Position(float sx, float sy, float cx, float cy) {
		setStartPosition(sx, sy);
		setControl1Position(cx, cy);
	}
	
	public void setEndAndControl2Position(float ex, float ey, float cx, float cy) {
		setEndPosition(ex, ey);
		setControl2Position(cx, cy);
	}
	
	/**
	 * Equivalent to setAbsolutePosition(). This method is purely syntax sugar to make the start/end points more clear from a writing perspective.
	 * 
	 * @param sx
	 * @param sy
	 */
	public void setStartPosition(float sx, float sy) {
		this.sx = sx;
		this.sy = sy;
	}
	
	/**
	 * Sets the ending point of the bezier line.
	 * 
	 * @param x1 - end x
	 * @param x2 - end y
	 */
	public void setEndPosition(float ex, float ey) {
		this.ex = ex;
		this.ey = ey;
	}
	
	/**
	 * Sets the first control point of the line.
	 * 
	 * @param c1x
	 * @param c1y
	 */
	public void setControl1Position(float c1x, float c1y) {
		this.c1x = c1x;
		this.c1y = c1y;
	}

	/**
	 * Sets the first control point of the line.
	 * 
	 * @param c1x
	 * @param c1y
	 */
	public void setControl2Position(float c2x, float c2y) {
		this.c2x = c2x;
		this.c2y = c2y;
	}
	
	/**
	 * Sets the thickness of the line. By default it's 1.0f.
	 * 
	 * @param strokeThickness
	 */
	public void setStrokeThickness(float strokeThickness) {
		this.strokeThickness = strokeThickness;
	}

	public void setStrokeAlpha(float alpha) {
		strokeFill.alpha(alpha);
	}
}
