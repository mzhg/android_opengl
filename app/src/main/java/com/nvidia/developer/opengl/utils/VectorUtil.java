package com.nvidia.developer.opengl.utils;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

@Deprecated
public class VectorUtil {

	public static final Vector3f UNIT_X = new Vector3f(1, 0, 0);
	public static final Vector3f UNIT_Y = new Vector3f(0, 1, 0);
	public static final Vector3f UNIT_Z = new Vector3f(0, 0, 1);
	public static final Vector3f ZERO3 = new Vector3f(0, 0, 0);

	public static void store(Matrix4f m, float[][] mat) {
		mat[0][0] = m.m00;
		mat[0][1] = m.m01;
		mat[0][2] = m.m02;
		mat[0][3] = m.m03;
		mat[1][0] = m.m10;
		mat[1][1] = m.m11;
		mat[1][2] = m.m12;
		mat[1][3] = m.m13;
		mat[2][0] = m.m20;
		mat[2][1] = m.m21;
		mat[2][2] = m.m22;
		mat[2][3] = m.m23;
		mat[3][0] = m.m30;
		mat[3][1] = m.m31;
		mat[3][2] = m.m32;
		mat[3][3] = m.m33;
	}

	public static void load(Matrix4f m, float[] buffer) {
		m.m00 = buffer[0];
		m.m01 = buffer[1];
		m.m02 = buffer[2];
		m.m03 = buffer[3];
		m.m10 = buffer[4];
		m.m11 = buffer[5];
		m.m12 = buffer[6];
		m.m13 = buffer[7];
		m.m20 = buffer[8];
		m.m21 = buffer[9];
		m.m22 = buffer[10];
		m.m23 = buffer[11];
		m.m30 = buffer[12];
		m.m31 = buffer[13];
		m.m32 = buffer[14];
		m.m33 = buffer[15];
	}

	public static final Vector3f transformVector3(Vector3f point,
			Matrix4f transform, Vector3f out) {
		if (out == null)
			out = new Vector3f();
		float x = transform.m00 * point.x + transform.m10 * point.y
				+ transform.m20 * point.z + transform.m30;
		float y = transform.m01 * point.x + transform.m11 * point.y
				+ transform.m21 * point.z + transform.m31;
		float z = transform.m02 * point.x + transform.m12 * point.y
				+ transform.m22 * point.z + transform.m32;

		out.set(x, y, z);
		return out;
	}

	public static final Vector3f transformNormal(Vector3f point,
			Matrix4f transform, Vector3f out) {
		if (out == null)
			out = new Vector3f();
		float x = transform.m00 * point.x + transform.m10 * point.y
				+ transform.m20 * point.z;
		float y = transform.m01 * point.x + transform.m11 * point.y
				+ transform.m21 * point.z;
		float z = transform.m02 * point.x + transform.m12 * point.y
				+ transform.m22 * point.z;
		out.set(x, y, z);
		return out;
	}

	/** Calculates the signed distance of a point to a plane. */
	public static final float getDistanceToPlane(Vector3f point, Vector4f plane) {
		return point.x * plane.x + point.y * plane.y + point.z * plane.z
				+ plane.w;
	}

	/**
	 * Calculate the plane equation of the plane that the three specified points
	 * lay in. The points are given in clockwise winding order, with normal
	 * pointing out of clockwise face planeEq contains the A,B,C, and D of the
	 * plane equation coefficients
	 */
	public static final Vector4f getPlaneEquation(Vector3f p1, Vector3f p2,
			Vector3f p3, Vector4f out) {
		Vector3f v1 = Vector3f.sub(p3, p1, null);
		Vector3f v2 = Vector3f.sub(p2, p1, null);

		Vector3f v3 = Vector3f.cross(v1, v2, null);
		v3.normalise();

		if (out == null)
			out = new Vector4f();
		out.set(v3.x, v3.y, v3.z);
		out.w = -Vector3f.dot(v3, p3);
		return out;
	}

