package nokori.clear.vg.widget.assembly;

import org.lwjgl.glfw.GLFW;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.MouseButtonEvent;

import static nokori.clear.vg.ClearStaticResources.*;

/**
 * DraggableWidgetAssemblies are exactly what the name implies. It's a WidgetAssembly that can be dragged with the mouse. 
 * With the right settings, you can put a bunch of DraggableWidgetAssembly objects in one DraggableWidgetAssembly and create a system 
 * where nodes are draggable inside of a canvas that is pannable. 
 * <br><br>
 * Check out <code>ClearDraggableWidgetDemo.java</code> to see an example of this in action.
 */
public class DraggableWidgetAssembly extends WidgetAssembly {

	private boolean dragging = false;

	private boolean requiresMouseToBeWithinWidgetToDrag = true;
	private boolean ignoreChildrenWidgets = false;
	
	public DraggableWidgetAssembly() {
		this(0f, 0f, 0f, 0f);
	}

	public DraggableWidgetAssembly(float x, float y, float width, float height) {
		super(x, y, width, height);
		
		setOnInternalMouseButtonEvent(e -> {
			boolean bDragging = dragging;
			dragging = (canDrag(e.getWindow()) && canFocus(this) && isDragButtonEvent(e));
			
			//System.err.println(canDrag(e.getWindow()) + " " + canFocus(this) + " " + isDragButtonEvent(e));

			//If we start dragging, focus on this widget
			if (!bDragging && dragging) {
				setFocusedWidget(this);
			}
			
			//If dragging stops, unfocus
			if (bDragging && !dragging) {
				setFocusedWidget(null);
			}
		});
		
		setOnMouseMotionEvent(e -> {
			if (dragging) {
				setX(getX() + (float) e.getDX());
				setY(getY() + (float) e.getDY());
			}
		});
	}

	private boolean canDrag(Window window) {
		//We won't let the user drag the widget if the mouse is hovering one of its draggable widget assembly children (normal children don't count)
		boolean hoveringChildren = false;
		
		if (!ignoreChildrenWidgets) {
			for (int i = 0; i < children.size(); i++) {
				Widget w = children.get(i);

				if (w.isMouseWithinThisWidget(window) && w.isInputEnabled()) {
					hoveringChildren = true;
					break;
				}
			}
		}
		
		return ((isMouseWithinThisWidget(window) || !requiresMouseToBeWithinWidgetToDrag) && !hoveringChildren);
	}
	
	/**
	 * @return true if the mouseButton is the button used for dragging.
	 */
	protected boolean isDragButtonEvent(MouseButtonEvent e) {
		return (e.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && e.isPressed());
	}

	/**
	 * This value determines whether or not the mouse has to actually be within the bounds of this Widget for the dragging functionality to be activated. 
	 * Disabling this can have some advantages, such as when you want to be able to drag an entire canvas around.
	 */
	public boolean requiresMouseToBeWithinWidgetToDrag() {
		return requiresMouseToBeWithinWidgetToDrag;
	}

	/**
	 * This value determines whether or not the mouse has to actually be within the bounds of this Widget for the dragging functionality to be activated. 
	 * Disabling this can have some advantages, such as when you want to be able to drag an entire canvas around.
	 * 
	 * @param requiresMouseToBeWithinWidgetToDrag
	 */
	public void setRequiresMouseToBeWithinWidgetToDrag(boolean requiresMouseToBeWithinWidgetToDrag) {
		this.requiresMouseToBeWithinWidgetToDrag = requiresMouseToBeWithinWidgetToDrag;
	}

	/**
	 * If this is set to true, this widget won't take into consideration whether or not the mouse is hovering one of its children widgets before allowing 
	 * the user to drag it. Otherwise, if this is false, you won't be able to drag the widget if the mouse is hovering another widget within this widget.
	 */
	public boolean ignoreChildrenWidgets() {
		return ignoreChildrenWidgets;
	}

	/**
	 * If this is set to true, this widget won't take into consideration whether or not the mouse is hovering one of its children widgets before allowing 
	 * the user to drag it. Otherwise, if this is false, you won't be able to drag the widget if the mouse is hovering another widget within this widget.
	 * 
	 * @param ignoreChildrenWidgets
	 */
	public void setIgnoreChildrenWidgets(boolean ignoreChildrenWidgets) {
		this.ignoreChildrenWidgets = ignoreChildrenWidgets;
	}
}
