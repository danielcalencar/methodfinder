package defectpred.allcommits;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static defectpred.util.IOUtil.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class GetAllCommits {

	private String project;
	private String separator = File.separator;

	public GetAllCommits(String project){
		this.project = project;
	}

	public static void main(String[] args) throws Exception {
		String input = args[0];
		GetAllCommits gac = new GetAllCommits(input);
		String result = gac.loadRevisionsAndFiles();
		gac.writeFile(result);
	}

	public String loadRevisionsAndFiles() {
		StringBuilder resultFile = new StringBuilder("revision,path\n");
		File workingDir = new File("C:\\repos\\" + this.project + "\\");//the name of the project is the name of the git repository
		InputStream outputCmd = null;
		InputStream errorCmd = null;
		OutputStream os = null;

		try{
			ProcessBuilder pb = new ProcessBuilder("git", "--no-pager",
					"log",
					"--pretty=format: %H");
			pb.redirectErrorStream(true);
			pb.directory(workingDir);
			Process process = pb.start();
			//int exitCode = process.waitFor();
			String result = readOutput(process.getInputStream());
			System.out.println("Result: " + result);
			String[] revisions = result.split(System.getProperty("line.separator"));
			System.out.println("printin revisions per line:");
			for(String line : revisions){
				ProcessBuilder pbFiles = new ProcessBuilder("git",
						"diff-tree",
						"--no-commit-id",
						"--name-only",
						"-r",
						line.trim());
				pbFiles.redirectErrorStream(true);
				pbFiles.directory(workingDir);
				Process pFiles = pbFiles.start();
				String[] files = readOutput(pFiles.getInputStream()).split(System.getProperty("line.separator")); 
				for(String file : files){
					String[] tokens = file.split("\\.");
					String extension = tokens[tokens.length-1];
					if(extension.equals("java")){
						resultFile.append(line + "," + file + "\n");
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		return resultFile.toString();
	}

	public void writeFile(String result) throws Exception {
		//now we write the output into a file
		File commitsFile = new File("allcommits" + separator + project + "-commits.csv"); 
		if(!commitsFile.exists()){
			commitsFile.createNewFile();
		}
		Path pathToWrite = Paths.get(commitsFile.getPath());
		Files.writeString(pathToWrite, result, StandardCharsets.UTF_8);
	}
}
