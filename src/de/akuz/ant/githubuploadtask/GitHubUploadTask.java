package de.akuz.ant.githubuploadtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.akuz.githubupload.GitHubUploadException;
import de.akuz.githubupload.GitHubUploader;

public class GitHubUploadTask extends Task {
	
	private String user;
	private String username;
	private String repo;
	private String token;
	private String path;
	private String description;
	
	private GitHubUploader uploader;

	@Override
	public void execute() throws BuildException {
		uploader = new GitHubUploader(user, username, repo, token);
		try {
			uploader.uploadFile(path, description);
		} catch (GitHubUploadException e) {
			e.printStackTrace();
			throw new BuildException(e);
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

}
