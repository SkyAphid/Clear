package nokori.clear.vg.util;

import org.lwjgl.nanovg.NanoVG;

import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;

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
			
			NanoVG.nvgBeginPath(vg);
			NanoVG.nvgMoveTo(vg, sx, sy);
			NanoVG.nvgBezierTo(vg, c1x, c1y, c2x, c2y, ex, ey);

			strokeFill.tallocNVG(strokeFill -> {
				NanoVG.nvgStrokeColor(vg, strokeFill);
				NanoVG.nvgStroke(vg);
				NanoVG.nvgStrokeWidth(vg, strokeThickness);
			});

			NanoVG.nvgClosePath(vg);
		}
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

}
