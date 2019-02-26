package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.listener.CharEventListener;
import nokori.clear.vg.widget.listener.KeyEventListener;
import nokori.clear.vg.widget.listener.MouseButtonEventListener;
import nokori.clear.vg.widget.listener.MouseMotionEventListener;
import nokori.clear.vg.widget.listener.MouseScrollEventListener;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;

/**
 * Widgets are components that can be added to containers. The container then handles updating and rendering them. 
 */
public abstract class Widget extends WidgetContainer {
	
	protected Widget parent = null;
	
	protected Vector2f pos = new Vector2f(0, 0);
	protected Vector2f size = new Vector2f(0, 0);
	
	private CharEventListener charEventListener = null;
	private KeyEventListener keyEventListener = null;
	private MouseButtonEventListener mouseButtonEventListener = null;
	private MouseMotionEventListener mouseMotionEventListener = null;
	private MouseScrollEventListener mouseScrollEventListener = null;
	
	public abstract void tick(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly);
	public abstract void render(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly);
	public abstract void dispose();
	
	/*
	 * 
	 * 
	 * Getters/Setters
	 * 
	 * 
	 */

	public float getX() {
		return pos.x();
	}

	public void setX(float x) {
		pos.x = x;
	}

	public float getY() {
		return pos.y();
	}

	public void setY(float y) {
		pos.y = y;
	}

	public float getWidth() {
		return size.x();
	}

	public void setWidth(float width) {
		size.x = width;
	}

	public float getHeight() {
		return size.y();
	}

	public void setHeight(float height) {
		size.y = height;
	}
	
	public float getRenderX(float x) {
		return (parent != null ? parent.getX() + x : x);
	}
	
	public float getRenderY(float y) {
		return (parent != null ? parent.getY() + y : y);
	}
	
	public boolean isMouseWithin(Window window) {
		return WidgetUtil.pointWithinRectangle(window.getMouseX(), window.getMouseY(), getX(), getY(), getWidth(), getHeight());
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
		}
		
		widget.parent = this;
	}

	@Override
	protected void removeChildCallback(Widget widget) {
		widget.parent = null;
	}
	
	/*
	 * 
	 * Input Callbacks
	 * 
	 * 
	 */

	public void charEvent(CharEvent event) {
		if (charEventListener != null) {
			charEventListener.listen(event);
		}
	}
	
	public void keyEvent(KeyEvent event) {
		if (keyEventListener != null) {
			keyEventListener.listen(event);
		}
	}
	
	public void mouseButtonEvent(MouseButtonEvent event) {
		if (mouseButtonEventListener != null) {
			mouseButtonEventListener.listen(event);
		}
	}
	
	public void mouseMotionEvent(MouseMotionEvent event) {
		if (mouseMotionEventListener != null) {
			mouseMotionEventListener.listen(event);
		}
	}
	
	public void mouseScrollEvent(MouseScrollEvent event) {
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

	public MouseScrollEventListener getMouseScrollEventListener() {
		return mouseScrollEventListener;
	}

	public void setOnMouseScrollEvent(MouseScrollEventListener mouseScrollEventListener) {
		this.mouseScrollEventListener = mouseScrollEventListener;
	}

}
