package jet.learning.opengl.fight404;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.utils.Glut;

/**
 * Created by mazhen'gui on 2017/12/20.
 */

final class Textures {
    static final int PARTICLE = 0;
    static final int EMITTER = 1;
    static final int CORONA = 2;
    static final int REFLECTION = 3;

    private final int[] textures = new int[4];

    public Textures() {
        GLES20.glGenTextures(4, textures, 0);

        String[] fileNames = { "corona.png", "emitter.png", "particle.png", "reflection.png" };

        for (int i = 0; i < 4; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            Bitmap image = Glut.loadBitmapFromAssets("textures/"+fileNames[i]);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES30.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
    }

    public void bind(int texture) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[texture]);
    }

    public void unbind() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
