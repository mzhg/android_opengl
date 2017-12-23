package jet.learning.opengl.fight404;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/12/20.
 */

final class Emitter {
    final Vector3f emiter_dir = new Vector3f();
    final Vector2f mouse_prev = new Vector2f();
    final Vector2f mouse_curr = new Vector2f();
    final Vector2f mouse_vel = new Vector2f();
    final Vector3f right = new Vector3f();
    final Vector3f up = new Vector3f();
    final Vector4f reflect_color = new Vector4f();
    final Matrix4f tempMat = new Matrix4f();
    float emiter_diam = 5;
    boolean has_emiter_reflect;

    NvGLSLProgram render_program;
    NvGLSLProgram reflect_program;
    int emitter_sprite;
    int reflect_sprite;

    private Fireworks mContext;
    private final FloatBuffer emiter_reflect_buf = BufferUtils.createFloatBuffer(16);

    public Emitter(Fireworks context) {
        mContext = context;

        reflect_program = NvGLSLProgram.createFromFiles("fight404/", "fight404/"); // TODO
        render_program = NvGLSLProgram.createFromFiles("fight404/EmiterRenderVS.vert", "fight404/EmiterRenderPS.frag");

        emitter_sprite = Glut.loadTextureFromFile("textures/emitter.png", GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        reflect_sprite = Glut.loadTextureFromFile("textures/reflection.png", GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
    }

    void update(){
        ReadableVector3f eyePos = /*camera.getEyePosition()*/mContext.mBlockData.eye_loc;

        if(mContext.isTouched()){ // update the emitter position if the finger touched
            float sx = mContext.getTouchX();
            float sy = mContext.getTouchY();
            mouse_curr.set(sx, sy);
            Vector2f.sub(mouse_curr, mouse_prev, mouse_vel);
            mouse_vel.scale(mContext.getFrameDeltaTime() * 5);
            Vector2f.add(mouse_prev, mouse_vel,mouse_prev);

            float half_height = mContext.getHeight() * 0.5f;
            if(mouse_prev.y > half_height){
                mouse_prev.y = half_height;
            }

            Vector3f dir = grap(mouse_prev.x, mouse_prev.y, mContext.getWidth(), mContext.getHeight(), emiter_dir);

            Vector3f plane_normal = right;
            plane_normal.set(eyePos.getX(), 0, eyePos.getZ());
            plane_normal.normalise();

            final Vector3f emiter_pos = mContext.mBlockData.emitPosition;
            float t = -Vector3f.dot(plane_normal, eyePos)/Vector3f.dot(plane_normal, dir);
            Vector3f.linear(eyePos, dir, t, emiter_pos);
            float radius = emiter_diam * 0.5f;
            Vector3f.cross(Vector3f.Y_AXIS, plane_normal, up);

			Vector3f out = dir; // reuse the vector3f instance.
//			v[0] = vec4(gin[0].CenterW + halfWidth*right - halfHeight*up, 1.0f);
//			v[1] = vec4(gin[0].CenterW + halfWidth*right + halfHeight*up, 1.0f);
//			v[2] = vec4(gin[0].CenterW - halfWidth*right - halfHeight*up, 1.0f);
//			v[3] = vec4(gin[0].CenterW - halfWidth*right + halfHeight*up, 1.0f);

			/*emiter_buf.position(8);
			Vector3f.linear(right, +radius, up, -radius, out); Vector3f.add(emiter_pos, out, out); out.store(emiter_buf);
			Vector3f.linear(right, +radius, up, +radius, out); Vector3f.add(emiter_pos, out, out); out.store(emiter_buf);
			Vector3f.linear(right, -radius, up, -radius, out); Vector3f.add(emiter_pos, out, out); out.store(emiter_buf);
			Vector3f.linear(right, -radius, up, +radius, out); Vector3f.add(emiter_pos, out, out); out.store(emiter_buf);
			emiter_buf.flip();*/

			float altitude = emiter_pos.y;
			float reflectMaxAltitude = 300.0f;
			float yPer = 1.0f - altitude / reflectMaxAltitude;
//			float distance = (emiter_pos.y - emiter_radius);
//			if(distance < 0.0f){
			if(yPer > 0.05f){
//				float half_size = (float) Math.sqrt(emiter_radius * emiter_radius - distance * distance);
				float half_size = radius * 5.0f;
				reflect_color.set(0.5f, 1.0f, yPer * .25f, yPer);
				Vector3f.linear(right, +half_size, plane_normal, -half_size, out); Vector3f.add(emiter_pos, out, out); out.y = 0; out.store(emiter_reflect_buf);
				Vector3f.linear(right, +half_size, plane_normal, +half_size, out); Vector3f.add(emiter_pos, out, out); out.y = 0; out.store(emiter_reflect_buf);
				Vector3f.linear(right, -half_size, plane_normal, -half_size, out); Vector3f.add(emiter_pos, out, out); out.y = 0; out.store(emiter_reflect_buf);
				Vector3f.linear(right, -half_size, plane_normal, +half_size, out); Vector3f.add(emiter_pos, out, out); out.y = 0; out.store(emiter_reflect_buf);
				emiter_reflect_buf.flip();
				has_emiter_reflect = true;

				/*if(camera.isRightButtonDown()){
					has_emiter_reflect2 = true;
					reflect_color2.set(1.0f, 0, 0, yPer);
				}else{
					has_emiter_reflect2 = false;
				}*/
			}else{
				has_emiter_reflect = false;
			}
        }
    }

    private Vector3f grap(double screenX, double screenY, int width, int height, Vector3f dir){
        if(width <= 0 || height <= 0)
            return dir;

        final Matrix4f projection = mContext.mRenderFrame.projection;
        final Matrix4f view = mContext.mRenderFrame.view;
        float vx = (float) ((+2.0*screenX/width  - 1.0)/ projection.m00);
        float vy = (float) ((-2.0*screenY/height + 1.0)/ projection.m11);
        Matrix4f.invertRigid(view, tempMat);

        if(dir == null) dir = new Vector3f();

        dir.set(vx, vy, -1f);
        Matrix4f.transformNormal(tempMat, dir, dir);

        return dir;
    }

    void draw(){
        // draw the emitter
        render_program.enable();
        /*render_program.applyPointSize(50);
        render_program.applyMVP(camera.getVP());*/
        mContext.updateRenderFrame(0, 50);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, emitter_sprite);
        FloatBuffer buf = GLUtil.getCachedFloatBuffer(4);
        final Vector3f emiter_pos = mContext.mBlockData.emitPosition;
        emiter_pos.store(buf);
        buf.flip();
        GLES20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, buf);
        GLES20.glEnableVertexAttribArray(0);
        mContext.enablePointSprite();
        GLES20.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GLES20.glDisableVertexAttribArray(0);
        mContext.disablePointSprite();

        // draw the reflect.
        /*reflect_program.enable();
        reflect_program.applyMVP(camera.getVP());
        reflect_program.applyRight(up);
        reflect_program.applyUp(right);
        GLES20.glActiveTexture(GL13.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, reflect_sprite);

        buf = GLUtil.getCachedFloatBuffer(4);
        emiter_pos.store(buf);
        buf.put(emiter_diam * 0.3f).flip();
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 16, buf);
        buf.position(3);
        GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 16, buf);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);*/
    }
}