	public static final Matrix4f lookAt(Vector3f eye, Vector3f center,
			Vector3f pUp, Matrix4f out) {
		if (out == null) {
			out = new Matrix4f();
		}

		Vector3f f = Vector3f.sub(center, eye, null);
		if (f.lengthSquared() > 0)
			f.normalise();
		Vector3f up = new Vector3f(pUp);
		up.normalise();

		Vector3f s = Vector3f.cross(f, up, null);
		if (s.lengthSquared() > 0)
			s.normalise();
		Vector3f u = Vector3f.cross(s, f, null);

		out.setIdentity();

		out.m00 = s.x;
		out.m10 = s.y;
		out.m20 = s.z;

		out.m01 = u.x;
		out.m11 = u.y;
		out.m21 = u.z;

		out.m02 = -f.x;
		out.m12 = -f.y;
		out.m22 = -f.z;

		Matrix4f translate = new Matrix4f();
		translate.translate(new Vector3f(-eye.x, -eye.y, -eye.z));

		Matrix4f.mul(out, translate, out);
		return out;
	}

	public static final Matrix4f perspective(float fov, float aspect,
			float zNear, float zFar, Matrix4f out) {
		float r = (float) Math.toRadians(fov / 2);
		float deltaZ = zFar - zNear;
		float s = (float) Math.sin(r);
		float cotangent = 0;

		if (deltaZ == 0 || s == 0 || aspect == 0) {
			return out;
		}

		if (out == null)
			out = new Matrix4f();

		// cos(r) / sin(r) = cot(r)
		cotangent = (float) Math.cos(r) / s;

		out.setIdentity();
		out.m00 = cotangent / aspect;
		out.m11 = cotangent;
		out.m22 = -(zFar + zNear) / deltaZ;
		out.m23 = -1;
		out.m32 = -2 * zNear * zFar / deltaZ;
		out.m33 = 0;

		return out;
	}

	public static final Matrix4f perspective_inverse(float fov, float aspect,
			float zNear, float zFar, Matrix4f out) {
		double tangent = Math.tan(Math.toRadians(fov / 2.0));
		float y = (float) tangent * zNear;
		float x = aspect * y;

		return frustum_inverse(-x, x, -y, y, zNear, zFar,out);
	}

	public static final Matrix4f frustum_inverse(float left, float right,
			float bottom, float top, float zNear, float zFar, Matrix4f out) {
		if (out == null)
			out = new Matrix4f();
		else
			out.setIdentity();
		
		out.m00 = (right - left) / (2 * zNear);
		out.m30 = (right + left) / (2 * zNear);
		
		out.m11 = (top - bottom) / (2 * zNear);
		out.m31 = (top + bottom) / (2 * zNear);

		out.m22 = 0;
		out.m32 = -1;
		
		out.m23 = -(zFar - zNear) / (2 * zFar * zNear);
		out.m33 = (zFar + zNear) / (2 * zFar * zNear);
		
		return out;
	}

	public static final Matrix4f ortho(float left, float right, float bottom,
			float top, float near, float far, Matrix4f out) {
		float tx = -((right + left) / (right - left));
		float ty = -((top + bottom) / (top - bottom));
		float tz = -((far + near) / (far - near));

		if (out == null)
			out = new Matrix4f();
		else
			out.setIdentity();

		out.m00 = 2 / (right - left);
		out.m11 = 2 / (top - bottom);
		out.m22 = -2 / (far - near);
		out.m30 = tx;
		out.m31 = ty;
		out.m32 = tz;

		return out;
	}

	public static final Matrix4f lookAt(float eyex, float eyey, float eyez,
			float centerx, float centery, float centerz, float upx, float upy,
			float upz, Matrix4f out) {
		return lookAt(new Vector3f(eyex, eyey, eyez), new Vector3f(centerx,
				centery, centerz), new Vector3f(upx, upy, upz), out);
	}

	public static final Matrix3f getRotation(Matrix4f src, Matrix3f out) {
		if (out == null)
			out = new Matrix3f();

		out.m00 = src.m00;
		out.m01 = src.m01;
		out.m02 = src.m02;
		out.m10 = src.m10;
		out.m11 = src.m11;
		out.m12 = src.m12;
		out.m20 = src.m20;
		out.m21 = src.m21;
		out.m22 = src.m22;

		return out;
	}

	public static final Matrix3f mat3(float v) {
		Matrix3f m = new Matrix3f();
		m.m00 = m.m11 = m.m22 = v;
		return m;
	}

	public static final Matrix3f mat3(Vector3f first, Vector3f second,
			Vector3f thrid) {
		Matrix3f m = new Matrix3f();
		m.m00 = first.x;
		m.m01 = first.y;
		m.m02 = first.z;

		m.m10 = second.x;
		m.m11 = second.y;
		m.m12 = second.z;

		m.m20 = thrid.x;
		m.m21 = thrid.y;
		m.m22 = thrid.z;
		return m;
	}

