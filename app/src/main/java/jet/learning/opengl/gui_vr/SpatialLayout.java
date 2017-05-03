package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.ReadableVector3f;

/**
 * Created by mazhen'gui on 2017/4/19.
 */

abstract class SpatialLayout {
    protected float m_MinX;
    protected float m_MinY;
    protected float m_MinZ;

    protected float m_MaxX;
    protected float m_MaxY;
    protected float m_MaxZ;

    private int m_ChildrenCount;

    public SpatialLayout(ReadableVector3f min, ReadableVector3f max){
        m_MinX = min.getX();
        m_MinY = min.getY();
        m_MinZ = min.getZ();

        m_MaxX = max.getX();
        m_MaxY = max.getY();
        m_MaxZ = max.getZ();
    }

    public void setChildrenCount(int num){
        m_ChildrenCount = num;
    }

    public int getChildrenCount() { return m_ChildrenCount;}
//    const glm::vec3& GetMin() const { return m_Min;}
//    const glm::vec3& GetMax() const { return m_Max;}

    protected abstract void getChildTransform(int x, int y, int z, Transform transform);
}
