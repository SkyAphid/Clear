package nokori.clear.vg.widget.assembly;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.util.NanoVGScaler;
import nokori.clear.windows.Window;
import nokori.clear.windows.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

/**
 * WidgetContainers are used to manage groups of Widgets in one class. By default, Widgets always extend this class so that it can contain children. 
 * However, sometimes problems can be solved by using a WidgetContainer by itself. Just remember that a WidgetContainer by itself isn't a working Widget.
 */
public class WidgetContainer {
	
	protected ArrayList<Widget> children = new ArrayList<>();
	
	private boolean tickChildren = true;
	private boolean renderChildren = true;
	private boolean invertInputOrder = false;
	
	protected NanoVGScaler scaler = new NanoVGScaler();
	
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
	
	public void tickChildren(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (tickChildren) {
				w.tick(context, rootWidgetAssembly);
				w.tickChildren(context, rootWidgetAssembly);
			}
		}
	}
	
	public void renderChildren(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (renderChildren) {
				w.render(context, rootWidgetAssembly);
				w.renderChildren(context, rootWidgetAssembly);
			}
		}
	}
	
	public void childrenCharEvent(Window window, CharEvent event) {
		iterateChildren(invertInputOrder, (i) -> {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.charEvent(window, event);
				w.childrenCharEvent(window, event);
			}
		});
	}
	
	public void childrenKeyEvent(Window window, KeyEvent event) {
		iterateChildren(invertInputOrder, (i) -> {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.keyEvent(window, event);
				w.childrenKeyEvent(window, event);
			}
		});
	}
	
	public void childrenMouseButtonEvent(Window window, MouseButtonEvent event) {
		iterateChildren(invertInputOrder, (i) -> {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseButtonEvent(window, event);
				w.childrenMouseButtonEvent(window, event);
			}
		});
	}
	
	public void childrenMouseMotionEvent(Window window, MouseMotionEvent event) {
		iterateChildren(invertInputOrder, (i) -> {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseMotionEvent(window, event);
				w.childrenMouseMotionEvent(window, event);
			}
		});
	}
	
	public void childrenMouseScrollEvent(Window window, MouseScrollEvent event) {
		iterateChildren(invertInputOrder, (i) -> {
			Widget w = children.get(i);
			
			if (w.isInputEnabled()) {
				w.mouseScrollEvent(window, event);
				w.childrenMouseScrollEvent(window, event);
			}
		});
	}
	
	private void iterateChildren(boolean reverseOrder, IterationHandler h) {
		if (reverseOrder) {
			for (int i = children.size()-1; i >= 0; i--) {
				h.event(i);
			}
		} else {
			for (int i = 0; i < children.size(); i++) {
				h.event(i);
			}
		}
	}

	private abstract interface IterationHandler {
		public void event(int index);
	};
	
	/**
	 * @see <code>setInvertInputOrder()</code>
	 * @return
	 */
	public boolean isInvertInputOrder() {
		return invertInputOrder;
	}

	/**
	 * If true, the input of children will be reversed to prioritize an inverse input order 
	 * (e.g. things rendered last, thus on top, will be prioritized for input over things rendered first)
	 * 
	 * @param invertInputOrder
	 */
	public void setInvertInputOrder(boolean invertInputOrder) {
		this.invertInputOrder = invertInputOrder;
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
	
	public void clearChildren() {
		while(!children.isEmpty()){
			removeChild(0);
		}
	}
	
	/**
	 * Sorts the children of this WidgetContainer with the given Comparator.
	 * 
	 * @param comparator
	 */
	public void sortChildren(Comparator<Widget> comparator) {
		children.sort(comparator);
	}
	
	/**
	 * Reverses the list containing all of the child widgets
	 */
	public void reverseChildren() {
		Collections.reverse(children);
	}
	
	/**
	 * A shortcut function that automatically sorts the children of this WidgetContainer by their Y-Values using a Comparator.
	 */
	public void sortChildrenByYCoordinates() {
		sortChildren(new Comparator<Widget>() {
			@Override
			public int compare(Widget w1, Widget w2) {
				return Float.compare(w1.getClippedY(), w2.getClippedY());
			}
		});
	}
	
	public Widget getChild(int index) {
		return children.get(index);
	}
	
	public Stack<Widget> getChildrenWithinMouse(Window window){
		Stack<Widget> intersecting = new Stack<Widget>();
		
		for (int i = 0; i < children.size(); i++) {
			Widget w = children.get(i);
			
			if (w.isMouseIntersectingThisWidget(window)) {
				intersecting.add(w);
			}
			
			intersecting.addAll(w.getChildrenWithinMouse(window));
		}
		
		return intersecting;
	}
	
	public int getNumChildren() {
		return children.size();
	}
	
	/**
	 * Compiles a Stack containing every single child of this WidgetContainer (including all children of children).
	 */
	public Stack<Widget> getAllChildren(){
		Stack<Widget> stack = new Stack<Widget>();
		addChildrenToStack(stack, this);
		return stack;
	}
	
	private void addChildrenToStack(Stack<Widget> stack, WidgetContainer c) {
		stack.addAll(c.children);
		
		for (int i = 0; i < c.children.size(); i++) {
			if (c.children.get(i) instanceof WidgetContainer) {
				WidgetContainer wC = (WidgetContainer) c.children.get(i);
				addChildrenToStack(stack, wC);
			}
		}
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
	
	public NanoVGScaler getScaler() {
		return scaler;
	}

	public void setScaler(NanoVGScaler scaler) {
		this.scaler = scaler;
	}

	public void dispose() {
		for (Widget w : children) {
			w.dispose();
		}
	}
}
