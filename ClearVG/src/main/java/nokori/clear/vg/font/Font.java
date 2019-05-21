package nokori.clear.vg.font;

import nokori.clear.vg.NanoVGContext;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

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
import java.util.Locale;

import static org.lwjgl.nanovg.NanoVG.*;

public class Font {
	
	public static final int DEFAULT_TEXT_ALIGNMENT = NVG_ALIGN_LEFT|NVG_ALIGN_TOP;
	
	private String fontNameRegular, fontNameBold, fontNameItalic, fontNameLight;
	private File fileRegular, fileBold, fileItalic, fileLight;
	
	private ByteBuffer regularDataBuffer, boldDataBuffer, italicDataBuffer, lightDataBuffer;
	
	private FloatBuffer tempBuffer = BufferUtils.createFloatBuffer(1);
	private FloatBuffer boundsTempBuffer = null;
	
	/**
	 * Creates a font just using the font path. The files in the given folder are scanned and their type will be assumed.
	 * <br><br>
	 * Here's how to ensure your font files work with this constructor:
	 * <br>• Regular font files must include "regular" somewhere in their name (case does not matter)
	 * <br>• Bold font files must include "bold" somewhere in their name (case does not matter)
	 * <br>• Italic font files must include "italic" somewhere in their name (case does not matter)
	 * <br>• Light font files must include "light" somewhere in their name (case does not matter)
	 * 
	 * @param path
	 */
	public Font(File path) {
		this(path.getAbsolutePath() + "/", getFontName(path, "regular"), getFontName(path, "bold"), getFontName(path, "italic"), getFontName(path, "light"));
	}
	
	private static String getFontName(File path, String nameIncludes) {
		File[] files = path.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			
			if (name.toLowerCase(Locale.ENGLISH).contains(nameIncludes)) {
				String fontName = name.substring(0, name.lastIndexOf("."));
				//System.out.println(nameIncludes + " -> " + fontName);
				return fontName;
			}
		}
		
