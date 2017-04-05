package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Vector3f;

public class Xnacollision {

	
	public static boolean intersectTriangleAxisAlignedBox(Vector3f v0, Vector3f v1, Vector3f v2, AxisAlignedBox pVolume){
		float min0, max0;
		float[] min_max = new float[2];
		Vector3f d = new Vector3f();
		Vector3f edge0 = new Vector3f();
		Vector3f edge1 = new Vector3f();
		
		// Test the direction of triangle normal.
		Vector3f.sub(v1, v0, edge0);
		Vector3f.sub(v2, v0, edge1);
		Vector3f.cross(edge0, edge1, d);
		
		min0 = Vector3f.dot(d, v0);
		max0 = min0;
		
		getProjection(d, pVolume, min_max);
		float min1 = min_max[0];
		float max1 = min_max[1];
		
		if (max1 < min0 || max0 < min1)
	    {
	        return false;
	    }
		
		// Test direction of box faces.
//	    for (int i = 0; i < 3; ++i)
	    {// x-axis test
//	        D = bounds.Axis[i];
	    	d.set(1, 0, 0);
//	        IntrAxis<Real>::GetProjection(D, *mTriangle, min0, max0);
	    	getProjection(d, v0,  v1, v2, min_max);
	    	min0 = min_max[0];
	    	max0 = min_max[1];
	    	
	        float DdC = Vector3f.dot(d, pVolume.center); //D.Dot(bounds.Center);
	        min1 = DdC - pVolume.extents.x;
	        max1 = DdC + pVolume.extents.x;
	        if (max1 < min0 || max0 < min1)
	        {
	            return false;
	        }
	    }
	    
	    {// y-axis test
//	        D = bounds.Axis[i];
	    	d.set(0, 1, 0);
//	        IntrAxis<Real>::GetProjection(D, *mTriangle, min0, max0);
	    	getProjection(d, v0,  v1, v2, min_max);
	    	min0 = min_max[0];
	    	max0 = min_max[1];
	    	
	        float DdC = Vector3f.dot(d, pVolume.center); //D.Dot(bounds.Center);
	        min1 = DdC - pVolume.extents.y;
	        max1 = DdC + pVolume.extents.y;
	        if (max1 < min0 || max0 < min1)
	        {
	            return false;
	        }
	    }
	    
	    {// z-axis test
//	        D = bounds.Axis[i];
	    	d.set(0, 0, 1);
//	        IntrAxis<Real>::GetProjection(D, *mTriangle, min0, max0);
	    	getProjection(d, v0,  v1, v2, min_max);
	    	min0 = min_max[0];
	    	max0 = min_max[1];
	    	
	        float DdC = Vector3f.dot(d, pVolume.center); //D.Dot(bounds.Center);
	        min1 = DdC - pVolume.extents.z;
	        max1 = DdC + pVolume.extents.z;
	        if (max1 < min0 || max0 < min1)
	        {
	            return false;
	        }
	    }
	    
	    // Test direction of triangle-box edge cross products.
	    Vector3f edge2 = Vector3f.sub(edge1, edge0, null);
	    Vector3f[] edge = {edge0, edge1, edge2};
	    Vector3f[] axis = {VectorUtil.UNIT_X, VectorUtil.UNIT_Y, VectorUtil.UNIT_Z};
	    for (int i0 = 0; i0 < 3; ++i0)
	    {
	        for (int i1 = 0; i1 < 3; ++i1)
	        {
//	            D = edge[i0].Cross(bounds.Axis[i1]);
	        	Vector3f.cross(edge[i0], axis[i1], d);
//	            IntrAxis<Real>::GetProjection(D, *mTriangle, min0, max0);
//	            IntrAxis<Real>::GetProjection(D, *mBox, min1, max1);
	        	
	        	getProjection(d, v0, v1, v2, min_max);
	        	min0 = min_max[0];
		    	max0 = min_max[1];
		    	
		    	getProjection(d, pVolume, min_max);
		    	min1 = min_max[0];
				max1 = min_max[1];
	            if (max1 < min0 || max0 < min1)
	            {
	                return false;
	            }
	        }
	    }
	    
	    return true;
	}
	
