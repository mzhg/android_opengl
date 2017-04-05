package jet.learning.opengl.common;


import com.nvidia.developer.opengl.utils.StackFloat;
import com.nvidia.developer.opengl.utils.StackInt;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

//***************************************************************************************
//Octree.h by Frank Luna (C) 2011 All Rights Reserved.
//
//Simple octree for doing ray/triangle intersection queries.
//***************************************************************************************
public class Octree {

	OctreeNode mRoot;
	StackFloat mVertices;
	
	public void build(StackFloat vertices, StackInt indices){
		// reference copy for best performance.
		mVertices = vertices;
		
		// Allocate the root node and set its AABB to contain the scene mesh.
		mRoot = new OctreeNode();
		mRoot.bounds = new AxisAlignedBox();
		
		// Build AABB to contain the scene mesh.
		buildAABB(mRoot.bounds);
		
		buildOctree(mRoot, indices);
	}
	
	private void buildAABB(AxisAlignedBox bounds) {
		float max = Float.POSITIVE_INFINITY;
		float min = Float.NEGATIVE_INFINITY;
		
		Vector3f vmin = new Vector3f(max, max, max);
		Vector3f vmax = new Vector3f(min, min, min);
		
		for(int i = 0; i < mVertices.size()/3; ++i){
			final float x = mVertices.get(i * 3 + 0);
			final float y = mVertices.get(i * 3 + 1);
			final float z = mVertices.get(i * 3 + 2);
			
			vmin.x = Math.min(vmin.x, x); 
			vmin.y = Math.min(vmin.y, y); 
			vmin.z = Math.min(vmin.z, z);
			
			vmax.x = Math.max(vmax.x, x); 
			vmax.y = Math.max(vmax.y, y); 
			vmax.z = Math.max(vmax.z, z);
		}
		
		// bounds.extents = (vmax - vmin) * 0.5;
		((Vector3f)Vector3f.sub(vmax, vmin, bounds.extents)).scale(0.5f);
		// bounds.center = (vmax + vmin) * 0.5;
		((Vector3f)Vector3f.add(vmax, vmin, bounds.center)).scale(0.5f);
	}

	private void buildOctree(OctreeNode parent, StackInt indices) {
		int triCount = indices.size()/3;
		
		if(triCount < 60) 
		{
			parent.isLeaf = true;
			parent.indices = indices;  // TODO reference copy, get best performance but very dangerous.
		}
		else{
			parent.isLeaf = false;
			
			AxisAlignedBox[] subbox = new AxisAlignedBox[8];
			for(int i = 0; i < 8; i++) subbox[i] = new AxisAlignedBox();
			parent.subdivide(subbox);
			
			Vector3f v0 = new Vector3f();
			Vector3f v1 = new Vector3f();
			Vector3f v2 = new Vector3f();
			for(int i = 0; i < 8; ++i)
			{
				// Allocate a new subnode.
				parent.children[i] = new OctreeNode();
				parent.children[i].bounds = subbox[i];
				
				// Find triangles that intersect this node's bounding box.
				StackInt intersectedTriangleIndices = new StackInt(8);
				for(int j = 0; j < triCount; ++j)
				{
					int i0 = indices.get(j*3+0);
					int i1 = indices.get(j*3+1);
					int i2 = indices.get(j*3+2);
					
//					XMVECTOR v0 = XMLoadFloat3(&mVertices[i0]);
//					XMVECTOR v1 = XMLoadFloat3(&mVertices[i1]);
//					XMVECTOR v2 = XMLoadFloat3(&mVertices[i2]);
					
					loadFloat3(i0, v0);
					loadFloat3(i1, v1);
					loadFloat3(i2, v2);

					if(Xnacollision.intersectTriangleAxisAlignedBox(v0, v1, v2, subbox[i]))
					{
						intersectedTriangleIndices.push(i0);
						intersectedTriangleIndices.push(i1);
						intersectedTriangleIndices.push(i2);
					}
				}
				
				// Recurse.
				buildOctree(parent.children[i], intersectedTriangleIndices);
			}
		}
	}
	
