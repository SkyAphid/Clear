package nokori.util.glfw;

public class PixelFormat {
	

	
	private int redBits;
	private int greenBits;
	private int blueBits;
	
	private int alphaBits;
	
	private int depthBits;
	private int stencilBits;
	
	private int msaaSamples;
	
	
	public PixelFormat() {
		this(8, 8, 8);
	}


	public PixelFormat(int redBits, int greenBits, int blueBits) {
		this(redBits, greenBits, blueBits, 0);
	}


	public PixelFormat(int redBits, int greenBits, int blueBits, int alphaBits) {
		this(redBits, greenBits, blueBits, alphaBits, 0);
	}

	public PixelFormat(int redBits, int greenBits, int blueBits, int alphaBits, int depthBits) {
		this(redBits, greenBits, blueBits, alphaBits, depthBits, 0);
	}

	public PixelFormat(int redBits, int greenBits, int blueBits, int alphaBits, int depthBits, int stencilBits) {
		this(redBits, greenBits, blueBits, alphaBits, depthBits, stencilBits, 0);
	}

	public PixelFormat(int redBits, int greenBits, int blueBits, int alphaBits, int depthBits, int stencilBits, int msaaSamples) {
		
		this.redBits = redBits;
		this.greenBits = greenBits;
		this.blueBits = blueBits;
		
		this.alphaBits = alphaBits;
		
		this.depthBits = depthBits;
		this.stencilBits = stencilBits;
		
		this.msaaSamples = msaaSamples;
	}
	
	public int getRedBits() {
		return redBits;
	}
	
	public int getGreenBits() {
		return greenBits;
	}
	
	public int getBlueBits() {
		return blueBits;
	}
	
	public int getColorBitCount(){
		return redBits + greenBits + blueBits;
	}
	
	public int getAlphaBits() {
		return alphaBits;
	}
	
	public int getDepthBits() {
		return depthBits;
	}
	
	public int getStencilBits() {
		return stencilBits;
	}
	
	 public int getMsaaSamples() {
		return msaaSamples;
	}
}