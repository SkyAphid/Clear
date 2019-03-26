package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.listener.CharEventListener;
import nokori.clear.vg.widget.listener.KeyEventListener;
import nokori.clear.vg.widget.listener.MouseButtonEventListener;
import nokori.clear.vg.widget.listener.MouseEnteredEventListener;
import nokori.clear.vg.widget.listener.MouseExitedEventListener;
import nokori.clear.vg.widget.listener.MouseMotionEventListener;
import nokori.clear.vg.widget.listener.MouseScrollEventListener;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;
import nokori.clear.windows.event.vg.MouseEnteredEvent;
import nokori.clear.windows.event.vg.MouseExitedEvent;

/**
 * Widgets are components that can be added to containers. The container then handles updating and rendering them. 
 */
public abstract class Widget extends WidgetContainer {
	
	protected Widget parent = null;
	
	private Vector2f pos = new Vector2f(0f, 0f);
	private Vector2f size = new Vector2f(0f, 0f);

	private boolean inputEnabled = true;
	private CharEventListener charEventListener = null;
	private KeyEventListener keyEventListener = null;
	private MouseButtonEventListener mouseButtonEventListener = null;
	private MouseMotionEventListener mouseMotionEventListener = null;
	private MouseEnteredEventListener mouseEnteredEventListener = null;
	private MouseExitedEventListener mouseExitedEventListener = null;
	private MouseScrollEventListener mouseScrollEventListener = null;
	
	private boolean mouseWithin = false;
	
	public Widget() {
		
	}
	
	public Widget(float x, float y, float width, float height) {
		pos.x = x;
		pos.y = y;
		size.x = width;
		size.y = height;
	}
	
