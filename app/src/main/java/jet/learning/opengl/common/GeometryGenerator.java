/*
 * Copyright by Frank Luna (C) 2011 All Rights Reserved.
 * Copyright 2017 mzhg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.StackShort;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

//***************************************************************************************
//GeometryGenerator.h
//
//Defines a static class for procedurally generating the geometry of 
//common mathematical objects.
//
//All triangles are generated "outward" facing.  If you want "inward" 
//facing triangles (for example, if you want to place the camera inside
//a sphere to simulate a sky), you will need to:
//1. Change the Direct3D cull mode or manually reverse the winding order.
//2. Invert the normal.
//3. Update the texture coordinates and tangent vectors.
//***************************************************************************************
public class GeometryGenerator {

	public void createGrid(float width, float depth, int m, int n, MeshData meshData){
		int vertexCount = m*n;
		int faceCount   = (m-1)*(n-1)*2;

		//
		// Create the vertices.
		//

		float halfWidth = 0.5f*width;
		float halfDepth = 0.5f*depth;

		float dx = width / (n-1);
		float dz = depth / (m-1);

		float du = 1.0f / (n-1);
		float dv = 1.0f / (m-1);
		
		if(meshData.vertices == null)
			meshData.vertices = new ArrayList<>(vertexCount);
		
		for(int i = 0; i < m; ++i)
		{
			float z = halfDepth - i*dz;
			for(int j = 0; j < n; ++j)
			{
				float x = -halfWidth + j*dx;

//				vertices[i*n+j].Position = XMFLOAT3(x, 0.0f, z);
//				vertices[i*n+j].Normal   = XMFLOAT3(0.0f, 1.0f, 0.0f);
//				vertices[i*n+j].TangentU = XMFLOAT3(1.0f, 0.0f, 0.0f);
//
//				// Stretch texture over grid.
//				vertices[i*n+j].TexC.x = j*du;
//				vertices[i*n+j].TexC.y = i*dv;
				
				Vertex vertex = new Vertex();
				vertex.setPosition(x, 0.0f, z);
				vertex.setNormal(0.0f, 1.0f, 0.0f);
				vertex.setTangentU(1.0f, 0.0f, 0.0f);
				vertex.setTexCoord(i * du, j * dv);
				
				meshData.vertices.add(vertex);
			}
		}
		
		//
		// Create the indices.
		//
		if(meshData.indices == null)
			meshData.indices = new StackShort(faceCount * 3);
		
		meshData.indices.resize(faceCount * 3); // 3 indices per face
		short[] indices = meshData.indices.getData();
		
		// Iterate over each quad and compute indices.
		int k = 0;
		for(int i = 0; i < m-1; ++i)
		{
			for(int j = 0; j < n-1; ++j)
			{
				indices[k]   = (short) (i*n+j);
				indices[k+1] = (short) (i*n+j+1);
				indices[k+2] = (short) ((i+1)*n+j);

				indices[k+3] = (short) ((i+1)*n+j);
				indices[k+4] = (short) (i*n+j+1);
				indices[k+5] = (short) ((i+1)*n+j+1);

				k += 6; // next quad
			}
		}
	}
	
	/** Creates a box centered at the origin with the given dimensions. */
	public void createBox(float width, float height, float depth, MeshData meshData){
		float w2 = 0.5f*width;
		float h2 = 0.5f*height;
		float d2 = 0.5f*depth;
		
		if(meshData.vertices == null) meshData.vertices = new ArrayList<>(24);
		List<Vertex> vertices = meshData.vertices;
		
		// Fill in the front face vertex data.
		vertices.add(new Vertex(-w2, -h2, -d2, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(-w2, +h2, -d2, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(+w2, +h2, -d2, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f));
		vertices.add(new Vertex(+w2, -h2, -d2, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f));

		// Fill in the back face vertex data.
		vertices.add(new Vertex(-w2, -h2, +d2, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f));
		vertices.add(new Vertex(+w2, -h2, +d2, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(+w2, +h2, +d2, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(-w2, +h2, +d2, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f));

		// Fill in the top face vertex data.
		vertices.add(new Vertex(-w2, +h2, -d2, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(-w2, +h2, +d2, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(+w2, +h2, +d2, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f));
		vertices.add(new Vertex(+w2, +h2, -d2, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f));

		// Fill in the bottom face vertex data.
		vertices.add(new Vertex(-w2, -h2, -d2, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f));
		vertices.add(new Vertex(+w2, -h2, -d2, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(+w2, -h2, +d2, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(-w2, -h2, +d2, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f));

		// Fill in the left face vertex data.
		vertices.add(new Vertex(-w2, -h2, +d2, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(-w2, +h2, +d2, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(-w2, +h2, -d2, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f));
		vertices.add(new Vertex(-w2, -h2, -d2, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f));

		// Fill in the right face vertex data.
		vertices.add(new Vertex(+w2, -h2, -d2, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f));
		vertices.add(new Vertex(+w2, +h2, -d2, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		vertices.add(new Vertex(+w2, +h2, +d2, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f));
		vertices.add(new Vertex(+w2, -h2, +d2, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f));
		
		if(meshData.indices == null) meshData.indices = new StackShort(36);
		meshData.indices.resize(36);
		short[] i = meshData.indices.getData();
		
		// Fill in the front face index data
		i[0] = 0; i[1] = 1; i[2] = 2;
		i[3] = 0; i[4] = 2; i[5] = 3;

		// Fill in the back face index data
		i[6] = 4; i[7]  = 5; i[8]  = 6;
		i[9] = 4; i[10] = 6; i[11] = 7;

		// Fill in the top face index data
		i[12] = 8; i[13] =  9; i[14] = 10;
		i[15] = 8; i[16] = 10; i[17] = 11;

		// Fill in the bottom face index data
		i[18] = 12; i[19] = 13; i[20] = 14;
		i[21] = 12; i[22] = 14; i[23] = 15;

		// Fill in the left face index data
		i[24] = 16; i[25] = 17; i[26] = 18;
		i[27] = 16; i[28] = 18; i[29] = 19;

		// Fill in the right face index data
		i[30] = 20; i[31] = 21; i[32] = 22;
		i[33] = 20; i[34] = 22; i[35] = 23;
	}
	
	/**
	 * Creates a sphere centered at the origin with the given radius.  The
	 * slices and stacks parameters control the degree of tessellation.
	 * @param radius
	 * @param sliceCount
	 * @param stackCount
	 * @param meshData
	 */
	public void createSphere(float radius, final int sliceCount, final int stackCount, MeshData meshData){
		if(meshData.vertices == null) meshData.vertices = new ArrayList<>(stackCount * (sliceCount + 1) + 2);
		if(meshData.indices == null) meshData.indices = new StackShort(6 * ((stackCount - 1) * sliceCount + 1));
		meshData.vertices.clear();
		meshData.indices.clear();

		//
		// Compute the vertices stating at the top pole and moving down the stacks.
		//

		// Poles: note that there will be texture coordinate distortion as there is
		// not a unique point on the texture map to assign to the pole when mapping
		// a rectangular texture onto a sphere.
		Vertex topVertex = new Vertex(0.0f, +radius, 0.0f, 0.0f, +1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
		Vertex bottomVertex = new Vertex(0.0f, -radius, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		
		meshData.vertices.add( topVertex );

		float phiStep   = (float) (Math.PI/stackCount);
		float thetaStep = (float) (2.0f*Math.PI/sliceCount);

		Vector3f n = new Vector3f(); 
		// Compute vertices for each stack ring (do not count the poles as rings).
		for(int i = 1; i <= stackCount-1; ++i)
		{
			float phi = i*phiStep;

			// Vertices of ring.
			for(int j = 0; j <= sliceCount; ++j)
			{
				float theta = j*thetaStep;

				Vertex v = new Vertex();

				// spherical to cartesian
				v.positionX = (float) (radius * Math.sin(phi) * Math.cos(theta));
				v.positionY = (float) (radius * Math.cos(phi));
				v.positionZ = (float) (radius * Math.sin(phi) * Math.sin(theta));

				// Partial derivative of P with respect to theta
				v.tangentUX = (float) (-radius * Math.sin(phi) * Math.sin(theta));
				v.tangentUY = 0.0f;
				v.tangentUZ = (float) (+radius * Math.sin(phi) * Math.cos(theta));

//				XMVECTOR T = XMLoadFloat3(&v.TangentU);
//				XMStoreFloat3(&v.TangentU, XMVector3Normalize(T));
				n.set(v.tangentUX, v.tangentUY, v.tangentUZ);
				n.normalise();
				v.setTangentU(n.x, n.y, n.z);

//				XMVECTOR p = XMLoadFloat3(&v.Position);
//				XMStoreFloat3(&v.Normal, XMVector3Normalize(p));
				n.set(v.positionX, v.positionY, v.positionZ);
				n.normalise();
				v.setNormal(n.x, n.y, n.z);

				v.texCX = (float) (theta / (2.0 * Math.PI));
				v.texCY = (float) (phi / Math.PI);

				meshData.vertices.add( v );
			}
		}

		meshData.vertices.add( bottomVertex );

		//
		// Compute indices for top stack.  The top stack was written first to the vertex buffer
		// and connects the top pole to the first ring.
		//

		for(int i = 1; i <= sliceCount; ++i)
		{
			meshData.indices.push((short)0);
			meshData.indices.push((short)(i+1));
			meshData.indices.push((short)i);
		}
		
		//
		// Compute indices for inner stacks (not connected to poles).
		//

		// Offset the indices to the index of the first vertex in the first ring.
		// This is just skipping the top pole vertex.
		int baseIndex = 1;
		int ringVertexCount = sliceCount+1;
		for(int i = 0; i < stackCount-2; ++i)
		{
			for(int j = 0; j < sliceCount; ++j)
			{
				meshData.indices.push((short) (baseIndex + i*ringVertexCount + j));
				meshData.indices.push((short) (baseIndex + i*ringVertexCount + j+1));
				meshData.indices.push((short) (baseIndex + (i+1)*ringVertexCount + j));

				meshData.indices.push((short) (baseIndex + (i+1)*ringVertexCount + j));
				meshData.indices.push((short) (baseIndex + i*ringVertexCount + j+1));
				meshData.indices.push((short) (baseIndex + (i+1)*ringVertexCount + j+1));
			}
		}

		//
		// Compute indices for bottom stack.  The bottom stack was written last to the vertex buffer
		// and connects the bottom pole to the bottom ring.
		//

		// South pole vertex was added last.
		int southPoleIndex = meshData.vertices.size()-1;

		// Offset the indices to the index of the first vertex in the last ring.
		baseIndex = southPoleIndex - ringVertexCount;
		
		for(int i = 0; i < sliceCount; ++i)
		{
			meshData.indices.push((short) southPoleIndex);
			meshData.indices.push((short) (baseIndex+i));
			meshData.indices.push((short) (baseIndex+i+1));
		}
	}
	
	private void subdivide(MeshData meshData){
		// Save a copy of the input geometry.
		MeshData inputCopy = meshData;


		List<Vertex> vertices = new ArrayList<>();
		StackShort indices = new StackShort();

		//       v1
		//       *
		//      / \
		//     /   \
		//  m0*-----*m1
		//   / \   / \
		//  /   \ /   \
		// *-----*-----*
		// v0    m2     v2
		int numTris = inputCopy.indices.size()/3;
		for(int i = 0; i < numTris; ++i)
		{
			Vertex v0 = inputCopy.vertices.get( inputCopy.indices.get(i*3+0) );
			Vertex v1 = inputCopy.vertices.get( inputCopy.indices.get(i*3+1) );
			Vertex v2 = inputCopy.vertices.get( inputCopy.indices.get(i*3+2) );

			//
			// Generate the midpoints.
			//

			Vertex m0 = new Vertex();
			Vertex m1 = new Vertex();
			Vertex m2 = new Vertex();

			// For subdivision, we just care about the position component.  We derive the other
			// vertex components in CreateGeosphere.

			m0.setPosition(
				0.5f*(v0.positionX + v1.positionX),
				0.5f*(v0.positionY + v1.positionY),
				0.5f*(v0.positionZ + v1.positionZ));

			m1.setPosition(
				0.5f*(v1.positionX + v2.positionX),
				0.5f*(v1.positionY + v2.positionY),
				0.5f*(v1.positionZ + v2.positionZ));

			m2.setPosition(
				0.5f*(v0.positionX + v2.positionX),
				0.5f*(v0.positionY + v2.positionY),
				0.5f*(v0.positionZ + v2.positionZ));

			//
			// Add new geometry.
			//

			vertices.add(v0); // 0
			vertices.add(v1); // 1
			vertices.add(v2); // 2
			vertices.add(m0); // 3
			vertices.add(m1); // 4
			vertices.add(m2); // 5
	 
			indices.push((short) (i*6+0));
			indices.push((short) (i*6+3));
			indices.push((short) (i*6+5));

			indices.push((short) (i*6+3));
			indices.push((short) (i*6+4));
			indices.push((short) (i*6+5));

			indices.push((short) (i*6+5));
			indices.push((short) (i*6+4));
			indices.push((short) (i*6+2));

			indices.push((short) (i*6+3));
			indices.push((short) (i*6+1));
			indices.push((short) (i*6+4));
		}
		
		meshData.vertices = vertices;
		meshData.indices = indices;
	}
	
	public void createCylinder(float bottomRadius, float topRadius, float height, int sliceCount, int stackCount, MeshData meshData)
	{
		if(meshData.vertices == null) meshData.vertices = new ArrayList<>(16);
		if(meshData.indices == null) meshData.indices = new StackShort(16);
		meshData.vertices.clear();
		meshData.indices.clear();

		//
		// Build Stacks.
		// 

		float stackHeight = height / stackCount;

		// Amount to increment radius as we move up each stack level from bottom to top.
		float radiusStep = (topRadius - bottomRadius) / stackCount;

		int ringCount = stackCount+1;

		Vector3f bitangent = new Vector3f();
		Vector3f normal = new Vector3f();
		// Compute vertices for each stack ring starting at the bottom and moving up.
		for(int i = 0; i < ringCount; ++i)
		{
			float y = -0.5f*height + i*stackHeight;
			float r = bottomRadius + i*radiusStep;

			// vertices of ring
			double dTheta = 2.0*Math.PI/sliceCount;
			for(int j = 0; j <= sliceCount; ++j)
			{
				Vertex vertex = new Vertex();

				float c = (float) Math.cos(j*dTheta);
				float s = (float) Math.sin(j*dTheta);

				vertex.setPosition(r*c, y, r*s);

				vertex.texCX = (float)j/sliceCount;
				vertex.texCY = 1.0f - (float)i/stackCount;

				// Cylinder can be parameterized as follows, where we introduce v
				// parameter that goes in the same direction as the v tex-coord
				// so that the bitangent goes in the same direction as the v tex-coord.
				//   Let r0 be the bottom radius and let r1 be the top radius.
				//   y(v) = h - hv for v in [0,1].
				//   r(v) = r1 + (r0-r1)v
				//
				//   x(t, v) = r(v)*cos(t)
				//   y(t, v) = h - hv
				//   z(t, v) = r(v)*sin(t)
				// 
				//  dx/dt = -r(v)*sin(t)
				//  dy/dt = 0
				//  dz/dt = +r(v)*cos(t)
				//
				//  dx/dv = (r0-r1)*cos(t)
				//  dy/dv = -h
				//  dz/dv = (r0-r1)*sin(t)

				// This is unit length.
				vertex.setTangentU(-s, 0.0f, c);

				float dr = bottomRadius-topRadius;
//				XMFLOAT3 bitangent(dr*c, -height, dr*s);
//
//				XMVECTOR T = XMLoadFloat3(&vertex.TangentU);
//				XMVECTOR B = XMLoadFloat3(&bitangent);
//				XMVECTOR N = XMVector3Normalize(XMVector3Cross(T, B));
//				XMStoreFloat3(&vertex.Normal, N);
				
				bitangent.set(dr*c, -height, dr*s);
				normal.set(-s, 0.0f, c);
				Vector3f.cross(normal, bitangent, normal);
				normal.normalise();
				vertex.setNormal(normal.x, normal.y, normal.z);

				meshData.vertices.add(vertex);
			}
		}

		// Add one because we duplicate the first and last vertex per ring
		// since the texture coordinates are different.
		int ringVertexCount = sliceCount+1;

		// Compute indices for each stack.
		for(int i = 0; i < stackCount; ++i)
		{
			for(int j = 0; j < sliceCount; ++j)
			{
				meshData.indices.push((short) (i*ringVertexCount + j));
				meshData.indices.push((short) ((i+1)*ringVertexCount + j));
				meshData.indices.push((short) ((i+1)*ringVertexCount + j+1));

				meshData.indices.push((short) (i*ringVertexCount + j));
				meshData.indices.push((short) ((i+1)*ringVertexCount + j+1));
				meshData.indices.push((short) (i*ringVertexCount + j+1));
			}
		}

		buildCylinderTopCap(bottomRadius, topRadius, height, sliceCount, stackCount, meshData);
		buildCylinderBottomCap(bottomRadius, topRadius, height, sliceCount, stackCount, meshData);
	}

	void buildCylinderTopCap(float bottomRadius, float topRadius, float height, int sliceCount, int stackCount, MeshData meshData)
	{
		int baseIndex = meshData.vertices.size();

		float y = 0.5f*height;
		double dTheta = 2.0*Math.PI/sliceCount;

		// Duplicate cap ring vertices because the texture coordinates and normals differ.
		for(int i = 0; i <= sliceCount; ++i)
		{
			float x = (float) (topRadius*Math.cos(i*dTheta));
			float z = (float) (topRadius*Math.sin(i*dTheta));

			// Scale down by the height to try and make top cap texture coord area
			// proportional to base.
			float u = x/height + 0.5f;
			float v = z/height + 0.5f;

			meshData.vertices.add(new Vertex(x, y, z, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, u, v) );
		}

		// Cap center vertex.
		meshData.vertices.add(new Vertex(0.0f, y, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 0.5f) );

		// Index of center vertex.
		int centerIndex = meshData.vertices.size()-1;

		for(int i = 0; i < sliceCount; ++i)
		{
			meshData.indices.push((short) centerIndex);
			meshData.indices.push((short) (baseIndex + i+1));
			meshData.indices.push((short) (baseIndex + i));
		}
	}

	void buildCylinderBottomCap(float bottomRadius, float topRadius, float height, int sliceCount, int stackCount, MeshData meshData)
	{
		// 
		// Build bottom cap.
		//

		int baseIndex = meshData.vertices.size();
		float y = -0.5f*height;

		// vertices of ring
		double dTheta = 2.0* Math.PI/sliceCount;
		for(int i = 0; i <= sliceCount; ++i)
		{
			float x = (float) (bottomRadius*Math.cos(i*dTheta));
			float z = (float) (bottomRadius*Math.sin(i*dTheta));

			// Scale down by the height to try and make top cap texture coord area
			// proportional to base.
			float u = x/height + 0.5f;
			float v = z/height + 0.5f;

			meshData.vertices.add(new Vertex(x, y, z, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, u, v) );
		}

		// Cap center vertex.
		meshData.vertices.add(new Vertex(0.0f, y, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 0.5f) );

		// Cache the index of center vertex.
		int centerIndex = meshData.vertices.size()-1;

		for(int i = 0; i < sliceCount; ++i)
		{
			meshData.indices.push((short) centerIndex);
			meshData.indices.push((short) (baseIndex + i));
			meshData.indices.push((short) (baseIndex + i+1));
		}
	}


	public void createFullscreenQuad(MeshData meshData)
	{
//		meshData.Vertices.resize(4);
//		meshData.Indices.resize(6);
		if(meshData.vertices == null) meshData.vertices = new ArrayList<>(4);
		if(meshData.indices == null) meshData.indices = new StackShort(6);

		// Position coordinates specified in NDC space.
		meshData.vertices.add(new  Vertex(
			-1.0f, -1.0f, 0.0f, 
			0.0f, 0.0f, -1.0f,
			1.0f, 0.0f, 0.0f,
			0.0f, 0.0f));

		meshData.vertices.add(new  Vertex(
			-1.0f, +1.0f, 0.0f, 
			0.0f, 0.0f, -1.0f,
			1.0f, 0.0f, 0.0f,
			0.0f, 1.0f));

		meshData.vertices.add(new  Vertex(
			+1.0f, +1.0f, 0.0f, 
			0.0f, 0.0f, -1.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 1.0f));

		meshData.vertices.add(new Vertex(
			+1.0f, -1.0f, 0.0f, 
			0.0f, 0.0f, -1.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f));

		meshData.indices.push((short) 0);
		meshData.indices.push((short) 1);
		meshData.indices.push((short) 2);

		meshData.indices.push((short) 0);
		meshData.indices.push((short) 2);
		meshData.indices.push((short) 3);
	}
}
