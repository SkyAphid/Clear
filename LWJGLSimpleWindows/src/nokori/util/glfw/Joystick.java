package nokori.util.glfw;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import nokori.util.glfw.callback.JoystickCallback;

public class Joystick {
	
	private int index;
	private String name;
	
	private float[] axes;
	private boolean[] buttons;
	
	private JoystickCallback callback;
	
	public Joystick(int index) {
		
		this.index = index;
		
		name = glfwGetJoystickName(index);
		
		FloatBuffer axisData = glfwGetJoystickAxes(index);
		axes = new float[axisData.remaining()];
		for(int i = 0; i < axes.length; i++){
			axes[i] = axisData.get(i);
		}
		
		ByteBuffer buttonData = glfwGetJoystickButtons(index);
		buttons = new boolean[buttonData.remaining()];
		for(int i = 0; i < buttons.length; i++){
			buttons[i] = buttonData.get(i) == GLFW_PRESS;
		}
		
		callback = null;
	}
	
	public void setCallback(JoystickCallback callback) {
		this.callback = callback;
	}
	
	void poll(long timestamp){
		
		FloatBuffer axisData = glfwGetJoystickAxes(index);
		if (axisData != null){
			for(int i = 0; i < axes.length; i++){
				
				float newValue = axisData.get(i);
				if(callback != null && axes[i] != newValue){
					callback.axisMoved(this, timestamp, i, newValue);
				}
				axes[i] = newValue;
			}
		}

		ByteBuffer buttonData = glfwGetJoystickButtons(index);
		
		if (buttonData != null){
			for(int i = 0; i < buttons.length; i++){
				boolean newValue = buttonData.get(i) == GLFW_PRESS;
	
				if(callback != null && buttons[i] != newValue){
					callback.buttonStateChanged(this, timestamp, i, newValue);
				}
				buttons[i] = newValue;
			}
		}
	}
	
	public int getIndex(){
		return index;
	}
	
	public String getName() {
		return name;
	}
	
	public int getAxisCount(){
		return axes.length;
	}
	
	public int getButtonCount(){
		return buttons.length;
	}
}