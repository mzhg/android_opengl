////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright 2017 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package com.nvidia.developer.opengl.ui;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Vector2f;

import javax.microedition.khronos.opengles.GL11;

//structure encapsulating a given loaded font
//including texture and all data to map into it.
/**
 * An OpenGL bitmap-font text rendering library<p>
 * Library for rendering text strings under Open GL and GL ES, using bitmap fonts alongside
 * xml files with glyph data annotations.  Fonts can be made using fairly-standard 'angelcode'
 * output-format bitmap font rasterizers.<p>
 * 
 * BitFont is able to:<ul>
 * <li> Load multiple bitmap fonts simultaneously, from compressed/DDS files, with or without mipmaps </li>
 * <li> Handle extended ASCII character codes </li>
 * <li> Manage multi-style fonts, thus able to combine normal and bold 'runs' of text styling in a single string. </li>
 * <li> Render text aligned to screen edges or any sub-rect 'box' on the screen </li>
 * <li> Support multi-line output, including auto-wrapping </li>
 * <li> Apply a specific font, size, base color, and multiplied color to each string </li>
 * <li> Optionally apply drop-shadowing under the text for more 'pop' </li>
 * <li> Optionally embed special escape codes for on-the-fly color or style changes </li>
 * <li> Given screen size (and rotation), can automatically re-scale and rotate text output </li>
 * <li> Allow overriding the default shader to implement custom raster effects </li>
 * <li> Allow overriding the default matrix calc to apply custom transformations to text </li>
 * </ul>
 * @author Nvidia 2014-9-8 : 22: 19
 *
 */
class NvBitFont {
	
	private static boolean s_bfInitialized = false;

	// these track the bitmap font loading.
	static NvBitFont bitFontLL = null;
	private static byte bitFontID = 1; // NOT ZERO, start at 1 so zero is 'invalid'...

	// for ES2, always vbos, but we need to track shader program...
	static NvGLSLProgram fontProg = null;
	private static boolean fontProgAllocInternal = true;
	static int fontProgLocMat;
	private static int fontProgLocTex;
	static int fontProgAttribPos, fontProgAttribCol, fontProgAttribTex;

	//========================================================================
	private static float dispW = 0, dispH = 0;
	private static float dispAspect = 0;
	static float dispRotation = 0;
	
	static int lastColorArray = 0;
	static int lastFontTexture = 0;
	static byte lastTextMode = 111;

	//========================================================================
	//static float lastTextHigh = 0;

	// the master index VBO for rendering ANY text, since they're all sequences of rects.
	static int maxIndexChars = 0;
	static short[] masterTextIndexList = null;
	static int masterTextIndexVBO = 0;

	static float s_pixelToClipMatrix[][] = new float[4][4];
	static float s_pixelScaleFactorX = 2.0f / 640.0f;
	static float s_pixelScaleFactorY = 2.0f / 480.0f;
	
	static float[] m_matrixOverride = null;
	
	private static final String s_fontVertShader =
			"#version 100\n"+
			"// this is set from higher level.  think of it as the upper model matrix\n"+
			"uniform mat4 pixelToClipMat;\n"+
			"attribute vec2 pos_attr;\n"+
			"attribute vec2 tex_attr;\n"+
			"attribute vec4 col_attr;\n"+
			"varying vec4 col_var;\n"+
			"varying vec2 tex_var;\n"+
			"void main() {\n"+
			"    // account for translation and rotation of the primitive into [-1,1] spatial default.\n"+
			"    gl_Position = pixelToClipMat * vec4(pos_attr.x, pos_attr.y, 0, 1);\n"+
			"    col_var = col_attr;"+
			"    tex_var = tex_attr;\n"+
			"}\n";
	
	private static final String s_fontFragShader = 
			"#version 100\n"+
			"precision mediump float;\n"+
			"uniform sampler2D fontTex;\n"+
			"varying vec4 col_var;\n"+
			"varying vec2 tex_var;\n"+
			"void main() {\n"+
			"    float alpha = texture2D(fontTex, tex_var).a;\n"+
			"    gl_FragColor = col_var * vec4(1.0, 1.0, 1.0, alpha);\n"+
			"}\n";
	
	public byte m_id; // no need for more than 255, right??
	public boolean m_alpha;
	public boolean m_rgb;
	
	public String m_filename;
	
	public int m_tex;
	
	public AFont m_afont;
	public AFont m_afontBold;  // if we support bold.
	
	public float m_canonPtSize = 10;
	
	public NvBitFont m_next;
	
	public NvBitFont() {
		m_next = bitFontLL;
		bitFontLL = this;
	}
	
	static NvBitFont bitFontFromID(byte fontnum){
		NvBitFont bitfont = bitFontLL;
		while(bitfont != null){
			if(bitfont.m_id == fontnum)
				return bitfont;
			bitfont = bitfont.m_next;
		}
		
		return null;
	}
	
