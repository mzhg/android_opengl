package jet.learning.opengl.gtao;

import com.nvidia.developer.opengl.utils.CommonUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;

final class FGTAOShaderParameters {
    static final int SIZE = Matrix4f.SIZE + Vector4f.SIZE * (9 + 4 + 2);

    final Matrix4f ProjInverse = new Matrix4f();
    final Vector4f ProjInfo = new Vector4f();
    final Vector4f BufferSizeAndInvSize = new Vector4f();
    final Vector4f[] GTAOParams = new Vector4f[5];

    final Vector4f  WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness = new Vector4f();
    final Vector4f  FadeRadiusMulAdd_FadeDistance_AttenFactor = new Vector4f();
    final Vector4f   ViewSizeAndInvSize = new Vector4f();

    final Vector2f DepthUnpackConsts = new Vector2f();
    float   InvTanHalfFov;
    float   AmbientOcclusionFadeRadius;

    final Vector3f ProjDia = new Vector3f();
    float   AmbientOcclusionFadeDistance;

    final Vector2f  Power_Intensity_ScreenPixelsToSearch = new Vector2f();
    int ViewRectMinX;
    int ViewRectMinY;


    FGTAOShaderParameters() {
        CommonUtil.initArray(GTAOParams);
    }

    void store(ByteBuffer buf){
        ProjInverse.store(buf);
        ProjInfo.store(buf);
        BufferSizeAndInvSize.store(buf);
        for (int i = 0; i < GTAOParams.length; i++){
            GTAOParams[i].store(buf);
        }

        WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness.store(buf);
        FadeRadiusMulAdd_FadeDistance_AttenFactor.store(buf);
        ViewSizeAndInvSize.store(buf);

        DepthUnpackConsts.store(buf);
        buf.putFloat(InvTanHalfFov).putFloat(AmbientOcclusionFadeRadius);

        ProjDia.store(buf);
        buf.putFloat(AmbientOcclusionFadeDistance);

        Power_Intensity_ScreenPixelsToSearch.store(buf);
        buf.putInt(ViewRectMinX).putInt(ViewRectMinY);

        for(int i = 0; i < 8; i++){
            buf.putFloat(10+i);
        }
    }
}