package jet.learning.opengl.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FileUtils {
    public static FileLoader g_IntenalFileLoader = FileLoader.g_DefaultFileLoader;

    private FileUtils(){}

    public static StringBuilder loadText(InputStream in, boolean igoreComment, String charset, LineFilter filter) throws IOException {
        return loadText(new InputStreamReader(in, charset == null ? Charset.defaultCharset() : Charset.forName(charset)), igoreComment, filter);
    }

    public interface LineFilter {
        String filter(String line);
    }

    public static void setIntenalFileLoader(FileLoader fileLoader){
        if(fileLoader == null)
            throw new NullPointerException("fileLoader is null!");

        g_IntenalFileLoader = fileLoader;
    }

    public static StringBuilder loadText(Reader in, boolean igoreComment, LineFilter filter)throws IOException{
        StringBuilder out;
        if(igoreComment){
            StringBuilder buf = new StringBuilder(64);
            CommentFilter cf = new CommentFilter(in);
            String line;
            while((line = cf.nextLine()) != null){
                if(filter != null)
                    line = filter.filter(line);

                buf.append(line).append('\n');
            }

            out = buf;
        }else{
            out = loadText(in);
        }
        return out;
    }

    public static InputStream open(String file) throws IOException {
        return g_IntenalFileLoader.open(file);
    }

    public static ByteBuffer loadNative(String filepath) throws IOException{
        try(InputStream inputStream = g_IntenalFileLoader.open(filepath)){
            ByteBuffer buf = null;

            try {
                buf = ByteBuffer.allocateDirect(inputStream.available()).order(ByteOrder.nativeOrder());
            }catch (OutOfMemoryError e){
                System.out.printf("The required momery(%.2fMB) is exceed the avaiable heap memory(%.2fMB).\n", inputStream.available()/(1024.f * 1024), Runtime.getRuntime().freeMemory()/(1024.f * 1024));
                e.printStackTrace();
            }

            if(inputStream instanceof FileInputStream){
                FileChannel in = ((FileInputStream)inputStream).getChannel();
                in.read(buf);
                in.close();
                buf.flip();
            }else{
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                buf.put(bytes).flip();
            }

            return buf;
        }
    }

    public static byte[] loadBytes(String file) throws  IOException{
        try(InputStream inputStream = open(file)){
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return bytes;
        }
    }

    public static StringBuilder loadText(String filename) throws IOException{
        return loadText(new FileReader(filename), false, null);
    }

    public static StringBuilder loadText(Reader in) throws IOException{
        String line;
        BufferedReader reader;
        if(in instanceof BufferedReader)
            reader = (BufferedReader)in;
        else
            reader = new BufferedReader(in);

        StringBuilder sb = new StringBuilder(64);
        while((line = reader.readLine()) != null)
            sb.append(line).append('\n');

        sb.setLength(sb.length() - 1);
        return sb;
    }

    /**
     * Load the text from the classpath, the 'filename' have in the form of aaa/bbb/ccc.ext
     */
    public static final StringBuilder loadTextFromClassPath(String filename){
        InputStream input = ClassLoader.getSystemResourceAsStream(filename);
        if(input == null) return null;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuilder sb = new StringBuilder(Math.max(1, input.available()));
            String s;

            while ((s = in.readLine()) != null)
                sb.append(s).append('\n');
            in.close();
            input.close();
            return sb;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final String getParent(String path){
        if(path == null || path.length() == 0)
            throw new NullPointerException("path is empty");

        int index;
        char c;  // record the last charactor
        // eat up the charactors '/' and '\'
        for(index = path.length() -1; index >=0; index--){
            c = path.charAt(index);
            if(c != '/' && c != '\\'){
                break;
            }
        }

        // eat up all the letters and other stuff
        for(; index >=0; index--){
            c = path.charAt(index);
            if(c == '/' || c == '\\'){
                break;
            }
        }

        // eat up the charactors '/' and '\'
        for(; index >=0; index--){
            c = path.charAt(index);
            if(c != '/' && c != '\\'){
                break;
            }
        }

        return path.substring(0, index+1);
    }

    public static String getFile(String path){
        if(path == null || path.length() == 0)
            throw new NullPointerException("path is empty");

        char c = path.charAt(path.length() - 1);
        if(c == '/' || c == '\\'){
            return "";
        }

        int index = path.length() - 1;
        // eat up all the letters and other stuff
        for(; index >=0; index--){
            c = path.charAt(index);
            if(c == '/' || c == '\\'){
                break;
            }
        }

        return path.substring(index + 1);
    }

    public static String removePathDots(String path){
        if(path == null || path.length() == 0)
            return path;

        if(!path.contains(".."))
            return path;

        ArrayList<String> tokens = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(path, "/\\");
        while (tokenizer.hasMoreElements()){
            String token = tokenizer.nextToken();
            if(token.equals("..")){
                if(tokens.isEmpty())
                    throw new IllegalStateException("may be invalid path: " + path);

                tokens.remove(tokens.size() - 1);
            }else{
                tokens.add(token);
            }
        }

        StringBuilder sb = new StringBuilder(path.length());
        for(String t : tokens){
            sb.append(t).append('/');
        }

        sb.setLength(sb.length() - 1); // remove last '/'
        return sb.toString();
    }
}