	public static final Matrix4f viewport(float width, float height,
			Matrix4f out) {
		if (out == null)
			out = new Matrix4f();
		else
			out.setIdentity();

		float w2 = width * 0.5f;
		float h2 = height * 0.5f;

		out.m00 = w2;
		out.m11 = h2;
		out.m30 = w2;
		out.m31 = h2;
		out.m33 = 1.0f;

		return out;
	}
	
	public static final Vector3f transformCoord(Matrix4f m, Vector3f v, Vector3f out){
		if(out == null)
			out = new Vector3f();
		
		float w = v.x * m.m03 + v.y * m.m13 + v.z * m.m23 + m.m33;
		transformVector3(v, m, out);
		
		if(w != 0) {
			w = 1.0f/w;
			out.scale(w);
		}
		
		return out;
	}
	
	public static void transformBoundingBox(Vector3f[] bbox1, Matrix4f matrix, Vector3f[] bbox2){
		for(int i = 0; i < 2; i ++){
			if(bbox2[i] == null)
			   bbox2[i] = new Vector3f();
		}
		
		bbox2[0].x = bbox2[0].y = bbox2[0].z =  Float.MAX_VALUE;
	    bbox2[1].x = bbox2[1].y = bbox2[1].z = -Float.MAX_VALUE;
	    // Transform the vertices of BBox1 and extend BBox2 accordingly
	    Vector3f v = new Vector3f();
	    for (int i = 0; i < 8; ++i)
	    {
	        v.set(
	            bbox1[(i & 1) != 0 ? 0 : 1].x,
	            bbox1[(i & 2) != 0 ? 0 : 1].y,
	            bbox1[(i & 4) != 0 ? 0 : 1].z);

	        Vector3f v1 = transformCoord(matrix, v, v);
	        
	        bbox2[0].x = Math.min(bbox2[0].x, v1.x);
	        bbox2[1].x = Math.max(bbox2[1].x, v1.x);
	        bbox2[0].y = Math.min(bbox2[0].y, v1.y);
	        bbox2[1].y = Math.max(bbox2[1].y, v1.y);
	        bbox2[0].z = Math.min(bbox2[0].z, v1.z);
            bbox2[1].z = Math.max(bbox2[1].z, v1.z);
            
	    }
	}
	
	public static final Matrix4f perspectiveFrustum(float w, float h, float n,float f, Matrix4f out){
		float ymax = h / 2.0f;
	    float ymin = -ymax;

	    float aspect = w / h;
	    float xmin = ymin * aspect;
	    float xmax = ymax * aspect;
	    
	    return frustum(xmin, xmax, ymin, ymax, n, f, out);
	}

	public static final Matrix4f frustum(float left, float right, float bottom,
			float top, float znear, float zfar, Matrix4f out) {
		float n2 = 2 * znear;
		float w = right - left;
		float h = top - bottom;
		float d = zfar - znear;

		// out.set(n2 / w, 0, (right + left) / w, 0,
		// 0, n2 / h, (top + bottom) / h, 0,
		// 0, 0, -(zfar + znear) / d, -(n2 * zfar) / d,
		// 0, 0, -1, 0);

		if (out == null)
			out = new Matrix4f();

		out.m00 = n2 / w;
		out.m01 = 0;
		out.m02 = 0;
		out.m03 = 0;
		out.m10 = 0;
		out.m11 = n2 / h;
		out.m12 = 0;
		out.m13 = 0;
		out.m20 = (right + left) / w;
		out.m21 = (top + bottom) / h;
		out.m22 = -(zfar + znear) / d;
		out.m23 = -1;
		out.m30 = 0;
		out.m31 = 0;
		out.m32 = -(n2 * zfar) / d;
		out.m33 = 0;

		return out;
	}

	/** Build a rotate 2d matrix. The angle in radians. */
	public static final Matrix2f rotate(float angle, Matrix2f out) {
		if (out == null)
			out = new Matrix2f();

		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		out.m00 = cos;
		out.m01 = sin;
		out.m10 = -sin;
		out.m11 = cos;

		return out;
	}

	public static final boolean closeEnough(Vector3f v, Vector3f u, float e) {
		return Math.abs(v.x - u.x) < e && Math.abs(v.y - u.y) < e
				&& Math.abs(v.z - u.z) < e;
	}

