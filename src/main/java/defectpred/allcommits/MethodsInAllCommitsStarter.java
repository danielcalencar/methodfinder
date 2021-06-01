package defectpred.allcommits;

import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;

import static defectpred.util.IOUtil.*;

import java.util.List;
import java.util.Properties;

import defectpred.model.PathRevision;

public class MethodsInAllCommitsStarter {

	private static String separator = File.separator;
	private static String project;
	private static PathRevision curPathRev;

	public static void main(String[] args) throws Exception {
		if(args.length == 0){
			throw new IllegalArgumentException("please provide the project you are interested in");
		}
		project = args[0];

		List<PathRevision> pathRevisions = loadCommits();
		loadSourceFiles(pathRevisions);
		loadMethods(pathRevisions);
		deleteSourceFiles();
	}

	//{{{ loadCommits();
	private static List<PathRevision> loadCommits() throws Exception {
		List<PathRevision> pathRevisions = new ArrayList<PathRevision>();

		File commitsFile = new File("allcommits" + separator + project + "-commits.csv");
		BufferedReader br = new BufferedReader(new FileReader(commitsFile));

		boolean header = true;
		while(br.ready()){
			String line = br.readLine();
			if(header){
				header = false;
				continue;
			}
			String[] tokens = line.split(",");
			String revision = tokens[0];
			String path = tokens[1];
			PathRevision pr = new PathRevision();
			pr.setRevision(revision);
			pr.setPath(path);
			pathRevisions.add(pr);
		}
		return pathRevisions;
	}
	//}}}
	
	//{{{ loadSourceFiles()
	private static void loadSourceFiles(List<PathRevision> paths) throws Exception {
		File workingDir = new File("C:\\repos\\" + project + "\\");//the name of the project is the name of the git repository
		InputStream outputCmd = null;
		InputStream errorCmd = null;
		OutputStream os = null;
		System.out.println("working dir: " + workingDir.getPath());
		for(PathRevision path : paths){
			try{
				String sourcePath = path.getPath();
				String revision = path.getRevision();
				String revPath = (revision + ":" + sourcePath).trim();
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
				File sourceFile = new File("sourcefiles2" + separator + revision + "_" + path.retrieveFileName()); 
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
	//}}}
	
	//{{{ loadMethods()
	private static void loadMethods(List<PathRevision> prs) throws Exception {
		StringBuilder sb = new StringBuilder("project,revision,path.method\n");
		for(PathRevision pathRev : prs){
			System.out.println("loading file: " + pathRev.retrieveFileName());
			CompilationUnit cu = null;
			try{
				cu = StaticJavaParser.parse(new File("sourcefiles2" + 
							separator + 
							pathRev.getRevision() + 
							"_" + 
							pathRev.retrieveFileName()));
			} catch(Exception e){
				System.out.println("problem with file: " + pathRev.retrieveFileName());
				continue;
			}
			curPathRev = pathRev;
			VoidVisitor<?> methodNameVisitor = new MethodNameLoader();
			methodNameVisitor.visit(cu, null);
			if(curPathRev.getMethods().size() < 1){
				continue;
			}
			sb.append(project + ",");
			sb.append(curPathRev.getRevision() + ",");
			sb.append(curPathRev.getPath() + "#");

			// no need to report if there's no method

			for(String methodName : pathRev.getMethods()){
				sb.append(methodName + ";");
			}
			sb.append("\n");
		}

		//now let's write the outputFile;
		File outputFile = new File("output" + separator + project + "_allcommits-output.csv"); 
		if(!outputFile.exists()){
			outputFile.createNewFile();
		}
		Path outputPath = Paths.get(outputFile.getPath());
		Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
	}
	//}}}
	

	private static class MethodNameLoader extends VoidVisitorAdapter <Void> {
		@Override
		public void visit(MethodDeclaration md, Void arg){
			super.visit(md, arg);
			String methodDeclarationString = md.getDeclarationAsString(true,true,false).replaceAll(",",";");
			//now we have to replace all white spaces before the parenthesis
			String[] methodTokens = methodDeclarationString.split("\\(");
			String beforeParentheses = methodTokens[0].replaceAll(" ","_");
			methodDeclarationString = beforeParentheses + "(" + methodTokens[1];

			System.out.println("Method Name Printed: " + methodDeclarationString);
			curPathRev.getMethods().add(methodDeclarationString);
		
		}

	}

	private static void deleteSourceFiles() throws IOException {
		File sourceFilesDir = new File("sourcefiles2");
		String[] entries = sourceFilesDir.list(); 
		for(String entry : entries){
			System.out.println("Deleting file: " + entry);
			File sourceFile = new File(sourceFilesDir.getPath(), entry);
			sourceFile.delete();
		}
	}
}
