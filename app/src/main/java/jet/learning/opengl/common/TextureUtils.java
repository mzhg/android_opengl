package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextureUtils {
    private static final int[] IntArray1 = new int[1];
    private static final float[] FloatArray1 = new float[1];

    public static void glDeleteTextures(int textureID){
        IntArray1[0] = textureID;
        GLES20.glDeleteTextures(1, IntArray1, 0);
    }

    private static final int RED = GLES30.GL_RED;
    private static final int RG  = GLES30.GL_RG;
    private static final int RGB = GLES30.GL_RGB;
    private static final int RGBA= GLES30.GL_RGBA;

    private static final int RED_INTEGER = GLES30.GL_RED_INTEGER;
    private static final int RG_INTEGER = GLES30.GL_RG_INTEGER;
    private static final int RGB_INTEGER = GLES30.GL_RGB_INTEGER;
    private static final int RGBA_INTEGER = GLES30.GL_RGBA_INTEGER;

    private static final int[] compressed_formats = {
            0x8225,  // COMPRESSED_RED
            0x8226,  // COMPRESSED_RG
            0x84ed,  // COMPRESSED_RGB
            0x84ee,  // COMPRESSED_RGBA
            0x8c48,  // COMPRESSED_SRGB
            0x8c49,  // COMPRESSED_SRGB_ALPHA
            0x8dbb,  // COMPRESSED_RED_RGTC1
            0x8dbc,  // COMPRESSED_SIGNED_RED_RGTC1
            0x8dbd,  // COMPRESSED_RG_RGTC2
            0x8dbe,  // COMPRESSED_SIGNED_RG_RGTC2
            0x8e8c,  // COMPRESSED_RGBA_BPTC_UNORM
            0x8e8d,  // COMPRESSED_SRGB_ALPHA_BPTC_UNORM
            0x8e8e,  // COMPRESSED_RGB_BPTC_SIGNED_FLOAT
            0x8e8f,  // GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT
            0x9274,  // GL_COMPRESSED_RGB8_ETC2
            0x9275,  // GL_COMPRESSED_SRGB8_ETC2
            0x9276,  // GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2
            0x9277,  // GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2
            0x9278,  // GL_COMPRESSED_RGBA8_ETC2_EAC
            0x9279,  // GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC
            0x9270,  // GL_COMPRESSED_R11_EAC
            0x9271,  // GL_COMPRESSED_SIGNED_R11_EAC
            0x9272,  // GL_COMPRESSED_RG11_EAC
            0x9273,  // GL_COMPRESSED_SIGNED_RG11_EAC
    };

    static {
        Arrays.sort(compressed_formats);
    }

    private TextureUtils(){}

    public static int glGetTexLevelParameteri(int target, int level, int pname){
        GLES31.glGetTexLevelParameteriv(target, level, pname, IntArray1, 0);
        GLES.checkGLError();

        return IntArray1[0];
    }

    public static float glGetTexParameterf(int target,int pname){
        GLES31.glGetTexParameterfv(target, pname, FloatArray1,0);
        GLES.checkGLError();

        return FloatArray1[0];
    }

    public static int glGetTexParameteri(int target, int pname){
        GLES31.glGetTexParameteriv(target, pname, IntArray1, 0);
        GLES.checkGLError();

        return IntArray1[0];
    }

    /**
     * Binding the texture currently and returns the bytes of the texture by the given miplevels.
     * @param target
     * @param textureID
     * @param baseLevel
     * @param levelCount
     * @return
     */
    public static long getTextureMemorySize(int target, int textureID, int baseLevel, int levelCount){
        GLES20.glBindTexture(target, textureID);

        long totalSize = 0;

        for(int level = baseLevel; level < levelCount + baseLevel; level++){
            int width = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_WIDTH);
            int height = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_HEIGHT);
            int depth = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_DEPTH);
            if(width == 0 && height == 0 && depth == 0)
                return totalSize;

            int red_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_RED_SIZE);
            int green_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_GREEN_SIZE);
            int blue_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_BLUE_SIZE);
            int alpha_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_ALPHA_SIZE);
            int depth_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_DEPTH_SIZE);
            int stencil_bits = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_STENCIL_SIZE);

            int totalBytes = Math.max(width, 1) * Math.max(height, 1) * Math.max(depth, 1) *
                    (red_bits + green_bits + blue_bits + alpha_bits + depth_bits + stencil_bits) /8;

            totalSize += totalBytes;
        }

        return totalSize;
    }

    public static void flipY(ByteBuffer bytes, int height){
        // flip the image.
        final int widthBytes = bytes.remaining()/ height;
        byte[] firstRow = new byte[widthBytes];
        byte[] secondRow = new byte[widthBytes];

        for(int i = 0; i < height/2; i++){
            // Read the first row
            bytes.position(i * widthBytes);
            bytes.get(firstRow);

            // Read the second row
            bytes.position((height -1 - i) * widthBytes);
            bytes.get(secondRow);

            // Put the secondRow in the location of the first row.
            bytes.position(i * widthBytes);
            bytes.put(secondRow);

            // Put the firstRow in the location of the second row.
            bytes.position((height -1 - i) * widthBytes);
            bytes.put(firstRow);
        }

        // reset the position.
        bytes.position(0);
    }


    public static Texture2D createTexture2D(int target, int textureID){
        return createTexture2D(target, textureID, null);
    }

    public static Texture2D createTexture2D(int target, int textureID, Texture2D out){
        if(!GLES20.glIsTexture(textureID))
            return null;

        if(target != GLES31.GL_TEXTURE_2D && target != GLES31.GL_TEXTURE_2D_ARRAY &&
                target != GLES31.GL_TEXTURE_2D_MULTISAMPLE && target != GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY)
            throw new IllegalArgumentException("Invalid target: " + getTextureTargetName(target));

        GLES.glBindTexture(target, textureID);
        Texture2D result = out != null ? out : new Texture2D();
        result.width  = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_WIDTH);
        result.height = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_HEIGHT);
        result.format = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_INTERNAL_FORMAT);
        result.samples= glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_SAMPLES);
        result.target = target;
        result.textureID = textureID;

        result.arraySize    = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_DEPTH);
        if(result.width > 0){
            int level = 1;
            while(true){
                int width = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_WIDTH);
                if(width == 0){
                    break;
                }

                level++;
            }

            result.mipLevels = level;
        }
        return result;
    }

    public static boolean isTexture2D(int target){
        if(target == GLES31.GL_TEXTURE_2D || target == GLES31.GL_TEXTURE_2D_MULTISAMPLE)
            return true;

        return false;
    }

    public static boolean isTextureLayered(int target){
        if(target == GLES31.GL_TEXTURE_3D || target == GLES31.GL_TEXTURE_CUBE_MAP || target == GLES31.GL_TEXTURE_2D_ARRAY
                /*||target == GLES31.GL_TEXTURE_1D_ARRAY*/ || target == GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY)
            return true;
        return false;
    }


    public static boolean isCompressedFormat(int format){
        return Arrays.binarySearch(compressed_formats, format) >= 0;
    }

    static boolean valid_texture2D = true;

    private static void check(Texture2DDesc desc){
        if(desc.width == 0 )
            throw new IllegalArgumentException("width can't be 0.");
        if(desc.height == 0 )
            throw new IllegalArgumentException("height can't be 0.");
    }

    public static Texture2D resizeTexture2D(Texture2D origin, int width, int height, int format){
        if(origin == null || origin.getWidth() != width || origin.getHeight() != height || origin.getFormat() != format){
            Texture2DDesc desc = (origin != null ? origin.getDesc() : new Texture2DDesc());
            desc.width = width;
            desc.height =height;
            desc.format = format;

            if(origin != null)
                origin.dispose();

            origin = createTexture2D(desc, null);
        }

        return origin;
    }

    public static Texture2D createTexture2D(Texture2DDesc textureDesc, TextureDataDesc dataDesc){
        return createTexture2D(textureDesc, dataDesc, null);
    }

    @SuppressWarnings("unchecked")
    public static Texture2D createTexture2D(Texture2DDesc textureDesc, TextureDataDesc dataDesc, Texture2D out){
        int textureID;
        int target;
        int format;
        boolean isCompressed = false;
        final boolean isDSA = false; // = GL.getCapabilities().GL_ARB_direct_state_access;
        boolean multiSample; // = GL.getCapabilities().OpenGL32 && textureDesc.sampleDesc.count > 1;
        int mipLevels = Math.max(1, textureDesc.mipLevels);
        final boolean isSupportMSAA = true; // todo

        check(textureDesc);

//        isSupportMSAA = version.major >= 3 && ((version.ES && version.minor >= 1) || version.minor >= 2);
        multiSample = isSupportMSAA && textureDesc.sampleCount > 1;

        // measure texture target.
        if(textureDesc.arraySize > 1){
            target = multiSample ? GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY  : GLES31.GL_TEXTURE_2D_ARRAY;
        }else{
            target = multiSample ? GLES31.GL_TEXTURE_2D_MULTISAMPLE : GLES31.GL_TEXTURE_2D;
        }

        // measure texture internal format
        if(dataDesc != null){
            isCompressed = Arrays.binarySearch(compressed_formats, dataDesc.format) >= 0;
            if(isCompressed){
                format = dataDesc.format;
            }else{
                format = textureDesc.format;
            }
        }else{
            format = textureDesc.format;
        }

        {
            boolean allocateStorage = false;

            // 1. Generate texture ID
            textureID = GLES.glGenTextures();

            // 2. Allocate storage for Texture Object
//			final GLCapabilities cap = GL.getCapabilities();
            final boolean textureStorage = true; // TODO version.ES && version.major >= 3 || gl.isSupportExt("GL_ARB_texture_storage");
            final boolean textureStorageMSAA = true; //TODO (version.ES && version.major >= 2 && version.minor >= 1) || gl.isSupportExt("GL_ARB_texture_storage_multisample");
            if(!isCompressed){
                GLES20.glBindTexture(target, textureID);
                switch (target) {
                    case GLES31.GL_TEXTURE_2D_MULTISAMPLE:
                        GLES31.glTexStorage2DMultisample(target, textureDesc.sampleCount, format, textureDesc.width, textureDesc.height, false);
                        mipLevels = 1;  // multisample_texture doesn't support mipmaps.
                        break;
                    case GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY:
                        // TODO, need GLES3.2
//                        GLES31.glTexStorage3DMultisample(target, textureDesc.sampleCount, format, textureDesc.width, textureDesc.height, textureDesc.arraySize, false);
                        mipLevels = 1;  // multisample_texture doesn't support mipmaps.
                        break;
                    case GLES31.GL_TEXTURE_2D_ARRAY:
                    case GLES31.GL_TEXTURE_2D:
                        if(textureStorage){
                            allocateStorage = true;
                            if(target == GLES31.GL_TEXTURE_2D){
                                GLES31.glTexStorage2D(GLES31.GL_TEXTURE_2D, mipLevels, format, textureDesc.width, textureDesc.height);
                                if(valid_texture2D) GLES.checkGLError();
                            }else{
                                GLES31.glTexStorage3D(GLES31.GL_TEXTURE_2D_ARRAY, mipLevels, format, textureDesc.width, textureDesc.height, textureDesc.arraySize);
                                if(valid_texture2D) GLES.checkGLError();
                            }
                        }

                        break;

                    default:
                        break;
                }
            }else{
                // remove multisample symbol
                if(target == GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY){
                    target = GLES31.GL_TEXTURE_2D_ARRAY;
                    multiSample = false;
                }

                if(target == GLES31.GL_TEXTURE_2D_MULTISAMPLE){
                    target = GLES31.GL_TEXTURE_2D;
                    multiSample = false;
                }

                GLES31.glBindTexture(target, textureID);
            }

            // 3. Fill the texture Data�� Ignore the multisample texture.
            if(target != GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY && target != GLES31.GL_TEXTURE_2D_MULTISAMPLE){
                int width = textureDesc.width;
                int height = textureDesc.height;
                int depth = textureDesc.arraySize;
                enablePixelStore(dataDesc);

                int dataFormat = measureFormat(format);
                int type = measureDataType(format);
                Object pixelData = null;
                if(dataDesc != null){
                    dataFormat = dataDesc.format;
                    type = dataDesc.type;
                    pixelData = dataDesc.data;
                }

                if(mipLevels > 1){
                    int loop;
                    List<Object> mipData = null;
                    if(dataDesc.data instanceof  List<?>){
                        mipData = (List<Object>)dataDesc.data;
                    }else if(dataDesc.data.getClass().isArray()){
                        mipData = Arrays.asList((Object[])dataDesc.data);
                    }else{
                        mipData = Arrays.asList(dataDesc.data);
                    }

                    loop = Math.min(mipData.size(), mipLevels);

                    for(int i = 0; i < loop; i++){
                        Object mipmapData = null;
                        if(mipData != null){
                            mipmapData = mipData.get(i);
                        }

                        if(isCompressed){
                            if(target == GLES31.GL_TEXTURE_2D_ARRAY){
                                compressedTexImage3D(target, width, height, depth, i, dataFormat, dataDesc.type, dataDesc.imageSize, mipmapData);
                            }else{
                                compressedTexImage2D(target, width, height, i, dataFormat, dataDesc.type, dataDesc.imageSize, mipmapData);
                            }
                        }else if(target == GLES31.GL_TEXTURE_2D_ARRAY){
                            if(allocateStorage){
                                subTexImage3D(target, width, height, depth, i, dataFormat, type, mipmapData);
                            }else{
                                texImage3D(target, format, width, height, depth, i, dataFormat, type, mipmapData);
                            }
                        }else if(target == GLES31.GL_TEXTURE_2D){
                            if(allocateStorage){
                                subTexImage2D(target, width, height, i, dataFormat, type, mipmapData);
                            }else{
                                texImage2D(target, format, width, height, i, dataFormat, type, mipmapData);
                            }
                        }

                        width = Math.max(1, width >> 1);
                        height = Math.max(1, height >> 1);
                        depth = Math.max(1, depth >> 1);
                    }

                    GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR_MIPMAP_LINEAR);
                    if(mipData.size() < mipLevels) {
                        GLES31.glGenerateMipmap(target);
                    }
                }else{
                    if(isCompressed){
                        compressedTexImage3D(target, width, height, depth, 0, dataDesc.format, dataDesc.type, dataDesc.imageSize, dataDesc.data);
                    }else if(target == GLES31.GL_TEXTURE_2D_ARRAY){
                        if(allocateStorage){
                            subTexImage3D(target, width, height, depth, 0, dataFormat, type, pixelData);
                        }else{
                            texImage3D(target, format, width, height, depth, 0, dataFormat, type, pixelData);
                        }
                    }else if(target == GLES31.GL_TEXTURE_2D){
                        if(allocateStorage){
                            subTexImage2D(target, width, height, 0, dataFormat, type, pixelData);
                        }else{
                            texImage2D(target, format, width, height, 0, dataFormat, type, pixelData);
                        }
                    }
                }

                disablePixelStore(dataDesc);
            }

            if(!multiSample) {
                // setup the defualt parameters.
                GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
                GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_MIN_FILTER, mipLevels > 1 ? GLES31.GL_LINEAR_MIPMAP_LINEAR:GLES31.GL_LINEAR);
                GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
                GLES31.glTexParameteri(target, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);
            }
            GLES31.glBindTexture(target, 0);  // unbind Texture
        }

        GLES.checkGLError();
        Texture2D texture = (out != null ? out : new Texture2D());
        texture.arraySize = textureDesc.arraySize;
        texture.format = format;
        texture.height = textureDesc.height;
        texture.width  = textureDesc.width;
        texture.target = target;
        texture.textureID = textureID;
        texture.mipLevels = mipLevels;
        texture.samples = textureDesc.sampleCount;
        return texture;
    }

    private static void compressedTexImage3D(int target, int width, int height, int depth, int level, int internalformat, int type, int imageSize,Object data){
        if(data == null){
            GLES31.glCompressedTexImage3D(target, level, internalformat, width, height, depth, 0, imageSize, null);
        }else if(data instanceof  Buffer){
            Buffer bufferData = (Buffer)data;
            GLES31.glCompressedTexImage3D(target, level, internalformat, width, height, depth, 0, bufferData.remaining(), bufferData);
        }else{
//            GLES31.glCompressedTexImage3D(target, level, internalformat, width, height, depth, 0, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D)
            GLES.checkGLError();
    }

    private static void compressedTexImage2D(int target, int width, int height, int level, int internalformat, int type, int imageSize,Object data){
        if(data == null){
            GLES31.glCompressedTexImage2D(target, level, internalformat, width, height, 0, imageSize, null);
        }else if(data instanceof  Buffer){
            Buffer bufferData = (Buffer)data;
            GLES31.glCompressedTexImage2D(target, level, internalformat, width, height, 0, bufferData.remaining(), bufferData);
        }else{
//            gl.glCompressedTexImage2D(target, level, internalformat, width, height, 0, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D)
            GLES.checkGLError();
    }

    private static void subTexImage3D(int target, int width, int height, int depth, int level, int format, int type, Object data){
        if(data == null){
//            GLES31.glTexSubImage3D(target, level, 0, 0, 0, width, height, depth, format, type, null);
        }else if(data instanceof  Buffer){
            Buffer bufferData = (Buffer)data;
            GLES31.glTexSubImage3D(target, level, 0, 0, 0, width, height, depth, format, type, bufferData);
        }else{
//            gl.glTexSubImage3D(target, level, 0, 0, 0, width, height, depth, format, type, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D) {
            GLES.checkGLError();
        }
    }

    private static void texImage3D(int target, int internalformat, int width, int height, int depth, int level, int format, int type, Object data){
        if(data == null){
            GLES31.glTexImage3D(target, level, internalformat, width, height, depth,0,format, type,null);
        }else if(data instanceof  Buffer){
            GLES31.glTexImage3D(target, level, internalformat, width, height, depth, 0, format, type, (Buffer)data);
        }else{
//            gl.glTexImage3D(target, level, internalformat, width, height, depth, 0, format, type, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D) {
            GLES.checkGLError();
        }
    }

    private static void subTexImage2D(int target, int width, int height, int level, int format, int type,  Object data){
        if(data == null){
//            GLES31.glTexSubImage2D(target, level, 0, 0, width, height, format, type, null);
        }else if(data instanceof  Buffer){
            GLES31.glTexSubImage2D(target, level, 0, 0, width, height, format, type, (Buffer)data);
        }else{
//            GLES31.glTexSubImage2D(target, level, 0, 0, width, height, format, type, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D) {
            GLES.checkGLError();
        }
    }

    private static void texImage2D(int target, int internalformat, int width, int height, int level, int format, int type, Object data){
        if(data == null){
            GLES31.glTexImage2D(target, level, internalformat, width, height, 0, format, type, null);
        }else if(data instanceof  Buffer){
            GLES31.glTexImage2D(target, level, internalformat, width, height, 0, format, type, (Buffer)data);
        }else{
//            GLES31.glTexImage2D(target, level, internalformat, width, height, 0, format, type, CacheBuffer.wrapPrimitiveArray(data));
            throw new IllegalArgumentException();
        }

        if(valid_texture2D) {
            GLES.checkGLError();
        }
    }

    private static void enablePixelStore(TextureDataDesc desc){
        if(desc == null)
            return;

        if(desc.unpackRowLength > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_ROW_LENGTH, desc.unpackRowLength);
        }

        if(desc.unpackSkipRows > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_ROWS, desc.unpackSkipRows);
        }

        if(desc.unpackSkipPixels > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_PIXELS, desc.unpackSkipPixels);
        }

        if(desc.unpackAlignment > 0 && desc.unpackAlignment != 4){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT, desc.unpackAlignment);
        }

        if(desc.unpackImageHeight > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_IMAGE_HEIGHT, desc.unpackImageHeight);
        }

        if(desc.unpackSkipImages > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_IMAGES, desc.unpackSkipImages);
        }

        if(valid_texture2D){
            GLES.checkGLError();
        }
    }

    private static void disablePixelStore(TextureDataDesc desc){
        if(desc == null)
            return;

        if(desc.unpackRowLength > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_ROW_LENGTH, 0);
        }

        if(desc.unpackSkipRows > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_ROWS, 0);
        }

        if(desc.unpackSkipPixels > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_PIXELS, 0);
        }

        if(desc.unpackAlignment > 0 && desc.unpackAlignment != 4){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT, 4);
        }

        if(desc.unpackImageHeight > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_IMAGE_HEIGHT, 0);
        }

        if(desc.unpackSkipImages > 0){
            GLES31.glPixelStorei(GLES31.GL_UNPACK_SKIP_IMAGES, 0);
        }
    }

    public static String getTextureTargetName(int target){
        switch (target) {
            case GLES31.GL_TEXTURE_2D:  return "GL_TEXTURE_2D";
            case GLES31.GL_TEXTURE_3D:  return "GL_TEXTURE_3D";
            case GLES31.GL_TEXTURE_CUBE_MAP:  return "GL_TEXTURE_CUBE_MAP";
            case GLES31.GL_TEXTURE_2D_ARRAY:  return "GL_TEXTURE_2D_ARRAY";
            case GLenum.GL_TEXTURE_CUBE_MAP_ARRAY:  return "GL_TEXTURE_CUBE_MAP_ARRAY";
            case GLenum.GL_TEXTURE_BUFFER:  return "GL_TEXTURE_BUFFER";
            case GLES31.GL_TEXTURE_2D_MULTISAMPLE :  return "GL_TEXTURE_2D_MULTISAMPLE ";
            case GLenum.GL_TEXTURE_2D_MULTISAMPLE_ARRAY:  return "GL_TEXTURE_2D_MULTISAMPLE_ARRAY";


            default:
                return "Unkown TextureTarget(0x" + Integer.toHexString(target) + ")";
        }
    }

    public static String getTextureFilterName(int filter){
        switch (filter) {
            case GLES31.GL_NEAREST: return "GL_NEAREST";
            case GLES31.GL_LINEAR: return "GL_LINEAR";
            case GLES31.GL_NEAREST_MIPMAP_NEAREST: return "GL_NEAREST_MIPMAP_NEAREST";
            case GLES31.GL_NEAREST_MIPMAP_LINEAR: return "GL_NEAREST_MIPMAP_LINEAR";
            case GLES31.GL_LINEAR_MIPMAP_NEAREST: return "GL_LINEAR_MIPMAP_NEAREST";
            case GLES31.GL_LINEAR_MIPMAP_LINEAR: return "GL_LINEAR_MIPMAP_LINEAR";

            default:
                return "Unkown TextureFilter(0x" + Integer.toHexString(filter) + ")";
        }
    }

    public static String getTextureSwizzleName(int swizzle){
        switch (swizzle) {
            case GLES31.GL_RED: return "GL_RED";
            case GLES31.GL_GREEN: return "GL_GREEN";
            case GLES31.GL_BLUE: return "GL_BLUE";
            case GLES31.GL_ALPHA: return "GL_ALPHA";
            case GLES31.GL_ONE: return "GL_ONE";
            case GLES31.GL_ZERO: return "GL_ZERO";

            default:
                return "Unkown TextureSwizzle(0x" + Integer.toHexString(swizzle) + ")";
        }
    }

    public static String getTextureWrapName(int wrap){
        switch (wrap) {
            case GLES31.GL_CLAMP_TO_EDGE: return "GL_CLAMP_TO_EDGE";
            case GLES31.GL_REPEAT: return "GL_REPEAT";
            case GLenum.GL_CLAMP_TO_BORDER: return "GL_CLAMP_TO_BORDER";
            case GLES31.GL_MIRRORED_REPEAT: return "GL_MIRRORED_REPEAT";

            default:
                return "Unkown TextureWrap(0x" + Integer.toHexString(wrap) + ")";
        }
    }

    public static int convertSRGBFormat(int internalFormat){
        switch (internalFormat){
            case GLES31.GL_RGBA8: return GLES31.GL_SRGB8_ALPHA8;
            case GLES31.GL_RGB8:  return GLES31.GL_SRGB8;
            default:
                throw new IllegalArgumentException("Can't convert " + getFormatName(internalFormat) + " to SRGB");
        }
    }

    public static String getTypeName(int type){
        switch (type) {
            case GLES31.GL_UNSIGNED_BYTE: return "GL_UNSIGNED_BYTE";
            case GLES31.GL_UNSIGNED_SHORT: return "GL_UNSIGNED_SHORT";
            case GLES31.GL_UNSIGNED_INT: return "GL_UNSIGNED_INT";
            case GLES31.GL_BYTE: return "GL_BYTE";
            case GLES31.GL_SHORT: return "GL_SHORT";
            case GLES31.GL_INT: return "GL_INT";
            case GLES31.GL_FLOAT: return "GL_FLOAT";

            default:
                return "Unkown Type(0x" + Integer.toHexString(type) + ")";
        }
    }

    public static String getDepthTextureModeName(int mode){
        switch (mode) {
            case GLES31.GL_LUMINANCE: return "GL_LUMINANCE";
            case GLES31.GL_ALPHA: return "GL_ALPHA";
            case GLES31.GL_RED: return "GL_RED";
            case GLES31.GL_NONE: return "GL_NONE";
            default:
                return "Unkown DepthTextureMode(0x" + Integer.toHexString(mode) + ")";
        }
    }

    public static String getTypeSignName(int type){
        switch (type) {
            case GLES31.GL_NONE: return "GL_NONE";
            case GLES31.GL_SIGNED_NORMALIZED: return "GL_SIGNED_NORMALIZED";
            case GLES31.GL_UNSIGNED_NORMALIZED: return "GL_UNSIGNED_NORMALIZED";
            case GLES31.GL_FLOAT: return "GL_FLOAT";
            case GLES31.GL_INT: return "GL_INT";
            case GLES31.GL_UNSIGNED_INT : return "GL_UNSIGNED_INT ";

            default:
                return "Unkown Type(0x" + Integer.toHexString(type) + ")";
        }
    }

    private static int measureInterformat(int req_comp){
        switch (req_comp) {
            case 1:  	return GLES31.GL_R8;
            case 2:		return GLES31.GL_RG8;
            case 3:     return GLES31.GL_RGB8;
            case 4:     return GLES31.GL_RGBA8;
            default:
                throw new IllegalArgumentException("req_comp = " + req_comp);
        }
    }

    public static boolean isNormalizedFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return true;
            case GLES31.GL_R8_SNORM:		    return true;
            case GLES31.GL_RG8:					return true;
            case GLES31.GL_RG8_SNORM:			return true;
            case GLES31.GL_RGB8:				return true;
            case GLES31.GL_RGB8_SNORM:			return true;
            case GLES31.GL_RGBA4:				return true;
            case GLES31.GL_RGB5_A1:				return true;
            case GLES31.GL_RGBA8:				return true;
            case GLES31.GL_RGBA8_SNORM:			return true;
            case GLES31.GL_RGB10_A2:			return true;
            case GLES31.GL_RGB10_A2UI:			return true;
            case GLES31.GL_SRGB8:				return true;
            case GLES31.GL_SRGB8_ALPHA8:		return true;
            case GLES31.GL_R16F:
            case GLES31.GL_RG16F:
            case GLES31.GL_RGB16F:
            case GLES31.GL_RGBA16F:
            case GLES31.GL_R32F:
            case GLES31.GL_RG32F:
            case GLES31.GL_RGB32F:
            case GLES31.GL_RGBA32F:
            case GLES31.GL_R11F_G11F_B10F:		return true; // TODO
            case GLES31.GL_RGB9_E5:				return true; // TODO ?
            case GLES31.GL_R8I:
            case GLES31.GL_R8UI:
            case GLES31.GL_R16I:
            case GLES31.GL_R16UI:
            case GLES31.GL_R32I:
            case GLES31.GL_R32UI:
            case GLES31.GL_RG8I:
            case GLES31.GL_RG8UI:
            case GLES31.GL_RG16I:
            case GLES31.GL_RG16UI:
            case GLES31.GL_RG32I:
            case GLES31.GL_RG32UI:
            case GLES31.GL_RGB8I:
            case GLES31.GL_RGB8UI:
            case GLES31.GL_RGB16I:
            case GLES31.GL_RGB16UI:
            case GLES31.GL_RGB32I:
            case GLES31.GL_RGB32UI:

            case GLES31.GL_RGBA8I:
            case GLES31.GL_RGBA8UI:
            case GLES31.GL_RGBA16I:
            case GLES31.GL_RGBA16UI:
            case GLES31.GL_RGBA32I:
            case GLES31.GL_RGBA32UI:			return false;

            default:
                throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
        }
    }

    public static int measureDataTypeSize(int format){
        switch (format)
        {
            case GLES31.GL_UNSIGNED_BYTE:
            case GLES31.GL_BYTE:
                return 1;
            case GLES31.GL_UNSIGNED_SHORT:
            case GLES31.GL_SHORT:
            case GLES31.GL_HALF_FLOAT:
                return 2;
            case GLES31.GL_UNSIGNED_INT:
            case GLES31.GL_INT:
            case GLES31.GL_FLOAT:
                return 4;
            default:
                throw new IllegalArgumentException("Unkown format: " + Integer.toHexString(format));
        }
    }

    public static float measureSizePerPixel(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return 1;
            case GLES31.GL_R8_SNORM:		    return 1;
            case GLES31.GL_RG8:				return 2;
            case GLES31.GL_RG8_SNORM:			return 2;
            case GLES31.GL_RGB8:				return 3;
            case GLES31.GL_RGB8_SNORM:		return 3;
            case GLES31.GL_RGBA4:				return 2;
            case GLES31.GL_RGB5_A1:			return 2;
            case GLES31.GL_RGBA8:				return 4;
            case GLES31.GL_RGBA8_SNORM:		return 4;
            case GLES31.GL_RGB10_A2:			return 4;
            case GLES31.GL_RGB10_A2UI:		return 4;
            case GLES31.GL_SRGB8:				return 3;
            case GLES31.GL_SRGB8_ALPHA8:		return 4;
            case GLES31.GL_R16F:				return 2;
            case GLES31.GL_RG16F:				return 4;
            case GLES31.GL_RGB16F:			return 6;
            case GLES31.GL_RGBA16F:			return 8;
            case GLES31.GL_R32F:				return 4;
            case GLES31.GL_RG32F:				return 8;
            case GLES31.GL_RGB32F:			return 12;
            case GLES31.GL_RGBA32F:			return 16;
            case GLES31.GL_R11F_G11F_B10F:	return 4;
            case GLES31.GL_RGB9_E5:			return 4; // TODO ?
            case GLES31.GL_R8I:				return 1;
            case GLES31.GL_R8UI:				return 1;
            case GLES31.GL_R16I:				return 2;
            case GLES31.GL_R16UI:				return 2;
            case GLES31.GL_R32I:				return 4;
            case GLES31.GL_R32UI:				return 4;
            case GLES31.GL_RG8I:				return 2;
            case GLES31.GL_RG8UI:				return 2;
            case GLES31.GL_RG16I:				return 4;
            case GLES31.GL_RG16UI:			return 4;
            case GLES31.GL_RG32I:				return 8;
            case GLES31.GL_RG32UI:			return 8;
            case GLES31.GL_RGB8I:				return 3;
            case GLES31.GL_RGB8UI:			return 3;
            case GLES31.GL_RGB16I:			return 6;
            case GLES31.GL_RGB16UI:			return 6;
            case GLES31.GL_RGB32I:			return 12;
            case GLES31.GL_RGB32UI:			return 12;

            case GLES31.GL_RGBA8I:			return 4;
            case GLES31.GL_RGBA8UI:			return 4;
            case GLES31.GL_RGBA16I:			return 8;
            case GLES31.GL_RGBA16UI:			return 8;
            case GLES31.GL_RGBA32I:			return 16;
            case GLES31.GL_RGBA32UI:			return 16;

            default:
                throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
        }
    }

    public static String getFormatName(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return "GL_R8";
            case GLES31.GL_R8_SNORM:		    return "GL_R8_SNORM";
            case GLES31.GL_RG8:				return "GL_RG8";
            case GLES31.GL_RG8_SNORM:			return "GL_RG8_SNORM";
            case GLES31.GL_RGB8:				return "GL_RGB8";
            case GLES31.GL_RGB8_SNORM:		return "GL_RGB8_SNORM";
            case GLES31.GL_RGBA4:				return "GL_RGBA4";  // TODO
            case GLES31.GL_RGB5_A1:			return "GL_RGB5_A1";  // TODO
            case GLES31.GL_RGBA8:				return "GL_RGBA8";
            case GLES31.GL_RGBA8_SNORM:		return "GL_RGBA8_SNORM";
            case GLES31.GL_RGB10_A2:			return "GL_RGB10_A2";
            case GLES31.GL_RGB10_A2UI:		return "GL_RGB10_A2UI";
            case GLES31.GL_SRGB8:				return "GL_SRGB8";
            case GLES31.GL_SRGB8_ALPHA8:		return "GL_SRGB8_ALPHA8";
            case GLES31.GL_R16F:				return "GL_R16F";
            case GLES31.GL_RG16F:				return "GL_RG16F";
            case GLES31.GL_RGB16F:			return "GL_RGB16F";
            case GLES31.GL_RGBA16F:			return "GL_RGBA16F";
            case GLES31.GL_R32F:				return "GL_R32F";
            case GLES31.GL_RG32F:				return "GL_RG32F";
            case GLES31.GL_RGB32F:			return "GL_RGB32F";
            case GLES31.GL_RGBA32F:			return "GL_RGBA32F";
            case GLES31.GL_R11F_G11F_B10F:	return "GL_R11F_G11F_B10F";
            case GLES31.GL_RGB9_E5:			return "GL_RGB9_E5";
            case GLES31.GL_R8I:				return "GL_R8I";
            case GLES31.GL_R8UI:				return "GL_R8UI";
            case GLES31.GL_R16I:				return "GL_R16I";
            case GLES31.GL_R16UI:				return "GL_R16UI";
            case GLES31.GL_R32I:				return "GL_R32I";
            case GLES31.GL_R32UI:				return "GL_R32UI";
            case GLES31.GL_RG8I:				return "GL_RG8I";
            case GLES31.GL_RG8UI:				return "GL_RG8UI";
            case GLES31.GL_RG16I:				return "GL_RG16I";
            case GLES31.GL_RG16UI:			return "GL_RG16UI";
            case GLES31.GL_RG32I:				return "GL_RG32I";
            case GLES31.GL_RG32UI:			return "GL_RG32UI";
            case GLES31.GL_RGB8I:				return "GL_RGB8I";
            case GLES31.GL_RGB8UI:			return "GL_RGB8UI";
            case GLES31.GL_RGB16I:			return "GL_RGB16I";
            case GLES31.GL_RGB16UI:			return "GL_RGB16UI";
            case GLES31.GL_RGB32I:			return "GL_RGB32I";
            case GLES31.GL_RGB32UI:			return "GL_RGB32UI";

            case GLES31.GL_RGBA8I:			return "GL_RGBA8I";
            case GLES31.GL_RGBA8UI:			return "GL_RGBA8UI";
            case GLES31.GL_RGBA16I:			return "GL_RGBA16I";
            case GLES31.GL_RGBA16UI:			return "GL_RGBA16UI";
            case GLES31.GL_RGBA32I:			return "GL_RGBA32I";
            case GLES31.GL_RGBA32UI:			return "GL_RGBA32UI";
            case GLES31.GL_DEPTH_COMPONENT16: return "GL_DEPTH_COMPONENT16";
            case GLES31.GL_DEPTH_COMPONENT24:	return "GL_DEPTH_COMPONENT24";
            case GLES31.GL_DEPTH_COMPONENT32F:
                return "GL_DEPTH_COMPONENT32F";
            case GLES31.GL_DEPTH24_STENCIL8:return "GL_DEPTH24_STENCIL8";
//		case GL12.GL_BGRA:				return "GL_BGRA8";
//		case GL12.GL_BGR:				return "";
            case GLES31.GL_RGBA             : return "GLRGBA";
            default:
                return "Unkown Format(0x" + Integer.toHexString(internalFormat) + ")";
        }
    }

    public static int measureFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return RED;
            case GLES31.GL_R8_SNORM:		    return RED;
            case GLES31.GL_RG8:					return RG;
            case GLES31.GL_RG8_SNORM:			return RG;
            case GLES31.GL_RGB8:				return RGB;
            case GLES31.GL_RGB8_SNORM:			return RGB;
            case GLES31.GL_RGBA4:				return RGBA;  // TODO
            case GLES31.GL_RGB5_A1:				return RGBA;  // TODO
            case GLES31.GL_RGBA8:				return RGBA;
            case GLES31.GL_RGBA8_SNORM:			return RGBA;
            case GLES31.GL_RGB10_A2:			return RGBA;
            case GLES31.GL_RGB10_A2UI:			return RGBA_INTEGER;
            case GLES31.GL_SRGB8:				return RGB;
            case GLES31.GL_SRGB8_ALPHA8:		return RGBA;
            case GLES31.GL_R16F:				return RED;
            case GLES31.GL_RG16F:				return RG;
            case GLES31.GL_RGB16F:				return RGB;
            case GLES31.GL_RGBA16F:				return RGBA;
            case GLES31.GL_R32F:				return RED;
            case GLES31.GL_RG32F:				return RG;
            case GLES31.GL_RGB32F:				return RGB;
            case GLES31.GL_RGBA32F:				return RGBA;
            case GLES31.GL_R11F_G11F_B10F:		return RGB;
            case GLES31.GL_RGB9_E5:				return RGB; // TODO ?
            case GLES31.GL_R8I:					return RED_INTEGER;
            case GLES31.GL_R8UI:				return RED_INTEGER;
            case GLES31.GL_R16I:				return RED_INTEGER;
            case GLES31.GL_R16UI:				return RED_INTEGER;
            case GLES31.GL_R32I:				return RED_INTEGER;
            case GLES31.GL_R32UI:				return RED_INTEGER;
            case GLES31.GL_RG8I:				return RG_INTEGER;
            case GLES31.GL_RG8UI:				return RG_INTEGER;
            case GLES31.GL_RG16I:				return RG_INTEGER;
            case GLES31.GL_RG16UI:				return RG_INTEGER;
            case GLES31.GL_RG32I:				return RG_INTEGER;
            case GLES31.GL_RG32UI:				return RG_INTEGER;
            case GLES31.GL_RGB8I:				return RGB_INTEGER;
            case GLES31.GL_RGB8UI:				return RGB_INTEGER;
            case GLES31.GL_RGB16I:				return RGB_INTEGER;
            case GLES31.GL_RGB16UI:				return RGB_INTEGER;
            case GLES31.GL_RGB32I:				return RGB_INTEGER;
            case GLES31.GL_RGB32UI:				return RGB_INTEGER;

            case GLES31.GL_RGBA8I:				return RGBA_INTEGER;
            case GLES31.GL_RGBA8UI:				return RGBA_INTEGER;
            case GLES31.GL_RGBA16I:				return RGBA_INTEGER;
            case GLES31.GL_RGBA16UI:			return RGBA_INTEGER;
            case GLES31.GL_RGBA32I:				return RGBA_INTEGER;
            case GLES31.GL_RGBA32UI:			return RGBA_INTEGER;
            case GLES31.GL_DEPTH_COMPONENT16:
            case GLES31.GL_DEPTH_COMPONENT24:
            case GLES31.GL_DEPTH_COMPONENT32F:
                return GLES31.GL_DEPTH_COMPONENT;
            case GLES31.GL_DEPTH24_STENCIL8:
            case GLES31.GL_DEPTH32F_STENCIL8:
                return GLES31.GL_DEPTH_STENCIL;
            case GLES31.GL_STENCIL_INDEX8:
                return GLES31.GL_STENCIL;
            default:
                throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
        }
    }

    public static String getImageFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return "r8";
            case GLES31.GL_R8_SNORM:		    return "r8_snorm";
            case GLES31.GL_RG8:				return "rg8";
            case GLES31.GL_RG8_SNORM:			return "rg8_snorm";
            case GLES31.GL_RGBA8:				return "rgba8";
            case GLES31.GL_RGBA8_SNORM:		return "rgba8_snorm";
            case GLES31.GL_RGB10_A2:			return "rgb10_a2";
            case GLES31.GL_RGB10_A2UI:		return "rgb10_a2ui";
            case GLES31.GL_R16F:				return "r16f";
            case GLES31.GL_RG16F:				return "rg16f";
            case GLES31.GL_RGBA16F:			return "rgba16f";
            case GLES31.GL_R32F:				return "r32f";
            case GLES31.GL_RG32F:				return "rg32f";
            case GLES31.GL_RGBA32F:			return "rgba32f";
            case GLES31.GL_R11F_G11F_B10F:	return "r11f_g11f_b10f";
            case GLES31.GL_R8I:				return "r8i";
            case GLES31.GL_R8UI:				return "r8ui";
            case GLES31.GL_R16I:				return "r16i";
            case GLES31.GL_R16UI:				return "r16ui";
            case GLES31.GL_R32I:				return "r32i";
            case GLES31.GL_R32UI:				return "r32ui";
            case GLES31.GL_RG8I:				return "rg8i";
            case GLES31.GL_RG8UI:				return "rg8ui";
            case GLES31.GL_RG16I:				return "rg16i";
            case GLES31.GL_RG16UI:			return "rg16ui";
            case GLES31.GL_RG32I:				return "rg32i";
            case GLES31.GL_RG32UI:			return "rg32ui";
            case GLES31.GL_RGBA8I:			return "rgba8i";
            case GLES31.GL_RGBA8UI:			return "rgba8ui";
            case GLES31.GL_RGBA16I:			return "rgba16i";
            case GLES31.GL_RGBA16UI:			return "rgba16ui";
            case GLES31.GL_RGBA32I:			return "rgba32i";
            case GLES31.GL_RGBA32UI:			return "rgba32ui";

