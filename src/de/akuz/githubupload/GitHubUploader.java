package de.akuz.githubupload;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * This class implements file uploading to github. Repository, user (owner of
 * repositoy), username, a description and the GitHub API token must be
 * specified to upload a file.
 * 
 * @author Till Klocke
 * 
 */
public class GitHubUploader {

	private String user;
	private String username;
	private String repo;
	private String token;
	private HttpClient httpclient;
	private HttpContext gitHubContext;
	private boolean debug = false;

	private final static String DOWNLOADS_URL = "https://github.com/%1$s/%2$s/downloads";
	private final static String S3_UPLOAD_URL = "https://%1$s.s3.amazonaws.com/";
	// "https://github.com/#{user}/#{repo}/downloads?login=#{github_user}&token=#{github_token}";
	private final static String FILE_LIST_URL = "https://github.com/%1$s/%2$s/downloads?login=%3$s&token=%4$s";
	private final static String AUTH_SUFFIX = "?login=%1$s&token=%2$s";	
	private final static String ENCODING = "UTF-8";

	private String currentURL;

	public GitHubUploader(String user, String username, String repo,
			String token) {
		this.user = user;
		this.username = username;
		this.repo = repo;
		this.token = token;
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
		        ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		gitHubContext = new BasicHttpContext();
	}

	public void uploadFile(File file, String description)
			throws GitHubUploadException {
		if (file == null || !file.exists() || file.length() == 0) {
			throw new GitHubUploadException(
					"The specified file does not exist or is empty");
		}
		Map<String, String> s3Details = getS3Details(file, description);
		postFileToS3(file, s3Details);
	}

	public void uploadFile(String filename, String description)
			throws GitHubUploadException {
		uploadFile(new File(filename), description);
	}

	private void setHeaders(AbstractHttpMessage message,
			Map<String, String> headers) {
		for (String s : headers.keySet()) {
			message.addHeader(s, headers.get(s));
		}
	}

	private Map<String, String> createAWSParameters(
			Map<String, String> details, String filename) {
		String key = details.get("path");
		String policy = details.get("policy");
		String accesskeyid = details.get("accesskeyid");
		String signature = details.get("signature");
		String acl = details.get("acl");
		String mime_type = details.get("mime_type");

		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("key", key);
		parameters.put("Filename", filename);
		parameters.put("Policy", policy);
		parameters.put("AWSAccessKeyId", accesskeyid);
		parameters.put("Signature", signature);
		parameters.put("acl", acl);
		parameters.put("success_action_status", "201");
		parameters.put("Content-Type", mime_type);

		return parameters;

	}

