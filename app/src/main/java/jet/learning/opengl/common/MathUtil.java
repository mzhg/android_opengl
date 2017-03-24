package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class MathUtil {

    public static void perspective(float fov, float aspect, float zNear, float zFar, Matrix4f out) {
        float r = NvUtils.PI / 360.0f * fov;
        //		float r = (float) Math.toRadians(fov / 2);
        float deltaZ = zFar - zNear;
        float s = (float) Math.sin(r);
        float cotangent = 0;

        // cos(r) / sin(r) = cot(r)
        cotangent = (float) Math.cos(r) / s;
        out.m00 = cotangent / aspect;
        out.m11 = cotangent;
        out.m22 = -(zFar + zNear) / deltaZ;
        out.m23 = -1;
        out.m32 = -2 * zNear * zFar / deltaZ;
        out.m33 = 0;
    }

    private MathUtil(){}
}