	public boolean rayOctreeIntersect(Vector3f rayPos, Vector3f rayDir){
		return rayOctreeIntersect(mRoot, rayPos, rayDir);
	}
	
	private boolean rayOctreeIntersect(OctreeNode parent, Vector3f rayPos, Vector3f rayDir){
		// Recurs until we find a leaf node (all the triangles are in the leaves).
		if( !parent.isLeaf )
		{
			for(int i = 0; i < 8; ++i)
			{
				// Recurse down this node if the ray hit the child's box.
				if( Xnacollision.intersectRayAxisAlignedBox(rayPos, rayDir, parent.children[i].bounds) )
				{
					// If we hit a triangle down this branch, we can bail out that we hit a triangle.
					if( rayOctreeIntersect(parent.children[i], rayPos, rayDir) )
						return true;
				}
			}

			// If we get here. then we did not hit any triangles.
			return false;
		}
		else
		{
			int triCount = parent.indices.size() / 3;
			Vector3f v0 = new Vector3f();
			Vector3f v1 = new Vector3f();
			Vector3f v2 = new Vector3f();
			IntersectionPoint point = new IntersectionPoint();
			
			for(int i = 0; i < triCount; ++i)
			{
//				int i0 = parent.Indices[i*3+0];
//				int i1 = parent.Indices[i*3+1];
//				int i2 = parent.Indices[i*3+2];
//
//				XMVECTOR v0 = XMLoadFloat3(&mVertices[i0]);
//				XMVECTOR v1 = XMLoadFloat3(&mVertices[i1]);
//				XMVECTOR v2 = XMLoadFloat3(&mVertices[i2]);
				
				int i0 = parent.indices.get(i*3+0);
				int i1 = parent.indices.get(i*3+1);
				int i2 = parent.indices.get(i*3+2);
				
				loadFloat3(i0, v0);
				loadFloat3(i1, v1);
				loadFloat3(i2, v2);

				if( intersectRayWithTriangle(rayPos, rayDir, v0, v1, v2, point) == INTERSECTION )
					if(point.getT() > 0)  return true;
			}

			return false;
		}
	}

    private void loadFloat3(int index, Vector3f v){
    	v.x = mVertices.get(3 * index + 0);
    	v.y = mVertices.get(3 * index + 1);
    	v.z = mVertices.get(3 * index + 2);
    }

	private static final class OctreeNode{
		AxisAlignedBox bounds;
		// This will be empty except for leaf nodes.
		StackInt indices;
		final OctreeNode[] children = new OctreeNode[8];
		boolean isLeaf;
		
		//Subdivides the bounding box of this node into eight subboxes (vMin[i], vMax[i]) for i = 0:7.
		void subdivide(AxisAlignedBox[] box){
			Vector3f halfExtent = new Vector3f(
					0.5f*bounds.extents.x,
					0.5f*bounds.extents.y,
					0.5f*bounds.extents.z);
			
			// "Top" four quadrants.
			box[0].center  .set(
				bounds.center.x + halfExtent.x,
				bounds.center.y + halfExtent.y,
				bounds.center.z + halfExtent.z);
			box[0].extents.set(halfExtent);

			box[1].center  .set(
				bounds.center.x - halfExtent.x,
				bounds.center.y + halfExtent.y,
				bounds.center.z + halfExtent.z);
			box[1].extents.set(halfExtent);

			box[2].center  .set(
				bounds.center.x - halfExtent.x,
				bounds.center.y + halfExtent.y,
				bounds.center.z - halfExtent.z);
			box[2].extents.set(halfExtent);

			box[3].center  .set(
				bounds.center.x + halfExtent.x,
				bounds.center.y + halfExtent.y,
				bounds.center.z - halfExtent.z);
			box[3].extents.set(halfExtent);

			// "Bottom" four quadrants.
			box[4].center  .set(
				bounds.center.x + halfExtent.x,
				bounds.center.y - halfExtent.y,
				bounds.center.z + halfExtent.z);
			box[4].extents.set(halfExtent);

			box[5].center  .set(
				bounds.center.x - halfExtent.x,
				bounds.center.y - halfExtent.y,
				bounds.center.z + halfExtent.z);
			box[5].extents.set(halfExtent);

			box[6].center  .set(
				bounds.center.x - halfExtent.x,
				bounds.center.y - halfExtent.y,
				bounds.center.z - halfExtent.z);
			box[6].extents.set(halfExtent);

			box[7].center  .set(
				bounds.center.x + halfExtent.x,
				bounds.center.y - halfExtent.y,
				bounds.center.z - halfExtent.z);
			box[7].extents.set(halfExtent);
		}
	}

