////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright 2017 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package com.nvidia.developer.opengl.app;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvUtils;
import com.nvidia.developer.opengl.utils.VectorUtil;

/**
 * Automatic mapping of input devices to camera movements.
 * <p>
 * Maps touch, mouse and gamepad into useful camera movements as represented by
 * a modelview transform. Lower-level matrices are also available for custom
 * interaction.
 * 
 * @author Nvidia 2014-9-13 12:45
 * 
 */
public class NvInputTransformer {

	private static final int TRANSLATE = 0;
	private static final int ROTATE = 1;
	private static final int SECONDARY_ROTATE = 2;
	private static final int ZOOM = 3;
	
	private static final float NV_MIN_SCALE_PCT = 0.035f;
	private static final float NV_MAX_SCALE_PCT = 500.0f;
	
	int m_motionMode = NvCameraMotionType.ORBITAL;
	boolean m_touchDown;
	byte m_maxPointsCount;
	int m_mode = TRANSLATE;
	final Vector2f m_firstInput = new Vector2f();
//	final Vector2f m_firstTouch = new Vector2f();
	final Vector2f m_lastInput = new Vector2f();
	final Vector2f m_lastPointer = new Vector2f();
	
	boolean m_lastUpExtra;
	boolean m_lastDownExtra;
	byte m_xVel_kb;
	byte m_zVel_kb;
	byte m_zVel_mouse;
	float m_xVel_gp;
	float m_zVel_gp;

	int m_width = 1;
	int m_height = 1;

	final Transform[] m_xforms = new Transform[NvCameraXformType.COUNT];
	
	public NvInputTransformer() {
		for (int i = 0; i < NvCameraXformType.COUNT; i++) {
			m_xforms[i] = new Transform();
	        m_xforms[i].m_scale = 1.0f;
	        m_xforms[i].m_dscale = 1.0f;
	        m_xforms[i].m_maxRotationVel = NvUtils.PI;
	        m_xforms[i].m_maxTranslationVel = 5.0f;
	    }
	}

	/**
	 * Set screen size.
	 * <p>
	 * Set the current screen size, so that the touch/mouse mapping methods
	 * scale correctly. Failure to keep this up to date with the screen size can
	 * lead to wildly incorrect mouse/touch mappings
	 * 
	 * @param width
	 *            the new window/screen width
	 * @param height
	 *            the new window/screen height
	 */
	public void setScreenSize(int width, int height) {
		m_width = width;
		m_height = height;
	}

	public int getScreenWidth() { return m_width; }
	public int getScreenHeight() { return m_height; }

	/**
	 * Set motion mode.
	 * <p>
	 * Set the desired input-to-motion mapping mode
	 * 
	 * @param mode the desired mapping mode
	 */
	public void setMotionMode(int mode) {
		m_motionMode = mode;
	}

	/**
	 * Get motion mode.
	 * <p>
	 * Get the current input-to-motion mapping
	 * 
	 * @return the current mapping mode
	 */
	public int getMotionMode() {
		return m_motionMode;
	}

	/**
	 * Get the current modelview transform.
	 * 
	 * @param xform <b>[</b>in<b>]</b> the index of the transform to be
	 *        referenced. For all modes except DUAL_ORBITAL, this can be left as
	 *        the default
	 * @param  out <b>[</b>out<b>]</b> hold the result if null a new matrix4f will created.
	 * @return the current combined transform matrix for the given mapping mode.
	 *         This matrix should be placed between the scene's modelview matrix
	 *         and the camera projection matrix: FinalMat = Projection X
	 *         {@link #getModelViewMat(int)} X ModelView
	 */
	public Matrix4f getModelViewMat(int xform, Matrix4f out) {
		if(out == null)
			out = new Matrix4f();
		Transform xf = m_xforms[xform];
	    if (m_motionMode == NvCameraMotionType.FIRST_PERSON) {
//	        return xf.m_rotateMat * xf.m_translateMat * xf.m_scaleMat;
	    	Matrix4f.mul(xf.m_rotateMat, xf.m_translateMat, out);
	    } else {
//	        return xf.m_translateMat * xf.m_rotateMat * xf.m_scaleMat;
	    	Matrix4f.mul(xf.m_translateMat, xf.m_rotateMat, out);
	    }
	    
	    Matrix4f.mul(out, xf.m_scaleMat, out);
	    return out;
	}
	
