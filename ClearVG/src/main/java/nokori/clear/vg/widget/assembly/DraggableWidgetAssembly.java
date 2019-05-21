package nokori.clear.vg.widget.assembly;

import nokori.clear.windows.Window;
import nokori.clear.windows.event.MouseButtonEvent;
import nokori.clear.windows.event.MouseMotionEvent;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

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
	private Vector2f startPos = new Vector2f();

	private boolean requiresMouseToBeWithinWidgetToDrag = true;
	private boolean ignoreChildrenWidgets = false;
	
	public DraggableWidgetAssembly() {
		this(0f, 0f, 0f, 0f);
	}

	public DraggableWidgetAssembly(float x, float y, float width, float height) {
		super(x, y, width, height);
		
		setOnInternalMouseButtonEvent(e -> {
			boolean bDragging = dragging;
			dragging = ((dragging || canDrag(e.getWindow(), 1.0f)) && isFocusedOrCanFocus(this) && isDragButtonEvent(e));
			startPos.set(getClippedX(), getClippedY());
			
			/*System.err.println(this + ": " + canDrag(e.getWindow()) + " " + isFocusedOrCanFocus(this) + " " + isDragButtonEvent(e) 
					+ " = " + dragging
					+ " | Dimensions: " + DraggableWidgetAssembly.this.getWidth() + "/" + DraggableWidgetAssembly.this.getHeight());*/

			//If we start dragging, focus on this widget
			if (!bDragging && dragging) {
				//The anchor is a relative X/Y value as to where the mouse was inside of the Widget when we clicked. 
				//That way we can factor this into dragging calculations by subtracting the anchor from the mouseX/Y
				//(Preventing the widget from snapping to the mouse coordinates from the upper-left)
				draggingAnchorCallback(e);
				setFocusedWidget(this);
			}
			
			//If dragging stops, unfocus if applicable
			if (!dragging) {
				clearFocusIfApplicable(this);
				draggingReleaseCallback(e, (startPos.x != getClippedX() || startPos.y != getClippedY()));
			}
		});
		
		setOnInternalMouseMotionEvent(e -> {
			if (dragging && isFocusedOrCanFocus(this)) {
				//Calculating the coordinates like this instead of using the mouse event DX/DY means that the user will have more freedom in how they use move()
				//E.G. calculating it like this will allow the user to modify move to allow for grid snapping without any issues arising from the new X/Y values
				//System.err.println(this + " Dragging: " + getX() + "/" + getY() + " " + anchor.x() + "/" + anchor.y());
				
				draggingCallback(e);
			}
		});
	}
	
	/**
	 * This is split into its own function so that it can be overriden separately from <code>clipDraggingAnchor()</code>
	 * @param e
	 */
	protected void draggingAnchorCallback(MouseButtonEvent e) {
		clipDraggingAnchor((float) e.getScaledMouseX(scaler.getScale()), (float) e.getScaledMouseY(scaler.getScale()));
	}
	
	/**
	 * This is split into its own function so that it can be overriden separately from <code>move()</code>
	 * @param e
	 */
	protected void draggingCallback(MouseMotionEvent e) {
		move(getDragX(e.getScaledMouseX(scaler.getScale())), getDragY(e.getScaledMouseY(scaler.getScale())));
	}
	
	protected void draggingReleaseCallback(MouseButtonEvent e, boolean wasMoved) {
		
	}
	
	public float getDragX(double mouseX) {
		return (float) (getX() + (mouseX - getX()) - anchor.x());
	}
	
	public float getDragY(double mouseY) {
		return (float) (getY() + (mouseY - getY()) - anchor.y());
	}

	/**
	 * This function handles moving the widget assembly when the dragging controls are used. Extension and overriding of this function will allow for the 
	 * modification of the movement behavior.
	 * 
	 * @param newX - the requested new X value for the widget
	 * @param newY - the requested new Y value for the widget
	 */
	public void move(float newX, float newY) {
		setX(newX);
		setY(newY);
	}
	
	protected boolean canDrag(Window window, float scale) {
		//We won't let the user drag the widget if the mouse is hovering one of its draggable widget assembly children (normal children don't count)
		boolean hoveringChildren = false;
		
		if (!ignoreChildrenWidgets) {
			for (int i = 0; i < children.size(); i++) {
				Widget w = children.get(i);

				if (w.isMouseIntersectingThisWidget(window) && w.isInputEnabled()) {
					hoveringChildren = true;
					break;
				}
			}
		}
		
		//System.err.println("canDrag() -> " + hoveringChildren + " " + isMouseWithinThisWidget(window) + " " + requiresMouseToBeWithinWidgetToDrag);
		
		return ((isMouseIntersectingThisWidget(window) || !requiresMouseToBeWithinWidgetToDrag) && !hoveringChildren);
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
	 * Disabling this can have some advantages, such as when you want to be able to drag an entire canvas (widget assembly containing multiple nodes) around 
	 * without worrying about the input not being recognized because you tried to drag outside of its bounds.
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
	
	/**
	 * Sets the dragging anchor for this DraggableWidgetAssembly (the relative X/Y coordinate of the mouse to the widget X/Y). Setting it manually may be useful for instances 
	 * where you need to drag multiple Widgets at the same time.
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void clipDraggingAnchor(float mouseX, float mouseY) {
		anchor.set(mouseX - getX(), mouseY - getY());
	}
}
