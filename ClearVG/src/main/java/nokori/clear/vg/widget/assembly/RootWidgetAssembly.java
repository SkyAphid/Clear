package nokori.clear.vg.widget.assembly;

/**
 * This is just a WidgetAssembly configured for the typical use-case of a root WidgetAssembly uses in a ClearApplication
 * @author Brayden
 *
 */
public class RootWidgetAssembly extends WidgetAssembly {
	public RootWidgetAssembly() {
		super(new WidgetSynch(WidgetSynch.Mode.WITH_FRAMEBUFFER));
	}
}