//            case GLES31.GL_RGB9_E5:			return "GL_RGB9_E5";
//            case GLES31.GL_RGB8I:				return "GL_RGB8I";
//            case GLES31.GL_RGB8UI:			return "GL_RGB8UI";
//            case GLES31.GL_RGB16I:			return "GL_RGB16I";
//            case GLES31.GL_RGB16UI:			return "GL_RGB16UI";
//            case GLES31.GL_RGB32I:			return "GL_RGB32I";
//            case GLES31.GL_RGB32UI:			return "GL_RGB32UI";
//            case GLES31.GL_RGB32F:			return "GL_RGB32F";
//            case GLES31.GL_RGB16F:			return "GL_RGB16F";
//            case GLES31.GL_SRGB8:				return "GL_SRGB8";
//            case GLES31.GL_SRGB8_ALPHA8:		return "GL_SRGB8_ALPHA8";
//            case GLES31.GL_RGBA4:				return "GL_RGBA4";  // TODO
//            case GLES31.GL_RGB5_A1:			return "GL_RGB5_A1";  // TODO
//            case GLES31.GL_RGB8:				return "GL_RGB8";
//            case GLES31.GL_RGB8_SNORM:		return "GL_RGB8_SNORM";
//            case GLES31.GL_DEPTH_COMPONENT16: return "GL_DEPTH_COMPONENT16";
//            case GLES31.GL_DEPTH_COMPONENT24:	return "GL_DEPTH_COMPONENT24";
//            case GLES31.GL_DEPTH_COMPONENT32F:
//                return "GL_DEPTH_COMPONENT32F";
//            case GLES31.GL_DEPTH24_STENCIL8:return "GL_DEPTH24_STENCIL8";
////		case GL12.GL_BGRA:				return "GL_BGRA8";
////		case GL12.GL_BGR:				return "";
//            case GLES31.GL_RGBA             : return "GLRGBA";
            default:
                return "Unsupported Format: " + getFormatName(internalFormat);
        }
    }

    public static int getFormatChannels(int internalFormat){
//		private static final int RED = GLES31.GL_RED;
//		private static final int RG  = GLES31.GL_RG;
//		private static final int RGB = GLES31.GL_RGB;
//		private static final int RGBA= GLES31.GL_RGBA;
//
//		private static final int RED_INTEGER = GLES31.GL_RED_INTEGER;
//		private static final int RG_INTEGER = GLES31.GL_RG_INTEGER;
//		private static final int RGB_INTEGER = GLES31.GL_RGB_INTEGER;
//		private static final int RGBA_INTEGER = GLES31.GL_RGBA_INTEGER;
        int format = measureFormat(internalFormat);
        switch (format){
            case RED:
            case RED_INTEGER:
            case GLES31.GL_DEPTH_COMPONENT:
            case GLES31.GL_STENCIL:
                return 1;
            case RG:
            case RG_INTEGER:
            case GLES31.GL_DEPTH_STENCIL:
                return 2;
            case RGB:
            case RGB_INTEGER:
                return 3;
            case RGBA:
            case RGBA_INTEGER:
                return 4;
            case GLES31.GL_NONE:
            default:
            {
                throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
            }
        }
    }

    public static boolean isDepthFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_DEPTH_COMPONENT16:
            case GLES31.GL_DEPTH_COMPONENT24:
            case GLES31.GL_DEPTH_COMPONENT32F:
            case GLES31.GL_DEPTH24_STENCIL8:
            case GLES31.GL_DEPTH32F_STENCIL8:
                return true;
            default:
                return false;
        }
    }

    public static boolean isStencilFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_DEPTH24_STENCIL8:
            case GLES31.GL_DEPTH32F_STENCIL8:
            case GLES31.GL_STENCIL_INDEX8:
                return true;
            default:
                return false;
        }
    }

    public static boolean isColorFormat(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:
            case GLES31.GL_R8_SNORM:
            case GLES31.GL_RG8:
            case GLES31.GL_RG8_SNORM:
            case GLES31.GL_RGB8:
            case GLES31.GL_RGB8_SNORM:
            case GLES31.GL_RGBA4:
            case GLES31.GL_RGB5_A1:
            case GLES31.GL_RGBA8:
            case GLES31.GL_RGBA8_SNORM:
            case GLES31.GL_RGB10_A2:
            case GLES31.GL_RGB10_A2UI:
            case GLES31.GL_SRGB8:
            case GLES31.GL_SRGB8_ALPHA8:
            case GLES31.GL_R16F:
            case GLES31.GL_RG16F:
            case GLES31.GL_RGB16F:
            case GLES31.GL_RGBA16F:
            case GLES31.GL_R32F:
            case GLES31.GL_RG32F:
            case GLES31.GL_RGB32F:
            case GLES31.GL_RGBA32F:
            case GLES31.GL_R11F_G11F_B10F:
            case GLES31.GL_RGB9_E5:
            case GLES31.GL_R8I:
            case GLES31.GL_R8UI:
            case GLES31.GL_R16I:
            case GLES31.GL_R16UI:
            case GLES31.GL_R32I:
            case GLES31.GL_R32UI:
            case GLES31.GL_RG8I:
            case GLES31.GL_RG8UI:
            case GLES31.GL_RG16I:
            case GLES31.GL_RG16UI:
            case GLES31.GL_RG32I:
            case GLES31.GL_RG32UI:
            case GLES31.GL_RGB8I:
            case GLES31.GL_RGB8UI:
            case GLES31.GL_RGB16I:
            case GLES31.GL_RGB16UI:
            case GLES31.GL_RGB32I:
            case GLES31.GL_RGB32UI:

            case GLES31.GL_RGBA8I:
            case GLES31.GL_RGBA8UI:
            case GLES31.GL_RGBA16I:
            case GLES31.GL_RGBA16UI:
            case GLES31.GL_RGBA32I:
            case GLES31.GL_RGBA32UI:
                return true;
            default:
                return false;
        }
    }

    public static int measureDataType(int internalFormat){
        switch (internalFormat) {
            case GLES31.GL_R8:  				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_R8_SNORM:		    return GLES31.GL_BYTE;
            case GLES31.GL_RG8:				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RG8_SNORM:			return GLES31.GL_BYTE;
            case GLES31.GL_RGB8:				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RGB8_SNORM:		return GLES31.GL_BYTE;
            case GLES31.GL_RGBA4:				return GLES31.GL_UNSIGNED_SHORT_4_4_4_4;  // TODO
            case GLES31.GL_RGB5_A1:			return GLES31.GL_UNSIGNED_SHORT_5_5_5_1;  // TODO
            case GLES31.GL_RGBA8:				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RGBA8_SNORM:		return GLES31.GL_BYTE;
//            case GLES31.GL_RGB10_A2:			return GLES31.GL_UNSIGNED_INT_10_10_10_2;
//            case GLES31.GL_RGB10_A2UI:		return GLES31.GL_UNSIGNED_INT_10_10_10_2; // TODO
            case GLES31.GL_SRGB8:				return GLES31.GL_BYTE;
            case GLES31.GL_SRGB8_ALPHA8:		return GLES31.GL_BYTE;
            case GLES31.GL_R16F:				return GLES31.GL_HALF_FLOAT;
            case GLES31.GL_RG16F:				return GLES31.GL_HALF_FLOAT;
            case GLES31.GL_RGB16F:			return GLES31.GL_HALF_FLOAT;
            case GLES31.GL_RGBA16F:			return GLES31.GL_HALF_FLOAT;
            case GLES31.GL_R32F:				return GLES31.GL_FLOAT;
            case GLES31.GL_RG32F:				return GLES31.GL_FLOAT;
            case GLES31.GL_RGB32F:			return GLES31.GL_FLOAT;
            case GLES31.GL_RGBA32F:			return GLES31.GL_FLOAT;
            case GLES31.GL_R11F_G11F_B10F:	return GLES31.GL_UNSIGNED_INT_10F_11F_11F_REV;
            case GLES31.GL_RGB9_E5:			return GLES31.GL_UNSIGNED_INT_5_9_9_9_REV; // TODO ?
            case GLES31.GL_R8I:				return GLES31.GL_BYTE;
            case GLES31.GL_R8UI:				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_R16I:				return GLES31.GL_SHORT;
            case GLES31.GL_R16UI:				return GLES31.GL_UNSIGNED_SHORT;
            case GLES31.GL_R32I:				return GLES31.GL_INT;
            case GLES31.GL_R32UI:				return GLES31.GL_UNSIGNED_INT;
            case GLES31.GL_RG8I:				return GLES31.GL_BYTE;
            case GLES31.GL_RG8UI:				return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RG16I:				return GLES31.GL_SHORT;
            case GLES31.GL_RG16UI:			return GLES31.GL_UNSIGNED_SHORT;
            case GLES31.GL_RG32I:				return GLES31.GL_INT;
            case GLES31.GL_RG32UI:			return GLES31.GL_UNSIGNED_INT;
            case GLES31.GL_RGB8I:				return GLES31.GL_BYTE;
            case GLES31.GL_RGB8UI:			return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RGB16I:			return GLES31.GL_SHORT;
            case GLES31.GL_RGB16UI:			return GLES31.GL_UNSIGNED_SHORT;
            case GLES31.GL_RGB32I:			return GLES31.GL_INT;
            case GLES31.GL_RGB32UI:			return GLES31.GL_UNSIGNED_INT;

            case GLES31.GL_RGBA8I:			return GLES31.GL_BYTE;
            case GLES31.GL_RGBA8UI:			return GLES31.GL_UNSIGNED_BYTE;
            case GLES31.GL_RGBA16I:			return GLES31.GL_SHORT;
            case GLES31.GL_RGBA16UI:			return GLES31.GL_UNSIGNED_SHORT;
            case GLES31.GL_RGBA32I:			return GLES31.GL_INT;
            case GLES31.GL_RGBA32UI:			return GLES31.GL_UNSIGNED_INT;
            case GLES31.GL_DEPTH_COMPONENT16: return GLES31.GL_UNSIGNED_SHORT;
            case GLES31.GL_DEPTH_COMPONENT24: return GLES31.GL_UNSIGNED_INT;
            case GLES31.GL_DEPTH24_STENCIL8:  return GLES31.GL_UNSIGNED_INT_24_8;
            case GLES31.GL_DEPTH_COMPONENT32F:return GLES31.GL_FLOAT;
            case GLES31.GL_DEPTH32F_STENCIL8: return GLES31.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;

            default:
                throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
        }
    }

    public static TextureDesc getTexParameters(int target, int textureID){

        GLES20.glBindTexture(target, textureID);

        TextureDesc desc = new TextureDesc();
        desc.target = target;
        desc.depthStencilTextureMode = glGetTexParameteri(target, GLES31.GL_DEPTH_STENCIL_TEXTURE_MODE);
//        desc.lodBias                 = glGetTexParameterf(target, GLES31.GL_TEXTURE_LOD_BIAS);
        desc.magFilter               = glGetTexParameteri(target, GLES31.GL_TEXTURE_MAG_FILTER);
        desc.minFilter               = glGetTexParameteri(target, GLES31.GL_TEXTURE_MIN_FILTER);
        desc.minLod                  = glGetTexParameterf(target, GLES31.GL_TEXTURE_MIN_LOD);
        desc.maxLod                  = glGetTexParameterf(target, GLES31.GL_TEXTURE_MAX_LOD);
        desc.baseLevel               = glGetTexParameteri(target, GLES31.GL_TEXTURE_BASE_LEVEL);
        desc.maxLevel                = glGetTexParameteri(target, GLES31.GL_TEXTURE_MAX_LEVEL);
        desc.swizzleR                = glGetTexParameteri(target, GLES31.GL_TEXTURE_SWIZZLE_R);
        desc.swizzleG                = glGetTexParameteri(target, GLES31.GL_TEXTURE_SWIZZLE_G);
        desc.swizzleB                = glGetTexParameteri(target, GLES31.GL_TEXTURE_SWIZZLE_B);
        desc.swizzleA                = glGetTexParameteri(target, GLES31.GL_TEXTURE_SWIZZLE_A);
        desc.wrapS                   = glGetTexParameteri(target, GLES31.GL_TEXTURE_WRAP_S);
        desc.wrapT                   = glGetTexParameteri(target, GLES31.GL_TEXTURE_WRAP_T);
        desc.wrapR                   = glGetTexParameteri(target, GLES31.GL_TEXTURE_WRAP_R);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        GLES31.glGetTexParameterfv(target, GLenum.GL_TEXTURE_BORDER_COLOR, buffer);
        buffer.get(desc.borderColor);
        desc.compareMode             = glGetTexParameteri(target, GLES31.GL_TEXTURE_COMPARE_MODE);
        desc.compareFunc             = glGetTexParameteri(target, GLES31.GL_TEXTURE_COMPARE_FUNC);
        desc.immutableFormat         = glGetTexParameteri(target, GLES31.GL_TEXTURE_IMMUTABLE_FORMAT) != 0;
        desc.imageFormatCompatibilityType = glGetTexParameteri(target, GLES31.GL_IMAGE_FORMAT_COMPATIBILITY_TYPE);
        if(desc.immutableFormat){
//            desc.textureViewMinLevel     = glGetTexParameteri(target, GLES31.GL_TEXTURE_VIEW_MIN_LEVEL);
//            desc.textureViewNumLevels    = glGetTexParameteri(target, GLES31.GL_TEXTURE_VIEW_NUM_LEVELS);
//            desc.textureViewMinLayer     = glGetTexParameteri(target, GLES31.GL_TEXTURE_VIEW_MIN_LAYER);
//            desc.textureViewNumLayers    = glGetTexParameteri(target, GLES31.GL_TEXTURE_VIEW_NUM_LAYERS);
            desc.immutableLayer          = glGetTexParameteri(target, GLES31.GL_TEXTURE_IMMUTABLE_LEVELS);
        }

        List<TextureLevelDesc> levelDescs = new ArrayList<TextureLevelDesc>();

        int level = 0;
        while (true) {
            TextureLevelDesc levelDesc = new TextureLevelDesc();
            levelDesc.width  = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_WIDTH);
            if(levelDesc.width == 0){
//				System.out.println("Level" + level + ": width = " + levelDesc.width);
                break;
            }

            levelDesc.height = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_HEIGHT);
            levelDesc.depth  = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_DEPTH);
            levelDesc.internalFormat = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_INTERNAL_FORMAT);

            levelDesc.redType   = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_RED_TYPE);
            levelDesc.greenType = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_GREEN_TYPE);
            levelDesc.blueType  = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_BLUE_TYPE);
            levelDesc.alphaType = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_ALPHA_TYPE);
            levelDesc.depthType = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_DEPTH_TYPE);

            levelDesc.redSize   = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_RED_SIZE);
            levelDesc.greenSize = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_GREEN_SIZE);
            levelDesc.blueSize  = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_BLUE_SIZE);
            levelDesc.alphaSize = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_ALPHA_SIZE);
            levelDesc.depthSize = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_DEPTH_SIZE);
            levelDesc.stencilSize = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_STENCIL_SIZE);
            levelDesc.samples   = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_SAMPLES);

            levelDesc.compressed = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_COMPRESSED) != 0;
            if(levelDesc.compressed){
//                levelDesc.compressedImageSize = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_COMPRESSED_IMAGE_SIZE);
            }

            if(target == GLenum.GL_TEXTURE_BUFFER){
                levelDesc.bufferOffset = glGetTexLevelParameteri(target, level, GLenum.GL_TEXTURE_BUFFER_OFFSET);
                levelDesc.bufferSize   = glGetTexLevelParameteri(target, level, GLenum.GL_TEXTURE_BUFFER_SIZE);

                levelDescs.add(levelDesc);
                break;
            }

            levelDescs.add(levelDesc);
            if(levelDesc.width == 1 && levelDesc.height == 1){
                break;
            }

            level++;
        }

        desc.levelDescs = levelDescs.toArray(new TextureLevelDesc[levelDescs.size()]);
        return desc;
    }

    public static String getCompareModeName(int mode){
        switch (mode) {
            case GLES31.GL_ALWAYS:  return "GL_ALWAYS";
            case GLES31.GL_NEVER:  return "GL_NEVER";
            case GLES31.GL_LESS:  return "GL_LESS";
            case GLES31.GL_LEQUAL:  return "GL_LEQUAL";
            case GLES31.GL_GREATER:  return "GL_GREATER";
            case GLES31.GL_GEQUAL:  return "GL_GEQUAL";
            case GLES31.GL_NOTEQUAL:  return "GL_NOTEQUAL";

            default:
                return "Unkown CompareMode(0x" + Integer.toHexString(mode) + ")";
        }
    }

    public static Texture3D createTexture3D(int target, int textureID){
        return createTexture3D(target, textureID, null);
    }

    public static Texture3D createTexture3D(int target, int textureID, Texture3D out){
        if(!GLES20.glIsTexture(textureID))
            return null;

        if(target != GLES30.GL_TEXTURE_3D)
            throw new IllegalArgumentException("Invalid target: " + getTextureTargetName(target));

        GLES20.glBindTexture(target, textureID);
        Texture3D result = out != null ? out : new Texture3D();
        result.width  = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_WIDTH);
        result.height = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_HEIGHT);
        result.depth    = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_DEPTH);
        result.format = glGetTexLevelParameteri(target, 0, GLES31.GL_TEXTURE_INTERNAL_FORMAT);
        result.target = target;
        result.textureID = textureID;

        boolean immutableFormat         = glGetTexParameteri(target, GLES31.GL_TEXTURE_IMMUTABLE_FORMAT) != 0;
        /*if(immutableFormat){
            result.mipLevels    = gl.glGetTexParameteri(target, GLES31.GL_TEXTURE_VIEW_NUM_LEVELS);
            if(result.mipLevels == 0){
                result.mipLevels = gl.glGetTexParameteri(target, GLES31.GL_TEXTURE_IMMUTABLE_LEVELS);
            }
        }else*/{

            if(result.width > 0){
                int level = 1;
                while(true){
                    int width = glGetTexLevelParameteri(target, level, GLES31.GL_TEXTURE_WIDTH);
                    if(width == 0){
                        break;
                    }

                    level++;
                }

                result.mipLevels = level;
            }
        }
        return result;
    }

    public static Texture3D createTexture3D(Texture3DDesc textureDesc, TextureDataDesc dataDesc){
        return createTexture3D(textureDesc, dataDesc, null);
    }

    @SuppressWarnings("unchecked")
    public static Texture3D createTexture3D(Texture3DDesc textureDesc, TextureDataDesc dataDesc, Texture3D out){
        int textureID;
        int target = GLES31.GL_TEXTURE_3D;
        int format;
        boolean isCompressed = false;
        final boolean isDSA = false; // = GL.getCapabilities().GL_ARB_direct_state_access;
        int mipLevels = Math.max(1, textureDesc.mipLevels);

//        GLFuncProvider gl = GLFuncProviderFactory.getGLFuncProvider();
//        GLAPIVersion version = gl.getGLAPIVersion();
//        isDSA = version.major >= 4 && version.minor >= 5; /*gl.isSupportExt("GL_ARB_direct_state_access")*/;  // We only use the standrad profile.

        // measure texture internal format
        if(dataDesc != null){
            isCompressed = Arrays.binarySearch(compressed_formats, dataDesc.format) >= 0;
            if(isCompressed){
                format = dataDesc.format;
            }else{
                format = textureDesc.format;
            }
        }else{
            format = textureDesc.format;
        }

        {
            boolean allocateStorage = false;

            // 1. Generate texture ID
            textureID = GLES.glGenTextures();

            // 2. Allocate storage for Texture Object
//			final GLCapabilities cap = GL.getCapabilities();
            final boolean textureStorage = true; // version.ES && version.major >= 3 || gl.isSupportExt("GL_ARB_texture_storage");
            GLES30.glBindTexture(target, textureID);
            if(!isCompressed && textureStorage){
                GLES30.glTexStorage3D(target, mipLevels, format, textureDesc.width, textureDesc.height, textureDesc.depth);
                allocateStorage = true;
            }

            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);

            // 3. Fill the texture Data�� Ignore the multisample texture.
            if(dataDesc != null){
                int width = textureDesc.width;
                int height = textureDesc.height;
                int depth = textureDesc.depth;
                enablePixelStore(dataDesc);

                int dataFormat = measureFormat(format);
                int type = GLES30.GL_UNSIGNED_BYTE;
                Object pixelData = null;
                if(dataDesc != null){
                    dataFormat = dataDesc.format;
                    type = dataDesc.type;
                    pixelData = dataDesc.data;
                }

                if(mipLevels > 1){
                    int loop = mipLevels;
                    List<Object> mipData = null;
                    if(dataDesc != null){
                        mipData = (List<Object>)dataDesc.data;
                        loop = Math.min(mipData.size(), mipLevels);
                    }

                    for(int i = 0; i < loop; i++){
                        Object mipmapData = null;
                        if(mipData != null){
                            mipmapData = mipData.get(i);
                        }

                        if(isCompressed){
                            compressedTexImage3D(target, width, height, depth, i, dataFormat, dataDesc.type, dataDesc.imageSize, mipmapData);
                        }else{
                            if(allocateStorage){
                                subTexImage3D(target, width, height, depth, i, dataFormat, type, mipmapData);
                            }else{
                                texImage3D(target, format, width, height, depth, i, dataFormat, type, mipmapData);
                            }
                        }

                        width = Math.max(1, width >> 1);
                        height = Math.max(1, height >> 1);
                        depth = Math.max(1, depth >> 1);
                    }

                    GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
                }else{
                    if(isCompressed){
                        compressedTexImage3D(target, width, height, depth, 0, dataDesc.format, dataDesc.type, dataDesc.imageSize, dataDesc.data);
                    }else{
                        if(allocateStorage){
                            subTexImage3D(target, width, height, depth, 0, dataFormat, type, pixelData);
                        }else{
                            texImage3D(target, format, width, height, depth, 0, dataFormat, type, pixelData);
                        }
                    }
                }

                disablePixelStore(dataDesc);
            }


            GLES30.glBindTexture(target, 0);  // unbind Texture
        }

        GLES.checkGLError();
        Texture3D texture = out!=null ? out : new Texture3D();
        texture.format = format;
        texture.height = textureDesc.height;
        texture.width  = textureDesc.width;
        texture.depth  = textureDesc.depth;
        texture.target = target;
        texture.textureID = textureID;
        texture.mipLevels = mipLevels;
        return texture;
    }
}
