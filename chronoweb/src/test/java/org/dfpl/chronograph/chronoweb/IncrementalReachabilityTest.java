package org.dfpl.chronograph.chronoweb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IncrementalReachabilityTest {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		// INPUT
		Integer source = 0;
		Integer neighbors = 1000;
		String filePath = "D:\\chronoweb\\results\\incr_8_4.txt";

		OutputStreamWriter osw;
		String host = "http://localhost/chronoweb";
		Integer baseTime = 9999;

		Map<String, String> payload = new HashMap<>();
		payload.put("k", "v");

		// 1. SUBSCRIBE
		String subURLString = String.format("%s/graph/%s/IsAfterReachability/%s", host, baseTime - 1, source);
		URL subURL = new URL(subURLString);
		HttpURLConnection subCon = (HttpURLConnection) subURL.openConnection();
		subCon.setRequestMethod("PUT");
		subCon.setDoOutput(true);
		subCon.setRequestProperty("Content-Type", "application/json");
		subCon.setRequestProperty("Accept", "application/json");
		osw = new OutputStreamWriter(subCon.getOutputStream());
		osw.write("{ \"k\": \"v\"}");
		osw.flush();
		osw.close();
		System.out.println("SUBSCRIBE " + subURLString + " " + subCon.getResponseCode());
		subCon.disconnect();

		for (int destination = 1; destination < neighbors + 1; destination++) {
			Integer eventTime = baseTime + 1;

			// 2. POST
			String postURLString = String.format("%s/graph/%s|label|%s_%s", host, source, 1, eventTime);
			URL postURL = new URL(postURLString);
			HttpURLConnection postCon = (HttpURLConnection) postURL.openConnection();
			postCon.setRequestMethod("PUT");
			postCon.setDoOutput(true);
			postCon.setRequestProperty("Content-Type", "application/json");
			postCon.setRequestProperty("Accept", "application/json");
			osw = new OutputStreamWriter(postCon.getOutputStream());
			osw.write("{ \"k\": \"v\"}");
			osw.flush();
			osw.close();
			System.out.println("POST " + postURLString + " " + postCon.getResponseCode());
			postCon.disconnect();

			// 3. GET
			Long startTime = System.currentTimeMillis();
			String getURLString = String.format("%s/gammaTable/%s/IsAfterReachability/%s", host, baseTime - 1, source);

			URL getURL = new URL(getURLString);

			HttpURLConnection getCon = (HttpURLConnection) getURL.openConnection();
			getCon.setRequestMethod("GET");
			System.out.println("\tGET " + getURLString + " " + getCon.getResponseCode());

			Long computationTime = System.currentTimeMillis() - startTime;

			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			writer.append(computationTime.toString()).append("\n");
			writer.close();
		}
	}

}
