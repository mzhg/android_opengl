package com.nvidia.developer.opengl.app;

/**
 * Type of pointer input devices.
 * @author Nvidia 2014-9-12 17:39
 */
public interface NvInputDeviceType {

	/** Mouse (possibly multi-button) */
	public static final int MOUSE = 0;
	/** Touch (possibly multi-touch) */
	public static final int TOUCH = 1;
	/** Stylus (possibly multiple "point" types) */
	public static final int STYLUS = 2;
}