	/**
	 * Get the current modelview transform.
	 * 
	 * @param  out <b>[</b>out<b>]</b> hold the result if null a new matrix4f will created.
	 * @return the current combined transform matrix for the given mapping mode.
	 *         This matrix should be placed between the scene's modelview matrix
	 *         and the camera projection matrix: FinalMat = Projection X
	 *         {@link #getModelViewMat(int)} X ModelView
	 */
	public Matrix4f getModelViewMat( Matrix4f out) {
		return getModelViewMat(NvCameraXformType.MAIN, out);
	}
	
	private static float nv_clamp_scale(float s){
		return NvUtils.clamp(s, NV_MIN_SCALE_PCT, NV_MAX_SCALE_PCT);
	}

	/**
	 * Get the rotate matrices.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the given basic component matrix for the current mode and
	 *         settings. Most apps will not need these components, but will
	 *         instead use {@link #getModelViewMat(int)}
	 */
	public Matrix4f getRotationMat(int xform) {
		return m_xforms[xform].m_rotateMat;
	}
	
	/**
	 * Get the rotate matrices.
	 * 
	 * @return the given basic component matrix for the current mode and
	 *         settings. Most apps will not need these components, but will
	 *         instead use {@link #getModelViewMat(int)}
	 */
	public Matrix4f getRotationMat() {
		return m_xforms[NvCameraXformType.MAIN].m_rotateMat;
	}

	/**
	 * Get the translate matrices.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the given basic component matrix for the current mode and
	 *         settings. Most apps will not need these components, but will
	 *         instead use {@link #getModelViewMat(int)}
	 */
	public Matrix4f getTranslationMat(int xform) {
		return m_xforms[xform].m_translateMat;
	}
	

	/**
	 * Get the main translate matrices.
	 */
	public Matrix4f getTranslationMat() {
		return m_xforms[NvCameraXformType.MAIN].m_translateMat;
	}
	
	/**
	 * Get the scale matrices.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the given basic component matrix for the current mode and
	 *         settings. Most apps will not need these components, but will
	 *         instead use {@link #getModelViewMat(int)}
	 */
	public Matrix4f getScaleMat(int xform) {
		return m_xforms[xform].m_scaleMat;
	}
	
	/** Get the main scale matrices. */
	public Matrix4f getScaleMat() {
		return m_xforms[NvCameraXformType.MAIN].m_scaleMat;
	}


	/**
	 * Get the current rotation vector.
	 * <p>
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the angles (in radians) used to create the rotation component
	 *         matrix
	 */
	public Vector3f getRotationVec(int xform) {
		return m_xforms[xform].m_rotate;
	}
	
	/**
	 * Get the current rotation vector.
	 * <p>
	 * 
	 * @return the angles (in radians) used to create the rotation component
	 *         matrix
	 * @see #getRotationVec(int)
	 */
	public Vector3f getRotationVec() {
		return m_xforms[NvCameraXformType.MAIN].m_rotate;
	}

	/**
	 * Set the current rotation vector.
	 * <p>
	 * Set the rotation angles. Very useful for setting the initial view at
	 * application start.
	 * 
	 * @param vec
	 *            the angles (in radians) used to create the rotation component
	 *            matrix
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 */
	public void setRotationVec(Vector3f vec, int xform) {
		m_xforms[xform].m_rotate.set(vec);
	}
	
	/**
	 * Set the current rotation vector.
	 * <p>
	 * Set the rotation angles. Very useful for setting the initial view at
	 * application start.
	 * 
	 * @param vec
	 *            the angles (in radians) used to create the rotation component
	 *            matrix
	 * @see #setRotationVec(Vector3f, int)
	 */
	public void setRotationVec(Vector3f vec) {
		m_xforms[NvCameraXformType.MAIN].m_rotate.set(vec);
	}

