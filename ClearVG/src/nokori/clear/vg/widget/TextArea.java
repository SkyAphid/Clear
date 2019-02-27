package nokori.clear.vg.widget;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGGlyphPosition;
import org.lwjgl.nanovg.NVGTextRow;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.attachments.FillAttachment;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class TextArea extends Widget implements FillAttachment {

	/*
	 * Customization
	 */
	
	public static final String TAB = "\u200C        ";

	private ClearColor fill;
	private String text;
	private Font font;
	private float fontSize;
	private FontStyle fontStyle = FontStyle.REGULAR;
	
	private ClearColor highlightFill = ClearColor.LIGHT_BLUE;
	private ClearColor caretFill = ClearColor.LIGHT_BLACK;

	private boolean lineNumbersEnabled = true;
	private Font lineNumberFont;
	private FontStyle lineNumberFontStyle = FontStyle.REGULAR;
	private float lineNumberFontSize;
	private ClearColor lineNumberFill = ClearColor.LIGHT_BLACK;

	/*
	 * Data
	 */
	
	private static final int TEXT_ALIGNMENT = NVG_ALIGN_LEFT | NVG_ALIGN_TOP;
	
	private boolean allocationRequired = true;
	
	private NVGTextRow.Buffer rows;
	private NVGGlyphPosition.Buffer glyphs;
	private ByteBuffer paragraph;
	
	private FloatBuffer lineh = BufferUtils.createFloatBuffer(1);
	private FloatBuffer bounds = BufferUtils.createFloatBuffer(4);

	public TextArea(float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		this(0, 0, width, height, fill, text, font, fontSize);
	}

	public TextArea(float x, float y, float width, float height, ClearColor fill, String text, Font font, float fontSize) {
		super(x, y, width, height);
		this.fill = fill;
		this.font = font;
		this.fontSize = fontSize;

		lineNumberFont = font;
		lineNumberFontSize = (int) (fontSize * 0.75);

		setText(text);
	}
	
	public void setText(String text) {
		this.text = text;

		paragraph = null;
		
		if (rows != null) {
			rows.free();
			rows = null;
		}

		if (glyphs != null) {
			glyphs.free();
			glyphs = null;
		}
		
		allocationRequired = true;
	}

	@Override
	public void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {

	}

	@Override
	public void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		drawParagraph(context, getRenderX(), getRenderY(), size.x, size.y, (float) window.getMouseX(), (float) window.getMouseY());
	}
	
	private void drawParagraph(NanoVGContext context, float x, float y, float width, float height, float mx, float my) {
		long vg = context.get();
		
		if (allocationRequired) {
			paragraph = memUTF8(text, false);
			rows = NVGTextRow.create((int) (height / font.getHeight(context, fontSize, fontStyle) + 10));
			glyphs = NVGGlyphPosition.create(text.length());
		}
		
		NVGColor highlightFill = this.highlightFill.callocNVG();
		NVGColor fill = this.fill.callocNVG();

		float gx = 0.0f, gy = 0.0f;
		int lineNumber = 0;
		float scroll = 0f;

		// Save NVG and enable scissoring (only render text in given bounds)
		nvgSave(vg);
		nvgScissor(vg, x, y, width, height);
		nvgTranslate(vg, 0, -height * scroll);

		// Set font
		nvgFontSize(vg, fontSize);
		nvgFontFace(vg, font.getFontName(fontStyle));
		nvgTextAlign(vg, TEXT_ALIGNMENT);
		nvgTextMetrics(vg, null, null, lineh);

		long start = memAddress(paragraph);
		long end = start + paragraph.remaining();
		int nrows, lnum = 0;

		// Begin rendering glyphs
		while ((nrows = nnvgTextBreakLines(vg, start, end, width, memAddress(rows), 3)) != 0) {
			for (int i = 0; i < nrows; i++) {

				NVGTextRow row = rows.get(i);
				boolean hit = mx > x && mx < (x + width) && my >= y && my < (y + lineh.get(0));

				nvgBeginPath(vg);
				nvgFillColor(vg, highlightFill);
				nvgRect(vg, x, y, row.width(), lineh.get(0));
				nvgFill(vg);

				nvgFillColor(vg, fill);
				
				nnvgText(vg, x, y, row.start(), row.end());

				if (hit) {
					drawCaret(vg, row, lineh.get(0), x, y, mx);

					lineNumber = lnum + 1;
					gx = x - 10;
					gy = y + lineh.get(0) / 2;
				}
				
				lnum++;
				y += lineh.get(0);
			}

			// Keep going...
			start = rows.get(nrows - 1).next();
		}

		if (lineNumber != 0) {
			drawLineNumber(vg, lineNumber, gx, gy, bounds);
		}

		y += 20.0f;

		nvgRestore(vg);

		highlightFill.free();
		
		if (fill != null) {
			fill.free();
		}
	}
	
	private void drawCaret(long vg, NVGTextRow row, float lineh, float x, float y, float mx) {
		caretFill.stackPushLambda(fill -> {
			/*
			 * Calculate caret position
			 */

			float caretx = (mx < x + row.width() / 2) ? x : x + row.width();
			float px = x;
			int nglyphs = nnvgTextGlyphPositions(vg, x, y, row.start(), row.end(), memAddress(glyphs), 100);

			for (int j = 0; j < nglyphs; j++) {
				NVGGlyphPosition glyphPosition = glyphs.get(j);

				float x0 = glyphPosition.x();
				float x1 = (j + 1 < nglyphs) ? glyphs.get(j + 1).x() : x + row.width();
				float gx2 = x0 * 0.3f + x1 * 0.7f;

				if (mx >= px && mx < gx2) {
					caretx = glyphPosition.x();
				}

				px = gx2;
			}

			/*
			 * Render caret
			 */

			nvgBeginPath(vg);
			nvgFillColor(vg, fill);
			nvgRect(vg, caretx, y, 1, lineh);
			nvgFill(vg);
		});
	}

	private void drawLineNumber(long vg, int lineNumber, float gx, float gy, FloatBuffer bounds) {
		lineNumberFill.stackPushLambda(fill -> {
			String s = Integer.toString(lineNumber);

			nvgFontFace(vg, lineNumberFont.getFontName(lineNumberFontStyle));
			nvgFontSize(vg, lineNumberFontSize);
			nvgTextAlign(vg, NVG_ALIGN_RIGHT | NVG_ALIGN_MIDDLE);

			nvgTextBounds(vg, gx, gy, s, bounds);

			nvgBeginPath(vg);
			nvgFillColor(vg, fill);
			nvgText(vg, gx, gy, s);
		});
	}
	
	
	
	/*
	 * 
	 * General Settings
	 * 
	 */

	@Override
	public ClearColor getFill() {
		return fill;
	}

	public void setFill(ClearColor fill) {
		this.fill = fill;
	}
	
	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
	
	public ClearColor getHighlightFill() {
		return highlightFill;
	}

	public void setHighlightFill(ClearColor highlightFill) {
		this.highlightFill = highlightFill;
	}

	public ClearColor getCaretFill() {
		return caretFill;
	}

	public void setCaretFill(ClearColor caretFill) {
		this.caretFill = caretFill;
	}

	/*
	 * 
	 * Line Numbers
	 * 
	 */

	public boolean isLineNumbersEnabled() {
		return lineNumbersEnabled;
	}

	public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
		this.lineNumbersEnabled = lineNumbersEnabled;
	}

	public Font getLineNumberFont() {
		return lineNumberFont;
	}

	public void setLineNumberFont(Font lineNumberFont) {
		this.lineNumberFont = lineNumberFont;
	}

	public float getLineNumberFontSize() {
		return lineNumberFontSize;
	}

	public void setLineNumberFontSize(float lineNumberFontSize) {
		this.lineNumberFontSize = lineNumberFontSize;
	}

	public ClearColor getLineNumberFill() {
		return lineNumberFill;
	}

	public void setLineNumberFill(ClearColor lineNumberFill) {
		this.lineNumberFill = lineNumberFill;
	}

	public FontStyle getLineNumberFontStyle() {
		return lineNumberFontStyle;
	}

	public void setLineNumberFontStyle(FontStyle lineNumberFontStyle) {
		this.lineNumberFontStyle = lineNumberFontStyle;
	}

	@Override
	public void dispose() {
	}
}
