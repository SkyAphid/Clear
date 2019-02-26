package nokori.clear.vg.widget.assembly;

import java.util.ArrayList;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;

public abstract class WidgetContainer {
	
	protected ArrayList<Widget> children = new ArrayList<>();
	
	private boolean tickChildren = true;
	private boolean renderChildren = true;
	
	public void tickChildren(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (Widget w : children) {
			if (tickChildren) {
				w.tick(windowManager, window, context, rootWidgetAssembly);
				w.tickChildren(windowManager, window, context, rootWidgetAssembly);
			}
		}
	}
	
	public void renderChildren(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (Widget w : children) {
			if (renderChildren) {
				w.render(windowManager, window, context, rootWidgetAssembly);
				w.renderChildren(windowManager, window, context, rootWidgetAssembly);
			}
		}
	}
	
	public void childrenCharEvent(CharEvent event) {
		for (Widget w : children) {
			w.charEvent(event);
			w.childrenCharEvent(event);
		}
	}
	
	public void childrenKeyEvent(KeyEvent event) {
		for (Widget w : children) {
			w.keyEvent(event);
			w.childrenKeyEvent(event);
		}
	}
	
	public void childrenMouseButtonEvent(MouseButtonEvent event) {
		for (Widget w : children) {
			w.mouseButtonEvent(event);
			w.childrenMouseButtonEvent(event);
		}
	}
	
	public void childrenMouseMotionEvent(MouseMotionEvent event) {
		for (Widget w : children) {
			w.mouseMotionEvent(event);
			w.childrenMouseMotionEvent(event);
		}
	}
	
	public void childrenMouseScrollEvent(MouseScrollEvent event) {
		for (Widget w : children) {
			w.mouseScrollEvent(event);
			w.childrenMouseScrollEvent(event);
		}
	}
	
	protected void addChildCallback(Widget widget) {
		
	}

	protected void removeChildCallback(Widget widget) {
		
	}

	public void addChild(Widget... widget) {
		for (int i = 0; i < widget.length; i++) {
			Widget w = widget[i];
			
			children.add(w);
			addChildCallback(w);
		}
	}
	
	public boolean removeChild(Widget widget) {
		if (children.remove(widget)) {
			removeChildCallback(widget);
			return true;
		}
		
		return false;
	}
	
	public Widget removeChild(int index) {
		Widget widget = children.remove(index);
		
		if (widget != null) {
			removeChildCallback(widget);
		}
		
		return widget;
	}
	
	public Widget getChild(int index) {
		return children.get(index);
	}
	
	public int getNumChildren() {
		return children.size();
	}
	
	public boolean isTickingChildren() {
		return tickChildren;
	}

	public void setTickChildren(boolean tickChildren) {
		this.tickChildren = tickChildren;
	}

	public boolean isRenderingChildren() {
		return renderChildren;
	}

	public void setRenderChildren(boolean renderChildren) {
		this.renderChildren = renderChildren;
	}

	public void dispose() {
		for (Widget w : children) {
			w.dispose();
		}
	}
}