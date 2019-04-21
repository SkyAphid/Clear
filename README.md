# Clear
Clear is a wrapper for LWJGL3 that makes it simple to build basic programs that require a user-interface. The windowing and UI wrappers are separated into two projects in case you want to use one and not the other. The goal of the project is to provide a very sound foundation for creating custom user-interfaces that aren't limited by foundational design decisions (such as forcing certain layout types to be used, or so on). This is for GUI creators who want something "free-form" to build their interfaces in.

![clear_helloworld](https://user-images.githubusercontent.com/6147299/53410619-f24dd280-3989-11e9-91dd-5c653870fc59.png) 

## General Features
- Heavily customizable: focus on user extendability
- High performance: system performance was kept in mind when creating the system, and as far as I know, it doesn't have any sort of resources leak or the like. Tools are provided to help keep resource management sweet and simple.
- Documentation: the project has carefully been documented with numerous comments so far to allow users to quickly learn how to begin adding their own functionality to the base systems
- Detailed Examples: demo programs are included that will show users how to use all of the currently implemented systems. The default LWJGL3 NanoVG demos are included as well for additional support options.

## ClearWindows
- Create OpenGL-capable windows in two lines
- Contains utilities for creating windowed programs with simple looping functionality
- Has classes for making input callbacks for windows
- Has framework for opening multiple windows at once and exerting fine control over them
- Has a TinyFileDialog wrapper class that allows you to open various message/open/save/input dialogs in one line each

## ClearVG
- Create a NanoVG-capable application within 100 lines
- Has basic tools for making pretty user-interfaces
- Reliable and customizable
- Contains basic Widgets such as shapes, buttons, draggables, and systems for assembling Widgets into complex UI elements (WidgetAssembly)
- Contains a fully realized TextAreaWidget that has text input systems, extendability, custom formatting, automatic formatting (for creating code areas), and much more

# Examples

### [Hello World (basic clickable button)](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearHelloWorldDemo.java)
![clear_helloworld](https://user-images.githubusercontent.com/6147299/53410619-f24dd280-3989-11e9-91dd-5c653870fc59.png) 


### [Text Area Support (formatting, line numbers, editing, in-depth customization)](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearTextAreaDemo.java)
![clear_textarea](https://user-images.githubusercontent.com/6147299/53695030-3a029e80-3d7c-11e9-9375-ff3f71f0b5db.png)

#### Other Examples:
- [Draggable Widget Demo](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearDraggableWidgetDemo.java)
- [Input App Demo](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearInputAppDemo.java)
- [TextAreaWidget Code Editor Demo](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearTextAreaCodeEditorDemo.java)
- [TextAreaWidget Text Field Demo](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearTextFieldDemo.java)
- [Circle Rendering Demo](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearCircleDemo.java)

# Recommended Projects (See Also)
- [LWJGUI - LWJGL3 JavaFX Alternative](https://github.com/orange451/LWJGUI)
I contributed a bit to this project, of which some of those contributions helped form the basis of Clear. If you're wanting more in-depth functionality closer to JavaFX, I highly recommend this project. Clear is meant to be somewhat more minimalistic and straight-forward, whereas LWJGUI will give you a close approximation of JavaFX's general structure. It's well-made and concise, I highly recommend this UI solution if Clear doesn't have the features you're looking for.