	/**
	 * This is the update function for this widget. <code>tick()</code> is always called directly before <code>render()</code> if you're using the default application wrappers. 
	 * Any sort of update logic can be inserted here. It's by default functionally the same as <code>render()</code>, but it was added to help keep code tidy and organized.
	 * 
	 * @param windowManager
	 * @param window
	 * @param context
	 * @param rootWidgetAssembly
	 */
	public abstract void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly);
	
	/**
	 * This is the rendering function for this widget. NanoVG rendering calls should be inserted here.
	 * 
	 * @param windowManager
	 * @param window
	 * @param context
	 * @param rootWidgetAssembly
	 */
	public abstract void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly);
	
	/**
	 * This is called when a widget is removed from a widget container or at the end of the program's life. Make sure to dispose any native resources that will leak otherwise.
	 */
	public abstract void dispose();
	
	/*
	 * 
	 * 
	 * Getters/Setters
	 * 
	 * 
	 */

	/**
	 * @return the parent widget of this widget (meaning that the parent is a WidgetContainer containing this Widget). If this widget has no parent, then null is returned.
	 * 
	 * @see <code>WidgetContainer.addChild(widget)</code>
	 */
	public Widget getParent() {
		return parent;
	}

	public Vector2f getPosition() {
		return pos;
	}

	public Vector2f getSize() {
		return size;
	}

	/**
	 * @return the x value of this Widget. Keep in mind, the way this value is used can vary between Widgets. For example, a widget inside of a WidgetContainer may be using x coordinates 
	 * relative to being clipped to the parent (e.g. <code>x = 10</code>, <code>parent x = 100</code>, thus the correct coordinate to render to the child is <code>x = 110</code>.) 
	 * To get the render x for a "clipped" widget inside of a parent widget, use <code>getClippedX()</code> instead.
	 * 
	 * @see getClippedX()
	 */
	public float getX() {
		return pos.x();
	}

	/**
	 * @see getX()
	 * @see getClippedX()
	 * @param x
	 */
	public void setX(float x) {
		pos.x = x;
	}

	public void offsetX(float xOffset) {
		setX(getX() + xOffset);
	}
	
	/**
	 * @return the y value of this Widget. Keep in mind, the way this value is used can vary between Widgets. For example, a widget inside of a WidgetContainer may be using y coordinates 
	 * relative to being clipped to the parent (e.g. <code>y = 10</code>, <code>parent y = 100</code>, thus the correct coordinate to render to the child is <code>y = 110</code>.) 
	 * To get the render y for a "clipped" widget inside of a parent widget, use <code>getClippedY()</code> instead.
	 * 
	 * @see getClippedY()
	 */
	public float getY() {
		return pos.y();
	}

	/**
	 * @see getY()
	 * @see getClippedY()
	 * @param y
	 */
	public void setY(float y) {
		pos.y = y;
	}
	
	public void offsetY(float yOffset) {
		setY(getY() + yOffset);
	}

	public float getWidth() {
		return size.x;
	}

	public void setWidth(float width) {
		size.x = width;
	}

	public float getHeight() {
		return size.y;
	}

	public void setHeight(float height) {
		size.y = height;
	}
	
	/**
	 * @return an x value that takes into account the parent widgets position (if applicable). Used to make sure that child widgets are rendered in the parents bounds appropriately. 
	 * e.g. <code>clippedX = parent.getX() + widget.getX()</code>
	 * @see getX()
	 */
	public float getClippedX() {
		return (parent != null ? parent.getClippedX() + pos.x : pos.x);
	}
	
	/**
	 * @return an y value that takes into account the parent widgets position (if applicable). Used to make sure that child widgets are rendered in the parents bounds appropriately.
	 * 	 * e.g. <code>clippedY = parent.getY() + widget.getY()</code>
	 * @see getY()
	 */
	public float getClippedY() {
		return (parent != null ? parent.getClippedY() + pos.y : pos.y);
	}

	/**
	 * @return whether or not this widget is currently accepting user inputs.
	 */
	public boolean isInputEnabled() {
		return inputEnabled;
	}

	/**
	 * Toggles whether or not input is being accepted by this widget. 
	 * 
	 * @param inputEnabled - enables or disables input handling
	 * @see <code>isInputEnabled()</code>
	 */
	public void setInputEnabled(boolean inputEnabled) {
		this.inputEnabled = inputEnabled;
	}

	/**
	 * Checks if the mouse is within this widget on the fly by using the data stored in the given Window (rather than using the cached state).
	 * 
	 * @param window
	 * @return true if the Window's mouse coordinates fall within this widget.
	 */
	public boolean isMouseWithinThisWidget(Window window) {
		return isPointWithinThisWidget(window.getMouseX(), window.getMouseY());
	}
	
	/**
	 * Checks if the given coordinates are within this widget.
	 * 
	 * @param x - the X screen coordinate
	 * @param y - the Y screen coordinate
	 * @return - true if the coordinates are within this widget
	 */
	public boolean isPointWithinThisWidget(double x, double y) {
		return WidgetUtil.pointWithinRectangle(x, y, getClippedX(), getClippedY(), getWidth(), getHeight());
	}
	
	/*
	 * 
	 * 
	 * Misc. Callbacks
	 * 
	 * 
	 */
	
	@Override
	protected void addChildCallback(Widget widget) {
		if (widget.parent != null) {
			System.err.println("WARNING: Widget (" + widget + ") has two parents: " + this +", " + widget.parent + "\nThis is likely to result in erratic behavior.");
			Thread.dumpStack();
		}
		
		widget.parent = this;
	}

	@Override
	protected void removeChildCallback(Widget widget) {
		widget.parent = null;
	}
	
	/*
	 * 
	 * Input
	 * 
	 * 
	 */
	
	/**
	 * @return true if the mouse if the mouseWithin flag is true (set via the last mouse motion event call).
	 */
	public boolean isMouseWithinThisWidget() {
		return mouseWithin;
	}
	
	/**
	 * Resets the flag that controls onMouseEntered()/onMouseExited() indicating whether or not the mouse is hovering this widget to false. 
	 * The primary use of this function is in situations where <code>setInputEnabled(false)</code> is called and the flag needs to be reset manually. 
	 * This isn't done automatically in case the user wants to cache the state of the flag before input was disabled to check later.
	 */
	public void resetIsMouseWithin() {
		mouseWithin = false;
	}

	public void charEvent(Window window, CharEvent event) {
		if (charEventListener != null) {
			charEventListener.listen(event);
		}
	}

	public void keyEvent(Window window, KeyEvent event) {
		if (keyEventListener != null) {
			keyEventListener.listen(event);
		}
	}
	
	public void mouseButtonEvent(Window window, MouseButtonEvent event) {
		if (mouseButtonEventListener != null) {
			mouseButtonEventListener.listen(event);
		}
	}
	
	public void mouseMotionEvent(Window window, MouseMotionEvent event) {
		/*
		 * Standard Mouse Motion Listening
		 */
		
		if (mouseMotionEventListener != null) {
			mouseMotionEventListener.listen(event);
		}
		
		/*
		 * Mouse Entered/Exited Widget Callbacks
		 */
		
		double mouseX = event.getMouseX();
		double mouseY = event.getMouseY();
		
		boolean bMouseWithin = mouseWithin;
		mouseWithin = isPointWithinThisWidget(mouseX, mouseY);
		
		//Mouse entered
		if (!bMouseWithin && mouseWithin && mouseEnteredEventListener != null) {
			mouseEnteredEventListener.listen(MouseEnteredEvent.fire(window, System.nanoTime(), mouseX, mouseY));
		}
		
		//Mouse exited
		if (bMouseWithin && !mouseWithin && mouseExitedEventListener != null) {
			mouseExitedEventListener.listen(MouseExitedEvent.fire(window, System.nanoTime(), mouseX, mouseY));
		}
	}
	
	public void mouseScrollEvent(Window window, MouseScrollEvent event) {
		if (mouseScrollEventListener != null) {
			mouseScrollEventListener.listen(event);
		}
	}

	public CharEventListener getCharEventListener() {
		return charEventListener;
	}

	public void setOnCharEvent(CharEventListener charEventListener) {
		this.charEventListener = charEventListener;
	}

	public KeyEventListener getKeyEventListener() {
		return keyEventListener;
	}

	public void setOnKeyEvent(KeyEventListener keyEventListener) {
		this.keyEventListener = keyEventListener;
	}

	public MouseButtonEventListener getMouseButtonEventListener() {
		return mouseButtonEventListener;
	}

	public void setOnMouseButtonEvent(MouseButtonEventListener mouseButtonEventListener) {
		this.mouseButtonEventListener = mouseButtonEventListener;
	}

	public MouseMotionEventListener getMouseMotionEventListener() {
		return mouseMotionEventListener;
	}

	public void setOnMouseMotionEvent(MouseMotionEventListener mouseMotionEventListener) {
		this.mouseMotionEventListener = mouseMotionEventListener;
	}

	public MouseEnteredEventListener getMouseEnteredEventListener() {
		return mouseEnteredEventListener;
	}

	public void setOnMouseEnteredEvent(MouseEnteredEventListener mouseEnteredEventListener) {
		this.mouseEnteredEventListener = mouseEnteredEventListener;
	}

	public MouseExitedEventListener getMouseExitedEventListener() {
		return mouseExitedEventListener;
	}

	public void setOnMouseExitedEvent(MouseExitedEventListener mouseExitedEventListener) {
		this.mouseExitedEventListener = mouseExitedEventListener;
	}

	public MouseScrollEventListener getMouseScrollEventListener() {
		return mouseScrollEventListener;
	}

	public void setOnMouseScrollEvent(MouseScrollEventListener mouseScrollEventListener) {
		this.mouseScrollEventListener = mouseScrollEventListener;
	}

}
