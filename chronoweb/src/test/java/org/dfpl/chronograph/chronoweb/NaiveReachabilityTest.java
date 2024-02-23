package org.dfpl.chronograph.chronoweb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NaiveReachabilityTest {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		// INPUT
		Integer source = 0;
		Integer neighbors = 1000;
		String filePath = "D:\\chronoweb\\results\\8_4.txt";

		String host = "http://localhost/chronoweb";
		Integer baseTime = 9999;
		Map<String, String> payload = new HashMap<>();
		payload.put("k", "v");

		for (int destination = 1; destination < neighbors + 1; destination++) {
			Integer eventTime = baseTime + 1;
			String postURLString = String.format("%s/graph/%s|label|%s_%s", host, source, destination, eventTime);
			System.out.println("POST " + postURLString);
			URL postURL = new URL(postURLString);
			HttpURLConnection postCon = (HttpURLConnection) postURL.openConnection();
			postCon.setRequestMethod("PUT");
			postCon.setDoOutput(true);
			postCon.setRequestProperty("Content-Type", "application/json");
			postCon.setRequestProperty("Accept", "application/json");
			OutputStreamWriter osw = new OutputStreamWriter(postCon.getOutputStream());
			osw.write("{ \"k\": \"v\"}");
			osw.flush();
			osw.close();
			postCon.getResponseCode();
			postCon.disconnect();
			
			List<Integer> queue = new LinkedList<>();
			queue.add(source);
			List<Integer> reachableVertices = new LinkedList<>();

			Long startTime = System.currentTimeMillis();
			while (queue.size() > 0) {
				Integer currVertex = queue.removeFirst();
				String getURLString = String.format("%s/graph/%s_%s/outEe?temporalRelation=isAfter&label=label", host,
						currVertex, baseTime - 1);
				System.out.println("\tGET " + getURLString);
				URL getURL = new URL(getURLString);

				HttpURLConnection getCon = (HttpURLConnection) getURL.openConnection();
				getCon.setRequestMethod("GET");
				System.err.println(getCon.getResponseCode());
				if (getCon.getResponseCode() == 200) {
					BufferedReader in = new BufferedReader(new InputStreamReader(getCon.getInputStream()));
					String inputLine;
					List<String> edgeEvents = new LinkedList<>();
					while ((inputLine = in.readLine()) != null) {
						edgeEvents = Arrays.asList(inputLine.replace("[", "").replace("]", "").split(","));
					}
					in.close();

					for (String edgeEvent : edgeEvents) {
						System.out.println(edgeEvent);
						try {
							Integer inVertex = Integer.parseInt(edgeEvent.split("\\||\\||_")[2]);
							if (!reachableVertices.contains(inVertex)) {
								reachableVertices.add(inVertex);
								queue.add(inVertex);
							}
						} catch (Exception e) {

						}

					}
				}
				getCon.disconnect();
			}
			Long computationTime = System.currentTimeMillis() - startTime;
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
		    writer.append(computationTime.toString()).append("\n");
		    writer.close();
		}
	}

}
