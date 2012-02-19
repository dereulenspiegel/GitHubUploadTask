package de.akuz.ant.githubuploadtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

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
	private boolean debug = false;
	
	private GitHubUploader uploader;

	@Override
	public void execute() throws BuildException {
		System.out.println("Uploading file "+path+" to GitHub");
		uploader = new GitHubUploader(user, username, repo, token);
		uploader.setDebug(debug);
		try {
			uploader.uploadFile(path, description);
			System.out.println("Upload finished");
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

}
