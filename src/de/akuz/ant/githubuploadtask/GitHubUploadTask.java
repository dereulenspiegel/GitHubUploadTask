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
				List<GitHubFile> files = uploader.getListOfFiles();
				for(GitHubFile file : files){
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDebug(String value){
		debug = Boolean.parseBoolean(value);
	}

	public void setDeletePattern(String deletePattern) {
		this.deletePatternString = deletePattern;
	}
	
	private void out(String message){
		System.out.println(message);
	}

}