	/**
	 * Get the current translation vector.
	 * 
	 * @param[in] xform the index of the transform to be referenced. For all
	 *            modes except DUAL_ORBITAL, this can be left as the default
	 * @return the vector used to create the translation component matrix
	 */
	public Vector3f getTranslationVec(int xform) {
		return m_xforms[xform].m_translate;
	}
	
	/**
	 * Get the current translation vector.
	 * 
	 * @param[in] xform the index of the transform to be referenced. For all
	 *            modes except DUAL_ORBITAL, this can be left as the default
	 * @return the vector used to create the translation component matrix
	 */
	public Vector3f getTranslationVec() {
		return m_xforms[NvCameraXformType.MAIN].m_translate;
	}

	/**
	 * Set the current translation vector.
	 * <p>
	 * Set the translation. Very useful for setting the initial view at
	 * application start.
	 * 
	 * @param vec
	 *            the translation used to create the rotation component matrix
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 */
	public void setTranslationVec(Vector3f vec, int xform) {
		m_xforms[xform].m_translate.set(vec);
	}
	
	/**
	 * Set the current translation vector.
	 * <p>
	 * Set the translation. Very useful for setting the initial view at
	 * application start.
	 * 
	 * @param vec
	 *            the translation used to create the rotation component matrix
	 * @see #setTranslationVec(Vector3f, int)
	 */
	public void setTranslationVec(Vector3f vec) {
		m_xforms[NvCameraXformType.MAIN].m_translate.set(vec);
	}
	
	/**
	 * Set the current translation vector.
	 * <p>
	 * Set the translation. Very useful for setting the initial view at
	 * application start.
	 * 
	 * @see #setTranslationVec(Vector3f, int)
	 */
	public void setTranslation(float x, float y,float z) {
		m_xforms[NvCameraXformType.MAIN].m_translate.set(x, y, z);
	}

	/**
	 * Get the current (uniform) scale factor.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the factor used to create the scale component matrix
	 */
	public float getScale(int xform) {
		return m_xforms[xform].m_scale * m_xforms[xform].m_dscale;
	}

	/**
	 * Set the current (uniform) scale factor. Set the scaling. Very useful for
	 * setting the initial view at application start.
	 * 
	 * @param scale
	 *            the scale factor used to create the scale component matrix
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 */
	public void setScale(float scale, int xform) {
		m_xforms[xform].m_scale = scale;
	}

	/**
	 * Get the current rotation velocity.
	 * 
	 * @param[in] xform the index of the transform to be referenced. For all
	 *            modes except DUAL_ORBITAL, this can be left as the default
	 * @return the current rotation velocity about each axis in radians per
	 *         second
	 */
	public Vector3f getRotationVel(int xform) {
		return m_xforms[xform].m_rotateVel;
	}

	/**
	 * Set the current rotation velocity.
	 * <p>
	 * Note this this velocity will be overwritten by gamepad axis motion. It is
	 * mainly useful for adding an initial rotation animation on the camera
	 * before the user has provided input
	 * 
	 * @param[in] vec the current rotation velocity about each axis in radians
	 *            per second.
	 * @param[in] xform the index of the transform to be referenced. For all
	 *            modes except DUAL_ORBITAL, this can be left as the default
	 **/
	public void setRotationVel(Vector3f vec, int xform) {
		m_xforms[xform].m_rotateVel.set(vec);
	}
	
	/**
	 * Set the current rotation velocity.
	 * <p>
	 * Note this this velocity will be overwritten by gamepad axis motion. It is
	 * mainly useful for adding an initial rotation animation on the camera
	 * before the user has provided input
	 * 
	 * @param[in] vec the current rotation velocity about each axis in radians
	 *            per second.
	 * @see #setRotationVel(Vector3f, int)
	 **/
	public void setRotationVel(Vector3f vec) {
		m_xforms[NvCameraXformType.MAIN].m_rotateVel.set(vec);
	}

	/**
	 * Get the current translation velocity.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the current translation velocity in units per second
	 */
	public Vector3f getTranslationVel(int xform) {
		return m_xforms[xform].m_translateVel;
	}