	/**
	 * Source C language function: <br><code>int32_t NvBFInitialize(uint8_t count, const char* filename[][2])</code>;<p>
	 * Base initialization of the font system, once per application.<p>
	 * Initialize the BitFont system with one or more on-disk fonts.
	 * @param count total fonts to load
	 * @param filename array of two char* .fnt font descriptor files.  In case bold style is supported, second is the 
	 * bold .fnt variant -- note that the bold.fnt file MUST refer to the same texture/bitmap files as the normal/base 
	 * did (we only support when bold is embedded in same texture).
	 * @return zero if initialized fine, one if failed anywhere during init process.
	 */
	public static int initialize(int count, String[][] filename){
		int j;
		String texFilename;
		byte[] data = null;
		int len = 0;
	    NvImage image = null;

	    NvBitFont bitfont = null;
	    AFont afont = null;

	    int fontsLoaded = 0;
	    
	    if (fontProg == null)
	    { // then not one set already, load one...
	    // this loads from a file
	        fontProg = NvGLSLProgram.createFromStrings(s_fontVertShader, s_fontFragShader, false);
	        //fontProg = nv_load_program_from_strings(s_fontVertShader, s_fontFragShader);  
	        if (null==fontProg ) //|| 0==fontProg.getProgram())
	        {
	            NvLogger.e("!!> NvBFInitialize failure: couldn't load shader program...");
	            return(1);
	        }

	        fontProgAllocInternal = true;
	        fontProgramPrecache();

	        // The following entries are const
	        // so we set them up now and never change
	        s_pixelToClipMatrix[2][0] = 0.0f;
	        s_pixelToClipMatrix[2][1] = 0.0f;

	        // Bitfont obliterates Z right now
	        s_pixelToClipMatrix[0][2] = 0.0f;
	        s_pixelToClipMatrix[1][2] = 0.0f;
	        s_pixelToClipMatrix[2][2] = 0.0f;
	        s_pixelToClipMatrix[3][2] = 0.0f;

	        s_pixelToClipMatrix[0][3] = 0.0f;
	        s_pixelToClipMatrix[1][3] = 0.0f;
	        s_pixelToClipMatrix[2][3] = 0.0f;
	        s_pixelToClipMatrix[3][3] = 1.0f;
	    }

	    // since this is our 'system initialization' function, allocate the master index VBO here.
	    if (masterTextIndexVBO==0)
	    {
	    	masterTextIndexVBO = GLES.glGenBuffers();
	    }

	    for (j=0; j<count; j++)
	    {

	        if (getFontID(filename[j][0]) != 0)
	        {
	            fontsLoaded++;
	            continue; // already have this one.
	        }
	            
	        image = null;
	        bitfont = null;

	        afont = loadFontInfo(filename[j][0]);
	        if (null==afont)
	        {
	            NvLogger.e(">> FAILED TO PARSE afont file: %s...\n", filename[j][0]);
	            continue;
	        }
	        NvLogger.i("!> NvBF loaded afont: [%s]", afont.m_fontInfo.m_name);

	        // build the filename.
//	        memcpy(texFilename, afont.m_charCommon.m_filename, strlen(afont.m_charCommon.m_filename)+1); // account for null!
	        texFilename = afont.m_charCommon.m_filename;
	        
	        if (!texFilename.endsWith("dds"))
	        {
	            NvLogger.e("Font [%s] wasn't a .DDS file, the only supported format.\n", texFilename);
//	            if (afont)
//	                delete afont;
	            continue;            
	        }

	        NvImage.upperLeftOrigin( false );
	        if ((data = NvUIAssetData.embeddedAssetLookup(texFilename))!= null)
	        {
	            if (data.length != 0)
	            {
	                image = new NvImage();
	                if (!image.loadImageFromFileData(data, 0, len, "dds"))
	                {
//	                    delete image;
	                    image = null;
	                }
	            }
	        }
	        if (image==null)
	            image = NvImage.createFromDDSFile(texFilename);
	        NvImage.upperLeftOrigin( true );
	        if (image==null)
	        {
	            NvLogger.e("Font [%s] couldn't be loaded by the NVHHDDS library.\n", texFilename);
//	            if (afont)
//	                delete afont;
	            continue;
	        }
	        // !!!!!!TBD TODO
	        //texw = dds.width;

	        if (afont != null)
	        {
	            // create now and copy
	            bitfont = new NvBitFont();
	            bitfont.m_afont = afont;
	            bitfont.m_canonPtSize = afont.m_charCommon.m_lineHeight; // !!!!!TBD TODO what to use to get approx ACTUAL font pixel size?

	            // try to load second font style if passed in...
	            if (!NvUtils.isEmpty(filename[j][1]))
	            {
	                afont = loadFontInfo(filename[j][1]);
	                if (null==afont)
	                {
	                    NvLogger.e(">> FAILED TO PARSE secondary style afont file: %s...\n", filename[j][1]);
	                    continue;
	                }
	                NvLogger.i("!> NvBF loaded second style afont: [%s]", afont.m_fontInfo.m_name);
	                bitfont.m_afontBold = afont;
	            }
	        }

	        bitfont.m_id = bitFontID++; // store our 'index' away
	        // we've already checked earlier that filenames fit in our MAX LEN.
//	        memcpy(bitfont.m_filename, filename[j][0], strlen(filename[j][0])+1); // copy the null!
	        bitfont.m_filename = filename[j][0];

	        int fmt = image.getFormat();
	        bitfont.m_alpha = image.hasAlpha();
	        bitfont.m_rgb = (fmt!= GL11.GL_LUMINANCE && fmt!= GL11.GL_ALPHA && fmt!= GL11.GL_LUMINANCE_ALPHA); // this is a cheat!!

//	        TestPrintGLError("Error 0x%x NvBFInitialize before texture gen...\n");
	        // GL initialization...
	        bitfont.m_tex = image.updaloadTexture();
//	        TestPrintGLError("Error 0x%x NvBFInitialize after texture load...\n");
	    
	        // set up any tweaks to texture state here...
	        GLES.glActiveTexture(GL11.GL_TEXTURE0);
	        GLES.glBindTexture(GL11.GL_TEXTURE_2D, bitfont.m_tex);
	        if (image.getMipLevels()>1)
	        	GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
	        else
	        	GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
	        // then turn the bind back off!
	        GLES.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	        // okay, at this point we're ready to go, so flag we're okay, allocate a font struct, setup GL stuff.
	        fontsLoaded++;
	    }

	    if (fontsLoaded!=count)
	        NvLogger.e("!!> couldn't load all requested fonts\n");

	    s_bfInitialized = true;

	    return (fontsLoaded!=count) ? 1 : 0;
	}
	
