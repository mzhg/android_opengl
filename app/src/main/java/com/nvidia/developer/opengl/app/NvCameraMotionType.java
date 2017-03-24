package com.nvidia.developer.opengl.app;

/**
 * Automated input-to-camera motion mapping<p>
 * Camera motion mode.
 * @author Nvidia 2014-9-13 12:36
 */
public interface NvCameraMotionType {

	/** Camera orbits the world origin */
	public static final int ORBITAL = 0;
	/** Camera moves as in a 3D, first-person shooter */
	public static final int FIRST_PERSON = 1;
	/** Camera pans and zooms in 2D */
	public static final int PAN_ZOOM = 2;
	/** Two independent orbital transforms */
	public static final int DUAL_ORBITAL = 3;
}
