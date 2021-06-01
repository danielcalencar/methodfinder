package defectpred.model;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class PathRevision {


	private String path;
	private String revision;
	private String fixRevision;
	private Integer lineNumber;
	private String fileName;
	private String methodName = "noMethod";
	private String issueCode;
	private List<String> methods = new ArrayList<String>(); 

	public PathRevision(){
	}

	public PathRevision(String path, String revision, Integer lineNumber, 
			String issueCode, String fixRevision){
		this.path = path.replace("\"","");;
		this.revision = revision.replace("\"","");;
		this.lineNumber = lineNumber; 
		this.issueCode = issueCode;
		this.fixRevision = fixRevision;
		String fileNameTokens[] = path.split("/");
		this.fileName = fileNameTokens[fileNameTokens.length-1].replace("\"","");
		this.fileName = this.fileName + ".txt";
	}
	
	/**
	 * Get path.
	 *
	 * @return path as String.
	 */
	public String getPath() {
	    return path;
	}
	
	/**
	 * Set path.
	 *
	 * @param path the value to set.
	 */
	public void setPath(String path) {
	    this.path = path;
	}
	
	/**
	 * Get revision.
	 *
	 * @return revision as String.
	 */
	public String getRevision() {
	    return revision;
	}
	
	/**
	 * Set revision.
	 *
	 * @param revision the value to set.
	 */
	public void setRevision(String revision) {
	    this.revision = revision;
	}
	
	/**
	 * Get lineNumber.
	 *
	 * @return lineNumber as Integer.
	 */
	public Integer getLineNumber()
	{
	    return lineNumber;
	}
	
	/**
	 * Set lineNumber.
	 *
	 * @param lineNumber the value to set.
	 */
	public void setLineNumber(Integer lineNumber)
	{
	    this.lineNumber = lineNumber;
	}
	
	/**
	 * Get fileName.
	 *
	 * @return fileName as String.
	 */
	public String getFileName()
	{
	    return fileName;
	}

	public String retrieveFileName(){
		String[] tokens = this.path.split("/");
		String fileName = tokens[tokens.length-1];
		return fileName;
	}
	
	/**
	 * Set fileName.
	 *
	 * @param fileName the value to set.
	 */
	public void setFileName(String fileName)
	{
	    this.fileName = fileName;
	}
	
	/**
	 * Get methodName.
	 *
	 * @return methodName as String.
	 */
	public String getMethodName()
	{
	    return methodName;
	}
	
	/**
	 * Set methodName.
	 *
	 * @param methodName the value to set.
	 */
	public void setMethodName(String methodName)
	{
	    this.methodName = methodName;
	}
	
	/**
	 * Get issueCode.
	 *
	 * @return issueCode as String.
	 */
	public String getIssueCode()
	{
	    return issueCode;
	}
	
	/**
	 * Set issueCode.
	 *
	 * @param issueCode the value to set.
	 */
	public void setIssueCode(String issueCode)
	{
	    this.issueCode = issueCode;
	}
	
	/**
	 * Get fixRevision.
	 *
	 * @return fixRevision as String.
	 */
	public String getFixRevision()
	{
	    return fixRevision;
	}
	
	/**
	 * Set fixRevision.
	 *
	 * @param fixRevision the value to set.
	 */
	public void setFixRevision(String fixRevision)
	{
	    this.fixRevision = fixRevision;
	}
 
	
	/**
	 * Get methods.
	 *
	 * @return methods as List.
	 */
	public List<String> getMethods()
	{
	    return methods;
	}
	
	/**
	 * Set methods.
	 *
	 * @param methods the value to set.
	 */
	public void setMethods(List<String> methods)
	{
	    this.methods = methods;
	}
}
