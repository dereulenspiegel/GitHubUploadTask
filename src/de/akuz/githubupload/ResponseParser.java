package de.akuz.githubupload;

import java.util.HashMap;
import java.util.Map;

/**
 * This class parses the response from GitHub into a Map object
 * @author Till Klocke
 *
 */
public class ResponseParser {

	private ResponseParser() {

	}

	public static Map<String, String> parseResponse(String source) {
		if(source == null || source.trim().length()==0){
			throw new IllegalArgumentException("The source can't be an empty or null string");
		}
		String response = source.trim();
		response = response.replace("{", "");
		response = response.replace("}", "");
		String[] parts = response.split(",\"");
		Map<String, String> result = new HashMap<String, String>();
		for (String s : parts) {
			String[] pairs = s.split("\":");
			if(pairs.length == 2){
				String name = pairs[0].replace("\"", "");
				String value = pairs[1].replace("\"", "");
				result.put(name, value);
			} else {
				throw new IllegalArgumentException("The Input is not parseable! INVALID PART: "+s);
			}
		}
		return result;
	}

}