	private void setParameterToEntity(MultipartEntity entity,
			Map<String, String> headers) {
		for (String s : headers.keySet()) {
			try {
				entity.addPart(s, new StringBody(headers.get(s)));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private String responseToString(HttpResponse response) {
		StringBuffer buffer = new StringBuffer();
		Header[] headers = response.getAllHeaders();
		for (Header h : headers) {
			buffer.append("[Header] " + h.getName() + " : " + h.getValue()
					+ "\n");
		}
		buffer.append("-----Content-----\n");
		try {
			buffer.append(ResponseParser.inputStreamToString(response
					.getEntity().getContent()));
			buffer.append("\n");
		} catch (Exception e) {
			buffer.append("Couldn't parse content. Reason: " + e.getMessage());
		}
		buffer.append("-----Content-----\n");
		return buffer.toString();
	}

	private void postFileToS3(File file, Map<String, String> details)
			throws GitHubUploadException {
		String s3url = String.format(S3_UPLOAD_URL, details.get("bucket"));
		debug("Uploading to " + s3url);
		HttpPost post = new HttpPost(s3url);
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		setParameterToEntity(entity,
				createAWSParameters(details, file.getName()));
		entity.addPart("file", new FileBody(file, details.get("mime_type")));
		try {
			post.setEntity(entity);
			debug("Sending " + post.getEntity().getContentLength()
					+ " bytes of information to AWS S3...");
			HttpResponse response = httpclient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			debug("Received status v" + response.getStatusLine());
			// debug(responseToString(response));
			if (statusCode != 201) {
				throw new GitHubUploadException(
						"Error posting file to AWS S3! Status: "
								+ statusCode
								+ " Reason: "
								+ ResponseParser.inputStreamToString(response
										.getEntity().getContent()));
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post to AWS S3", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post to AWS S3", e);
		}
		// TODO Evaluate response
	}

	private Map<String, String> getS3Details(File file, String description)
			throws GitHubUploadException {
		currentURL = String.format(DOWNLOADS_URL, user, repo);
		debug("Posting to URL: " + currentURL);
		HttpPost httppost = new HttpPost(currentURL);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		try {
			nameValuePairs.add(new BasicNameValuePair("file_name", URLEncoder
					.encode(file.getName(), ENCODING)));
			nameValuePairs.add(new BasicNameValuePair("description",
					description));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't encode POST parameters", e);
		}

		nameValuePairs.add(new BasicNameValuePair("login", username));
		nameValuePairs.add(new BasicNameValuePair("token", token));
		nameValuePairs.add(new BasicNameValuePair("file_size", String
				.valueOf(file.length())));
		debug("Posted file_size: " + String.valueOf(file.length()));

		HttpResponse response;

		try {
			response = httpclient.execute(httppost,gitHubContext);
			debug("Got Response with status: " + response.getStatusLine());
			// debug(responseToString(response));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post request to GitHub", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post request to GitHub", e);
		}

		Map<String, String> map = ResponseParser.parse(response);
		return map;
	}

	public List<GitHubFile> getListOfFiles() throws GitHubUploadException {
		String url = String.format(FILE_LIST_URL, user, repo, username, token);
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(get,gitHubContext);
			return ResponseParser.parseDownloadResponse(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Failed to retrieve list of files",
					e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Failed to retrieve list of files",
					e);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Failed to retrieve list of files",
					e);
		}

	}

	public void deleteFile(GitHubFile file) throws GitHubUploadException{
		String url = String.format("https://github.com"+file.getDeletePath());
		debug("deleting from url "+url);
		HttpDelete deleteFile = new HttpDelete(url);
		try {
			HttpResponse response = httpclient.execute(deleteFile,gitHubContext);
			debug(responseToString(response));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't delete file "+file.getName(),e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't delete file "+file.getName(),e);
		}
	}
	
	public void deleteFilesByPattern(String delete) throws GitHubUploadException {

			if(delete != null){
				List<GitHubFile> files = getListOfFiles();
				for(GitHubFile file : files){
					if(Pattern.matches(delete, file.getName())){
						debug("Deleting file: "+file.getName());
						deleteFile(file);
					}
				}
			}
	}

	public static void main(String[] argv) {
		String user = null;
		String username = null;
		String repo = null;
		String token = null;
		String description = null;
		String filePath = null;
		String delete = null;
		boolean debug = false;

		int i = 0;
		for (String s : argv) {
			if (s.equals("-user")) {
				user = argv[i + 1];
			} else if (s.equals("-username")) {
				username = argv[i + 1];
			} else if (s.equals("-repo")) {
				repo = argv[i + 1];
			} else if (s.equals("-token")) {
				token = argv[i + 1];
			} else if (s.equals("-description")) {
				description = argv[i + 1];
			} else if (s.equals("-debug")) {
				debug = true;
			} else if(s.equals("-delete")){
				delete = argv[i+1];
			}
			i++;
		}
		filePath = argv[argv.length - 1];
		
		GitHubUploader uploader = new GitHubUploader(user, username, repo,
				token);
		uploader.setDebug(debug);
		
		try {
			if (delete != null) {
				uploader.deleteFilesByPattern(delete);
			}
			uploader.uploadFile(filePath, description);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private void debug(String message) {
		if (debug) {
			System.out.println(message);
		}
	}

}
