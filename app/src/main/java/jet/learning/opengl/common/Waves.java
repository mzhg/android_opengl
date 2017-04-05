package jet.learning.opengl.common;

import org.lwjgl.util.vector.Vector3f;

public class Waves {

	int mNumRows;
	int mNumCols;

	int mVertexCount;
	int mTriangleCount;

	// Simulation constants we can precompute.
	float mK1;
	float mK2;
	float mK3;

	float mTimeStep;
	float mSpatialStep;
	
	float mTime;

	Vector3f[] mPrevSolution;
	Vector3f[] mCurrSolution;
	Vector3f[] mNormals;
	Vector3f[] mTangentX;
	
	public int rowCount(){ return mNumRows; }
	public int columnCount(){ return mNumCols; }
	public int vertexCount(){ return mVertexCount; }
	public int triangleCount(){ return mTriangleCount;}
	public float width() { return mNumCols*mSpatialStep;}
	public float depth() { return mNumRows*mSpatialStep;}
	
	/** Returns the solution at the ith grid point. */
	public Vector3f get(int i) {return mCurrSolution[i];}
	/** Returns the solution normal at the ith grid point. */
	public Vector3f normal(int i) {return mNormals[i]; }
	/** Returns the unit tangent vector at the ith grid point in the local x-axis direction. */
	public Vector3f tangentX(int i) {return mTangentX[i]; }

	public void init(int m, int n, float dx, float dt, float speed, float damping)
	{
		mNumRows  = m;
		mNumCols  = n;

		mVertexCount   = m*n;
		mTriangleCount = (m-1)*(n-1)*2;

		mTimeStep    = dt;
		mSpatialStep = dx;

		float d = damping*dt+2.0f;
		float e = (speed*speed)*(dt*dt)/(dx*dx);
		mK1     = (damping*dt-2.0f)/ d;
		mK2     = (4.0f-8.0f*e) / d;
		mK3     = (2.0f*e) / d;

		// In case Init() called again.
//		delete[] mPrevSolution;
//		delete[] mCurrSolution;

		mPrevSolution = new Vector3f[m*n];
		mCurrSolution = new Vector3f[m*n];
		mNormals      = new Vector3f[m*n];
		mTangentX     = new Vector3f[m*n];

		// Generate grid vertices in system memory.

		float halfWidth = (n-1)*dx*0.5f;
		float halfDepth = (m-1)*dx*0.5f;
		for(int i = 0; i < m; ++i)
		{
			float z = halfDepth - i*dx;
			for(int j = 0; j < n; ++j)
			{
				float x = -halfWidth + j*dx;

				mPrevSolution[i*n+j] = new Vector3f(x, 0.0f, z);
				mCurrSolution[i*n+j] = new Vector3f(x, 0.0f, z);
				mNormals[i*n+j]      = new Vector3f(0.0f, 1.0f, 0.0f);
				mTangentX[i*n+j]     = new Vector3f(1.0f, 0.0f, 0.0f);
			}
		}
	}

	public void update(float dt)
	{
		// Accumulate time.
		mTime += dt;

		// Only update the simulation at the specified time step.
		if( mTime >= mTimeStep )
		{
			// Only update interior points; we use zero boundary conditions.
			for(int i = 1; i < mNumRows-1; ++i)
			{
				for(int j = 1; j < mNumCols-1; ++j)
				{
					// After this update we will be discarding the old previous
					// buffer, so overwrite that buffer with the new update.
					// Note how we can do this inplace (read/write to same element) 
					// because we won't need prev_ij again and the assignment happens last.

					// Note j indexes x and i indexes z: h(x_j, z_i, t_k)
					// Moreover, our +z axis goes "down"; this is just to 
					// keep consistent with our row indices going down.

					mPrevSolution[i*mNumCols+j].y = 
						mK1*mPrevSolution[i*mNumCols+j].y +
						mK2*mCurrSolution[i*mNumCols+j].y +
						mK3*(mCurrSolution[(i+1)*mNumCols+j].y + 
						     mCurrSolution[(i-1)*mNumCols+j].y + 
						     mCurrSolution[i*mNumCols+j+1].y + 
							 mCurrSolution[i*mNumCols+j-1].y);
				}
			}

			// We just overwrote the previous buffer with the new data, so
			// this data needs to become the current solution and the old
			// current solution becomes the new previous solution.
//			std::swap(mPrevSolution, mCurrSolution);
			Vector3f[] tmp = mPrevSolution;
			mPrevSolution = mCurrSolution;
			mCurrSolution = tmp;
			
			mTime = 0.0f; // reset time
			
			//
			// Compute normals using finite difference scheme.
			//
			for(int i = 1; i < mNumRows-1; ++i)
			{
				for(int j = 1; j < mNumCols-1; ++j)
				{
					float l = mCurrSolution[i*mNumCols+j-1].y;
					float r = mCurrSolution[i*mNumCols+j+1].y;
					float t = mCurrSolution[(i-1)*mNumCols+j].y;
					float b = mCurrSolution[(i+1)*mNumCols+j].y;
					mNormals[i*mNumCols+j].x = -r+l;
					mNormals[i*mNumCols+j].y = 2.0f*mSpatialStep;
					mNormals[i*mNumCols+j].z = b-t;

//					XMVECTOR n = XMVector3Normalize(XMLoadFloat3(&mNormals[i*mNumCols+j]));
//					XMStoreFloat3(&mNormals[i*mNumCols+j], n);
					mNormals[i*mNumCols+j].normalise();

//					mTangentX[i*mNumCols+j] = XMFLOAT3(2.0f*mSpatialStep, r-l, 0.0f);
//					XMVECTOR T = XMVector3Normalize(XMLoadFloat3(&mTangentX[i*mNumCols+j]));
//					XMStoreFloat3(&mTangentX[i*mNumCols+j], T);
					mTangentX[i*mNumCols+j].set(2.0f*mSpatialStep, r-l, 0.0f);
					mTangentX[i*mNumCols+j].normalise();
				}
			}
		}
	}

	public void disturb(int i, int j, float magnitude)
	{
		// Don't disturb boundaries.
		assert(i > 1 && i < mNumRows-2);
		assert(j > 1 && j < mNumCols-2);

		float halfMag = 0.5f*magnitude;

		// Disturb the ijth vertex height and its neighbors.
		mCurrSolution[i*mNumCols+j].y     += magnitude;
		mCurrSolution[i*mNumCols+j+1].y   += halfMag;
		mCurrSolution[i*mNumCols+j-1].y   += halfMag;
		mCurrSolution[(i+1)*mNumCols+j].y += halfMag;
		mCurrSolution[(i-1)*mNumCols+j].y += halfMag;
	}
}
