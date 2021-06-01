package defectpred;

import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import defectpred.model.PathRevision;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import static defectpred.util.IOUtil.*;

public class CollectSourceFiles {

	private String separator = File.separator;
	private List<PathRevision> paths;
	private String project;

	public CollectSourceFiles(List<PathRevision> paths, String project) {
		this.paths = paths;
		this.project = project;
	}

	public void loadFiles() throws Exception {
		File workingDir = new File("C:\\repos\\" + this.project + "\\");//the name of the project is the name of the git repository
		InputStream outputCmd = null;
		InputStream errorCmd = null;
		OutputStream os = null;
		for(PathRevision path : paths){
			try{
				String sourcePath = path.getPath();
				String revision = path.getRevision();
				String revPath = revision + ":" + sourcePath;
				System.out.println("revPath: " + revPath);
				ProcessBuilder pb = new ProcessBuilder("git",
						"--no-pager",
						"show",
						revPath);
				pb.redirectErrorStream(true);
				pb.directory(workingDir);
				Process process = pb.start();
				//int exitCode = process.waitFor();
				String result = readOutput(process.getInputStream());
				System.out.println("Result: " + result);
				//now we write the output into a file
				File sourceFile = new File("sourcefiles" + separator + revision + "_" + path.getFileName()); 
				if(!sourceFile.exists()){
					sourceFile.createNewFile();
				}
				Path pathToWrite = Paths.get(sourceFile.getPath());
				Files.writeString(pathToWrite, result, StandardCharsets.UTF_8);
			} finally {
				//outputCmd.close();
			}
		}
	}
}
