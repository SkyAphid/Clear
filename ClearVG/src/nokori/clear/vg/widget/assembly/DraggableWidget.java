package nokori.clear.vg.widget.assembly;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import nokori.clear.vg.ClearColor;

//TODO: Complete
@SuppressWarnings("all")
public abstract class DraggableWidget extends ColoredWidgetImpl {

	private boolean dragging = false;
	private Vector2f anchor = new Vector2f();

	public DraggableWidget(float x, float y, float width, float height, ClearColor fill, ClearColor strokeFill) {
		super(x, y, width, height, fill, strokeFill);
	}
	
	/**
	 * @return true if the mouseButton is the button used for dragging.
	 */
	protected boolean isDragButton(int mouseButton) {
		return (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT);
	}

	/*@Override
	public void mouseButtonEvent(Window window, long timestamp, double mouseX, double mouseY, int button, boolean pressed, int mods) {
		boolean bDragging = dragging;
		dragging = (SharedStaticVariables.canFocus(this) && PositionSizeAttachment.isMouseWithin(window, this) && pressed && isDragButton(button));
		anchor.set((float) (mouseX - pos.x), (float) (mouseY - pos.y));
		
		//If we start dragging, focus on this widget
		if (!bDragging && dragging) {
			SharedStaticVariables.setFocusedWidget(this);
		}
		
		//If dragging stops, unfocus
		if (bDragging && !dragging) {
			SharedStaticVariables.setFocusedWidget(null);
		}
	}

	@Override
	public void mouseMotionEvent(Window window, long timestamp, double mouseX, double mouseY, double dx, double dy) {
		if (dragging) {
			pos.set((float) (mouseX - anchor.x), (float) (mouseY - anchor.y));
		}
	}*/

}
