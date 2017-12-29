package jet.learning.opengl.fight404;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/12/20.
 */

final class ParticleSystem {
    private static final int MAX_PARTICLE_TAIL_COUNT = 0;
    static final int MAX_PARTICLE_COUNT = 10_000;
    static final int MAX_EMITTER_COUNT = 16;

    private static final int TYPE_BORN = 0;      // particles born
    private static final int TYPE_UPDATE = 1;    // update the particles
    private static final int TYPE_NEBULA = 2;    // update the nebulas
    private static final int TYPE_NEBORN = 3;    // born the emitter nebulas

    static final int PARTICLE_SIZE = 48;

    static final int PAR_LOC_OFFSET = 0;
    static final int PAR_VEL_OFFSET = 12;
    static final int PAR_RADIUS_OFFSET = 24;
    static final int PAR_AGE_OFFSET = 28;
    static final int PAR_LIFE_SPAN_OFFSET = 32;
    static final int PAR_GEN_OFFSET = 36;
    static final int PAR_BOUNCE_AGE_OFFSET = 40;
    static final int PAR_TYPE_OFFSET = 44;

    private BufferChain[] particle_chains = new BufferChain[2];
    // Internal variables
    private long last_update_time;
    private int current_chain;
    private boolean first_loop;
    private int count = 0;
    private IntBuffer render_data = BufferUtils.createIntBuffer(4);

    // OpenGL Textures
    private int particle_sprite;
    private int nebula_sprite;

    // Shader buffers
    private int render_data_buffer;
    private int dispatch_direct_buffer;
    private int draw_indirect_buffer;

    // OpenGL Programs
    private NvGLSLProgram particle_update;
    private NvGLSLProgram particle_args;
    private NvGLSLProgram newbula_args;
    private NvGLSLProgram particle_render;

    private Fireworks mContext;

