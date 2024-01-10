package org.dfpl.chronograph.khronos.memory.dataloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.dfpl.chronograph.chronoweb.Server;

import com.tinkerpop.blueprints.Graph;

public class DataLoader {
	public static void EgoFacebook(String baseURL, Graph graph, String label) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(baseURL + "\\static\\facebook_combined.txt"));

		int cnt = 0;
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] arr = line.split("\\s");
			graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[1]), label);
			graph.addEdge(graph.addVertex(arr[1]), graph.addVertex(arr[0]), label);
			if (++cnt % 1000 == 0)
				Server.logger.debug("[EgoFacebook] read lines " + cnt + " ... ");
		}
		Server.logger.debug("[EgoFacebook] read lines " + cnt + " completed ");
		r.close();
	}

	public static void EUEmailCommunicationNetwork(String baseURL, Graph graph, String label) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(baseURL + "\\static\\Email-EuAll.txt"));

		int cnt = 0;
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			if (line.startsWith("#"))
				continue;
			String[] arr = line.split("\t");
			graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[1]), label);
			if (++cnt % 1000 == 0)
				Server.logger.debug("[Email-EuAll] read lines " + cnt + " ... ");
		}
		Server.logger.debug("[Email-EuAll] read lines " + cnt + " completed ");
		r.close();
	}
}
