package com.nvidia.developer.opengl.app;

/**
 * Mouse button masks.The button indices are bitfields.  i.e., button-3 == middle == 1<<3.
 * @author Nvidia 2014-9-12 17:46
 */
public interface NvMouseButton {

	/** Left button */
	public static final int LEFT = 0x00000001;
	/** Right button */
	public static final int RIGHT = 0x00000002;
	/** Middle button */
	public static final int MIDDLE = 0x00000004;
}
