package jet.learning.opengl.gtao;

import com.nvidia.developer.opengl.utils.CommonUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

final class FGTAOShaderParameters {
    final Matrix4f ProjInverse = new Matrix4f();
    final Vector4f ProjInfo = new Vector4f();
    final Vector4f BufferSizeAndInvSize = new Vector4f();
    final Vector4f[] GTAOParams = new Vector4f[5];

    final Vector2f DepthUnpackConsts = new Vector2f();
    float   InvTanHalfFov;
    float   AmbientOcclusionFadeRadius;

    float   AmbientOcclusionFadeDistance;
    final Vector3f ProjDia = new Vector3f();
    float   RadiusToScreen;        // radius
//    UINT   ScreenLocation[2];

    FGTAOShaderParameters() {
        CommonUtil.initArray(GTAOParams);
    }
}
