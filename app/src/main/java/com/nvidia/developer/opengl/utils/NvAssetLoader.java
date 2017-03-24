//----------------------------------------------------------------------------------
// File:        NvAssetLoader/NvAssetLoader.h
// SDK Version: v1.2 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package com.nvidia.developer.opengl.utils;

import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Cross-platform binary asset loader.  Requires files
 * to be located in a subdirectory of the application's source
 * tree named "assets" in order to be able to find them.  This
 * is enforced so that assets will be automatically packed into
 * the application's APK on Android (ANT defaults to packing the
 * tree under "assets" into the binary assets of the APK).<p>
 * On platforms that use file trees for storage (Windows and
 * Linux), the search method for finding each file passed in as the
 * partial path <code>filepath</code> is as follows:<ul>
 * <li>Start at the application's current working directory
 * <li>Do up to 10 times:<ul>
 * <li>For each search path <search> in the search list:<ul>
 * <li>Try to open <code>currentdir</code>/<code>search</code>/<code>filepath</code>
 * <li>If it is found, return it
 * <li>Otherwise, move to next path in <code>search</code> and iterate
 * </ul>
 * <li>Change directory up one level and iterate
 * </ul>
 * </ul>
 * On Android, the file opened is always <code>filepath</code>, since the "assets"
 * directory is known (it is the APK's assets).
 * @author Nvidia 2014-9-4
 *
 */
public final class NvAssetLoader {

	private static AssetManager s_assetManager;
	
	/**
	 * Initializes the loader at application start.<p>
	 * In most cases, the platform-specific application framework or main loop should make this call.  It
	 * requires a different argument on each platform.
	 * @param platform a platform-specific context pointer used by
     * the implementation<br>
     * - On Android, this should be the Activity(app's AssetManager) instance<br>
     * - On Windows and Linux, this is currently ignored and should be null
	 * @return
	 */
	public static boolean init(Object platform){
		if(platform == null)
			return false;
		
		s_assetManager = (AssetManager)platform;
		return true;
	}
	
	/**
	 * Shuts down the system
	 * @return true on success and false on failure
	 */
	public static boolean shutdown(){
		s_assetManager = null;
		return true;
	}
	
	/**
	 * Adds a search path for finding the root of the assets tree.<p>
	 * Adds a search path to be prepended to "assets" when searching
	 * for the correct assets tree.  Note that this must be a relative
	 * path, and it is not used directly to find the file.  It is only
	 * used on path-based platforms (Linux and Windows) to find the
	 * "assets" directory.
	 * @param path The relative path to add to the set of paths used to
	 * find the "assets" tree.  See the package description for the file search methods
	 * @return true on success and false on failure
	 */
	public static boolean addSearchPath(String path){
		return true;
	}
	
	/**
	 * Removes a search path from the lists. 
	 * @param path the path to remove
	 * @return true on success and false on failure (not finding the path
     * on the list is considered success)
	 */
	public static boolean removeSearchPath(String path){
		return true;
	}
	
	public static Reader openReaderStream(String filePath){
		return new InputStreamReader(openInputStream(filePath));
	}
	
	/**
	 * Reads an asset file as a String.
	 * @param filePath the partial path (below "assets") to the file
	 * @return
	 */
	public static StringBuilder readText(String filePath){
		StringBuilder buffer = null;
		try {
			BufferedReader reader = new BufferedReader(openReaderStream(filePath));
			String line;
			buffer = new StringBuilder();
			while((line = reader.readLine()) != null){
				buffer.append(line).append('\n');
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffer;
	}
	
	public static InputStream openInputStream(String filePath){
		try {
			return s_assetManager.open(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Reads an asset file as a binary bytes.
	 * @param filePath the partial path (below "assets") to the file
	 * @return
	 */
	public static byte[] read(String filePath){
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(bout);
		try {
			
			BufferedInputStream in = new BufferedInputStream(s_assetManager.open(filePath));
			byte[] bytes = new byte[1024];
			int len;
			while((len = in.read(bytes)) > 0){
				out.write(bytes, 0, len);
			}
			
			in.close();
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bout.toByteArray();
	}
}
