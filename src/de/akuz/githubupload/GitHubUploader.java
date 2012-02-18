package de.akuz.githubupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class GitHubUploader {

	private String user;
	private String username;
	private String repo;
	private String token;
	private HttpClient httpclient;

	private final static String DOWNLOADS_URL = "https://github.com/%1$s/%2$s/downloads";
	private final static String S3_UPLOAD_URL = "http://github.s3.amazonaws.com/";
	private final static String ENCODING = "UTF-8";

	private String currentURL;

	public GitHubUploader(String user, String username, String repo,
			String token) {
		this.user = user;
		this.username = username;
		this.repo = repo;
		this.token = token;
		httpclient = new DefaultHttpClient();
	}

	public void uploadFile(File file, String description)
			throws GitHubUploadException {
		Map<String, String> s3Details = getS3Details(file, description);
		uploadFileToS3(file, s3Details);
	}
	
	public void uploadFile(String filename, String description) throws GitHubUploadException{
		uploadFile(new File(filename), description);
	}

	private void uploadFileToS3(File file, Map<String, String> details)
			throws GitHubUploadException {
		HttpPost post = new HttpPost(S3_UPLOAD_URL);
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(file));
		try {
			for (String s : details.keySet()) {
				entity.addPart(s, new StringBody(details.get(s)));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't encode POST for AWS S3",e);
		}
		post.setEntity(entity);
		try {
			HttpResponse response = httpclient.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post to AWS S3",e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post to AWS S3",e);
		}
		// TODO Evaluate response
	}

	private Map<String, String> getS3Details(File file, String description) throws GitHubUploadException {
		currentURL = String.format(DOWNLOADS_URL, user, repo);

		HttpPost httppost = new HttpPost(currentURL);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		try {
			nameValuePairs.add(new BasicNameValuePair("file_name", URLEncoder
					.encode(file.getName(), ENCODING)));
			nameValuePairs.add(new BasicNameValuePair("description", URLEncoder
					.encode(description, ENCODING)));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't encode POST parameters",e);
		}
		
		nameValuePairs.add(new BasicNameValuePair("login", username));
		nameValuePairs.add(new BasicNameValuePair("token", token));
		nameValuePairs.add(new BasicNameValuePair("file_size", String
				.valueOf(file.length())));
		
		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post request to GitHub",e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't post request to GitHub",e);
		}
		// TODO: Evaluate headers
		InputStreamReader ir;
		try {
			ir = new InputStreamReader(response.getEntity()
					.getContent());
			BufferedReader reader = new BufferedReader(ir);
			String s = "";
			StringBuffer buffer = new StringBuffer();
			while ((s = reader.readLine()) != null) {
				buffer.append(s);
			}
			Map<String, String> map = ResponseParser.parseResponse(buffer
					.toString());
			return map;

		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't read response from GitHub",e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't read response from GitHub",e);
		}
		
	}

}