	/**
	 * Source C language function: <br><code>void NvBFCleanup(void)</code>;<p>
	 * Clean up all BitFont resources.
	 */
	public static synchronized void cleanup(){
		if (s_bfInitialized)
	    {
	    // free the shader
	        fontProg = null;
	    // NvFree each font in the fontLL
	        NvBitFont bitfont = bitFontLL, currFont;
	        while (bitfont != null)
	        {
	            currFont = bitfont;
	            bitfont = bitfont.m_next;
	            // delete font texture
//	            GLES.glDeleteTextures(currFont.m_tex); TODO can't do this on Android 
	            currFont.m_tex = 0;
	            // delete new AFont objects
//	            delete currFont.m_afont;
//	            delete currFont.m_afontBold;
	            // free the font itself.
//	            delete currFont;
	        }
	        bitFontLL = null;
	        bitFontID = 1;
	    // NvFree the master index vbo
	        if (masterTextIndexVBO != 0)
	        {
//	        	GLES.glDeleteBuffers(masterTextIndexVBO);  TODO can't do this on Android 
	            masterTextIndexVBO = 0;
	        }
	        masterTextIndexList = null;
	        maxIndexChars = 0;
	    // !!!!TBD

	        // for safety, we're going to clear _everything_ here to init's
	        bitFontLL = null;
	        bitFontID = 1; // NOT ZERO, start at 1 so zero is 'invalid'...
	        fontProg = null;
	        fontProgAllocInternal = true;
	        fontProgLocMat = 0;
	        fontProgLocTex = 0;
	        fontProgAttribPos = 0;
	        fontProgAttribCol = 0;
	        fontProgAttribTex = 0;
	        dispW = 0;
	        dispH = 0;
	        dispAspect = 0;
	        dispRotation = 0;
	        lastColorArray = 0;
	        lastFontTexture = 0;
	        lastTextMode = 111;
	        maxIndexChars = 0;
	        masterTextIndexList = null;
	        masterTextIndexVBO = 0;
	        //s_pixelToClipMatrix[4][4];
	        s_pixelScaleFactorX = 2.0f / 640.0f;
	        s_pixelScaleFactorY = 2.0f / 480.0f;
	        m_matrixOverride = null;
	    }
	    
	    s_bfInitialized = false;
	}
	
