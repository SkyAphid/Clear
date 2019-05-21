package nokori.clear.vg;

import nokori.clear.vg.font.Font;
import nokori.clear.vg.font.FontStyle;
import nokori.clear.vg.widget.LabelWidget;
import nokori.clear.vg.widget.assembly.DraggableWidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.vg.widget.assembly.WidgetClip;
import nokori.clear.vg.widget.assembly.WidgetSynch;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

import java.io.IOException;

public class ClearDraggableWidgetDemo extends ClearApp {

	private static final int WINDOW_WIDTH = 1280;
	private static final int WINDOW_HEIGHT = 720;
	
	public static void main(String[] args) {
		ClearApp.launch(new ClearDraggableWidgetDemo(), args);
	}

	public ClearDraggableWidgetDemo() {
		super(new WidgetAssembly(new WidgetSynch(WidgetSynch.Mode.WITH_FRAMEBUFFER)));
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {

		try {
			Font font = new Font("fonts/NotoSans/", "NotoSans-Regular", "NotoSans-Bold", "NotoSans-Italic", "NotoSans-Light").load(context);
			
			/*
			 * Create canvas that can be panned around
			 */
			
			DraggableWidgetAssembly canvas = new DraggableWidgetAssembly();
			canvas.setRequiresMouseToBeWithinWidgetToDrag(false);
			
			/*
			 * Create draggable nodes
			 */
			
			int draggableWidth = 100;
			int draggableHeight = 100;
			
			float x1 = WINDOW_WIDTH/2 - draggableWidth;
			float y1 = WINDOW_HEIGHT/2 - draggableHeight;
			
			canvas.addChild(createDraggableDemoNode(x1, y1, draggableWidth, draggableHeight, ClearColor.LIGHT_BLACK, font, "Drag me!"));
			
			float x2 = WINDOW_WIDTH/2;
			float y2 = WINDOW_HEIGHT/2;
			
			canvas.addChild(createDraggableDemoNode(x2, y2, draggableWidth, draggableHeight, ClearColor.LIGHT_BLACK, font, "Drag me too!"));
			
			/*
			 * Add button to root assembly
			 */
			
			rootWidgetAssembly.addChild(canvas);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public DraggableWidgetAssembly createDraggableDemoNode(float x, float y, float width, float height, ClearColor fill, Font font, String text) {
		DraggableWidgetAssembly draggable = new DraggableWidgetAssembly(x, y, width, height);
		draggable.setIgnoreChildrenWidgets(true);
		draggable.setBackgroundFill(fill);
		
		LabelWidget label = new LabelWidget(ClearColor.WHITE_SMOKE, text, font, FontStyle.REGULAR, 20);
		label.addChild(new WidgetClip(WidgetClip.Alignment.CENTER));
		
		draggable.addChild(label);
		
		return draggable;
	}


	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow("Clear DraggableWidget Demo", WINDOW_WIDTH, WINDOW_HEIGHT, true, true);
	}

}