	public static final boolean closeEnough(Vector2f v, Vector2f u, float e) {
		return Math.abs(v.x - u.x) < e && Math.abs(v.y - u.y) < e;
	}

	public static Matrix4f makeRotate(Vector3f x, Vector3f y, Vector3f z,
			Matrix4f out) {
		if (out == null)
			out = new Matrix4f();
		else
			out.setIdentity();

		out.m00 = x.x;
		out.m01 = x.y;
		out.m02 = x.z;

		out.m10 = y.x;
		out.m11 = y.y;
		out.m12 = y.z;

		out.m20 = z.x;
		out.m21 = z.y;
		out.m22 = z.z;

		return out;
	}

	public static Matrix4f setTranslate(Vector3f trans, Matrix4f out) {
		out.m30 = trans.x;
		out.m31 = trans.y;
		out.m32 = trans.z;
		return out;
	}

	public static Matrix4f invertRigid(Matrix4f src, Matrix4f out) {
		float m00 = src.m00;
		float m01 = src.m10;
		float m02 = src.m20;
		float m10 = src.m01;
		float m11 = src.m11;
		float m12 = src.m21;
		float m20 = src.m02;
		float m21 = src.m12;
		float m22 = src.m22;

		Vector3f right = new Vector3f(-src.m30, -src.m31, -src.m32);
		float x = m00 * right.x + m10 * right.y + m20 * right.z;
		float y = m01 * right.x + m11 * right.y + m21 * right.z;
		float z = m02 * right.x + m12 * right.y + m22 * right.z;

		if (out == null)
			out = new Matrix4f();

		out.m00 = m00;
		out.m01 = m01;
		out.m02 = m02;
		out.m10 = m10;
		out.m11 = m11;
		out.m12 = m12;
		out.m20 = m20;
		out.m21 = m21;
		out.m22 = m22;
		out.m30 = x;
		out.m31 = y;
		out.m32 = z;

		return out;
	}

	public static Matrix4f toMatrix(Quaternion q, Matrix4f mat) {
		if (mat == null)
			mat = new Matrix4f();

		final float q0 = q.x;
		final float q1 = q.y;
		final float q2 = q.z;
		final float q3 = q.w;

		float q00 = q0 * q0;
		float q11 = q1 * q1;
		float q22 = q2 * q2;
		float q33 = q3 * q3;
		// Diagonal elements
		mat.m00 = q00 + q11 - q22 - q33;
		mat.m11 = q00 - q11 + q22 - q33;
		mat.m22 = q00 - q11 - q22 + q33;
		// 0,1 and 1,0 elements
		float q03 = q0 * q3;
		float q12 = q1 * q2;
		mat.m10 = 2.0f * (q12 - q03);
		mat.m01 = 2.0f * (q03 + q12);
		// 0,2 and 2,0 elements
		float q02 = q0 * q2;
		float q13 = q1 * q3;
		mat.m20 = 2.0f * (q02 + q13);
		mat.m02 = 2.0f * (q13 - q02);
		// 1,2 and 2,1 elements
		float q01 = q0 * q1;
		float q23 = q2 * q3;
		mat.m21 = 2.0f * (q23 - q01);
		mat.m12 = 2.0f * (q01 + q23);
		return mat;
	}

	public static Matrix3f toMatrix(Quaternion q, Matrix3f mat) {
		if (mat == null)
			mat = new Matrix3f();

		final float q0 = q.x;
		final float q1 = q.y;
		final float q2 = q.z;
		final float q3 = q.w;

		float q00 = q0 * q0;
		float q11 = q1 * q1;
		float q22 = q2 * q2;
		float q33 = q3 * q3;
		// Diagonal elements
		mat.m00 = q00 + q11 - q22 - q33;
		mat.m11 = q00 - q11 + q22 - q33;
		mat.m22 = q00 - q11 - q22 + q33;
		// 0,1 and 1,0 elements
		float q03 = q0 * q3;
		float q12 = q1 * q2;
		mat.m10 = 2.0f * (q12 - q03);
		mat.m01 = 2.0f * (q03 + q12);
		// 0,2 and 2,0 elements
		float q02 = q0 * q2;
		float q13 = q1 * q3;
		mat.m20 = 2.0f * (q02 + q13);
		mat.m02 = 2.0f * (q13 - q02);
		// 1,2 and 2,1 elements
		float q01 = q0 * q1;
		float q23 = q2 * q3;
		mat.m21 = 2.0f * (q23 - q01);
		mat.m12 = 2.0f * (q01 + q23);
		return mat;
	}
	
