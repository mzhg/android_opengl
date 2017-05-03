package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

public class RaycastSelector {
    private final List<BoundingBox> m_BoundingBoxList = new ArrayList<>();
    private final float[] m_DistanceRayToBoundingBox = new float[1];
    private OnTouchEventListener touchEventListener;
    private boolean active = true;
    private final Ray mRay = new Ray();
    private Vector3f m_LastDirection = null;
    private BoundingBox m_PreviousHintedBox;
    private float m_WatchingTime;
    // DeltaAngle = cos(angle), for fast comparistion.
    float m_DeltaAngle;
    float m_WatchingThreshold;

    public void update(Matrix4f viewMat, float dt){
        if(!active){
            return;
        }

        if(m_BoundingBoxList.isEmpty()){
            return;
        }

//        m_DistanceRayToBoundingBox.resize(m_BoundingBoxList.size());

        Matrix4f.decompseRigidMatrix(viewMat, mRay.m_position, null,null, mRay.m_direction);
        mRay.m_direction.scale(-1);

        BoundingBox closedBox = null;
        float minDistance = Float.MAX_VALUE;
        for(BoundingBox box: m_BoundingBoxList){
            box.testWithRay(mRay, m_DistanceRayToBoundingBox, 0);

            if(m_DistanceRayToBoundingBox[0] > 0.0f && m_DistanceRayToBoundingBox[0] < minDistance){
                minDistance = m_DistanceRayToBoundingBox[0];
                closedBox = box;
            }
        }

        if(closedBox != null){
            if(m_PreviousHintedBox == null){
                m_PreviousHintedBox = closedBox;
                m_WatchingTime = 0.0f;
                if(touchEventListener != null){
                    touchEventListener.onEnter(m_PreviousHintedBox);
                }
            }else if(m_PreviousHintedBox != closedBox){
                if(touchEventListener != null){
                    touchEventListener.onLeval(m_PreviousHintedBox);
                }

                m_PreviousHintedBox = closedBox;
                m_WatchingTime = 0.0f;
                if(touchEventListener != null){
                    touchEventListener.onEnter(m_PreviousHintedBox);
                }
            }else{
                if (testDeltaAngle(mRay.m_direction)){
                    m_WatchingTime += dt;
                    onWatching(m_PreviousHintedBox);
                }else{  //
                    m_WatchingTime = 0.0f;
                }
            }
        }else if(m_PreviousHintedBox != null){
            if(touchEventListener != null){
                touchEventListener.onLeval(m_PreviousHintedBox);
            }
            m_PreviousHintedBox = null;
        }
    }

    // return true if the changed angle < m_DeltaAngle
    boolean testDeltaAngle(ReadableVector3f newDir){
        if(m_LastDirection == null){
            m_LastDirection = new Vector3f(newDir);
            return false;
        }

        float angleBetwwen = Vector3f.dot(m_LastDirection, newDir);
        boolean result = angleBetwwen > m_DeltaAngle;
        m_LastDirection.set(newDir);
        return result;
    }

    void onWatching(BoundingBox hintedBox){
        if (touchEventListener != null && m_WatchingTime > m_WatchingThreshold){
            touchEventListener.onWatching(hintedBox, m_WatchingTime);
        }
    }

    public void add(BoundingBox box){
        m_BoundingBoxList.add(box);
    }

    public boolean remove(BoundingBox box){
        return m_BoundingBoxList.remove(box);
    }

    public OnTouchEventListener getTouchEventListener() {
        return touchEventListener;
    }

    public void setTouchEventListener(OnTouchEventListener touchEventListener) {
        this.touchEventListener = touchEventListener;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