	public static final int ERROR           = 0;
	public static final int NO_INTERSECTION = 1;
	public static final int INTERSECTION    = 2;

	/** Allow roundoff error of this amount. Be very careful adjusting
	 this. Too big a value may cause valid triangles to be rejected.
	 Too small a value may trigger an assert in the code to create an
	 orthonormal basis in intersectRayWithTriangle. */
	private static final float epsilon = 1.0e-3f;

	/** Cast a ray starting at rayOrigin with rayDirection into the
	 triangle defined by vertices v0, v1, and v2. If intersection
	 occurred returns INTERSECTION and sets intersectionPoint
	 appropriately, including t parameter (scale factor for
	 rayDirection to reach intersection plane starting from
	 rayOrigin). Returns NO_INTERSECTION if no intersection, or ERROR
	 if triangle was degenerate or line was parallel to plane of
	 triangle. */
	public static int intersectRayWithTriangle(Vector3f rayOrigin,
											   Vector3f rayDirection,
											   Vector3f v0,
											   Vector3f v1,
											   Vector3f v2,
											   IntersectionPoint intersectionPoint) {
		// Returns INTERSECTION if intersection computed, NO_INTERSECTION
		// if no intersection with triangle, ERROR if triangle was
		// degenerate or line did not intersect plane containing triangle.

		// NOTE these rays are TWO-SIDED.

		// Find point on line. P = ray origin, D = ray direction.
		//   P + tD = W
		// Find point on plane. X, Y = orthonormal bases for plane; O = its origin.
		//   O + uX + vY = W
		// Set equal
		//   O + uX + vY = tD + P
		//   uX + vY - tD = P - O = "B"
		//   [X0 Y0 -D0] [u]   [B0]
		//   [X1 Y1 -D1] [v] = [B1]
		//   [X2 Y2 -D2] [t]   [B2]
		// Now we have u, v coordinates for the intersection point (if system
		// wasn't degenerate).
		// Find u, v coordinates for three points of triangle. (DON'T DUPLICATE
		// WORK.) Now easy to do 2D inside/outside test.
		// If point is inside, do some sort of interpolation to compute the
		// 3D coordinates of the intersection point (may be unnecessary --
		// can reuse X, Y bases from above) and texture coordinates of this
		// point (maybe compute "texture coordinate" bases using same algorithm
		// and just use u, v coordinates??).

		Vector3f O = new Vector3f(v0);
		Vector3f p2 = new Vector3f();
//    p2.sub(v1, O);
		Vector3f.sub(v1, O, p2);
		Vector3f p3 = new Vector3f();
//    p3.sub(v2, O);
		Vector3f.sub(v2, O, p3);

		Vector3f X = new Vector3f(p2);
		Vector3f Y = new Vector3f(p3);

		// Normalize X
		if (X.length() < epsilon)
			return ERROR;  // coincident points in triangle
		X.normalise();

		// Use Gramm-Schmitt to orthogonalize X and Y
		Vector3f tmp = new Vector3f(X);
		tmp.scale(/*X.dot(Y)*/ Vector3f.dot(X, Y));
//    Y.sub(tmp);
		Vector3f.sub(Y, tmp, Y);
		if (Y.length() < epsilon) {
			return ERROR;  // coincident points in triangle
		}
		Y.normalise();

		// X and Y are now orthonormal bases for the plane defined by the
		// triangle.

		Vector3f Bv = new Vector3f();
//    Bv.sub(rayOrigin, O);
		Vector3f.sub(rayOrigin, O, Bv);

		Matrix3f A = new Matrix3f();
//    A.setCol(0, X);
		A.m00 = X.x;
		A.m01 = X.y;
		A.m02 = X.z;
//    A.setCol(1, Y);
		A.m10 = Y.x;
		A.m11 = Y.y;
		A.m12 = Y.z;
		Vector3f tmpRayDir = new Vector3f(rayDirection);
		tmpRayDir.scale(-1.0f);
//    A.setCol(2, tmpRayDir);
		A.m20 = tmpRayDir.x;
		A.m21 = tmpRayDir.y;
		A.m22 = tmpRayDir.z;
		if (A.invert() == null) {
			return ERROR;
		}

		Vector3f B = new Vector3f();
//    A.xformVec(Bv, B);
		Matrix3f.transform(A, Bv, B);

		Vector2f W = new Vector2f(B.x,B.y);

		// Compute u,v coords of triangle
		Vector2f[] uv = new Vector2f[3];
		uv[0]      = new Vector2f(0,0);
		uv[1]      = new Vector2f(/*p2.dot(X)*/ Vector3f.dot(p2, X), /*p2.dot(Y)*/ Vector3f.dot(p2, Y));
		uv[2]      = new Vector2f(/*p3.dot(X)*/ Vector3f.dot(p3, X), /*p3.dot(Y)*/ Vector3f.dot(p3, Y));

		if (!(Math.abs(uv[1].y) < epsilon)) {
			throw new RuntimeException("Math.abs(uv[1].y()) >= epsilon");
		}

		// Test. For each of the sides of the triangle, is the intersection
		// point on the same side as the third vertex of the triangle?
		// If so, intersection point is inside triangle.
		for (int i = 0; i < 3; i++) {
			if (approxOnSameSide(uv[i], uv[(i+1)%3],
					uv[(i+2)%3], W) == false) {
				return NO_INTERSECTION;
			}
		}

		// Blend coordinates and texture coordinates according to
		// distances from 3 points
		// To do: find u,v coordinates of intersection point in coordinate
		// system of axes defined by uv[1] and uv[2].
		// Blending coords == a, b. 0 <= a,b <= 1.
		if (!(Math.abs(uv[2].y) > epsilon)) {
			throw new RuntimeException("Math.abs(uv[2].y()) <= epsilon");
		}
		if (!(Math.abs(uv[1].x) > epsilon)) {
			throw new RuntimeException("Math.abs(uv[1].x()) <= epsilon");
		}
		float a, b;
		b = W.y / uv[2].y;
		a = (W.x - b * uv[2].x) / uv[1].x;

		p2.scale(a);
		p3.scale(b);
//    O.add(p2);
		Vector3f.add(O, p2, O);
//    O.add(p3);
		Vector3f.add(O, p3, O);
		intersectionPoint.setIntersectionPoint(O);
		intersectionPoint.setT(B.z);
		return INTERSECTION;
	}

