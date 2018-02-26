////////////////////////////////////////////////////////////////////////////////
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
package jet.learning.opengl.fight404;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvPointerActionType;
import com.nvidia.developer.opengl.app.NvPointerEvent;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/12/20.
 */
public final class Fireworks extends NvSampleApp {
    private int mBlockBuffer;
    final BlockData mBlockData = new BlockData();
    private final ByteBuffer mBlockMemory = BufferUtils.createByteBuffer(Math.max(BlockData.SIZE, RenderFrame.SIZE));

    private int mFrameBuffer;
    final RenderFrame mRenderFrame = new RenderFrame();

    private int mRandomTex2D;

    private boolean mTouched;
    private int mTouchID;
    private float mTouchX, mTouchY;
    private boolean point_sprite_switcher;

    private Emitter emitter;
    private ParticleSystem particles;

    @Override
    protected void initRendering() {
        float mTheta = PI/4;
        float mPhi = PI/3;
        float mRadius = (float)Math.sqrt(100 * 100 + 1500 * 1500);
        float x = (float) (mRadius*Math.sin(mPhi)*Math.cos(mTheta));
        float z = (float) (mRadius*Math.sin(mPhi)*Math.sin(mTheta));
        float y = (float) (mRadius*Math.cos(mPhi));

        initCamera(0, new Vector3f(-x,y,-z), Vector3f.ZERO);

        mBlockBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mBlockBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, BlockData.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        mFrameBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mFrameBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, RenderFrame.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        mRandomTex2D = createRandomTexture1D(256);

        emitter = new Emitter(this);
        particles = new ParticleSystem(this);
    }

    private void updateCamera(){
        m_transformer.getModelViewMat(mRenderFrame.view);
        Matrix4f.decompseRigidMatrix(mRenderFrame.view, mBlockData.eye_loc, null, null, mBlockData.lookat);

        mBlockData.timeAmout = getFrameDeltaTime();
        mBlockData.gravity.set(0, -9.8f, 0);
        emitter.update();
        particles.update(isTouched());
    }

    @Override
    protected void draw() {
        updateCamera();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        emitter.draw();
        particles.draw();
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix4f.perspective(NvUtils.PI*2/3, (float)width/height, 0.1f, 3000f, mRenderFrame.projection);
    }

    void updateRenderFrame(int index, int type, float pointSize){
        mRenderFrame.render_particle = type;
        mRenderFrame.pointSize = pointSize;

        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, index, mFrameBuffer);
        mBlockMemory.clear();
        mRenderFrame.store(mBlockMemory);
        mBlockMemory.flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mBlockMemory.remaining(), mBlockMemory);
    }

    void updateBlockdataAndBind(int index){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, index, mBlockBuffer);
        mBlockMemory.clear();
        mBlockData.store(mBlockMemory);
        mBlockMemory.flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mBlockMemory.remaining(), mBlockMemory);
    }

    final boolean isTouched() { return mTouched;}
    final float getTouchX()   { return mTouchX;}
    final float getTouchY()   { return mTouchY;}

    void enablePointSprite(){
        if(!point_sprite_switcher){
//            GLES20.glEnable(GLES20.GL_POINT_SPRITE);
//            GLES20.glEnable(GLES30.GL_PROGRAM_POINT_SIZE);
            point_sprite_switcher = true;
        }
    }

    void disablePointSprite(){
        if(point_sprite_switcher){
//            GLES20.glDisable(GLES32.GL_POINT_SPRITE);
//            GLES20.glDisable(GLES32.GL_PROGRAM_POINT_SIZE);
            point_sprite_switcher = false;
        }
    }

    @Override
    public boolean handlePointerInput(int device, int action, int modifiers, int count, NvPointerEvent[] points) {
        if(action == NvPointerActionType.DOWN){
            if(!mTouched){
                mTouched = true;
                mTouchID = points[0].m_id;
                mTouchX = points[0].m_x;
                mTouchY = points[0].m_y;

                return true;
            }
        }else if(action == NvPointerActionType.UP){
            if(mTouched ){
                for(int i = 0; i < count; i++){
                    if(points[i].m_id == mTouchID){
                        mTouched = false;
                        return true;
                    }
                }
            }
        }else if(action == NvPointerActionType.MOTION){
            if(mTouched){
                for(int i = 0; i < count; i++){
                    if(points[i].m_id == mTouchID){
                        mTouchX = points[i].m_x;
                        mTouchY = points[i].m_y;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    void bindRandomTex(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRandomTex2D);
    }

    static NvGLSLProgram createShader(String filename, int shaderType){
        NvGLSLProgram program = new NvGLSLProgram();
        CharSequence source = NvAssetLoader.readText(filename);

        NvGLSLProgram.ShaderSourceItem item = new NvGLSLProgram.ShaderSourceItem(source, shaderType);
        program.setSourceFromStrings(item);

        return program;
    }

    static int createRandomTexture1D(int width){
        int internalFormat = GLES30.GL_RGB8;

        ByteBuffer buf = BufferUtils.createByteBuffer(width * 3);
        for(int i = 0; i < width; i++){
            for(int j = 0;j < 3; j++)
                buf.put((byte)(Math.random() * 255));
        }

        buf.flip();

        int texture = GLES.glGenTextures();
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, 1,0, GLES20.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return texture;
    }
}