		return null;
	}
	
	/**
	 * Creates a font that only supports the regular style. Mostly meant to be used for testing purposes. Supports only TrueTypeFonts (TTF)
	 * 
	 * @param fontNameRegular - the identifier for the regular font
	 * @param fileRegular - the file location of the regular font
	 */
	public Font(String fontNameRegular, File fileRegular) {
		this(fontNameRegular, null, null, null, fileRegular, null, null, null);
	}
	
	/**
	 * A more straight-forward short-hand version of the main constructor. Supports only TrueTypeFonts (TTF)
	 * 
	 * @param path - a single path with all font files
	 * @param fontNameRegular - the file name of the regular font, which will also be used as the identifier
	 * @param fontNameBold - the file name of the bold font, which will also be used as the identifier
	 * @param fontNameItalic - the file name of the italic font, which will also be used as the identifier
	 * @param fontNameLight - the file name of the light font, which will also be used as the identifier
	 */
	public Font(String path, String fontNameRegular, String fontNameBold, String fontNameItalic, String fontNameLight) {
		this(fontNameRegular, fontNameBold, fontNameItalic, fontNameLight, 
				new File(path + fontNameRegular + ".ttf"), new File(path + fontNameBold + ".ttf"), new File(path + fontNameItalic + ".ttf"), new File(path + fontNameLight + ".ttf"));
	}
	
	/**
	 * Creates a font with the given styles. Supports only TrueTypeFonts (TTF)
	 * 
	 * @param fontNameRegular - the identifier for the regular font
	 * @param fontNameBold - the identifier for the bold font
	 * @param fontNameItalic - the identifier for the italic font
	 * @param fontNameLight - the identifier for the light font
	 * @param fileRegular - the file location of the regular font
	 * @param fileBold - the file location of the bold font
	 * @param fileItalic - the file location of the italic font
	 * @param fileLight - the file location of the light font
	 */
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
	
	/**
	 * Get's the font height using the given settings.
	 * 
	 * @param context
	 * @param fontSize
	 * @param fontStyle
	 * @return
	 */
	public float getHeight(NanoVGContext context, float fontSize, FontStyle fontStyle) {
		return getHeight(context, fontSize, DEFAULT_TEXT_ALIGNMENT, fontStyle);
	}
	
	/**
	 * Get's the font height using the given settings.
	 * 
	 * @param context
	 * @param fontSize
	 * @param textAlignment
	 * @param fontStyle
	 * @return
	 */
	public float getHeight(NanoVGContext context, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		return getHeight(context);
	}
	
	/**
	 * Get's the font height using the current NanoVG settings. This function will assume the font has already been set up either by using <code>configureNVG</code> or manually 
	 * through NanoVG directly.
	 * 
	 * @param context
	 * @return
	 */
	public float getHeight(NanoVGContext context) {
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
	
	public Vector2f getTextBounds(NanoVGContext context, Vector2f boundsResultVector, ArrayList<String> lines, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		
		boundsResultVector.set(0f, 0f);
		Vector2f tempVector = new Vector2f();
		
		for (int i = 0; i < lines.size(); i++) {
			tempVector = getTextBounds(context, tempVector, lines.get(i));
			
			if (tempVector.x() > boundsResultVector.x()) {
				boundsResultVector.x = tempVector.x();
			}
			
			boundsResultVector.y += tempVector.y();
		}
		
		return boundsResultVector;
	}
	
	/**
	 * @return a Vector2f with the width and height of the given settings (x = w, y = h)
	 */
	public Vector2f getTextBounds(NanoVGContext context, Vector2f boundsResultVector, String string, float fontSize, int textAlignment, FontStyle fontStyle) {
		configureNVG(context, fontSize, textAlignment, fontStyle);
		return getTextBounds(context, boundsResultVector, string);
	}
	
	/**
	 * Gets the given text bounds, assuming that you've already configured the font in NanoVG yourself. To have this class do it for you, call the longer version of this method.
	 * 
	 * @param context
	 * @param boundsResultVector
	 * @param string
	 * @return
	 */
	public Vector2f getTextBounds(NanoVGContext context, Vector2f boundsResultVector, String string) {
		if (boundsTempBuffer == null) {
			boundsTempBuffer = BufferUtils.createFloatBuffer(4);
		}

		nvgTextBounds(context.get(), 0, 0, string, boundsTempBuffer);

		float width = boundsTempBuffer.get(2) - boundsTempBuffer.get(0);
		float height = boundsTempBuffer.get(3) - boundsTempBuffer.get(1);
		
		boundsResultVector.set(width, height);
		
		return boundsResultVector;
	}
	
	public void split(NanoVGContext context, ArrayList<String> lines, String string, float maxLineWidth, float fontSize, int textAlignment, FontStyle fontStyle) {
		if (lines == null) {
			lines = new ArrayList<String>();
		} else {
			lines.clear();
		}
		
		//System.out.println("Split start");
		
		StringBuilder builder = new StringBuilder();
		Vector2f boundsResultVector = new Vector2f(0f, 0f);
		
		int len = string.length();
		float advanceX = 0;
		
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			
			//System.out.println("Char " + i + " " + (c == '\n'));
			
			builder.append(c);

			if (c == '\n') {
				//System.out.println("Add (1) - > " + builder.toString());
				lines.add(builder.toString());
				builder.setLength(0);
				advanceX = 0;
				continue;
			}

			float a = getTextBounds(context, boundsResultVector, Character.toString(c), fontSize, textAlignment, fontStyle).x;

			if (advanceX + a > maxLineWidth) {
				//System.out.println("Add (2) - > " + builder.toString());
				lines.add(builder.toString());
				builder.setLength(0);
				advanceX = 0;
			}

			advanceX += a;

			if (isBreakable(c)) {
				float wordLength = getWordLength(context, boundsResultVector, string, len, i+1, fontSize, textAlignment, fontStyle);

				if (advanceX + wordLength > maxLineWidth) {
					//System.out.println("Add (3) - > " + builder.toString());
					lines.add(builder.toString());
					builder.setLength(0);
					advanceX = 0;
				}
			}
		}

		if(builder.length() > 0){
			//System.out.println("Add (4) - > " + builder.toString());
			lines.add(builder.toString());
		}
		
		if (string.endsWith("\n")) {
			//System.out.println("Add (3) Blank New Line");
			lines.add("");
		}
		
		//System.out.println("Split end: " + lines.size());
	}
	
	private float getWordLength(NanoVGContext context, Vector2f boundsVector, String string, int len, int i, float fontSize, int textAlignment, FontStyle fontStyle) {

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

				advance += getTextBounds(context, boundsVector, Character.toString(c), fontSize, textAlignment, fontStyle).x;
					
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
	
	public enum FallbackFontMode {
		/**
		 * Only the regular font is added as a fallback to this fonts regular font
		 */
		REGULAR_ONLY,
		
		/**
		 * The regular font on the fallback font is applied to all of the fonts in the receiving font (regular, italic, bold, light)
		 */
		REGULAR_TO_ALL,
		
		/**
		 * All corresponding fonts are added as fallback fonts to the receiving font
		 */
		ALL
	}
	
	/**
	 * Adds a Fallback Font to this Font with the given settings (FallbackMode). 
	 * 
	 * <br><br>An example of a fallback font being needed is a situation where font doesn't have a special character that you need (e.g. an emoji character), you can 
	 * add a font that contains the desired special character as a fallback font and it'll render the special character from that font in the place of the missing one.
	 * 
	 * @param context - the NanoVGContext necessary for assigning the fallback font
	 * @param font - the font to assign as a fallback font to this font
	 * @param mode - the fallback font mode. See the descriptions of each enumerator to see what they do.
	 * @return
	 */
	public Font addFallbackFont(NanoVGContext context, Font font, FallbackFontMode mode) {
		nvgAddFallbackFont(context.get(), fontNameRegular, font.fontNameRegular);
		
		if (mode == FallbackFontMode.REGULAR_TO_ALL) {
			nvgAddFallbackFont(context.get(), fontNameBold, font.fontNameRegular);
			nvgAddFallbackFont(context.get(), fontNameItalic, font.fontNameRegular);
			nvgAddFallbackFont(context.get(), fontNameLight, font.fontNameRegular);
		}
		
		if (mode == FallbackFontMode.ALL) {
			nvgAddFallbackFont(context.get(), fontNameBold, font.fontNameBold);
			nvgAddFallbackFont(context.get(), fontNameItalic, font.fontNameItalic);
			nvgAddFallbackFont(context.get(), fontNameLight, font.fontNameLight);
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
