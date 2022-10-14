package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.NvAssetLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileLoader {

    public static FileLoader g_DefaultFileLoader = new FileLoader() {
        @Override
        public InputStream open(String filename) throws IOException {
            return NvAssetLoader.openInputStream(filename);
        }

        @Override
        public String getCanonicalPath(String file) throws IOException {
//            return new File(file).getCanonicalPath();
           return file;
        }

        @Override
        public boolean exists(String file) {
//            return new File(file).exists();
            InputStream in = null;
            try {
                in= NvAssetLoader.getAssetManager().open(file);
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        public String resolvePath(String file) {
            return file;
        }
    };

    public InputStream open(String file) throws IOException;
    public String getCanonicalPath(String file) throws IOException;
    public boolean exists(String file);
    public String resolvePath(String file);
}