package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;
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

	private Vector2f anchor = new Vector2f();
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
			dragging = ((dragging || canDrag(e.getWindow())) && isFocusedOrCanFocus(this) && isDragButtonEvent(e));

			/*System.err.println(this + ": " + canDrag(e.getWindow()) + " " + isFocusedOrCanFocus(this) + " " + isDragButtonEvent(e) 
					+ " = " + dragging
					+ " | Dimensions: " + DraggableWidgetAssembly.this.getWidth() + "/" + DraggableWidgetAssembly.this.getHeight());*/

			//If we start dragging, focus on this widget
			if (!bDragging && dragging) {
				//The anchor is a relative X/Y value as to where the mouse was inside of the Widget when we clicked. 
				//That way we can factor this into dragging calculations by subtracting the anchor from the mouseX/Y
				//(Preventing the widget from snapping to the mouse coordinates from the upper-left)
				anchor.set((float) (e.getMouseX() - getX()), (float) (e.getMouseY() - getY()));
				setFocusedWidget(this);
			}
			
			//If dragging stops, unfocus
			if (bDragging && !dragging) {
				setFocusedWidget(null);
			}
		});
		
		setOnInternalMouseMotionEvent(e -> {
			if (dragging) {
				//Calculating the coordinates like this instead of using the mouse event DX/DY means that the user will have more freedom in how they use move()
				//E.G. calculating it like this will allow the user to modify move to allow for grid snapping without any issues arising from the new X/Y values
				move(getX() + (float) (e.getMouseX() - getX()) - anchor.x(), getY() + (float) (e.getMouseY() - getY()) - anchor.y());
				//System.err.println(this + " Dragging: " + getX() + "/" + getY() + " " + anchor.x() + "/" + anchor.y());
			}
		});
	}

	/**
	 * This function handles moving the widget assembly when the dragging controls are used. Extension and overriding of this function will allow for the 
	 * modification of the movement behavior.
	 * 
	 * @param newX - the requested new X value for the widget
	 * @param newY - the requested new Y value for the widget
	 */
	protected void move(float newX, float newY) {
		setX(newX);
		setY(newY);
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
		
		//System.err.println("canDrag() -> " + hoveringChildren + " " + isMouseWithinThisWidget(window) + " " + requiresMouseToBeWithinWidgetToDrag);
		
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

	/**
	 * @return true if this DraggableWidgetAssembly is being dragged via user input.
	 */
	public boolean isDragging() {
		return dragging;
	}

	/**
	 * @return the current dragging anchor. This is a value of the relative X/Y coordinate of the mouse to the widget X/Y when it's clicked for the first time for dragging.
	 */
	public Vector2f getDraggingAnchor() {
		return anchor;
	}
}
