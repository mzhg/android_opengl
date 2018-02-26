package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.android.base.GLConfiguration;
import jet.learning.opengl.android.base.OpenGLBaseActivity;

public class Mandelbro extends OpenGLBaseActivity{

	static final String g_vertShaderCode = 
			"attribute vec3 a_position;"+
//			"attribute vec3 a_texCoord;"+
//	        "verying vec3 v_texCoord;" +
	        "void main()"+
			"{"+
			" gl_Position = vec4(a_position.x, a_position.y, a_position.z, 1);"+
//	        " v_texCoord = a_texCoord;"+
			"}";
	
	static final String g_ShaderCode =
			"precision highp float;\n"+
			"uniform float halfHeight;"+
	        "uniform float ratio;" +
			"uniform vec4 texCoord;" +
//	        "verying vec3 v_texCoord;" +
			"void main()"+
		    "{"+
		    "vec4 x=(gl_FragCoord/halfHeight -vec4(ratio,1,1,1))*texCoord.z+texCoord;"+
//				"vec4 x=texCoord;"+
		        "vec4 y=x;" +
		        "vec4 z=y;"+

				"int w=256;"+

				"while(--w>0&&y.x<10.0)"+
				"{"+
					"y=x*x;"+
					"x.y*=x.x*2.0;"+
					"x.x=y.x-y.y;"+
					"x+=z;"+
					"y.x+=y.y;"+
				"}"+

				"gl_FragColor=sin(float(w)/18.0 +log(y.x)/28.0+vec4(2,3.5,5,5))/2.0+.5;"+
//                "gl_FragColor = x + y;" +
		    "}";
	
	static final float g_Coords[] =
		{
			+0.30078125f, +0.02343750f,
			-0.82421875f, +0.18359375f,
			+0.07031250f, -0.62109375f,
			-0.07421875f, -0.66015625f,
			-1.65625000f, +0.00000000f,
		};

	int last_t, tt = 0xFFFFFFFF;
	int last_i, i = 0;
	
	int texLoc;
	FloatBuffer buffer;
	@Override
	protected void onGLConfig(GLConfiguration glConfig) {
		glConfig.version = GLConfiguration.GLES2;
	}

	@Override
	protected void onCreate() {
		int prog = GLES20.glCreateProgram();
		int shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(shader, g_ShaderCode);
		GLES20.glCompileShader(shader);
		int[] i = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, i, 0);
		if(i[0] == 0){
			String log = GLES20.glGetShaderInfoLog(shader);
			Log.e("Mandelbro", "fragment shader compile error:" + log);
		}
		
		GLES20.glAttachShader(prog, shader);
		GLES20.glDeleteShader(shader);
		
		shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(shader, g_vertShaderCode);
		GLES20.glCompileShader(shader);
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, i, 0);
		if(i[0] == 0){
			String log = GLES20.glGetShaderInfoLog(shader);
			Log.e("Mandelbro", "vertex shader compile error:\n" + log);
		}
		GLES20.glAttachShader(prog, shader);
		GLES20.glDeleteShader(shader);
		
//		int prog = compileShader(g_vertShaderCode, g_ShaderCode);
		
		GLES20.glLinkProgram(prog);
		
		GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, i, 0);
		if(i[0] == 0){
			String log = GLES20.glGetProgramInfoLog(prog);
			Log.e("Mandelbro", "Link err: " + log);
		}
		GLES20.glUseProgram(prog);
		
		GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "halfHeight"), getHeight()/2);
		GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "ratio"), (float)getWidth()/getHeight());
		texLoc = GLES20.glGetUniformLocation(prog, "texCoord");
		
		float[] positions = {
			-1, -1,0,
			1,  -1,0,
			1,   1,0,
			-1,  1,0,
		};
		
//		int index = GLES20.glGetAttribLocation(prog, "a_position");
		GLES20.glBindAttribLocation(prog, 0, "a_position");
		GLES20.glEnableVertexAttribArray(0);
		
		buffer = ByteBuffer
				.allocateDirect(positions.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.put(positions).flip();
		GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, buffer);
		
		GLES20.glClearColor(1, 0, 0, 1);
		Log.i("Mandelbro", "onCreate!");
		
		int err = GLES20.glGetError();
		if(err != 0){
			Log.e("Mandelbro", "OpenGL Error: " + err);
		}
	}

	@Override
	protected void draw() {
		GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);
		int rnd = (int) System.currentTimeMillis();
		
		last_t = tt;
		tt = (rnd & 8191);
		
		if (tt <last_t){
			last_i = i;
			++i;
			
			if(i >= g_Coords.length/2){
				i = 0;
			}
		}
			
		float t1 = tt * (1.0f / 8192.0f);

		float f = 4 * (t1 - t1 * t1);
		f *= f;
		f *= f;
		f *= f;
		f *= f;

		float s = t1;
		s = s * s * (3 - s - s);
		s = s * s * (3 - s - s);
		s = s * s * (3 - s - s);
		s = s * s * (3 - s - s);
		
//		GL11.glTexCoord3f(g_Coords[2*last_i+0] - s * g_Coords[2*last_i+0] + s * g_Coords[2*i+0],
//			 g_Coords[2*last_i+1] - s * g_Coords[2*last_i+1] + s * g_Coords[2*i+1],
//			 f + (1.0f / 8192.0f));
		GLES20.glUniform4f(texLoc, g_Coords[2*last_i+0] - s * g_Coords[2*last_i+0] + s * g_Coords[2*i+0],
				 g_Coords[2*last_i+1] - s * g_Coords[2*last_i+1] + s * g_Coords[2*i+1],
				 f + (1.0f / 8192.0f), 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
	}

	@Override
	protected void onResize(int width, int height) {
		
	}
}
