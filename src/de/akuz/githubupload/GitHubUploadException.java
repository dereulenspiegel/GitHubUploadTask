package de.akuz.githubupload;

/**
 * This exception is thrown every time a call to the github servers or AWS S3
 * servers fails or parsing of a response fails. The reason for the failure us
 * provided.
 * 
 * @author Till Klocke
 * 
 */
public class GitHubUploadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5538485810343372809L;

	public GitHubUploadException(String message) {
		super(message);
	}

	public GitHubUploadException(String message, Throwable reason) {
		super(message, reason);
	}

	public GitHubUploadException(Throwable reason) {
		super(reason);
	}
}