	private static boolean approxOnSameSide(Vector2f linePt1, Vector2f linePt2,
											Vector2f testPt1, Vector2f testPt2) {
		// Evaluate line equation for testPt1 and testPt2

		// ((y2 - y1) / (x2 - x1)) - ((y1 - y) / (x1 - x))
		// y - (mx + b)
		float num0 = linePt2.y - linePt1.y;
		float den0 = linePt2.x - linePt1.x;
		float den1 = linePt1.x - testPt1.x;
		float den2 = linePt1.x - testPt2.x;

		if (Math.abs(den0) < epsilon) {
			// line goes vertically.
			if ((Math.abs(den1) < epsilon) ||
					(Math.abs(den2) < epsilon)) {
				return true;
			}

			if (Math.signum(den1) == Math.signum(den2)) {
				return true;
			}

			return false;
		}

		float m = num0 / den0;
		// (y - y1) - m(x - x1)
		float val1 = testPt1.y - linePt1.y - m * (testPt1.x - linePt1.x);
		float val2 = testPt2.y - linePt1.y - m * (testPt2.x - linePt1.x);
		if ((Math.abs(val1) < epsilon) ||
				(Math.abs(val2) < epsilon)) {
			return true;
		}

		if (Math.signum(val1) == Math.signum(val2)) {
			return true;
		}

		return false;
	}
}
