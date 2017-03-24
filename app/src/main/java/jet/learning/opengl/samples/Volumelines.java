package jet.learning.opengl.samples;

import javax.microedition.khronos.opengles.GL11;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.utils.FieldControl;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.ImmediateRenderer;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.Pixels;
import com.nvidia.developer.opengl.utils.VectorUtil;

public final class Volumelines extends NvSampleApp{

	CVLines lines = new CVLines();
	
	//CPLines plines;
    float m_fTestConstant1 = 1;
    float m_fTestConstant2 = 3.7f;
    float epais = 0.20f;

    int curtex = 1;
    boolean autoRotate = true;
    String[] texturesnames =
    {
      "textures/1d_INNER1.png",
      "textures/1d_INNER2.png",
      "textures/1d_tube.png",
      "textures/1d_SPIRAL.png",
      "textures/1d_SPIRAL_.png",
      "textures/1d_RING2.png",
      "textures/1d_glow1.png",
      "textures/1d_debug.png",
      "textures/1d_debug2.png"
    };
    
    float rotx, roty;
    int[] textureIDs = new int[texturesnames.length];
    
    final Matrix4f projection = new Matrix4f();
    final Matrix4f modelView = new Matrix4f();
    
     @Override
    protected void initRendering() {
    	 Glut.init(this);
    	 
    	 m_transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, -3.5f));
    	 m_transformer.setRotationVec(new Vector3f(PI*0.35f, 0.0f, 0.0f));
         
    	 GLES20.glClearColor(0.25f, .25f, .25f, 1);
