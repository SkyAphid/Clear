# Clear
Clear is a wrapper for LWJGL3 that makes it simple to build basic programs that require a user-interface. The windowing and UI wrappers are separated into two projects in case you want to use one and not the other. The goal of the project is to provide a very sound foundation for creating custom user-interfaces that aren't limited by foundational design decisions (such as forcing certain layout types to be used, or so on). This is for GUI creators who want something "free-form" to build their interfaces in.

![clear_helloworld](https://user-images.githubusercontent.com/6147299/53410619-f24dd280-3989-11e9-91dd-5c653870fc59.png) 

## ClearWindows
- Create OpenGL-capable windows in two lines
- Contains utilities for creating windowed programs with simple looping functionality
- Has classes for making input callbacks for windows
- Has a TinyFileDialog wrapper class that allows you to open various message/open/save dialogs in one line each

## ClearVG
- Create a NanoVG-capable application within 100 lines
- Has basic tools for making pretty user-interfaces
- Reliable and customizable

# Examples

### [Hello World (basic clickable button)](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearTextFieldDemo.java)
![clear_helloworld](https://user-images.githubusercontent.com/6147299/53410619-f24dd280-3989-11e9-91dd-5c653870fc59.png) 


### [Text Area Support (formatting, line numbers, editing, in-depth customization)](https://github.com/SkyAphid/Clear/blob/master/ClearVG/demo/nokori/clear/vg/ClearTextFieldDemo.java)
![clear_textarea](https://user-images.githubusercontent.com/6147299/53695030-3a029e80-3d7c-11e9-9375-ff3f71f0b5db.png)


# See Also
- [LWJGUI - LWJGL3 JavaFX Alternative](https://github.com/orange451/LWJGUI)
I contributed a bit to this project, of which some of those contributions helped for the basis of Clear. If you're wanting more in-depth functionality closer to JavaFX, I recommend this project. Clear is meant to be somewhat more minimalistic and straight-forward, whereas LWJGUI will give you a close approximation of JavaFX's general structure. It's well-made and concise, I highly recommend this UI solution if Clear doesn't have the features you're looking for.
