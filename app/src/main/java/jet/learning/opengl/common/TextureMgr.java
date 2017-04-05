package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvImage;

import java.nio.IntBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL11;

/**
 * Simple texture manager to avoid loading duplicate textures from file.  That can
 * happen, for example, if multiple meshes reference the same texture filename. 
 */
public class TextureMgr {

	final HashMap<String, Integer> mTextureSRV = new HashMap<String, Integer>();
	
	public void dispose(){
		if(!mTextureSRV.isEmpty()){
			IntBuffer buf = GLUtil.getCachedIntBuffer(mTextureSRV.size());
			for(Integer i : mTextureSRV.values()){
				buf.put(i);
			}
			buf.flip();
			GLES20.glDeleteTextures(mTextureSRV.size(), buf);
			
			mTextureSRV.clear();
		}
		
	}
	
	public int createTexture(String filename){
		Integer tex = mTextureSRV.get(filename);
		if(tex != null)
			return tex;
		else{
			try {
				int textureID = NvImage.uploadTextureFromDDSFile(filename);
				GLES20.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				mTextureSRV.put(filename, textureID);
				return textureID;
			} catch (Exception e) {
				System.err.println("Load texture " + filename + " failed!");
				return 0;
			}
		}
	}
}
