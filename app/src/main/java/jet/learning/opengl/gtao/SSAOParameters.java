package jet.learning.opengl.gtao;

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.Texture2D;

public class SSAOParameters {
    public final Matrix4f Projection = new Matrix4f();
    public Texture2D SceneDepth;
    public Texture2D SceneNormal;
    public Texture2D ResultAO;
    public int SceneWidth;
    public int SceneHeight;

    public float CameraFar;
    public float CameraNear;

    public int GTAOQuality = 2;
    public int DownscaleFactor = 1;
    public int GTAONumAngles = 2;
    public int GTAOFilterWidth = 5;

    public float ThicknessBlend = 0.5f;
    public float GTAOFalloffEnd = 2.0f;
    public float GTAOFalloffStartRatio = 0.5f;
    public float AmbientOcclusionFadeRadius = 60.f;
    public float AmbientOcclusionFadeDistance = 100.f;

    public float AmbientOcclusionIntensity = 1.0f;
    public float AmbientOcclusionPower = 0.25f;
    public float AmbientOcclusionSearchRadius = 0.3f;  // 0.3 meter
}
