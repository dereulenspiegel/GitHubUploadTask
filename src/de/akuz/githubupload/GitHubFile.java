package de.akuz.githubupload;

/**
 * This class represents files in the download section of a github repository
 * @author Till Klocke
 *
 */
public class GitHubFile {

	String downloadPath;
	String deletePath;
	String name;
	String description;

	/**
	 * Returns the path to the download without the host. i.e. /downloads/dereulenspiegel/GitHubUploadTask/GitHubUploadTask-20120224.zip
	 * @return
	 */
	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	/**
	 * Returns the path used to delete the file. 
	 * @return
	 */
	public String getDeletePath() {
		return deletePath;
	}

	public void setDeletePath(String deletePath) {
		this.deletePath = deletePath;
	}

	/**
	 * Returns the file name of the file (the name the user sees in the download section).
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the description of the file.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
