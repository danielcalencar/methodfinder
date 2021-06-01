package defectpred;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;

import defectpred.model.PathRevision;

import java.util.List;
import java.util.Properties;

public class VoidVisitorStarter {
	private static String separator = File.separator;
	private static String project; // the project you are interested to work on 
	private static List<PathRevision> bugIntroLines;
	private static PathRevision curBugIntroLine;
	private static Boolean loadFiles;
	private static Boolean loadMethods;

	public static void main(String[] args) throws Exception {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		if(args.length == 0){
			throw new IllegalArgumentException("please provide the project you are interested in");
		}
		project = args[0];
		try(InputStream input = new FileInputStream("tasks.properties")){ 
			Properties prop = new Properties();
			prop.load(input);
			loadFiles = Boolean.valueOf(prop.getProperty("load.files"));
			loadMethods = Boolean.valueOf(prop.getProperty("load.methods"));
		}
		catch (IOException e) {
			throw new IOException("where is the .properties file?");
		}
		//let's load the bug-introducing lines and build the PathRevision objects;
		bugIntroLines = LoadBugIntroLines.loadData(project);
		if(loadFiles){	
			//we should load the sourcefiles in order to visit them and find the methods of the bug
			//intro lines
			CollectSourceFiles csf = new CollectSourceFiles(bugIntroLines, project);
			csf.loadFiles();
		}
		if(loadMethods){
			//now we iterate the files and load the implicated method for each bug-intro-line
			loadMethods(bugIntroLines);
			//delete the source files
			deleteSourceFiles();
		}
	}

	private static void loadMethods(List<PathRevision> bugIntroLines) throws Exception {
		StringBuilder sb = new StringBuilder("project,ticketid,fixcommitid,line,bugcommitid,buggymethod,\n");
		for(PathRevision bugIntroLine : bugIntroLines){
			System.out.println("loading file: " + bugIntroLine.getFileName());
			CompilationUnit cu = null;
			try{
				cu = StaticJavaParser.parse(new File("sourcefiles" + 
							separator + 
							bugIntroLine.getRevision() + 
							"_" + 
							bugIntroLine.getFileName()));
			} catch(Exception e){
				System.out.println("problem with file: " + bugIntroLine.getFileName());
				continue;
			}
			VoidVisitor<?> methodNameVisitor = new MethodNameLoader();
			curBugIntroLine = bugIntroLine;
			methodNameVisitor.visit(cu, null);
			sb.append(project + ",");
			sb.append(curBugIntroLine.getIssueCode() + ",");
			sb.append(curBugIntroLine.getFixRevision() + ",");
			sb.append(curBugIntroLine.getLineNumber() + ",");
			sb.append(curBugIntroLine.getRevision() + ",");
			sb.append(curBugIntroLine.getPath() + "#");
			sb.append(curBugIntroLine.getMethodName() + "\n");
		}

		//now let's write the outputFile;
		File outputFile = new File("output" + separator + project + "-output.csv"); 
		if(!outputFile.exists()){
			outputFile.createNewFile();
		}
		Path outputPath = Paths.get(outputFile.getPath());
		Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
	}

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
			int begin = 0;
			int end = 0;
			begin = md.getRange().get().begin.line;
			end = md.getRange().get().end.line;
			if(begin <= curBugIntroLine.getLineNumber() && 
					end >= curBugIntroLine.getLineNumber()){
				System.out.println("this is the method!");
				curBugIntroLine.setMethodName(methodDeclarationString);
			}
		}

	}

	private static void deleteSourceFiles() throws IOException {
		File sourceFilesDir = new File("sourcefiles");
		String[] entries = sourceFilesDir.list(); 
		for(String entry : entries){
			System.out.println("Deleting file: " + entry);
			File sourceFile = new File(sourceFilesDir.getPath(), entry);
			sourceFile.delete();
		}
	}
		
}
