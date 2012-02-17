package no.kinsey.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.net.MalformedURLException;

import java.lang.StringBuilder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;


public class GitHubUploadTask extends Task {
	// Inspiration for this comes from http://github.com/tekkub/github-gem/blob/master/lib/commands/commands.rb
	
	private String user;
	private String repo;
	private String username;
	private String token;
	private String path;
	private String description;

	Document PostToGitHub(String filename, long filesize) {	
		String postUrl = String.format("https://github.com/%1$s/%2$s/downloads", user, repo);
		System.out.println("Posting to url " + postUrl);
		
		StringBuilder sb = new StringBuilder();

		try {
			// Construct data
			String data = "file_size=" + Long.toString(filesize) + "&" + 
			"file_name=" + filename + "&" + 
			"description" + URLEncoder.encode(description, "UTF-8") + "&" +
			"login=" + username + "&" +
			"token=" + token;

			// Send data
			URL url = new URL(postUrl);
			URLConnection conn =  url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStreamReader reader = new InputStreamReader(conn.getInputStream());
			BufferedReader breader = new BufferedReader(reader);
			String s = "";
			while((s=breader.readLine())!=null){
				System.out.println(s);
			}
			Document doc = docBuilder.parse(conn.getInputStream());
			doc.getDocumentElement ().normalize ();
			return doc;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
	
	void WriteFormField(DataOutputStream dos, String header, String field, String value) throws IOException{
		System.out.println("Writing field  " + field + " with value " + value);
		dos.writeBytes(header);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + field + "\"\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes(value);
		dos.writeBytes("\r\n");
	}
	
	boolean UploadToS3(File file, String key, String policy, String accesskeyid, String signature, String acl) {
		String postUrl = "http://github.s3.amazonaws.com/";
		System.out.println("Posting to url " + postUrl);
		
		HttpURLConnection conn = null;
		int bytesRead, bytesAvailable, bufferSize, maxBufferSize = 1 * 1024 * 1024;;
		byte[] buffer;
		
		try {
			// Open the file
			FileInputStream fileInputStream = new FileInputStream(file);
			
			// Prepare the http connection
			URL url = new URL(postUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false); 
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
			
			DataOutputStream dos =  new DataOutputStream(conn.getOutputStream());
			String header = "--*****\r\n";
			String footer = "--*****--\r\n";
			
			// Write the form data
			WriteFormField(dos, header, "Filename", file.getName());
			WriteFormField(dos, header, "key", key);
			WriteFormField(dos, header, "policy", policy);
			WriteFormField(dos, header, "AWSAccessKeyId", accesskeyid);
			WriteFormField(dos, header, "signature", signature);
			WriteFormField(dos, header, "acl", acl);
			WriteFormField(dos, header, "success_action_status", "201");
			
			dos.writeBytes(header);
			dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() +"\"\r\n");
			dos.writeBytes("\r\n");
			
			// Write the file data
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			dos.writeBytes("\r\n");
			dos.writeBytes(footer);
			
			// Close streams
			fileInputStream.close();
			dos.flush();
			dos.close();
		}
		catch (IOException ioe) {
			System.out.println(ioe);
			return false;
		}
		
		// Read the response
		try {
			DataInputStream inStream = new DataInputStream(conn.getInputStream());
			String str;
			while ((str = inStream.readLine()) != null){
				System.out.println("Server response is: " + str);
			}
			inStream.close();
		}
		catch (IOException ioex){
			System.out.println(ioex);
			return false;
		}
		return true;
	}
	
	public void execute() throws BuildException {
		System.out.println("Preparing to upload " + path);
		
		// Get the file information
		File file = new File(path);
		if (!file.exists()) {
			throw new BuildException("The file " + path + " does not exist");
		}
		
		Document xml = PostToGitHub(file.getName(), file.length());
		if (xml != null) {
			NodeList list = xml.getElementsByTagName("*");
			String prefix = "", accesskeyid = "", bucket = "", https = "", acl = "", policy = "", mimeType = "", signature = "", redirect = "", expirationdate = "";
			
			// Extract the data
			for (int i=0; i<list.getLength(); i++) {
				Element element = (Element)list.item(i);
				String nodeName = element.getNodeName();
				String value = element.getFirstChild().getNodeValue();
				
				if (nodeName == "prefix") {
					prefix = value;
				}else if (nodeName =="accesskeyid") {
					accesskeyid = value;
				}else if (nodeName =="bucket") {
					bucket = value;
				}else if (nodeName =="https") {
					https = value;
				}else if (nodeName =="acl") {
					acl = value;
				}else if (nodeName =="policy") {
					policy = value;
				}else if (nodeName =="mimetype") {
					mimeType = value;
				}else if (nodeName =="signature") {
					signature = value;
				}else if (nodeName =="redirect") {
					redirect = value;
				}else if (nodeName =="expirationdate") {
					expirationdate = value;
				}
			}
						
			// Upload file
			if (UploadToS3(file, prefix + file.getName(), policy, accesskeyid, signature, acl)) {
				System.out.println("File uploaded successfully");
			} else{
				throw new BuildException("Could not upload the file to S3");
			}
		} else {
			throw new BuildException("Could not register the file with GitHub");
		}
	}

	// Property setters
	public void setUser(String user) {
		this.user = user;
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setDescription(String description){
		this.description = description;
	}
}