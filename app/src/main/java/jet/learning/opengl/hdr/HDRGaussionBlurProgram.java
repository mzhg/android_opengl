package jet.learning.opengl.hdr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/20.
 */

public class HDRGaussionBlurProgram extends SimpleOpenGLProgram {

    public static final float[] std_weights = {0.42f,0.25f,0.15f,0.10f};

    public  HDRGaussionBlurProgram(int img_width, int img_height, boolean vertical, float weight){
        CharSequence vert_src = NvAssetLoader.readText("hdr_shaders/quad_es3.vert");
        float []weights = generateGaussianWeights(weight);
        int width = (int)(3.0/weight);
        CharSequence frag_src = generate1DConvolutionFP_filter(weights, width, vertical, true, img_width, img_height);
//        System.out.println(frag);
//		loop++;
        NvGLSLProgram program = new NvGLSLProgram();
//		program.addBeforeLinkTask(bindAttribs(posBind, texBind));
        program.setSourceFromStrings(vert_src, frag_src, false);
        programID = program.getProgram();

        GLES.checkGLError("HDRGaussionBlurProgram");
        int uniformTexSampler = program.getUniformLocation("TexSampler");

        GLES20.glUseProgram(programID);
        GLES20.glUniform1i(uniformTexSampler, 0);
        GLES20.glUseProgram(0);
    }

    static float gaussian(float x, float s)
    {
        double sx = s * x;
        return (float) Math.exp(-sx * sx);
    }

    static float[] generateGaussianWeights(float s){
        int width = (int)(3.0/s);
        int size = width*2+1;
        float[] weight = new float [size];

        float sum = 0.0f;
        int x;
        for(x=0; x<size; x++) {
            weight[x] = gaussian((float) x-width, s);
            sum += weight[x];
        }

        for(x=0; x<size; x++) {
            weight[x] /= sum;
        }
        return weight;
    }

    static float[] generateTriangleWeights(int width)
    {
        float []weights = new float [width];
        float sum = 0.0f;
        for(int i=0; i<width; i++) {
            float t = i / (float) (width-1);
            weights[i] = 1.0f - Math.abs(t-0.5f)*2.0f;
            sum += weights[i];
        }
        for(int i=0; i<width; i++) {
            weights[i] /= sum;
        }
        return weights;
    }

    /**
     Generate fragment program code for a separable convolution, taking advantage of linear filtering.
     This requires roughly half the number of texture lookups.<p>

     We want the general convolution:<pre>
     a*f(i) + b*f(i+1)</pre>
     Linear texture filtering gives us:<pre>
     f(x) = (1-alpha)*f(i) + alpha*f(i+1); </pre>
     It turns out by using the correct weight and offset we can use a linear lookup to achieve this:
     <pre>(a+b) * f(i + b/(a+b))</pre>
     as long as <code>0 <= b/(a+b) <= 1</code>
     */

    static CharSequence generate1DConvolutionFP_filter(float []weights, int width, boolean vertical, boolean tex2D, int img_width, int img_height)
    {
        // calculate new set of weights and offsets
        int nsamples = 2*width+1;
        int nsamples2 = (int) Math.ceil(nsamples/2.0f);
        float []weights2 = new float [nsamples2];
        float []offsets = new float [nsamples2];

        for(int i=0; i<nsamples2; i++) {
            float a = weights[i*2];
            float b;
            if (i*2+1 > nsamples-1)
                b = 0;
            else
                b = weights[i*2+1];
            weights2[i] = a + b;
            offsets[i] = b / (a + b);
            //    printf("%d: %f %f\n", i, weights2[i], offsets[i]);
        }
        //    printf("nsamples = %d\n", nsamples2);

//		char szBuffer[16];
        StringBuilder ost = new StringBuilder(200);
//	    std.ostringstream ost;
//	    ost <<
        ost.append("#version 300 es\n");
        ost.append("precision highp float;\n");
        ost.append("uniform sampler2D TexSampler;\n");
        ost.append("in vec2 a_texCoord;\n");
        ost.append("out vec4 gl_FragColor;\n");
        ost.append("void main()\n");
        ost.append("{\n");
        ost.append("vec4 sum = vec4(0);\n");
        ost.append("vec2 texcoord;\n");
        for(int i=0; i<nsamples2; i++) {
            float x_offset = 0, y_offset = 0;
            if (vertical) {
                y_offset = (i*2)-width+offsets[i];
            } else {
                x_offset = (i*2)-width+offsets[i];
            }
            if (tex2D) {
                x_offset = x_offset / img_width;
                y_offset = y_offset / img_height;
            }
            float weight = weights2[i];
//			ost << "texcoord = a_texCoord + vec2(" << szBuffer;
            ost.append("texcoord = a_texCoord + vec2(").append(String.format("%f", x_offset));
//			ost << ", " << szBuffer << ");\n";
            ost.append(", ").append(String.format("%f", y_offset)).append(");\n");
//			ost << "sum += texture2D(TexSampler, texcoord).rgb*" << szBuffer << ";\n";
            ost.append("sum += texture(TexSampler, texcoord)*").append(String.format("%f", weight)).append(";\n");
        }

//	    ost <<
        ost.append("gl_FragColor = sum;\n");
        ost.append("}\n");

//	    delete [] weights2;
//	    delete [] offsets;
        return ost;
    }
}