	/**
	 * Set the current translation velocity.
	 * <p>
	 * Note this this velocity will be overwritten by gamepad axis motion. It is
	 * mainly useful for adding an initial translation animation on the camera
	 * before the user has provided input
	 * 
	 * @param[in] vec the current translation velocity in units per second.
	 * @param[in] xform the index of the transform to be referenced. For all
	 *            modes except DUAL_ORBITAL, this can be left as the default
	 */
	public void setTranslationVel(Vector3f vec, int xform) {
		m_xforms[xform].m_translateVel.set(vec);
	}

	/**
	 * Get the current "max" rotation velocity.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the current "max" rotation velocity (the velocity produced when
	 *         the corresponding gamepad axis is at full lock) about each axis
	 *         in radians per second
	 */
	public float getMaxRotationVel(int xform) {
		return m_xforms[xform].m_maxRotationVel;
	}

	/**
	 * Set the current "max" rotation velocity.
	 * 
	 * @param maxVel
	 *            the current "max" rotation velocity (the velocity produced
	 *            when the
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 *            corresponding gamepad axis is at full lock) about each axis in
	 *            radians per second
	 */
	public void setMaxRotationVel(float maxVel, int xform) {
		m_xforms[xform].m_maxRotationVel = maxVel;
	}

	/**
	 * Get the current "max" translation velocity.
	 * 
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 * @return the current "max" translation velocity (the velocity produced
	 *         when the corresponding gamepad axis is at full lock) in units per
	 *         second
	 */
	public float getMaxTranslationVel(int xform) {
		return m_xforms[xform].m_maxTranslationVel;
	}

	/**
	 * Set the current "max" translation velocity.
	 * 
	 * @param maxVel
	 *            the current "max" translation velocity (the velocity produced
	 *            when the
	 * @param xform
	 *            the index of the transform to be referenced. For all modes
	 *            except DUAL_ORBITAL, this can be left as the default
	 *            corresponding gamepad axis is at full lock) in units per
	 *            second
	 */
	public void setMaxTranslationVel(float maxVel, int xform) {
		m_xforms[xform].m_maxTranslationVel = maxVel;
	}
	
	/**
	 * Set the current "max" translation velocity.
	 * 
	 * @param maxVel
	 *            the current "max" translation velocity (the velocity produced
	 *            when the
	 */
	public void setMaxTranslationVel(float maxVel) {
		m_xforms[NvCameraXformType.MAIN].m_maxTranslationVel = maxVel;
	}

	/**
	 * Update the matrices.
	 * <p>
	 * Update the matrices based on the current inputs, velocities, and delta
	 * time.
	 * 
	 * @param[in] deltaTime the time since the last call to #update, in seconds
	 */
	public void update(float deltaTime) {
		if (m_motionMode == NvCameraMotionType.DUAL_ORBITAL) {
	        Transform xfm = m_xforms[NvCameraXformType.MAIN];
	        Transform xfs = m_xforms[NvCameraXformType.SECONDARY];
//	        xfm.m_rotate += xfm.m_rotateVel*deltaTime;
			Vector3f.linear(xfm.m_rotate, xfm.m_rotateVel, deltaTime, xfm.m_rotate);
//	        xfs.m_rotate += xfs.m_rotateVel*deltaTime;
			Vector3f.linear(xfs.m_rotate, xfs.m_rotateVel, deltaTime, xfs.m_rotate);
//	        xfm.m_translate += xfm.m_translateVel * deltaTime;
			Vector3f.linear(xfm.m_translate, xfm.m_translateVel, deltaTime, xfm.m_translate);

	        updateMats(NvCameraXformType.MAIN);
	        updateMats(NvCameraXformType.SECONDARY);
	    } else {
	        Transform xf = m_xforms[NvCameraXformType.MAIN];
//	        xf.m_rotate += xf.m_rotateVel*deltaTime;
			Vector3f.linear(xf.m_rotate, xf.m_rotateVel, deltaTime, xf.m_rotate);
	        Vector3f transVel;
	        if (m_motionMode == NvCameraMotionType.FIRST_PERSON) {
	            // obviously, this should clamp to [-1,1] for the mul, but we don't care for sample use.
	            xf.m_translateVel.x = xf.m_maxTranslationVel * (m_xVel_kb+m_xVel_gp);
	            xf.m_translateVel.z = xf.m_maxTranslationVel * (m_zVel_mouse+m_zVel_kb+m_zVel_gp);
//	            transVel = nv.vec3f(nv.transpose(xf.m_rotateMat) * 
//	                nv.vec4f(-xf.m_translateVel.x, xf.m_translateVel.y, xf.m_translateVel.z, 0.0f));
	            xf.m_rotateMat.transpose();
	            transVel = new Vector3f(-xf.m_translateVel.x, xf.m_translateVel.y, xf.m_translateVel.z);
	            Matrix4f.transformNormal(xf.m_rotateMat, transVel, transVel);
	            xf.m_rotateMat.transpose();
	        } else {
	            transVel = xf.m_translateVel;
	        }

//	        xf.m_translate += transVel * deltaTime;
	        Vector3f.linear(xf.m_translate, transVel, deltaTime, xf.m_translate);
	        updateMats(NvCameraXformType.MAIN);
	    }
	}
	
