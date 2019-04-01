package nokori.clear.vg.widget.assembly;

import java.util.ArrayList;
import java.util.Stack;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;
import nokori.clear.windows.event.CharEvent;
import nokori.clear.windows.event.KeyEvent;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import nokori.clear.windows.event.MouseScrollEvent;

/**
 * WidgetContainers are used to manage groups of Widgets in one class. By default, Widgets always extend this class so that it can contain children. 
 * However, sometimes problems can be solved by using a WidgetContainer by itself. Just remember that a WidgetContainer by itself isn't a working Widget.
 */
public class WidgetContainer {
	
	protected ArrayList<Widget> children = new ArrayList<>();
	
	private boolean tickChildren = true;
	private boolean renderChildren = true;
	
	/**
	 * This is a lambda function that allows outside classes to quickly iterate through all of the Widgets contained by this object.
	 * @param processor -> the lambda class, taking in only one argument: the current widget
	 */
	public void iterateChildren(WidgetProcessor processor) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			processor.process(w);
		}
	}
	
	private interface WidgetProcessor {
		public void process(Widget w);
	}
	
	public void tickChildren(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (tickChildren) {
				w.tick(windowManager, window, context, rootWidgetAssembly);
				w.tickChildren(windowManager, window, context, rootWidgetAssembly);
			}
		}
	}
	
	public void renderChildren(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (renderChildren) {
				w.render(windowManager, window, context, rootWidgetAssembly);
				w.renderChildren(windowManager, window, context, rootWidgetAssembly);
			}
		}
	}
	
	public void childrenCharEvent(Window window, CharEvent event) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.charEvent(window, event);
				w.childrenCharEvent(window, event);
			}
		}
	}
	
	public void childrenKeyEvent(Window window, KeyEvent event) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.keyEvent(window, event);
				w.childrenKeyEvent(window, event);
			}
		}
	}
	
	public void childrenMouseButtonEvent(Window window, MouseButtonEvent event) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseButtonEvent(window, event);
				w.childrenMouseButtonEvent(window, event);
			}
		}
	}
	
	public void childrenMouseMotionEvent(Window window, MouseMotionEvent event) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseMotionEvent(window, event);
				w.childrenMouseMotionEvent(window, event);
			}
		}
	}
	
	public void childrenMouseScrollEvent(Window window, MouseScrollEvent event) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseScrollEvent(window, event);
				w.childrenMouseScrollEvent(window, event);
			}
		}
	}
	
	protected void addChildCallback(Widget widget) {
		
	}

	protected void removeChildCallback(Widget widget) {
		
	}

	/**
	 * All of the Widgets in the given container will be copied over to this one. 
	 * Be careful when using this, it's not recommended that Widgets be the children of multiple Widgets at one time. 
	 * However, it's fine if the Widget is the child of one Widget but contained by multiple detached WidgetContainers by themselves.
	 * 
	 * @param container
	 */
	public void addAllChildrenOfContainer(WidgetContainer container) {
		for (Widget w : container.children) {
			addChild(w);
		}
	}
	
	public void addChildInFrontOf(Widget find, Widget... widgets) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w == find) {
				for (int j = 0; j < widgets.length; j++) {
					Widget child = widgets[j];
					
					//Insert the child into the list
					//If the insertion point is larger than the size, just throw it onto the end
					if (i + 1 >= children.size()) {
						children.add(child);
					} else {
						children.add(i + 1, child);
					}
					
					addChildCallback(child);
				}
				
				return;
			}
		}
		
		System.err.println("WARNING: addChildInFrontOf() couldn't find the designated Widget.");
	}
	
	/**
	 * Adds the given Widget objects to this WidgetContainer as a child, meaning that this WidgetContainer will be in charge of ticking and rendering that Widget. 
	 * This is sensitive to order. The first added Widget is updated/rendered first, the last added Widget is updated/rendered last. Keep that in mind when creating interfaces.
	 */
	public void addChild(Widget... widgets) {
		for (int i = 0; i < widgets.length; i++) {
			Widget w = widgets[i];
			
			children.add(w);
			addChildCallback(w);
		}
	}
	
	public boolean removeChild(Widget widget) {
		if (children.remove(widget)) {
			removeChildCallback(widget);
			widget.dispose();
			return true;
		}
		
		return false;
	}
	
	public Widget removeChild(int index) {
		Widget widget = children.get(index);
		removeChild(widget);
		return widget;
	}
	
	public Widget getChild(int index) {
		return children.get(index);
	}
	
	public Stack<Widget> getChildrenWithinMouse(Window window){
		Stack<Widget> intersecting = new Stack<Widget>();
		
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isMouseWithinThisWidget(window)) {
				intersecting.add(w);
			}
			
			intersecting.addAll(w.getChildrenWithinMouse(window));
		}
		
		return intersecting;
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
