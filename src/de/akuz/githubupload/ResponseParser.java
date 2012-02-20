package de.akuz.githubupload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

/**
 * This class parses the response from GitHub into a Map object
 * 
 * @author Till Klocke
 * 
 */
public class ResponseParser {

	private ResponseParser() {

	}

	public static Map<String, String> parse(HttpResponse response)
			throws GitHubUploadException {
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode == 200) {
			try {
				return parseResponseString(inputStreamToString(response
						.getEntity().getContent()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new GitHubUploadException("Can't parse HTTPResponse", e);
			}
		} else if (responseCode >= 400) {
			try {
				String body = inputStreamToString(response.getEntity()
						.getContent());
				throw new GitHubUploadException(
						"Got an Error from GitHub! Status: " + responseCode
								+ " Message: " + body);
			} catch (Exception e) {
				if (!(e instanceof GitHubUploadException)) {
					e.printStackTrace();
					throw new GitHubUploadException(
							"Failed to parse error message received from GitHub",
							e);
				} else {
					throw (GitHubUploadException)e;
				}
			}
		} else {
			throw new GitHubUploadException("Got unknwon response "
					+ response.getStatusLine());
		}
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		InputStreamReader ir = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(ir);
		String s = "";
		StringBuffer buffer = new StringBuffer();
		while ((s = reader.readLine()) != null) {
			buffer.append(s);
		}
		return buffer.toString();
	}

	public static Map<String, String> parseResponseString(String source) {
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(
					"The source can't be an empty or null string");
		}
		String response = source.trim();
		response = response.replace("{", "");
		response = response.replace("}", "");
		String[] parts = response.split(",\"");
		Map<String, String> result = new HashMap<String, String>();
		for (String s : parts) {
			String[] pairs = s.split("\":");
			if (pairs.length == 2) {
				String name = pairs[0].replace("\"", "");
				String value = pairs[1].replace("\"", "");
				result.put(name, value);
			} else {
				throw new IllegalArgumentException(
						"The Input is not parseable! INVALID PART: " + s);
			}
		}
		return result;
	}

	private static void debug(String message) {
		System.out.println(message);
	}

}