	private void updateMats(int xform){
		Transform xf = m_xforms[xform];
		VectorUtil.rotationYawPitchRoll(xf.m_rotate.y, xf.m_rotate.x, 0.0f, xf.m_rotateMat);
		xf.m_translateMat.setIdentity(); //TODO need to do this?
		xf.m_translateMat.m30 = xf.m_translate.x;
		xf.m_translateMat.m31 = xf.m_translate.y;
		xf.m_translateMat.m32 = xf.m_translate.z;
		
		xf.m_scaleMat.setIdentity();
		float f = nv_clamp_scale(xf.m_scale*xf.m_dscale);
	    xf.m_scaleMat.scale(new Vector3f(f, f, f));
	}

	/**
	 * Pointer event input.
	 * <p>
	 * Used to pass pointer input events to the transformer. The signature
	 * explicitly matches that of the input callbacks used to provide input to
	 * an app or app framework for ease of calling
	 * 
	 * @param device the input device
	 * @param action the input action
	 * @param modifiers the input modifiers
	 * @param count the number of elements in the #points array
	 * @param points the input event points
	 * @return true if the event was used and "eaten" by the input transformer
	 *         and should not be processed by any other input system.
	 */
	public boolean processPointer(int device, int action, int modifiers,
			int count, NvPointerEvent[] points) {
		
		{ // log informations
			
//			String actionName;
//			
//			if(action == NvPointerActionType.DOWN)
//				actionName = "DOWN";
//			else if(action == NvPointerActionType.UP)
//				actionName = "UP";
//			else if(action == NvPointerActionType.MOTION)
//				actionName = "MOTION";
//			else if(action == NvPointerActionType.EXTRA_DOWN)
//				actionName = "EXTRA_DOWN";
//			else if(action == NvPointerActionType.EXTRA_UP)
//				actionName = "EXTRA_UP";
//			else
//				actionName = "UNKOWN";
//			
//			Log.e("processPointer", "action: " + actionName + ", count: " + count);
		}
		Transform xfm = m_xforms[NvCameraXformType.MAIN];
	    Transform xfs = m_xforms[NvCameraXformType.SECONDARY];
	    float x = points[0].m_x;
	    float y = points[0].m_y;
	    int button = points[0].m_id;
	    boolean needsUpdate = false;

	    if (action == NvPointerActionType.UP) {
	        // if count == 0, it's 'cancel' with no 'release' location
	        if (count==0) {
	        } else {
	        }
	        m_touchDown = false;
	        m_maxPointsCount=0;
	        // lock in scaling
	        if (m_motionMode != NvCameraMotionType.FIRST_PERSON) {
	            xfm.m_scale = nv_clamp_scale(xfm.m_scale*xfm.m_dscale);
	            xfm.m_dscale = 1.0f;
	        }
	    } else if (action == NvPointerActionType.MOTION) {
	        if (m_touchDown) {
	        	m_lastPointer.set(points[0].m_x, points[0].m_y);
	            if (m_motionMode == NvCameraMotionType.FIRST_PERSON) {
	                xfm.m_rotate.x += ((y - m_lastInput.y) / (float) m_height) * xfm.m_maxRotationVel;
	                xfm.m_rotate.y += ((x - m_lastInput.x) / (float) m_width) * xfm.m_maxRotationVel;
	                needsUpdate = true;
	            } else if (m_maxPointsCount==1) {
	                switch(m_mode) {
	                case ROTATE:
	                	if(m_lastUpExtra){
	                		m_lastUpExtra = false;
	                		m_lastInput.set(x, y);
	                	}
	                    xfm.m_rotate.x += ((y - m_lastInput.y) / (float) m_height) * xfm.m_maxRotationVel;
	                    xfm.m_rotate.y += ((x - m_lastInput.x) / (float) m_width) * xfm.m_maxRotationVel;
	                    needsUpdate = true;
	                    break;
	                case SECONDARY_ROTATE:
	                    xfs.m_rotate.x += ((y - m_lastInput.y) / (float) m_height) * xfs.m_maxRotationVel;
	                    xfs.m_rotate.y += ((x - m_lastInput.x) / (float) m_width) * xfs.m_maxRotationVel;
	                    needsUpdate = true;
	                    break;
	                case TRANSLATE:
	                    xfm.m_translate.x += ((x - m_lastInput.x) / (float) m_width) * xfm.m_maxTranslationVel;
	                    xfm.m_translate.y -= ((y - m_lastInput.y) / (float) m_height)* xfm.m_maxTranslationVel;
	                    needsUpdate = true;
	                    break;
	                case ZOOM: {
	                    float dy = y - m_firstInput.y; // negative for moving up, positive moving down.
	                    if (dy > 0) // scale up...
	                        xfm.m_dscale = 1.0f + dy/(m_height/16.0f);
	                    else if (dy < 0) { // scale down...
	                        xfm.m_dscale = 1.0f - (-dy/(m_height/2));
	                        xfm.m_dscale *= xfm.m_dscale; // accelerate the shrink...
	                    }
	                    xfm.m_dscale = nv_clamp_scale(xfm.m_dscale);
	                    {
	                    	NvLogger.e("processPointer", "zoom dscale = " + xfm.m_dscale);
	                    }
	                    needsUpdate = true;
	                    break;
	                }
	                default:
	                    break;
	                }
	            } else { // >1 points during this 'gesture'
	                 if (m_maxPointsCount==2 && count==2) {
	                    if (m_motionMode == NvCameraMotionType.DUAL_ORBITAL) {
	                        xfs.m_rotate.x += ((y - m_lastInput.y) / (float) m_height) * xfs.m_maxRotationVel;
	                        xfs.m_rotate.y += ((x - m_lastInput.x) / (float) m_width) * xfs.m_maxRotationVel;
	                        needsUpdate = true;
	                    } else if (m_motionMode != NvCameraMotionType.FIRST_PERSON) {
	                        // calc pinch dx,dy.
	                        float dx = points[0].m_x - points[1].m_x;
	                        float dy = points[0].m_y - points[1].m_y;
	                        
	                        Vector2f curr = new Vector2f(points[0].m_x - points[1].m_x, points[0].m_y - points[1].m_y);
	                        if(m_lastDownExtra){
	                        	m_firstInput.set(dx, dy);
	                        	m_lastDownExtra = false;
	                        }else{

		                        // first, handle setting scale values.
		                        // firstInput holds gesture-start dx,dy
		                        xfm.m_dscale = curr.length()/m_firstInput.length(); 
		                        xfm.m_dscale = nv_clamp_scale(xfm.m_dscale);
	                        }
	                        {
//	                        	Log.e("processPointer", "dscale = " + xfm.m_dscale);
	                        }

	                        // second, handle translation, calc new center of pinch
	                        // lastInput handles prior move x/y centerpoint.
	                        curr.set(points[0].m_x - (dx/2), points[0].m_y - (dy/2));
	                        xfm.m_translate.x += ((curr.x - m_lastInput.x) / (float) m_width) * xfm.m_maxTranslationVel;
	                        xfm.m_translate.y -= ((curr.y - m_lastInput.y) / (float) m_height)* xfm.m_maxTranslationVel;
//	                        NvLogger.i("old center = %0.2f,%0.2f . new center = %0.2f,%0.2f", m_lastInput.x, m_lastInput.y, curr.x, curr.y);
//	                        NvLogger.i("trans = %0.2f,%0.2f", xfm.m_translate.x, xfm.m_translate.y);
	                        m_lastInput.set(curr); // cache current center.

	                        needsUpdate = true;
	                    }
	                }
	            }
	        }
	    } else { // down or extra_down or extra_up
	        if (action == NvPointerActionType.DOWN) {
	        	if(m_motionMode != NvCameraMotionType.FIRST_PERSON || (!m_touchDown && m_maxPointsCount == 0)) {
					m_touchDown = true;
					m_maxPointsCount = 1;
					xfm.m_dscale = 1.0f; // for sanity reset to 1.

					if (m_motionMode == NvCameraMotionType.PAN_ZOOM)
						m_mode = TRANSLATE;
					else
						m_mode = ROTATE;

					if (m_motionMode != NvCameraMotionType.FIRST_PERSON) {
						if (device == NvInputDeviceType.MOUSE) {
							if (m_motionMode == NvCameraMotionType.ORBITAL) {
								if ((button & NvMouseButton.MIDDLE) != 0)
									m_mode = ZOOM;
								else if ((button & NvMouseButton.RIGHT) != 0)
									m_mode = TRANSLATE;
							} else if (m_motionMode == NvCameraMotionType.DUAL_ORBITAL) {
								if ((button & NvMouseButton.LEFT) != 0)
									m_mode = ROTATE;
								else if ((button & NvMouseButton.RIGHT) != 0)
									m_mode = SECONDARY_ROTATE;
							} else { // PAN_ZOOM
								if ((button & NvMouseButton.RIGHT) != 0)
									m_mode = ZOOM;
							}
						}
					}

//	            m_firstTouch.set(points[0].m_x, points[0].m_y);
					m_firstInput.set(points[0].m_x, points[0].m_y);
					m_lastPointer.set(points[0].m_x, points[0].m_y);
				}
	        } else if (action == NvPointerActionType.EXTRA_DOWN) {
	        	if( m_motionMode != NvCameraMotionType.FIRST_PERSON) {
					m_maxPointsCount++;
					if (m_maxPointsCount == 2) m_lastDownExtra = true;
					if ((m_motionMode != NvCameraMotionType.FIRST_PERSON) && (m_maxPointsCount == 2)) {
						if (m_motionMode == NvCameraMotionType.DUAL_ORBITAL)
							m_mode = SECONDARY_ROTATE;
						else
							m_mode = ZOOM;
						// cache starting distance across pinch
						float dx, dy;
						if (count > 1) {
							dx = points[0].m_x - points[1].m_x;
							dy = points[0].m_y - points[1].m_y;
							m_lastPointer.set(points[1].m_x, points[1].m_y);
						} else {
							dx = points[0].m_x - m_lastPointer.x;
							dy = points[0].m_y - m_lastPointer.y;
							m_lastPointer.set(points[0].m_x, points[0].m_y);
						}
						m_firstInput.set(dx, dy);
						// cache center of pinch.
						m_lastInput.set(points[0].m_x - (dx / 2), points[0].m_y - (dy / 2));
					}
				}
	        } else {
	            // extra up.
	        	m_maxPointsCount --;
	        	if(m_maxPointsCount == 1)
	        		m_lastUpExtra = true;
	        	
	        	if(m_motionMode == NvCameraMotionType.ORBITAL && m_maxPointsCount == 1){
	        		m_mode = ROTATE;
	        		
	        		 if (m_motionMode != NvCameraMotionType.FIRST_PERSON) {
	     	            xfm.m_scale = nv_clamp_scale(xfm.m_scale*xfm.m_dscale);
	     	            xfm.m_dscale = 1.0f;
	     	        }
	        		 
//	        		m_firstInput.set(m_firstTouch);
//	        		m_lastInput.set(m_firstTouch);
	        	}
	        }
	    }

	    /*if (m_motionMode == NvCameraMotionType.FIRST_PERSON) {
	        m_zVel_mouse = 0;
	        if ( (count > 1) 
	            || ((device == NvInputDeviceType.MOUSE) && (button & NvMouseButton.MIDDLE) != 0
	                    && m_touchDown) )
	            m_zVel_mouse = +1;
	    }*/

	    if ((m_maxPointsCount==1) || (m_motionMode == NvCameraMotionType.FIRST_PERSON) ||
	        (m_motionMode == NvCameraMotionType.DUAL_ORBITAL)) {
	        m_lastInput.set(x,y);
	    }

	    if (needsUpdate) {
	        updateMats(NvCameraXformType.MAIN);
	        if (m_motionMode == NvCameraMotionType.DUAL_ORBITAL)
	            updateMats(NvCameraXformType.SECONDARY);
	    }

	    return true;
	}