	private static void getProjection(Vector3f axis, AxisAlignedBox box, float[] dest){
		float origin = Vector3f.dot(axis, box.center);
		float maximumExtent = 
				Math.abs(box.extents.x * axis.x) + 
				Math.abs(box.extents.y * axis.y) + 
				Math.abs(box.extents.z * axis.z);
		
		dest[0] = origin - maximumExtent;
		dest[1] = origin + maximumExtent;
	}
	
	private static void getProjection(Vector3f axis, Vector3f v0, Vector3f v1, Vector3f v2, float[] dest){
		float imin, imax;
		
		float dot0 = Vector3f.dot(axis, v0);
		float dot1 = Vector3f.dot(axis, v1);
		float dot2 = Vector3f.dot(axis, v2);
		
		imin = dot0;
	    imax = imin;

	    if (dot1 < imin)
	    {
	        imin = dot1;
	    }
	    else if (dot1 > imax)
	    {
	        imax = dot1;
	    }

	    if (dot2 < imin)
	    {
	        imin = dot2;
	    }
	    else if (dot2 > imax)
	    {
	        imax = dot2;
	    }
	    
	    dest[0] = imin;
	    dest[1] = imax;
	}

	public static boolean intersectRayAxisAlignedBox(Vector3f rayPos, Vector3f rayDir, AxisAlignedBox bounds) {
		float WdU0, WdU1, WdU2;
		float AWdU0, AWdU1, AWdU2;
		float DdU0, DdU1, DdU2;
		float ADdU0, ADdU1, ADdU2;
		float AWxDdU0, AWxDdU1, AWxDdU2;
		float RHS;
		
		Vector3f diff = Vector3f.sub(rayPos, bounds.center, null);
		
		WdU0 = Vector3f.dot(rayDir, VectorUtil.UNIT_X); //mRay->Direction.Dot(bounds.Axis[0]);
	    AWdU0 = Math.abs(WdU0);
	    DdU0 = Vector3f.dot(diff, VectorUtil.UNIT_X); //diff.Dot(bounds.Axis[0]);
	    ADdU0 = Math.abs(DdU0);
	    if (ADdU0 > bounds.extents.x && DdU0*WdU0 >= 0)
	    {
	        return false;
	    }

	    WdU1 = Vector3f.dot(rayDir, VectorUtil.UNIT_Y); //mRay->Direction.Dot(bounds.Axis[1]);
	    AWdU1 = Math.abs(WdU1);
	    DdU1 = Vector3f.dot(diff, VectorUtil.UNIT_Y); //diff.Dot(bounds.Axis[1]);
	    ADdU1 = Math.abs(DdU1);
	    if (ADdU1 > bounds.extents.y && DdU1*WdU1 >= 0)
	    {
	        return false;
	    }

	    WdU2 = Vector3f.dot(rayDir, VectorUtil.UNIT_Z); //mRay->Direction.Dot(bounds.Axis[2]);
	    AWdU2 = Math.abs(WdU2);
	    DdU2 = Vector3f.dot(diff, VectorUtil.UNIT_Z); //diff.Dot(bounds.Axis[2]);
	    ADdU2 = Math.abs(DdU2);
	    if (ADdU2 > bounds.extents.z && DdU2*WdU2 >= 0)
	    {
	        return false;
	    }

//	    Vector3<Real> WxD = mRay->Direction.Cross(diff);
	    Vector3f WxD = Vector3f.cross(rayDir, diff, diff);

	    AWxDdU0 = Math.abs(WxD.x /*WxD.Dot(bounds.Axis[0])*/);
	    RHS = bounds.extents.y*AWdU2 + bounds.extents.z*AWdU1;
	    if (AWxDdU0 > RHS)
	    {
	        return false;
	    }

	    AWxDdU1 = Math.abs(WxD.y/*WxD.Dot(bounds.Axis[1])*/);
	    RHS = bounds.extents.x*AWdU2 + bounds.extents.z*AWdU0;
	    if (AWxDdU1 > RHS)
	    {
	        return false;
	    }

	    AWxDdU2 = Math.abs(WxD.z/*WxD.Dot(bounds.Axis[2])*/);
	    RHS = bounds.extents.x*AWdU1 + bounds.extents.y*AWdU0;
	    if (AWxDdU2 > RHS)
	    {
	        return false;
	    }

	    return true;
		    
	}
}
