package de.akuz.githubupload;

import java.util.HashMap;
import java.util.Map;

public class ResponseParser {
	
	public static Map<String,String> parseResponse(String source){
		String response = source.trim();
		response = response.replace("{", "");
		response = response.replace("}", "");
		String[] parts = response.split("\",\"");
		Map<String,String> result = new HashMap<String, String>();
		for(String s : parts){
			String[] pairs = s.split("\":\"");
			String name = pairs[0].replace("\"", "");
			String value =  pairs[1].replace("\"", "");
			result.put(name, value);
		}
		return result;
	}

}
