package org.dfpl.chronograph.chronoweb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class IncrementalReachabilityTest {

	public static void main(String[] args) throws IOException {
		// INPUT
		Integer sourceVertex = 9;
		Integer sourceTime = 0;
		String filePath = "D:\\chronoweb\\results\\incr_8_4.txt"; // results
		String graphFile = "D:\\chronoweb\\datasets\\bitcoin-alpha.txt"; // input graph

		String host = "http://localhost/chronoweb";

		// 1. SUBSCRIBE
		subscribe(host, sourceVertex, sourceTime);

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

			// 2. POST
			postEdgeEvent(host, from, to, time);

			Long startTime = System.currentTimeMillis();
			// 3. GET
			getReachability(host, sourceVertex, sourceTime);
			Long computationTime = System.currentTimeMillis() - startTime;

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
	private static void getReachability(String host, Integer sourceVertex, Integer sourceTime) throws IOException {
		String getURLString = String.format("%s/gammaTable/%s/IsAfterReachability/%s", host, sourceTime, sourceVertex);

		URL getURL = new URL(getURLString);

		HttpURLConnection getCon = (HttpURLConnection) getURL.openConnection();
		getCon.setRequestMethod("GET");
		System.out.println("\tGET " + getURLString + " " + getCon.getResponseCode());

	}

	@SuppressWarnings("deprecation")
	private static void postEdgeEvent(String host, Integer from, Integer to, Integer time) throws IOException {
		String postURLString = String.format("%s/graph/%s|label|%s_%s", host, from, to, time);
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
		System.out.println("POST " + postURLString + " " + postCon.getResponseCode());

		postCon.disconnect();
	}

	@SuppressWarnings("deprecation")
	public static void subscribe(String host, Integer sourceVertex, Integer sourceTime) throws IOException {
		String subURLString = String.format("%s/graph/%s/IsAfterReachability/%s", host, sourceTime, sourceVertex);
		URL subURL = new URL(subURLString);
		HttpURLConnection subCon = (HttpURLConnection) subURL.openConnection();
		subCon.setRequestMethod("PUT");
		subCon.setDoOutput(true);
		subCon.setRequestProperty("Content-Type", "application/json");
		subCon.setRequestProperty("Accept", "application/json");

		OutputStreamWriter osw = new OutputStreamWriter(subCon.getOutputStream());
		osw.write("{ \"k\": \"v\"}");
		osw.flush();
		osw.close();

		System.out.println("SUBSCRIBE " + subURLString + " " + subCon.getResponseCode());
		subCon.disconnect();
	}
}
