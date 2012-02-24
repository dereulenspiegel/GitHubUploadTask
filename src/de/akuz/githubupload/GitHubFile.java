package de.akuz.githubupload;

public class GitHubFile {

	String downloadPath;
	String deletePath;
	String name;
	String description;

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public String getDeletePath() {
		return deletePath;
	}

	public void setDeletePath(String deletePath) {
		this.deletePath = deletePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
