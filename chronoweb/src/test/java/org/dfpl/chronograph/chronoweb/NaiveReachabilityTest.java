package org.dfpl.chronograph.chronoweb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NaiveReachabilityTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		// INPUT
		Integer sourceVertex = 960;
		Integer sourceTime = 0;
		String filePath = "D:\\chronoweb\\results\\email_api8.txt"; // results
		String graphFile = "D:\\chronoweb\\datasets\\email.txt"; // input graph

		// Constants
		String host = "http://localhost/chronoweb";

		int lineRead = 0;
		BufferedReader br = new BufferedReader(new FileReader(graphFile));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			String[] arr = line.split("\\s");

			Integer from = Integer.parseInt(arr[0]);
			Integer to = Integer.parseInt(arr[1]);

			Integer time = Integer.parseInt(arr[2]);

			postEdgeEvent(host, from, to, time);

			Long computationTime = getReachability(host, sourceVertex, sourceTime);

			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			writer.append(computationTime.toString()).append("\n");
			writer.close();

			if (++lineRead % 10000 == 0) {
				System.out.println(lineRead + " lines read...");
			}
		}

		br.close();
	}

	@SuppressWarnings("deprecation")
	public static void postEdgeEvent(String host, Integer from, Integer to, Integer time) throws IOException {
		String postURLString = String.format("%s/graph/%s|label|%s_%s", host, from, to, time);
//		System.out.println("POST " + postURLString);

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
	}

	@SuppressWarnings("deprecation")
	public static Long getReachability(String host, Integer sourceVertex, Integer sourceTime)
			throws IOException, InterruptedException {
		List<Integer> queue = new LinkedList<>();
		queue.add(sourceVertex);
		List<Integer> reachableVertices = new LinkedList<>();
		Long totalTime = (long) 0;
		while (queue.size() > 0) {
			Integer currVertex = queue.removeFirst();
			TimeUnit.MILLISECONDS.sleep(10);
			Long startTime = System.currentTimeMillis();
			String getURLString = String.format("%s/graph/%s_%s/outEe?temporalRelation=isAfter&label=label", host,
					currVertex, sourceTime);
//			System.out.println("\tGET " + getURLString);
			URL getURL = new URL(getURLString);

			HttpURLConnection getCon = (HttpURLConnection) getURL.openConnection();
			getCon.setRequestMethod("GET");
//			System.err.println(getCon.getResponseCode());
			if (getCon.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(getCon.getInputStream()));
				String inputLine;
				List<String> edgeEvents = new LinkedList<>();
				while ((inputLine = in.readLine()) != null) {
					edgeEvents = Arrays.asList(inputLine.replace("[", "").replace("]", "").split(","));
				}
				in.close();

				for (String edgeEvent : edgeEvents) {
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
			Long computationTime = System.currentTimeMillis() - startTime;
			totalTime += computationTime;
			getCon.disconnect();
		}
		return totalTime;
	}
}