    ParticleSystem(Fireworks context){
        mContext = context;

        particle_update = Fireworks.createShader("fight404/ParticleUpdateCS.comp", GLES31.GL_COMPUTE_SHADER);
        particle_args = Fireworks.createShader("fight404/ParticleComputeArgsCS.comp", GLES31.GL_COMPUTE_SHADER);
        newbula_args = Fireworks.createShader("fight404/NebulaComputeArgsCS.comp", GLES31.GL_COMPUTE_SHADER);
        particle_render = NvGLSLProgram.createFromFiles("fight404/ParticleRenderVS.vert", "fight404/ParticleRenderPS.frag");

        particleChans();
        particle_sprite = Glut.loadTextureFromFile("textures/particle.png", GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        nebula_sprite = Glut.loadTextureFromFile("textures/corona.png", GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
    }

    void update(boolean addSeed){
        // 1. update the emitter source.
        boolean needAddNewParticle = false;
        if(/*camera.isRightButtonDown()*/addSeed){
            long current_time = System.currentTimeMillis();
            if(current_time - last_update_time > 50){
                for(int i = 0; i < MAX_EMITTER_COUNT; i++){
                    float seed = (float)Math.random();
                    mContext.mBlockData.seeds[i] = seed;
                }

                last_update_time = System.currentTimeMillis();
                needAddNewParticle = true;
            }
        }

        GLES.checkGLError();

        // 2. perapre the transform feedback to record the particle data.
        particle_update.enable(); // we must bind the program first.
        mContext.updateBlockdataAndBind(0);
        particle_chains[current_chain].beginRecord(GLES20.GL_POINTS, 1, 3);
        GLES.checkGLError();
        mContext.bindRandomTex();

        {
            setUniform(TYPE_NEBORN, 1);
            dispatch(1);
        }
        GLES.checkGLError();

        // 3. Generate the new particles
        if(needAddNewParticle){
            setUniform(TYPE_BORN, MAX_EMITTER_COUNT);
            dispatch(MAX_EMITTER_COUNT);
        }

        //4. draw the previous transform feedback to current.
        if(first_loop){
            /*particle_update.applyType(TYPE_UPDATE);
            particle_update.applyRecord(count % 5 == 0);
            particle_chains[1-current_chain].drawVAO(0);
            GL11.glFlush();
            GLError.checkError();*/

            {
                GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 5, 0);
                GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, 0);
                GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, 0);

                updateParticleArgs();
                particle_update.enable();

                particle_chains[1-current_chain].bindResource(5);
                GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, render_data_buffer);
                GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, dispatch_direct_buffer);
                GLES31.glDispatchComputeIndirect(0);
                GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);
                GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, 0);
            }

            {
                GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 5, 0);
                GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, 0);
                GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, 0);
                updateNebulaArgs();
                particle_update.enable();

                particle_chains[1-current_chain].bindResource(5);
                GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, render_data_buffer);
                GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, dispatch_direct_buffer);
                GLES31.glDispatchComputeIndirect(0);
                GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);
                GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, 0);
                GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, 0);
            }
        }

        // 5. done.
        particle_chains[current_chain].endRecord();
        first_loop = true;
        count ++;

        // unbind all of the resources.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 1, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 2, 0);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 3, 0);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 4, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 5, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, 0);
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, 0);
    }

    /** draw the particles. */
    void draw(){
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ZERO, GLES20.GL_ZERO);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);

        //1, draw the particles.
        CopyStructureCount(draw_indirect_buffer, particle_chains[current_chain].getAtomicBuffer(0), 0);
        mContext.updateRenderFrame(0,0, 1);
        particle_render.enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, particle_sprite);

        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer);
        particle_chains[current_chain].drawIndirectArrays(0);
        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, 0);
        GLES.checkGLError();

        // 2, draw the nebulas
        CopyStructureCount(draw_indirect_buffer, particle_chains[current_chain].getAtomicBuffer(1), 0);
        mContext.updateRenderFrame(0,2, 1);
        particle_render.enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, nebula_sprite);

        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer);
        particle_chains[current_chain].drawIndirectArrays(1);
        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, 0);

        GLES.checkGLError();

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDepthMask(true);

        current_chain = 1 - current_chain;  // swap the buffer

    }

    private void updateParticleArgs(){
        particle_args.enable();

        // unordered acess views.
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, render_data_buffer);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 7, dispatch_direct_buffer);

        // shader resources views
        int atomic_buffer = particle_chains[1-current_chain].getAtomicBuffer(0);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 5, atomic_buffer);

        // Invoke the compute shader
        GLES31.glDispatchCompute(1,1, 1);
        GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);

        // unbind the resources
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 7, 0);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 5, 0);

        GLES.checkGLError();
    }

    private void updateNebulaArgs(){
        newbula_args.enable();

        // unordered acess views.
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, render_data_buffer);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 7, dispatch_direct_buffer);

        // shader resources views
        int atomic_buffer = particle_chains[1-current_chain].getAtomicBuffer(1);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 5, atomic_buffer);

        // Invoke the compute shader
        GLES31.glDispatchCompute(1,1, 1);
        GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);

        // unbind the resources
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 6, 0);
        GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 7, 0);
        GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 5, 0);

        GLES.checkGLError();
    }

    private void dispatch(int count){
        int x = NvUtils.divideAndRoundUp(count, 32);
        if(x < 1)
            throw new IllegalArgumentException();
        GLES31.glDispatchCompute(x, 1,1 );
        GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);
    }

    private void CopyStructureCount(int dstBuffer, int srcBuffer, int offset){
        GLES20.glBindBuffer(GLES30.GL_COPY_READ_BUFFER, srcBuffer);
        GLES20.glBindBuffer(GLES30.GL_COPY_WRITE_BUFFER, dstBuffer);
        GLES30.glCopyBufferSubData(GLES30.GL_COPY_READ_BUFFER, GLES30.GL_COPY_WRITE_BUFFER, offset,0, 4);
        GLES20.glBindBuffer(GLES30.GL_COPY_READ_BUFFER, 0);
        GLES20.glBindBuffer(GLES30.GL_COPY_WRITE_BUFFER, 0);

        GLES.checkGLError();
    }

    private void setUniform(int type, int maxParticleCount){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 7, render_data_buffer);
        render_data.put(type);
        render_data.put(maxParticleCount);
        render_data.position(4).flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER,0, 16, render_data);

        /*particle_update.setUniform1i("g_Type", type);
        particle_update.setUniform1i("g_MaxParticles", maxParticleCount);*/
    }

    private void particleChans(){
        int size = PARTICLE_SIZE * MAX_PARTICLE_COUNT;
        int[] sizes = {size + MAX_PARTICLE_TAIL_COUNT * 12, size};
        Runnable[] bindings = {ParticleSystem::vertexTailBinding, ParticleSystem::vertexBinding};
        particle_chains[0] = new BufferChain(sizes, bindings);
        particle_chains[1] = new BufferChain(sizes, bindings);

        render_data_buffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, render_data_buffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, 16, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        dispatch_direct_buffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, dispatch_direct_buffer);
        GLES20.glBufferData(GLES31.GL_DISPATCH_INDIRECT_BUFFER, 12, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES31.GL_DISPATCH_INDIRECT_BUFFER, 0);

        draw_indirect_buffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer);
        GLES.glBufferData(GLES31.GL_DRAW_INDIRECT_BUFFER, GLUtil.wrap(0,1,0,0), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES31.GL_DRAW_INDIRECT_BUFFER, 0);
    }

    private static void vertexTailBinding(){
        final int stride = PARTICLE_SIZE;
        GLES20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, PAR_LOC_OFFSET);
        GLES20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, PAR_VEL_OFFSET);
        GLES20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, stride, PAR_RADIUS_OFFSET);
        GLES20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, stride, PAR_AGE_OFFSET);
        GLES20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, stride, PAR_LIFE_SPAN_OFFSET);
        GLES20.glVertexAttribPointer(5, 1, GL11.GL_FLOAT, false, stride, PAR_GEN_OFFSET);
        GLES20.glVertexAttribPointer(6, 1, GL11.GL_FLOAT, false, stride, PAR_BOUNCE_AGE_OFFSET);
//        GLES30.glVertexAttribIPointer(7, 1, GLES20.GL_UNSIGNED_INT, stride, PAR_TYPE_OFFSET);

        for(int i = 0;i < MAX_PARTICLE_TAIL_COUNT; i++){
            GLES20.glVertexAttribPointer(8 + i, 3, GL11.GL_FLOAT, false, stride, PAR_BOUNCE_AGE_OFFSET + 4 + i * 12);
        }

        for(int k = 0; k < 7 + MAX_PARTICLE_TAIL_COUNT; k++)
            GLES20.glEnableVertexAttribArray(k);
    }

    private static void vertexBinding(){
        GLES20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_LOC_OFFSET);
        GLES20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_VEL_OFFSET);
        GLES20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_RADIUS_OFFSET);
        GLES20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_AGE_OFFSET);
        GLES20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_LIFE_SPAN_OFFSET);
        GLES20.glVertexAttribPointer(5, 1, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_GEN_OFFSET);
        GLES20.glVertexAttribPointer(6, 1, GL11.GL_FLOAT, false, PARTICLE_SIZE, PAR_BOUNCE_AGE_OFFSET);
        GLES20.glVertexAttribPointer(7, 1, GLES20.GL_UNSIGNED_INT, false, PARTICLE_SIZE, PAR_TYPE_OFFSET);

        for(int k = 0; k < 8; k++)
            GLES20.glEnableVertexAttribArray(k);
    }
}
