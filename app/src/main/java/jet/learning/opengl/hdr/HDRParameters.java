////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright (c) 2017 mzhg
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
package jet.learning.opengl.hdr;

import jet.learning.opengl.common.FrameBufferObject;

public class HDRParameters {

	/** The texture buffer that will be processing. */
	public int scene_texture;
	/** The width of the texture buffer.*/
	public int scene_width = -1;
	/** The height of the texture buffer. */
	public int scene_height = -1;
	/** The elapsed time between the sequences of two frames. */
	public float elpsedTime;
	/** The output framebuffer object. If null it will output to the default framebuffer. */
	public FrameBufferObject dest;
	
	public int viewport_width = -1;
	public int viewport_height = -1;
	
	// properties of the HDRPost
	public int  starGenLevel = 0;
	public float lumThreshold = 1.0f;
	public float lumScaler = 0.3f;
	public int glareType = HDRPostProcessing.CAMERA_GLARE;
	public float explosure = 1.4f;
	public float blendAmount = 0.33f;
	public float gamma = 1.0f / 1.8f;
	public boolean autoExposure = true;
	public boolean enableHDR = true;
	public boolean enableLightStreaker = true;
	public boolean enableLensFlare = true;
	
	@Override
	public String toString() {
		return "HDRParameters [scene_texture=" + scene_texture + ", scene_width=" + scene_width + ", scene_height="
				+ scene_height + ", elpsedTime=" + elpsedTime + ", dest=" + dest + ", viewport_width=" + viewport_width
				+ ", viewport_height=" + viewport_height + ", starGenLevel=" + starGenLevel + ", lumThreshold=" + lumThreshold + ", lumScaler=" + lumScaler + ", glareType="
				+ glareType + ", explosure=" + explosure + ", blendAmount=" + blendAmount + ", gamma=" + gamma + "]";
	}
}
