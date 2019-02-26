package nokori.clear.vg.font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.nanovg.NanoVG.*;

import nokori.clear.vg.NanoVGContext;

public class Font {
	
	public static final int DEFAULT_TEXT_ALIGNMENT = NVG_ALIGN_LEFT|NVG_ALIGN_TOP;
	
	private String fontNameRegular, fontNameBold, fontNameItalic, fontNameLight;
	private File fileRegular, fileBold, fileItalic, fileLight;
	
	private FloatBuffer tempBuffer = BufferUtils.createFloatBuffer(1);
	
	public Font(String fontNameRegular, File fileRegular) {
		this(fontNameRegular, null, null, null, fileRegular, null, null, null);
	}
	
	public Font(String path, String fontNameRegular, String fontNameBold, String fontNameItalic, String fontNameLight) {
		this(fontNameRegular, fontNameBold, fontNameItalic, fontNameLight, 
				new File(path + fontNameRegular + ".ttf"), new File(path + fontNameBold + ".ttf"), new File(path + fontNameItalic + ".ttf"), new File(path + fontNameLight + ".ttf"));
	}
	
	public Font(String fontNameRegular, String fontNameBold, String fontNameItalic, String fontNameLight, 
			File fileRegular, File fileBold, File fileItalic, File fileLight) {
		
		this.fontNameRegular = fontNameRegular;
		this.fontNameBold = fontNameBold;
		this.fontNameItalic = fontNameItalic;
		this.fontNameLight = fontNameLight;
		
		this.fileRegular = fileRegular;
		this.fileBold = fileBold;
		this.fileItalic = fileItalic;
		this.fileLight = fileLight;
	}

	/**
	 * Prepares the Font with the given settings. Text alignment is set to the default.
	 * 
	 * @param context
	 * @param size
	 * @param textAlignment
	 * @param fontStyle
	 */
	public void configureNVG(NanoVGContext context, float size, FontStyle fontStyle) {
		configureNVG(context, size, DEFAULT_TEXT_ALIGNMENT, fontStyle);
	}
	
	/**
	 * Prepares the Font with the given settings.
	 * 
	 * @param context
	 * @param size
	 * @param textAlignment
	 * @param fontStyle
	 */
	public void configureNVG(NanoVGContext context, float size, int textAlignment, FontStyle fontStyle) {
		long vg = context.get();
		nvgFontFace(vg, getFontName(fontStyle));
		nvgFontSize(vg, size);
		nvgTextAlign(vg, textAlignment);
	}
	
	/*
	 * 
	 * 
	 * Getters/Setters
	 * 
	 * 
	 */
	
	/**
	 * Gets the font handle that corresponds to the given style.
	 * 
	 * @param style
	 * @return
	 */
	public String getFontName(FontStyle style) {
		switch(style) {
		case BOLD:
			return fontNameBold;
		case ITALIC:
			return fontNameItalic;
		case LIGHT:
			return fontNameLight;
		case REGULAR:
		default:
			return fontNameRegular;
		}
	}
	
	public float getLineHeight(NanoVGContext context, float size, FontStyle fontStyle) {
		return getLineHeight(context, size, DEFAULT_TEXT_ALIGNMENT, fontStyle);
	}
	
	public float getLineHeight(NanoVGContext context, float size, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, size, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), null, null, tempBuffer);
		return tempBuffer.get(0);
	}
	
	public float getLineAscend(NanoVGContext context, float size, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, size, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), tempBuffer, null, null);
		return tempBuffer.get(0);
	}
	
	public float getLineDescend(NanoVGContext context, float size, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, size, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), null, tempBuffer, null);
		return tempBuffer.get(0);
	}

	/**
	 * @return a Vector2f with the width and height of the given settings (x = w, y = h)
	 */
	public Vector2f getTextBounds(NanoVGContext context, String string, float size, int textAlignment, FontStyle style) {
		float[] bounds = new float[4];

		configureNVG(context, size, textAlignment, style);
		nvgTextBounds(context.get(), 0, 0, string, bounds);
		
		float width = bounds[2] - bounds[0];
		float height = bounds[3] - bounds[1];
		
		return new Vector2f(width, height);
	}
	
	/*
	 * 
	 * 
	 * Font Loading
	 * 
	 */
	
	/**
	 * Loads all of the data for this Font.
	 * 
	 * @param context
	 * @throws IOException
	 * 
	 * @return this Font
	 */
	public Font load(NanoVGContext context) throws IOException {
		if (fontNameRegular != null && fileRegular != null) {
			load(context, fontNameRegular, fileRegular);
		}

		if (fontNameBold != null && fileBold != null) {
			load(context, fontNameBold, fileBold);
		}
		
		if (fontNameItalic != null && fileItalic != null) {
			load(context, fontNameItalic, fileItalic);
		}
		
		if (fontNameLight != null && fileLight != null) {
			load(context, fontNameLight, fileLight);
		}
		
		return this;
	}
	
	private void load(NanoVGContext context, String name, File file) throws IOException {
		ByteBuffer dataBuffer = ioResourceToByteBuffer(file.getAbsolutePath(), (int) file.getTotalSpace());
		nvgCreateFontMem(context.get(), name, dataBuffer, 0);
	}
	
	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param resource the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException if an IO error occurs
	 */
	private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		System.out.println(resource);
		
		ByteBuffer buffer;

		Path path = Paths.get(resource);
		if (Files.isReadable(path)) {
			
			try (SeekableByteChannel fc = Files.newByteChannel(path)) {
				buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
				while (fc.read(buffer) != -1) {
					;
				}
			}
			
		} else {
			try (InputStream source = Font.class.getClassLoader().getResourceAsStream(resource);
					ReadableByteChannel rbc = Channels.newChannel(source)) {
				
				buffer = BufferUtils.createByteBuffer(bufferSize);

				while (true) {
					int bytes = rbc.read(buffer);
					if (bytes == -1) {
						break;
					}
					if (buffer.remaining() == 0) {
						buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
					}
				}
			}
		}

		buffer.flip();
		return buffer.slice();
	}
}
