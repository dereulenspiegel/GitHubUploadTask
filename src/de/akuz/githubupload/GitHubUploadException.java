package de.akuz.githubupload;

public class GitHubUploadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5538485810343372809L;

	public GitHubUploadException(String message){
		super(message);
	}
	
	public GitHubUploadException(String message, Throwable reason){
		super(message,reason);
	}
	
	public GitHubUploadException(Throwable reason){
		super(reason);
	}
}