//     	GL11.glEnable(GL11.GL_BLEND);
//     	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     	
 		for(int i = 0; i < texturesnames.length; i++){
 			int id = GLES.glGenTextures();
 			GLES20.glBindTexture(GL11.GL_TEXTURE_2D, id);
 			
 			Pixels img = Glut.loadImageFromAssets(texturesnames[i]);
 			img.uploadTexture2D();
 			
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
 			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
 			
 			textureIDs[i] = id;
 		}
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
 		
 		GLES.checkGLError();
 		
     	lines.init();
     	lines.log();
     	lines.setTexture(textureIDs[curtex]);
     	
     	GLES.checkGLError();
     	
     	VectorUtil.perspective(60, (float)getWidth()/(float)getHeight(), 0.1f, 100.0f, projection);
		VectorUtil.lookAt(new Vector3f(0, 0, -4), VectorUtil.ZERO3, VectorUtil.UNIT_Y, modelView);
		
		modelView.scale(new Vector3f(0.5f, 0.5f, 0.5f));
		
		lines.program.enable();
		lines.program.setUniformMatrix4("modelView", modelView);
		GLES.checkGLError();
		lines.program.setUniformMatrix4("projection", projection);
		GLES.checkGLError();
		
		lines.program.disable();
    }
     
     @Override
    public void initUI() {
    	 NvTweakEnumi[] values = {
			new NvTweakEnumi("Inner1", 0),
			new NvTweakEnumi("Inner2", 1),
			new NvTweakEnumi("Tube", 2),
			new NvTweakEnumi("Spiral", 3),
			new NvTweakEnumi("Spiral_", 4),
			new NvTweakEnumi("Ring2", 5),
			new NvTweakEnumi("Glow", 6),
			new NvTweakEnumi("Debug", 7),
			new NvTweakEnumi("Debug2", 8),
		 };
    	 
    	 if(mTweakBar != null){
    		 mTweakBar.addValue("Auto Rotate", new FieldControl(this, "autoRotate", FieldControl.CALL_FIELD), false, 0);
    		 mTweakBar.addEnum("Textures", new FieldControl(this, "curtex", FieldControl.CALL_FIELD), values, 0);
    	 }
    }
     
     @Override
    	protected void draw() {
    	 GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

    	 GLES20.glEnable(GL11.GL_DEPTH_TEST);
    	 GLES20.glDisable(GL11.GL_CULL_FACE);
    	 
    	 m_transformer.setRotationVel(new Vector3f(0.0f,  autoRotate ? (PI * 0.05f) : 0, 0.0f));
    	 lines.setTexture(textureIDs[curtex]);
    	 
//         object.apply_transform();
//    	 GLES20.glColor3f(1.0f,0.0f,0.5f);
         //
         //----> Draw lines
         //
//         GL11.glScalef(0.5f,0.5f,0.5f);
//         GL11.glRotatef(rotx, 1, 0, 0);
//         GL11.glRotatef(roty, 0, 1, 0);

     //else
     {
     //#define lines.draw_line lines.draw_particle

         lines.begin();
         
         m_transformer.getModelViewMat(modelView);
         lines.program.setUniformMatrix4("modelView", modelView);
         {
//         glh::vec3f v1;
//         glh::vec3f v2;
         Vector3f v1 = new Vector3f();
         Vector3f v2 = new Vector3f();
             v1.set(0, -2, 0);
             v2.set(0,1, 2);
             lines.draw_line(v1, v2, epais, epais);
             
             v1.set(-2, 0, 0);
             v2.set(0,1, 2);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(2, 0, 0);
             v2.set(0,1, 2);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(0, 2, 0);
             v2.set(0,1, 2);
             lines.draw_line(v1, v2, epais, epais);

             v1.set(0, -2, 0);
             v2.set(0,-1, 2);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(-2, 0, 0);
             v2.set(0,-1, 2);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(2, 0, 0);
             v2.set(0,-1, 2);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(0, 2, 0);
             v2.set(0,-1, 2);
             lines.draw_line(v1, v2, epais, epais);

             v1.set(-m_fTestConstant1, m_fTestConstant1, -m_fTestConstant1);
             v2.set(m_fTestConstant1,m_fTestConstant1, -m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, m_fTestConstant1, -m_fTestConstant1);
             v2.set(m_fTestConstant1,-m_fTestConstant1, -m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, -m_fTestConstant1, -m_fTestConstant1);
             v2.set(-m_fTestConstant1,-m_fTestConstant1, -m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(-m_fTestConstant1, -m_fTestConstant1, -m_fTestConstant1);
             v2.set(-m_fTestConstant1, m_fTestConstant1, -m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);

             v1.set(-m_fTestConstant1, m_fTestConstant1, m_fTestConstant1);
             v2.set(m_fTestConstant1,m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, m_fTestConstant1, m_fTestConstant1);
             v2.set(m_fTestConstant1,-m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, -m_fTestConstant1, m_fTestConstant1);
             v2.set(-m_fTestConstant1,-m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(-m_fTestConstant1, -m_fTestConstant1, m_fTestConstant1);
             v2.set(-m_fTestConstant1, m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);

             v1.set(-m_fTestConstant1, m_fTestConstant1, -m_fTestConstant1);
             v2.set(-m_fTestConstant1,m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, m_fTestConstant1, -m_fTestConstant1);
             v2.set(m_fTestConstant1, m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(m_fTestConstant1, -m_fTestConstant1, -m_fTestConstant1);
             v2.set(m_fTestConstant1,-m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
             v1.set(-m_fTestConstant1, -m_fTestConstant1, -m_fTestConstant1);
             v2.set(-m_fTestConstant1, -m_fTestConstant1, m_fTestConstant1);
             lines.draw_line(v1, v2, epais, epais);
         }
         lines.end();
         
//         if(b['l']){
//         	b['l'] = false;
//         	
//         	System.out.println("Color: " + lines.gl.hasColor());
//         	System.out.println("Normal: " + lines.gl.hasNormal());
//         	System.out.println("texcoord: " + lines.gl.hasTexCoord());
//         }
     //  glutWireCube(2*m_fTestConstant1);
     }
         //
         //----> Finish the scene
         //
    	}
     
     @Override
    	protected void reshape(int width, int height) {
    	}
     
     private static final class CVLines{
 		private static final float TEXWIDTH = 0.25f;
 		private static final float TEXHEIGHT = 0.25f;
 		
 		NvGLSLProgram program;
 		int hTexture0;
 		
 		int hVarParam0;     ///< to comment
 	    int hVarParam1;     ///< to comment
 	    int hVarParam8;     ///< to comment
 	    int hVarParam9;     ///< to comment
 		
 		boolean binit;
 		boolean simpleLine = true;
 		
 		int texture0;
 		
 		ImmediateRenderer gl;
 		
 		int lineCount = 0;
 		
 		void init(){
 			if(binit)
 				return;
 			
 			program = NvGLSLProgram.createFromFiles("shaders/volumelines.glvs", "shaders/volumelines.glfs");
 			if(program  == null) throw new NullPointerException();
 			hTexture0 = program.getUniformLocation("texCoord0");
 			
 			hVarParam0 = program.getAttribLocation("startpos");
 			hVarParam1 = program.getAttribLocation("endpos");
 			hVarParam8 = program.getAttribLocation("param8");
 			hVarParam9 = program.getAttribLocation("param9");
 			
 			GLES.checkGLError();
 			
 			gl = new ImmediateRenderer(ImmediateRenderer.GLES2, 256);
 			
 			GLES.checkGLError();
 			binit = true;
 		}
 		
 		void log(){
 			System.out.println("hTexture0 = " + hTexture0);
 			
 			System.out.println("hVarParam0 = " + hVarParam0);
 			System.out.println("hVarParam1 = " + hVarParam1);
 			System.out.println("hVarParam8 = " + hVarParam8);
 			System.out.println("hVarParam9 = " + hVarParam9);
 		}
 		
 		void begin(){
// 			GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_ENABLE_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_POLYGON_BIT);

// 		    GL11.glDisable(GL11.GL_LIGHTING);
// 		    GL11.glDisable(GL11.GL_CULL_FACE);

 		    GLES20.glEnable(GL11.GL_BLEND);
 		   GLES20.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
 		    
 		  GLES20.glDepthMask(false);
 		 GLES20.glEnable(GL11.GL_DEPTH_TEST);
 		    
 		    program.enable();
 		    
 		   GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		  GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture0);
 			
 		 GLES20.glUniform1i(hTexture0, 0);

 			int flags = ImmediateRenderer.COLOR|  // for endpos
 					    ImmediateRenderer.NORMAL| // for param9
 					    ImmediateRenderer.TEXTURE| // for param8
 			            ImmediateRenderer.INDICE;  // 
 			
 			gl.positionSize = 3;
 			gl.positionLoc = hVarParam0;
 			
 			gl.colorSize = 3;
 			gl.colorLoc = hVarParam1;
 			
 			gl.normalLoc = hVarParam9;
 			
 			gl.texcoordsSize[0] = 4;
 			gl.texcoordsLoc[0] = hVarParam8;
 			lineCount = 0;
 			
 			gl.begin(GL11.GL_TRIANGLES, flags);
 		}
 		
 		void draw_line(Vector3f inFromPos, Vector3f inToPos, float inFromSize, float inToSize){
 			if(simpleLine)
 				draw_simpleline(inFromPos, inToPos, inFromSize, inToSize);
 			else
 				draw_linetoforget(inFromPos, inToPos, inFromSize, inToSize);
 		}
 		
 		void draw_simpleline(Vector3f inFromPos, Vector3f inToPos, float inFromSize, float inToSize){
 			gl.color(inToPos.x, inToPos.y, inToPos.z);
 			gl.texCoord(inFromSize,inToSize,0,0);
 			gl.normal(-inFromSize,0,inFromSize*0.5f);
 			gl.vertex(inFromPos.x, inFromPos.y, inFromPos.z);
 			
 			gl.color(inFromPos.x, inFromPos.y, inFromPos.z);
 	        gl.texCoord(inToSize,inFromSize,0.25f,0);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal( inToSize,0,inFromSize*0.5f);               // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z);

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z);
 	        gl.texCoord(inToSize,inFromSize,0.25f,0.25f);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal( -inToSize,0,inFromSize*0.5f);                  // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z);

 	        gl.color(inToPos.x, inToPos.y, inToPos.z);
 	        gl.texCoord(inFromSize,inToSize,0,0.25f);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal( inFromSize,0,inFromSize*0.5f);             // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z);
 	        
 	        final short i0 = (short) ((lineCount * 4) + 0);
 	        final short i1 = (short) ((lineCount * 4) + 1);
 	        final short i2 = (short) ((lineCount * 4) + 2);
 	        final short i3 = (short) ((lineCount * 4) + 3);
 	        gl.indice(i0);
 	        gl.indice(i1);
 	        gl.indice(i2);
 	        
 	        gl.indice(i0);
	        gl.indice(i2);
	        gl.indice(i3);
	        
	        lineCount ++;
 		}
 		
 		/**
 	     * Draw a line. Well fitted for long lines.<p>
 	     * used by the display list OR can be used directly after lines_begin()
 	     * uses 8 vertex<p>
 	     * Never use this method, too stupid!!!
 	     **/
 	    void draw_linetoforget(Vector3f inFromPos, Vector3f inToPos, float inFromSize, float inToSize)
 	    {
 	        //
 	        // Start quad
 	        //
 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,TEXWIDTH,0);  // (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inFromSize,-inToSize,0);         // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,0,0);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inFromSize,-inToSize,inFromSize);                // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,0,TEXHEIGHT);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inFromSize,inToSize,inFromSize);                  // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,TEXWIDTH,TEXHEIGHT);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inFromSize,inToSize,0);           // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z); // Attrib #0 MUST be called at the END
 	        //
 	        // Middle quad
 	        //
 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,0,0); // (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inFromSize,-inToSize,inFromSize);            // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,0.25f,0);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inToSize,inFromSize,inToSize);                // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z);

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,0.25f,0.25f);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inToSize,-inFromSize,inToSize);                  // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z);

 	        gl.color(inToPos.x, inToPos.y, inToPos.z, 1);
 	        gl.texCoord(inFromSize,inToSize,0,0.25f);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inFromSize,inToSize,inFromSize);              // SZy
 	        gl.vertex( inFromPos.x, inFromPos.y, inFromPos.z);
 	        //
 	        // End quad
 	        //
 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,TEXWIDTH,0);  // (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inToSize,inFromSize,0);           // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,0,0);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(inToSize,inFromSize,inToSize);                // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,0,TEXHEIGHT);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inToSize,-inFromSize,inToSize);                  // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z); // Attrib #0 MUST be called at the END

 	        gl.color(inFromPos.x, inFromPos.y, inFromPos.z, 1);
 	        gl.texCoord(inToSize,inFromSize,TEXWIDTH,TEXHEIGHT);// (SZx1, SZx2, TexX, TexY)
 	        gl.normal(-inToSize,-inFromSize,0);             // SZy
 	        gl.vertex( inToPos.x, inToPos.y, inToPos.z); // Attrib #0 MUST be called at the END
 	    }
 		
 		void end(){
 			gl.end();

 	        program.disable();
 	        
 	        GLES20.glDisable(GLES20.GL_BLEND);
 	        GLES20.glDepthMask(true);
 		}
 		
 		void setTexture(int tex){
 			texture0 = tex;
 			
 			System.out.println("texture0 = " + texture0);
 		}
 	}
}