	/**
	 * Source C language function: <br><code>uint8_t NvBFGetFontID(const char *filename)</code>;<p>
	 * Get the font ID for a named font file.<p>
	 * Look up the matching font ID for a given font filename previously loaded.
     * This is then used as argument to SetFont.  It gives us the ability
     * to abstract the order of loaded fonts from the ID/index used to reference
     * them later -- most useful when multiple, distinct systems in an app are
     * talking to BitFont, and separately loading some same, some different fonts.
	 * @param filename one of the filenames used in previous NvBFInitialize call.
	 * @return byte (bfuchar) ID/index that will refer to that font until app exit.
	 */
	public static byte getFontID(String filename){
		NvBitFont bitfont = bitFontLL;
		while(bitfont != null){
			if(bitfont.m_filename.equals(filename))
				return bitfont.m_id;
			bitfont = bitfont.m_next;
		}
		
		return 0;
	}
	
	/** Source C language function: <br><code>static void NvBFFontProgramPrecache(void)</code>;<p> */
	private static void fontProgramPrecache(){
		fontProg.enable();
	    
	    // grab new uniform locations.
	    fontProgLocMat = fontProg.getUniformLocation("pixelToClipMat");
	    fontProgLocTex = fontProg.getUniformLocation("fontTex");

	    fontProgAttribPos = fontProg.getAttribLocation("pos_attr");
	    fontProgAttribCol = fontProg.getAttribLocation("col_attr");
	    fontProgAttribTex = fontProg.getAttribLocation("tex_attr");

	    // and bind the uniform for the sampler
	    // as it never changes.
	    GLES20.glUniform1i(fontProgLocTex, 0);
	}
	
	// Test method.
//	public static void main(String[] args) {
//		byte[] data = NvUIAssetData.embeddedAssetLookup("RobotoCondensed-Regular-24.fnt");
//		System.out.println(new String(data));
//		
//		AFontTokenizer token = new AFontTokenizer("info face=\"RobotoCondensed-Regular\" size=24 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=2 padding=0,0,0,0 spacing=0,0");
////        while(token.readToken()){
////        	System.out.println(token.getLastToken());
////        }
//				AFontInfo info = new AFontInfo();
//		token.parseAFontInfoBlock(info);
//		System.out.println(info);
//	}
	
	private static AFont loadFontInfo(String fname){
		AFont afont = null;
	    byte[] data = null;
	    byte[] tmpdata = null;

//	    if (0!=strcmp(fname+strlen(fname)-3, "fnt")) // we now only support angelcode format specs.
	    if(!fname.endsWith("fnt"))
	    {
	        NvLogger.e(">> Invalid font file specified: %s...\n", fname);
	        return null;
	    }

	    if ((data = NvUIAssetData.embeddedAssetLookup(fname)) != null)
	    {
	        if (data.length!=0)
	        {
	            AFontTokenizer ftok = new AFontTokenizer(new String(data));
	            afont = ftok.parseAFont();
	            if (null==afont)
	            {
	                NvLogger.e(">> FAILED TO PARSE afont data file: %s...\n", fname);
	                return null;
	            }
	        }
	    }

	    if (null==afont)
	    { // didn't init the font yet, no embedded data, check file.
	        tmpdata = NvAssetLoader.read(fname);
	        if (tmpdata==null) // ugh
	        {
	        	NvLogger.e(">> FAILED TO FIND afont data file: %s...\n", fname);
	            return null;
	        }
	        // else... got data, load it up.
	        AFontTokenizer ftok = new AFontTokenizer(new String(tmpdata));
	        afont = ftok.parseAFont();
	        if (null==afont)
	        {
	        	NvLogger.e(">> FAILED TO PARSE afont data file: %s...\n", fname);
	            return null;
	        }
	    }

	    return afont;
	}
	
	/**
	 * Source C language function: <br><code>void NvBFSetScreenRes(float width, float height)</code>;<p>
	 * Set the destination size/scale to factor coords into.
	 */
	public static void setScreenRes(float width, float height){
		dispW = width;
	    dispH = height;
	    dispAspect = dispW/dispH;

	    s_pixelScaleFactorX = 2.0f / dispW;
	    s_pixelScaleFactorY = 2.0f / dispH;
	}
	
	/**
	 * Source C language function: <br><code>void NvBFGetScreenRes(float *width, float *height)</code>;<p>
	 * Get current destination size/scale for safe save/restore.
	 */
	public static Vector2f getScreenRes(){
		return new Vector2f(dispW, dispH);
	}
	
	/** Get the width of the current destination size/scale.
	 * @see {@link #getScreenRes}
	 * */
	public static float getScreenResWidth(){
		return dispW;
	}
	
	/** Get the height of the current destination size/scale.
	 * @see {@link #getScreenRes}
	 * */
	public static float getScreenResHeight(){
		return dispH;
	}
	
	/**
	 * Source C language function: <br><code>void NvBFSetScreenRot(float degrees)</code>;<p>
	 * Set the rotation/orientation of text output (in 90-degree increments, preferably).
	 */
	public static void setScreenRot(float degrees){
		dispRotation = degrees;
	}
	
}