	/**
	 * Gamepad event input.
	 * <p>
	 * Used to pass gamepad state to the transformer. The signature explicitly
	 * matches that of the input callbacks used to provide input to an app or
	 * app framework for ease of calling
	 * 
	 * @param[in] changedPadFlags the changes pad flags
	 * @param[in] pad the gamepad device
	 * @return true if the event was used and "eaten" by the input transformer
	 *         and should not be processed by any other input system.
	 */
	public boolean processGamepad(int changedPadFlags, /* NvGamepad& */int pad) {
		// TODO unimplemented
		return false;
	}

	/**
	 * Key event input.
	 * <p>
	 * Used to pass key input events to the transformer. The signature
	 * explicitly matches that of the input callbacks used to provide input to
	 * an app or app framework for ease of calling
	 * 
	 * @param code
	 *            the input keycode
	 * @param action
	 *            the input action
	 * @return true if the event was used and "eaten" by the input transformer
	 *         and should not be processed by any other input system.
	 */
	public boolean processKey(int code, int action) {
		if (m_motionMode == NvCameraMotionType.FIRST_PERSON) {
	        // FPS mode uses WASD for motion, so that UI can own arrows for focus control.
	        switch(code) {
	            case NvKey.K_W:
	                if (action == NvKeyActionType.UP) {
	                    if (m_zVel_kb == +1) // only turn our own value 'off'
	                        m_zVel_kb = 0;
	                } else {
	                    m_zVel_kb = +1;
	                }
	                return true;
	            case NvKey.K_S:
	                if (action == NvKeyActionType.UP) {
	                    if (m_zVel_kb == -1) // only turn our own value 'off'
	                        m_zVel_kb = 0;
	                } else {
	                    m_zVel_kb = -1;
	                }
	                return true;

	            case NvKey.K_A:
	                if (action == NvKeyActionType.UP) {
	                    if (m_xVel_kb == -1) // only turn our own value 'off'
	                        m_xVel_kb = 0;
	                } else {
	                    m_xVel_kb = -1;
	                }
	                return true;
	            case NvKey.K_D:
	                if (action == NvKeyActionType.UP) {
	                    if (m_xVel_kb == +1) // only turn our own value 'off'
	                        m_xVel_kb = 0;
	                } else {
	                    m_xVel_kb = +1;
	                }
	                return true;
	        }
	    }

	    return false;
	}

	private final class Transform {
		final Vector3f m_translateVel = new Vector3f();
		final Vector3f m_rotateVel = new Vector3f();
		float m_maxRotationVel;
		float m_maxTranslationVel;

		final Vector3f m_translate = new Vector3f();
		final Vector3f m_rotate = new Vector3f();
		float m_scale, m_dscale;

		final Matrix4f m_translateMat = new Matrix4f();
		final Matrix4f m_rotateMat = new Matrix4f();
		final Matrix4f m_scaleMat = new Matrix4f();
	}
}