	/**
	 * r = a + b * f
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector2f will create.
	 */
	public static Vector2f linear(Vector2f a, Vector2f b, float f, Vector2f r){
		if(r == null)
			r = new Vector2f();
		
		r.x  = a.x + b.x * f;
		r.y  = a.y + b.y * f;
		
		return r;
	}
	
	/**
	 * r = a + b * f
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector3f linear(Vector3f a, Vector3f b, float f, Vector3f r){
		if(r == null)
			r = new Vector3f();
		
		r.x  = a.x + b.x * f;
		r.y  = a.y + b.y * f;
		r.z  = a.z + b.z * f;
		
		return r;
	}
	
	/**
	 * r = a + b * f
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector4f linear(Vector4f a, Vector4f b, float f, Vector4f r){
		if(r == null)
			r = new Vector4f();
		
		r.x  = a.x + b.x * f;
		r.y  = a.y + b.y * f;
		r.z  = a.z + b.z * f;
		r.w  = a.w + b.w * f;
		
		return r;
	}
	
	/**
	 * r = a * f + b
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector2f will create.
	 */
	public static Vector2f linear(Vector2f a, float f, Vector2f b, Vector2f r){
		if(r == null)
			r = new Vector2f();
		
		r.x  = b.x + a.x * f;
		r.y  = b.y + a.y * f;
		
		return r;
	}
	
	/**
	 * r = a + b * f
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector3f linear(Vector3f a, float f, Vector3f b, Vector3f r){
		if(r == null)
			r = new Vector3f();
		
		r.x  = b.x + a.x * f;
		r.y  = b.y + a.y * f;
		r.z  = b.z + a.z * f;
		
		return r;
	}
	
	/**
	 * r = a + b * f
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector4f linear(Vector4f a, float f, Vector4f b, Vector4f r){
		if(r == null)
			r = new Vector4f();
		
		r.x  = b.x + a.x * f;
		r.y  = b.y + a.y * f;
		r.z  = b.z + a.z * f;
		r.w  = b.w + a.w * f;
		
		return r;
	}
	
	/**
	 * r = a * f + b * g
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector2f will create.
	 */
	public static Vector2f linear(Vector2f a, float f, Vector2f b, float g, Vector2f r){
		if(r == null)
			r = new Vector2f();
		
		r.x  = b.x * g + a.x * f;
		r.y  = b.y * g + a.y * f;
		
		return r;
	}
	
	/**
	 * r = a * f + b * g
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector3f linear(Vector3f a, float f, Vector3f b, float g, Vector3f r){
		if(r == null)
			r = new Vector3f();
		
		r.x  = b.x * g + a.x * f;
		r.y  = b.y * g + a.y * f;
		r.z  = b.z * g + a.z * f;
		
		return r;
	}
	
	/**
	 * r = a * f + b * g
	 * @param a
	 * @param b
	 * @param f
	 * @param r
	 * @return r if r is null, a new Vector3f will create.
	 */
	public static Vector4f linear(Vector4f a, float f, Vector4f b, float g, Vector4f r){
		if(r == null)
			r = new Vector4f();
		
		r.x  = b.x * g + a.x * f;
		r.y  = b.y * g + a.y * f;
		r.z  = b.z * g + a.z * f;
		r.w  = b.w * g + a.w * f;
		
		return r;
	}
	
	/**
	 * Rotation matrix creation. From euler angles:<ol>
	 * <li> Yaw around Y axis in radians
	 * <li> Pitch around X axis in radians
	 * <li> Roll around Z axis in radians
	 * </ol>
	 * return the rotation matrix [R] = [Roll].[Pitch].[Yaw]
	 * @param yaw
	 * @param pitch
	 * @param roll
	 * @param result hold the dest matrix4f.
	 * @return
	 */
	public static Matrix4f rotationYawPitchRoll(float yaw, float pitch, float roll, Matrix4f result){
		if(result == null)
			result = new Matrix4f();
		else
			result.setIdentity();
		
		if(roll != 0){
			result.rotate(roll, UNIT_Z);
		}
		
		if(pitch != 0)
			result.rotate(pitch, UNIT_X);
		
		if(yaw != 0)
			result.rotate(yaw, UNIT_Y);
		
		return result;
	}
}
