package jet.learning.opengl.common;

import java.util.List;

public class LoaderParameters {

	public String filepath;
	public boolean classFile;
	public boolean includeDefaultHeader = false;
	public List<FileDesc> includeFiles;
	
	public boolean igoreComment = true;
	public String charset = "utf-8";
	public FileLoader fileLoader = null;

	public static class FileDesc{
		public final String filepath;
		public final boolean isClassPath;

		FileDesc(String filepath, boolean isClassPath){
			this.filepath = filepath;
			this.isClassPath = isClassPath;
		}
	}

}
