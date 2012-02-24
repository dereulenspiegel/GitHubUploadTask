package de.akuz.ant.githubuploadtask;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.akuz.githubupload.GitHubFile;
import de.akuz.githubupload.GitHubUploadException;
import de.akuz.githubupload.GitHubUploader;

/**
 * This task makes the GitHubUploader available to Ant.
 * It can be used like this
 * <taskdef name="upload" classname="de.akuz.ant.githubuploadtask.GitHubUploadTask" />
 * <upload user="${github.user}" repo="${github.repo}" username="${github.username}" token="${github.token}" description="GitHubUploadTask nightly" path="${dist}/GitHubUploadTask-${DSTAMP}.zip" />
 * if the GitHubUploader is in the classpath of Ant
 * @author Till Klocke
 *
 */
public class GitHubUploadTask extends Task {
	
	private String user;
	private String username;
	private String repo;
	private String token;
	private String path;
	private String description;
	private String deletePatternString;
	private boolean debug = false;
	private Pattern deletePattern;
	
	private GitHubUploader uploader;

	@Override
	public void execute() throws BuildException {
		uploader = new GitHubUploader(user, username, repo, token);
		uploader.setDebug(debug);
		
		if(deletePatternString!=null && deletePatternString.trim().length() > 0){
			out("delete pattern is set, deleting all files matching the pattern");
			deletePattern = Pattern.compile(deletePatternString);
			try {
				debug("Retrieving list of files");
				List<GitHubFile> files = uploader.getListOfFiles();
				for(GitHubFile file : files){
					debug("Checking file for removal: "+file.getName());
					Matcher matcher = deletePattern.matcher(file.getName());
					if(matcher.matches()){
						out("Deleting file "+file.getName());
						uploader.deleteFile(file);
					}
				}
			} catch (GitHubUploadException e) {
				throw new BuildException(e);
			}
		}
		out("Uploading file "+path+" to GitHub");
		
		try {
			uploader.uploadFile(path, description);
			out("Upload finished");
		} catch (GitHubUploadException e) {
			e.printStackTrace();
			throw new BuildException("Can't upload file to GitHub",e);
		}
	}

	/**
	 * Sets the user the repor belongs to
	 * @return
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Set the username of the user which wants to upload the file
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the repo the file should be uploaded to
	 * @param repo
	 */
	public void setRepo(String repo) {
		this.repo = repo;
	}
	
	/**
	 * Set the API token of the user who wants to upload the file
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Set the path of the file the user wants to upload
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Set the description of the the user wants to upload
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Set to true to see some more output
	 * @param value
	 */
	public void setDebug(String value){
		debug = Boolean.parseBoolean(value);
	}
	
	/**
	 * If set all files in the download section if the repo matching this pattern will be deleted
	 * @param deletePattern a valid regex
	 */
	public void setDeletePattern(String deletePattern) {
		this.deletePatternString = deletePattern;
	}
	
	private void out(String message){
		System.out.println(message);
	}
	
	private void debug(String message){
		if(debug){
			out(message);
		}
	}

}
