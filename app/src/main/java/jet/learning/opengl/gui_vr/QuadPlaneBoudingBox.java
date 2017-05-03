package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.util.vector.Vector3f.sub;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

public class QuadPlaneBoudingBox extends BoundingBox{
    // Triangle Fan mode
    final Vector3f[] m_QuadPos = new Vector3f[4];

    public QuadPlaneBoudingBox(){
        for(int i = 0; i < 4; i++)
            m_QuadPos[i] = new Vector3f();
    }

    @Override
    public boolean testWithRay(Ray ray, float[] t, int offset) {
        boolean result = intersect_triangle(ray.m_position, ray.m_direction, m_QuadPos[0], m_QuadPos[1], m_QuadPos[2], t, offset);
        if(result){
            return true;
        }
        return intersect_triangle(ray.m_position, ray.m_direction, m_QuadPos[0], m_QuadPos[2], m_QuadPos[3], t, offset);
    }

    private static final Vector3f edge1 = new Vector3f();
    private static final Vector3f edge2 = new Vector3f();
    private static final Vector3f pvec = new Vector3f();
    private static final Vector3f tvec = new Vector3f();

    // Given a ray origin (orig) and direction (dir), and three vertices of a triangle, this
    // function returns TRUE and the interpolated texture coordinates if the ray intersects
    // the triangle
    private static boolean intersect_triangle(ReadableVector3f orig, ReadableVector3f dir,
                                              ReadableVector3f v0, ReadableVector3f v1, ReadableVector3f v2,
                                              float[] lenSequard, int offset){
        float t,u,v;

        // Find vectors for two edges sharing vert0
        sub(v1, v0, edge1);
        sub(v2, v0, edge2);

        // Begin calculating determinant - also used to calculate U parameter
        Vector3f.cross(dir, edge2, pvec);

        // If determinant is near zero, ray lies in plane of triangle
        float det = Vector3f.dot(edge1, pvec);

        if (det > 0)
        {
            Vector3f.sub(orig, v0, tvec);
        }
        else
        {
//            tvec = v0 - orig;
            Vector3f.sub(v0, orig, tvec);
            det = -det;
        }

        if (det < 0.0001f)
            return false;

        // Calculate U parameter and test bounds
        u = Vector3f.dot(tvec, pvec);
        if (u < 0.0f || u > det)
        return false;

        // Prepare to test V parameter
        Vector3f qvec = Vector3f.cross(tvec, edge1, edge1);
//	D3DXVec3Cross(&qvec, &tvec, &edge1);

        // Calculate V parameter and test bounds
        v = Vector3f.dot(dir, qvec);
        if (v < 0.0f || u + v > det)
        return false;

        if(lenSequard == null)
            return true;

        // Calculate t, scale parameters, ray intersects triangle
        t = Vector3f.dot(edge2, qvec);
        float fInvDet = 1.0f / det;
        t *= fInvDet;
        u *= fInvDet;
        v *= fInvDet;

        Vector3f intersect_point = edge1;
        intersect_point.x = v0.getX() * t + v1.getX() * u + v2.getX() * v;
        intersect_point.y = v0.getY() * t + v1.getY() * u + v2.getY() * v;
        intersect_point.z = v0.getZ() * t + v1.getZ() * u + v2.getZ() * v;

        Vector3f dirToPoint = Vector3f.sub(intersect_point, orig, edge2);
        lenSequard[offset] = dirToPoint.lengthSquared();
        return true;
    }
}
