package de.akuz.githubupload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class parses the response from GitHub into a Map object
 * 
 * @author Till Klocke
 * 
 */
public class ResponseParser {
	
	private final static String HREF_ATTRIBUTE="href";

	private ResponseParser() {

	}

	public static List<GitHubFile> parseDownloadResponse(HttpResponse response)
			throws GitHubUploadException {
		handleStatusCode(response);

		Document doc = null;
		try {
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(response.getEntity().getContent()));

			doc = parser.getDocument();
			Element downloads = doc.getElementById("manual_downloads");
			NodeList listNodes = downloads.getElementsByTagName("li");
			List<GitHubFile> gitHubFiles = new ArrayList<GitHubFile>(
					listNodes.getLength());
			for (int i = 0; i < listNodes.getLength(); i++) {
				Node fileNode = listNodes.item(i);
				NodeList fileSubNodes = fileNode.getChildNodes();
				Node deleteElement = fileSubNodes.item(1);
				Node downloadElement = fileSubNodes.item(5);
				String deletePath = getAttritubteByName(deleteElement, HREF_ATTRIBUTE);
				Node downloadNode = downloadElement.getChildNodes().item(1);
				String downloadPath = getAttritubteByName(downloadNode, HREF_ATTRIBUTE);
				String description = downloadElement.getChildNodes().item(2)
						.getNodeValue();
				String name = downloadNode.getChildNodes().item(0)
						.getNodeValue();
				GitHubFile file = new GitHubFile();
				file.setDeletePath(deletePath);
				file.setDownloadPath(downloadPath);
				file.setName(name);
				file.setDescription(description);
				gitHubFiles.add(file);
			}
			return Collections.unmodifiableList(gitHubFiles);
		} catch (Exception e) {
			try {
				printDocument(doc);
			} catch (TransformerException e1) {
				e1.printStackTrace();
			}
			throw new GitHubUploadException(e);
		}
	}

	private static void printDocument(Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		String xmlString = result.getWriter().toString();
		System.out.println(xmlString);
	}

	private static String getAttritubteByName(Node node, String name) {
		NamedNodeMap map = node.getAttributes();
		return map.getNamedItem(name).getNodeValue();
	}

	public static Map<String, String> parse(HttpResponse response)
			throws GitHubUploadException {
		handleStatusCode(response);
		try {
			return parseResponseString(inputStreamToString(response.getEntity()
					.getContent()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new GitHubUploadException("Can't parse HTTPResponse", e);
		}
	}

	private static void handleStatusCode(HttpResponse response)
			throws GitHubUploadException {
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode >= 400) {
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
					throw (GitHubUploadException) e;
				}
			}
		} else if (responseCode < 200) {
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
