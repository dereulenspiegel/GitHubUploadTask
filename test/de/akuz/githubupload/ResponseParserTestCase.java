package de.akuz.githubupload;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Test;


public class ResponseParserTestCase {
	
	public final static String VALID_RESPONSE = 
			"{\"bucket\":\"github\"," +
			"\"policy\":\"ewogICAgJ2V4cGlyYXRpb24nOiAnMjExMi0wMi0xOFQxODoxOTowNi4wMDBaJywKICAgICdjb25kaXRpb25zJzogWwogICAgICAgIHsnYnVja2V0JzogJ2dpdGh1Yid9LAogICAgICAgIHsna2V5JzogJ2Rvd25sb2Fkcy9kZXJldWxlbnNwaWVnZWwvR2l0SHViVXBsb2FkVGFzay90ZXN0Lmphcid9LAogICAgICAgIHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30sCiAgICAgICAgeydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sCiAgICAgICAgWydzdGFydHMtd2l0aCcsICckRmlsZW5hbWUnLCAnJ10sCiAgICAgICAgWydzdGFydHMtd2l0aCcsICckQ29udGVudC1UeXBlJywgJyddCiAgICBdCn0=\"," +
			"\"expirationdate\":\"2112-02-18T18:19:06.000Z\"," +
			"\"prefix\":\"downloads/dereulenspiegel/GitHubUploadTask\"," +
			"\"accesskeyid\":\"1DWESVTPGHQVTX38V182\"," +
			"\"redirect\":false," +
			"\"acl\":\"public-read\"," +
			"\"path\":\"downloads/dereulenspiegel/GitHubUploadTask/test.jar\"," +
			"\"mime_type\":\"application/java-archive\"," +
			"\"signature\":\"laNq1/RmoOJ6toScBkQWPHlMcpo=\"}";
	
	public final static String INVALID_RESPONSE = 
			"{\"bucket\":\"github\"," +
			"\"policy\":\"ewogICAgJ2V4cGlyYXRpb24nOiAnM\":\"jExMi0wMi0xOFQxODoxOTowNi4wMDBaJywKICAgICdjb25kaXRpb25zJzogWwogICAgICAgIHsnYnVja2V0JzogJ2dpdGh1Yid9LAogICAgICAgIHsna2V5JzogJ2Rvd25sb2Fkcy9kZXJldWxlbnNwaWVnZWwvR2l0SHViVXBsb2FkVGFzay90ZXN0Lmphcid9LAogICAgICAgIHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30sCiAgICAgICAgeydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sCiAgICAgICAgWydzdGFydHMtd2l0aCcsICckRmlsZW5hbWUnLCAnJ10sCiAgICAgICAgWydzdGFydHMtd2l0aCcsICckQ29udGVudC1UeXBlJywgJyddCiAgICBdCn0=\"," +
			"\"expirationdate\":\"2112-02-18T18:19:06.000Z\"," +
			"\"prefix\":\"downloads/dereulenspiegel/GitHubUploadTask\"," +
			"\"accesskeyid\":\"1DWESVTPGHQVTX38V182\"," +
			"\"redirect\":false," +
			"\"acl\":\"public-read\"," +
			"\"path\":\"downloads/dereulenspiegel/GitHubUploadTask/test.jar\"," +
			"\"mime_type\":\"application/java-archive\"," +
			"\"signature\":\"laNq1/RmoOJ6toScBkQWPHlMcpo=\"}";
	
	public final static String INVALID_RESPONSE_2 = 
			"{\"bucket\":\"github\"," +
			"\"policy\":\"ewogICAgJ2V4cGlyYXRpb24nOiAnMjExMi0wMi0xOVQxNDo0Mjo0NC4wMDBaJywKICAgICdjb25kaXRpb25zJzogWwogICAgICAgIHsnYnVja2V0JzogJ2dpdGh1Yid9LAogICAgICAgIHsna2V5JzogJ2Rvd25sb2Fkcy9kZXJldWxlbnNwaWVnZWwvR2l0SHViVXBsb2FkVGFzay9HaXRIdWJVcGxvYWRUYXNrLTIwMTIwMjE5LnppcCd9LAogICAgICAgIHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30sCiAgICAgICAgeydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sCiAgICAgICAgWydzdGFydHMtd2l0aCcsICckRmlsZW5hbWUnLCAnJ10sCiAgICAgICAgWydzdGFydHMtd2l0aCcsIckQ29udGVudC1UeXBlJywgJyddCiAgICBdCn0=\"," +
			"\"expirationdate\":\"2112-02-19T14:42:44.000Z\"," +
			"\"accesskeyid\":\"1DWESVTPGHQVTX38V182\"," +
			"\"prefix\":\"downloads/dereulenspiegel/GitHubUploadTask\"," +
			"\"redirect\":false," +
			"\"acl\":\"public-read\"," +
			"\"path\":\"downloads/dereulenspiegel/GitHubUploadTask/GitHubUploadTask-20120219.zip\"," +
			"\"mime_type\":\"application/zip\"," +
			"\"signature\":\"kYd3jmadx7G6Uvxna5kB35O9CeE=\"}";
	
	@Test
	public void testParsingWithValidResponse() throws Exception {
		Map<String,String> map = ResponseParser.parseResponse(VALID_RESPONSE);
		assertNotNull("The Parser returned no map object",map);
		assertEquals("We didn't get 9 key/value pairs", 9, map.size());
		assertEquals("github", map.get("bucket"));
	}
	
	@Test
	public void testParsingWithEmptyResponse() throws Exception {
		try{
			Map<String,String> map = ResponseParser.parseResponse("");
			fail();
		} catch(IllegalArgumentException e){
			assertNotNull(e);
		}
	}
	
	@Test
	public void testParsingWithNullResponse() throws Exception {
		try{
			Map<String,String> map = ResponseParser.parseResponse(null);
			fail();
		} catch(IllegalArgumentException e){
			assertNotNull(e);
		}
	}
	
	@Test
	public void testParsingWithInvalidResponse() throws Exception {
		try{
			Map<String,String> map = ResponseParser.parseResponse(INVALID_RESPONSE);
			fail();
		} catch(IllegalArgumentException e){
			assertNotNull(e);
		}
	}
	
	@Test
	public void testParsingProblematicResponse() throws Exception {
		Map<String,String> map = ResponseParser.parseResponse(INVALID_RESPONSE_2);
		Set<String> keys = map.keySet();
		for(String s : keys){
			System.out.println(s);
		}
		assertNotNull(map);
		assertEquals(9, map.size());
		assertTrue(map.keySet().contains("acl"));
		assertNotNull(map.get("acl"));
	}

}
