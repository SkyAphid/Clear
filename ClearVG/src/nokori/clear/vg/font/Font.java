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
import java.util.ArrayList;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

import nokori.clear.vg.NanoVGContext;

public class Font {
	
	public static final int DEFAULT_TEXT_ALIGNMENT = NVG_ALIGN_LEFT|NVG_ALIGN_TOP;
	
	private String fontNameRegular, fontNameBold, fontNameItalic, fontNameLight;
	private File fileRegular, fileBold, fileItalic, fileLight;
	
	private ByteBuffer regularDataBuffer, boldDataBuffer, italicDataBuffer, lightDataBuffer;
	
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
	 * @param fontSize
	 * @param textAlignment
	 * @param fontStyle
	 */
	public void configureNVG(NanoVGContext context, float fontSize, FontStyle fontStyle) {
		configureNVG(context, fontSize, DEFAULT_TEXT_ALIGNMENT, fontStyle);
	}
	
	/**
	 * Prepares the Font with the given settings.
	 * 
	 * @param context
	 * @param fontSize
	 * @param textAlignment
	 * @param fontStyle
	 */
	public void configureNVG(NanoVGContext context, float fontSize, int textAlignment, FontStyle fontStyle) {
		long vg = context.get();
		nvgFontFace(vg, getFontName(fontStyle));
		nvgFontSize(vg, fontSize);
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
	 * @param fontStyle
	 * @return
	 */
	public String getFontName(FontStyle fontStyle) {
		switch(fontStyle) {
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
	
	public float getHeight(NanoVGContext context, float fontSize, FontStyle fontStyle) {
		return getHeight(context, fontSize, DEFAULT_TEXT_ALIGNMENT, fontStyle);
	}
	
	public float getHeight(NanoVGContext context, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), null, null, tempBuffer);
		return tempBuffer.get(0);
	}
	
	public float getHeight(NanoVGContext context, int numberOfLines, float fontSize, int textAlignment, FontStyle fontStyle) {
		return (numberOfLines * getHeight(context, fontSize, textAlignment, fontStyle));
	}
	
	public float getAscend(NanoVGContext context, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), tempBuffer, null, null);
		return tempBuffer.get(0);
	}
	
	public float getDescend(NanoVGContext context, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		nvgTextMetrics(context.get(), null, tempBuffer, null);
		return tempBuffer.get(0);
	}
	
	/**
	 * @return a Vector2f with the width and height of the given settings (x = w, y = h)
	 */
	public Vector2f getTextBounds(NanoVGContext context, String string, float fontSize, int textAlignment, FontStyle fontStyle) {
		float[] bounds = new float[4];

		configureNVG(context, fontSize, textAlignment, fontStyle);
		nvgTextBounds(context.get(), 0, 0, string, bounds);
		NanoVG.nvgReset(context.get());

		float width = bounds[2] - bounds[0];
		float height = bounds[3] - bounds[1];
		
		return new Vector2f(width, height);
	}
	
	public void split(NanoVGContext context, ArrayList<String> lines, String string, float maxLineWidth, float fontSize, int textAlignment, FontStyle fontStyle) {
		if (lines == null) {
			lines = new ArrayList<String>();
		} else {
			lines.clear();
		}
		
		StringBuilder builder = new StringBuilder();
		
		int len = string.length();
		float advanceX = 0;
		
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			
			if (c != '\n') {
				builder.append(c);
			}

			if (c == '\n') {
				lines.add(builder.toString());
				builder.setLength(0);
				advanceX = 0;
				continue;
			}

			float a = getTextBounds(context, Character.toString(c), fontSize, textAlignment, fontStyle).x;

			if (advanceX + a > maxLineWidth) {
				lines.add(builder.toString());
				builder.setLength(0);
				advanceX = 0;
			}

			advanceX += a;

			if (isBreakable(c)) {
				float wordLength = getWordLength(context, string, len, i+1, fontSize, textAlignment, fontStyle);

				if (advanceX + wordLength > maxLineWidth) {
					lines.add(builder.toString());
					builder.setLength(0);
					advanceX = 0;
				}
			}
		}

		if(builder.length() > 0){
			lines.add(builder.toString());
		}
	}
	
	private float getWordLength(NanoVGContext context, String string, int len, int i, float fontSize, int textAlignment, FontStyle fontStyle) {

		boolean isCommand = false;
		
		float advance = 0;
		for(; i < string.length(); i++){
			
			char c = string.charAt(i);
			char next = (i+1) < len ? string.charAt(i+1) : 0;
			
			if(!isCommand){
				if(c == '\\' && next == '{'){
					c = next; // escaped curly brace {
					i++;
				}else{
					if(c == '{'){
						isCommand = true;
						continue;
					}
				}

				advance += getTextBounds(context, Character.toString(c), fontSize, textAlignment, fontStyle).x;
					
				if(isBreakable(c)){
					return advance;
				}
			}else{
				if(c == '}'){
					isCommand = false;
				}
			}
		}
		
		return advance;
	}
	
	private boolean isBreakable(char c) {
		return c == ' ' || c == '\n';
	}
	
	public String getFontNameRegular() {
		return fontNameRegular;
	}

	public String getFontNameBold() {
		return fontNameBold;
	}

	public String getFontNameItalic() {
		return fontNameItalic;
	}

	public String getFontNameLight() {
		return fontNameLight;
	}

	public ByteBuffer getRegularDataBuffer() {
		return regularDataBuffer;
	}

	public ByteBuffer getBoldDataBuffer() {
		return boldDataBuffer;
	}

	public ByteBuffer getItalicDataBuffer() {
		return italicDataBuffer;
	}

	public ByteBuffer getLightDataBuffer() {
		return lightDataBuffer;
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
	 * The buffers used to make the fonts are stored as variables to ensure that they stay in memory to prevent NanoVG errors/crashes.
	 * 
	 * @param context
	 * @throws IOException
	 * 
	 * @return this Font
	 */
	public Font load(NanoVGContext context) throws IOException {
		if (fontNameRegular != null && fileRegular != null) {
			regularDataBuffer = createFont(context, fontNameRegular, fileRegular);
		}

		if (fontNameBold != null && fileBold != null) {
			boldDataBuffer = createFont(context, fontNameBold, fileBold);
		}
		
		if (fontNameItalic != null && fileItalic != null) {
			italicDataBuffer = createFont(context, fontNameItalic, fileItalic);
		}
		
		if (fontNameLight != null && fileLight != null) {
			lightDataBuffer = createFont(context, fontNameLight, fileLight);
		}
		
		return this;
	}
	
	private ByteBuffer createFont(NanoVGContext context, String name, File file) throws IOException {
		ByteBuffer dataBuffer = ioResourceToByteBuffer(file.getAbsolutePath(), (int) file.getTotalSpace());
		nvgCreateFontMem(context.get(), name, dataBuffer, 0);
		return dataBuffer;
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
	
	/**
	 * For internal use by ioResourceToByteBuffer
	 */
	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

}
