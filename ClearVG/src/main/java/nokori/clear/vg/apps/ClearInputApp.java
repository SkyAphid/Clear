package nokori.clear.vg.apps;

import nokori.clear.vg.ClearApp;
import nokori.clear.vg.ClearColor;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.ButtonAssembly;
import nokori.clear.vg.widget.LabelWidget;
import nokori.clear.vg.widget.assembly.RootWidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.vg.widget.text.TextFieldWidget;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import java.io.File;
import java.io.IOException;

/**
 * This ClearApp is an input window you can open and use to get user input. See ClearInputAppDemo.java to learn more about it.
 */
public abstract class ClearInputApp extends ClearApp {

	private static final int FONT_SIZE = 16;
	
	private int width, height;
	private ClearApp parent;
	private ClearColor buttonOutline;
	private File fontLocation;
	private String title, message, defaultInput;
	
	public ClearInputApp(ClearApp parent, int windowWidth, int windowHeight, ClearColor buttonOutline, File fontLocation, String title, String message, String defaultInput) {
		this(parent.getWindowManager(), new RootWidgetAssembly(), parent, windowWidth, windowHeight, buttonOutline, fontLocation, title, message, defaultInput);
	}
	
	private ClearInputApp(WindowManager windowManager, WidgetAssembly rootWidgetAssembly, 
			ClearApp parent, int windowWidth, int windowHeight, ClearColor buttonOutline, File fontLocation, String title, String message, String defaultInput) {
		
		super(windowManager, rootWidgetAssembly);
		this.parent = parent;
		this.width = windowWidth;
		this.height = windowHeight;
		this.buttonOutline = buttonOutline;
		this.fontLocation = fontLocation;
		this.title = title;
		this.message = message;
		this.defaultInput = defaultInput;
	}

	public void show() throws GLFWException {
		parent.setPaused(true);
		parent.queueLaunch(this);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		
		float xPadding = 20f;
		float yPadding = 20f;

		try {
			Font font = new Font(fontLocation).load(context);

			/*
			 * Message
			 */

			float widgetWidth = width - (xPadding * 2f);

			LabelWidget messageLabel = new LabelWidget(widgetWidth, ClearColor.LIGHT_BLACK, message, font, FontStyle.REGULAR, FONT_SIZE).calculateBounds(context);
			messageLabel.addChild(new WidgetClip(WidgetClip.Alignment.TOP_LEFT, xPadding, yPadding));

			rootWidgetAssembly.addChild(messageLabel);
			
			/*
			 * Input field
			 */
			
			TextFieldWidget inputField = new TextFieldWidget(widgetWidth, ClearColor.LIGHT_BLACK, defaultInput, font, FONT_SIZE);
			inputField.setBackgroundFill(ClearColor.LIGHT_GRAY);
			inputField.addChild(new WidgetClip(WidgetClip.Alignment.TOP_LEFT, xPadding, (yPadding * 2f) + messageLabel.getHeight()));
			
			rootWidgetAssembly.addChild(inputField);
			
			/*
			 * Confirm button
			 */
			
			float buttonSynchX = xPadding;
			float buttonSynchY = (yPadding * 4f) + messageLabel.getHeight() + inputField.getHeight();
			
			ButtonAssembly confirmButton = new ButtonAssembly(75, 25, ClearColor.LIGHT_GRAY, buttonOutline, 0f, font, FONT_SIZE, ClearColor.LIGHT_BLACK, "Confirm");
			confirmButton.addChild(new WidgetClip(WidgetClip.Alignment.TOP_LEFT, buttonSynchX, buttonSynchY));
			
			confirmButton.setOnMouseButtonEvent(e -> {
				if (confirmButton.isMouseWithin() && !e.isPressed()) {
					confirmButtonPressed(inputField.getTextBuilder().toString());
					window.requestClose();
				}
			});
			
			rootWidgetAssembly.addChild(confirmButton);
			
			/*
			 * Exit button
			 */

			ButtonAssembly exitButton = new ButtonAssembly(75, 25, ClearColor.LIGHT_GRAY, buttonOutline, 0f, font, FONT_SIZE, ClearColor.LIGHT_BLACK, "Cancel");
			exitButton.addChild(new WidgetClip(WidgetClip.Alignment.TOP_LEFT, buttonSynchX + confirmButton.getWidth() + xPadding, buttonSynchY));
			
			exitButton.setOnMouseButtonEvent(e -> {
				if (exitButton.isMouseWithin() && !e.isPressed()) {
					window.requestClose();
				}
			});
			
			rootWidgetAssembly.addChild(exitButton);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void confirmButtonPressed(String text);

	@Override
	protected void endOfNanoVGApplicationCallback() {
		parent.setPaused(false);
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		Window parentWindow = parent.getWindow();
		
		int centerX = parentWindow.getX() + parentWindow.getWidth()/2 - width/2;
		int centerY = parentWindow.getY() + parentWindow.getHeight()/2 - height/2;
		
		Window newWindow = windowManager.createWindow(title, centerX, centerY, width, height, false, true);
		
		if (parentWindow.getIconFiles() != null) {
			newWindow.setIcons(parentWindow.getIconFiles());
		}

		return newWindow;
	}

	@Override
	protected boolean exitProgramOnEndOfApplication() {
		return false;
	}
}
