package nokori.clear.vg;

import nokori.clear.vg.ClearColor;

import nokori.clear.vg.ClearApp;
import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.CircleWidget;
import nokori.clear.vg.widget.HalfCircleWidget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;
import nokori.clear.windows.GLFWException;
import nokori.clear.windows.Window;
import nokori.clear.windows.WindowManager;

public class ClearCircleDemo extends ClearApp {

	private static final int WINDOW_WIDTH = 256;
	private static final int WINDOW_HEIGHT = 256;
	
	public static void main(String[] args) {
		ClearApp.launch(new ClearCircleDemo(), args);
	}
	
	@Override
	public void init(WindowManager windowManager, Window window, NanoVGContext context, WidgetAssembly rootWidgetAssembly, String[] args) {
		float circleRadius = 25f;
		float circleY = rootWidgetAssembly.getHeight()/2 - circleRadius;
		float circlePadding = circleRadius;
		
		/*
		 * Half Circle
		 */
		
		float sectorCircleX = circlePadding;
		HalfCircleWidget sectorCircleWidget = new HalfCircleWidget(sectorCircleX, circleY, circleRadius, ClearColor.BLACK, ClearColor.CORAL, HalfCircleWidget.Orientation.LEFT);
		sectorCircleWidget.setOnMouseEnteredEvent(e -> {
			System.out.println("Mouse entered sector circle");
		});
		
		sectorCircleWidget.setOnMouseExitedEvent(e -> {
			System.out.println("Mouse exited sector circle");
		});
		
		rootWidgetAssembly.addChild(sectorCircleWidget);
		
		/*
		 * Normal Circle
		 */
		float circleX = rootWidgetAssembly.getWidth() - (circleRadius * 2) - circlePadding;
		CircleWidget circleWidget = new CircleWidget(circleX, circleY, circleRadius, ClearColor.CORAL, ClearColor.BLACK);
		
		circleWidget.setOnMouseEnteredEvent(e -> {
			System.out.println("Mouse entered circle");
		});
		
		circleWidget.setOnMouseExitedEvent(e -> {
			System.out.println("Mouse exited circle");
		});
		
		rootWidgetAssembly.addChild(circleWidget);
	}


	@Override
	protected void endOfNanoVGApplicationCallback() {
		
	}

	@Override
	public Window createWindow(WindowManager windowManager) throws GLFWException {
		return windowManager.createWindow("Clear", WINDOW_WIDTH, WINDOW_HEIGHT, true, true);
	}

}
