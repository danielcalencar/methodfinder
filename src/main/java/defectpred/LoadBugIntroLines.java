package defectpred;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import defectpred.model.PathRevision;


public class LoadBugIntroLines {

	private static String separator = File.separator;
	private LoadBugIntroLines(){}

	public static List<PathRevision> loadData(String project) throws IOException {
		List<PathRevision> result = new ArrayList<PathRevision>();
		File file = new File("bugintrofiles" + separator + project + ".csv");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		boolean header = true;
		while(br.ready()){
			line = br.readLine();
			if(header){
				header = false;
				continue;
			}
			String[] tokens = line.split(",");
			Integer lineNumber = Integer.valueOf(tokens[1].replace("\"",""));
			System.out.println("line number: " + lineNumber);
			String path = tokens[2];
			System.out.println("path: " + path);
			String revision = tokens[3];
			System.out.println("revision: " + revision);
			String issueCode = tokens[4];
			System.out.println("issueCode: " + issueCode);
			String fixRevision = tokens[5];
			System.out.println("fixRevision: " + fixRevision);
			PathRevision pr = new PathRevision(path,revision,lineNumber,issueCode,fixRevision);
			result.add(pr);
		}
		return result;
	}
}
