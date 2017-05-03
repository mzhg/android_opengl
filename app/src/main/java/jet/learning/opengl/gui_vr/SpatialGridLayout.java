package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.ReadableVector3f;

/**
 * Created by mazhen'gui on 2017/4/19.
 */

final class SpatialGridLayout extends SpatialLayout{

    private int m_XStep = 1;
    private int m_YStep = 1;
    private int m_ZStep = 1;

    void setRows(int rows) {m_YStep = rows; assert(m_YStep > 0);}
    void setCols(int clos) {m_XStep = clos; assert(m_XStep > 0);}
    void setDepths(int depths) {m_ZStep = depths; assert(m_ZStep>0);}

    public SpatialGridLayout(ReadableVector3f min, ReadableVector3f max) {
        super(min, max);
    }

    @Override
    protected void getChildTransform(int x, int y, int z, Transform transform) {
        float lenX = m_MaxX - m_MinX;
        float lenY = m_MaxY - m_MinY;
        float lenZ = m_MaxZ - m_MinZ;

        transform.position.x = m_MinX + x * lenX + 0.5f * lenX;
        transform.position.y = m_MinY + y * lenY + 0.5f * lenY;
        transform.position.z = m_MinZ + z * lenZ + 0.5f * lenZ;

        transform.scale.x = lenX * 0.94f;
        transform.scale.y = lenY * 0.94f;
        transform.scale.z = lenZ * 0.94f;

        if(transform.scale.x == 0.0f){
            transform.scale.x = 1.0f;
        }

        if(transform.scale.y == 0.0f){
            transform.scale.y = 1.0f;
        }

        if(transform.scale.z == 0.0f){
            transform.scale.z = 1.0f;
        }
    }
}
