package nokori.clear.vg.widget;

import nokori.clear.vg.NanoVGContext;
import nokori.clear.vg.widget.assembly.Widget;
import nokori.clear.vg.widget.assembly.WidgetAssembly;

/**
 * Basic widget that has a simplified tick() function. Mostly useful for debugging (so that you don't have to override the long tick function every time).
 */
public abstract class TickerWidget extends Widget {

	@Override
	public void tick(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {
		tick();
	}

	/**
	 * A simplified tick() function that's called from the more complex default widget tick() function.
	 */
	public abstract void tick();
	
	@Override
	public void render(NanoVGContext context, WidgetAssembly rootWidgetAssembly) {}

	@Override
	public void dispose() {}

}
