/*
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

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/3/13.
 */

public class GaussionBlurProgram extends SimpleOpenGLProgram{

    private int halfPixelSizeIndex;
    public void init(int kernel){
        if (kernel < 1)
            // kernel less 1 we could igore the gauss blur.
            throw new IllegalArgumentException("Invalid kernel.");

        kernel = Math.max(3, kernel);
        int n = (kernel + 1) / 4;
        if (n >= 1 && (kernel + 1) % 4 != 0)
        {
            // rounded up
            n++;
        }

        kernel = 4 * n - 1;

        WeightsAndOffsets gaussionData = GenerateGaussShaderKernelWeightsAndOffsets(kernel);

        StringBuilder vert_shader = NvAssetLoader.readText("shaders/Quad_VS.vert");
        StringBuilder frag_shader = NvAssetLoader.readText("shaders/guass_blurPS.frag");

        frag_shader.insert(0, "#version 300 es\n#define stepCount " + gaussionData.offsets.length + "\n");

        NvGLSLProgram program = new NvGLSLProgram();
        program.setSourceFromStrings(vert_shader, frag_shader,true);

        program.enable();
        program.setUniform1fv("g_Weights", gaussionData.weights, 0, gaussionData.weights.length);
        program.setUniform1fv("g_Offsets", gaussionData.offsets, 0, gaussionData.offsets.length);
        program.setUniform1i("g_Texture", 0);

        halfPixelSizeIndex = program.getUniformLocation("g_HalfPixelSize");
        program.disable();

        programID = program.getProgram();
    }

    public void setHalfPixelSize(float x, float y){
        if(halfPixelSizeIndex >= 0){
            GLES20.glUniform2f(halfPixelSizeIndex, x,y);
        }
    }

    private static double[] GenerateSeparableGaussKernel( double sigma, int kernelSize )
    {
        /*
        if( (kernelSize % 2) != 1 )
        {
            assert( false ); // kernel size must be odd number
            return std::vector<double>();
        }*/

        int halfKernelSize = kernelSize/2;

//        std::vector<double> kernel;
//        kernel.resize( kernelSize );
        double[] kernel = new double[kernelSize];

        final double cPI= 3.14159265358979323846;
        double mean     = halfKernelSize;
        double sum      = 0.0;
        for (int x = 0; x < kernelSize; ++x)
        {
            kernel[x] = Math.sqrt( Math.exp( -0.5 * (Math.pow((x-mean)/sigma, 2.0) + Math.pow((mean)/sigma,2.0)) )
                    / (2 * cPI * sigma * sigma) );
            sum += kernel[x];
        }
        for (int x = 0; x < kernelSize; ++x)
            kernel[x] /= (float)sum;

        return kernel;
    }

    private static float[] GetAppropriateSeparableGauss(int kernelSize){
        // Search for sigma to cover the whole kernel size with sensible values (might not be ideal for all cases quality-wise but is good enough for performance testing)
        final double epsilon = 2e-2f / kernelSize;
        double searchStep = 1.0;
        double sigma = 1.0;
        while( true )
        {

            double[] kernelAttempt = GenerateSeparableGaussKernel( sigma, kernelSize );
            if( kernelAttempt[0] > epsilon )
            {
                if( searchStep > 0.02 )
                {
                    sigma -= searchStep;
                    searchStep *= 0.1;
                    sigma += searchStep;
                    continue;
                }

                float[] retVal = new float[kernelSize];
                for (int i = 0; i < kernelSize; i++)
                    retVal[i] = (float)kernelAttempt[i];
                return retVal;
            }

            sigma += searchStep;

            if( sigma > 1000.0 )
            {
                assert( false ); // not tested, preventing infinite loop
                break;
            }
        }

        return null;
    }

    private static WeightsAndOffsets GenerateGaussShaderKernelWeightsAndOffsets(int kernelSize){
        // Gauss filter kernel & offset creation
        float[] inputKernel = GetAppropriateSeparableGauss(kernelSize);

        float[] oneSideInputs = new float[kernelSize/2 + 1];
        for( int i = (kernelSize/2); i >= 0; i-- )
        {
            if( i == (kernelSize/2) )
                oneSideInputs[i] = ( inputKernel[i] * 0.5f );
            else
                oneSideInputs[i] = ( inputKernel[i] );
        }

        assert( (oneSideInputs.length % 2) == 0 );
        int numSamples = oneSideInputs.length/2;

        float[] weights = new float[numSamples];

        for( int i = 0; i < numSamples; i++ )
        {
            float sum = oneSideInputs[i*2+0] + oneSideInputs[i*2+1];
            weights[i] = sum;
        }

        float[] offsets = new float[numSamples];

        for( int i = 0; i < numSamples; i++ )
        {
            offsets[i] = ( i*2.0f + oneSideInputs[i*2+1] / weights[i] );
        }

        return new WeightsAndOffsets(weights, offsets);
    }

    private static final class WeightsAndOffsets{
        float[] weights;
        float[] offsets;

        WeightsAndOffsets(){}
        WeightsAndOffsets(float[] weights, float[] offsets){
            this.weights = weights;
            this.offsets = offsets;
        }
    }
}
