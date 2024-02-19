package org.dfpl.chronograph.khronos.dataloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.dfpl.chronograph.chronoweb.Server;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;

import io.vertx.ext.web.FileUpload;

/**
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
public class DataLoader {
	public static void EgoFacebook(FileUpload file, Graph graph, String label) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file.uploadedFileName()));

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

	public static void EUEmailCommunicationNetwork(FileUpload file, Graph graph, String label) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file.uploadedFileName()));

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

	public static void SxMathOverflow(FileUpload file, Graph graph, String label) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file.uploadedFileName()));

		int cnt = 0;
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] arr = line.split("\\s");
			Edge e = graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[1]), label);
			e.addEvent(Long.parseLong(arr[2]));
			if (++cnt % 1000 == 0)
				Server.logger.debug("[sx-mathoverflow] read lines " + cnt + " ... ");
		}
		Server.logger.debug("[sx-mathoverflow] read lines " + cnt + " completed ");
		r.close();
	}

	public static void tcpSample(FileUpload file, Graph graph, String label) throws IOException {

		BufferedReader r = new BufferedReader(new FileReader(file.uploadedFileName()));

		int cnt = 0;
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] arr = line.split("\t");
			Edge e = graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[1]), label);
			e.addEvent(Long.parseLong(arr[2]));
			if (++cnt % 1000 == 0)
				Server.logger.debug("[tcp_sample] read lines " + cnt + " ... ");
		}
		Server.logger.debug("[tcp_sample] read lines " + cnt + " completed ");
		r.close();
	}

	public static void upload(FileUpload file, Graph graph, String label) throws IOException {

		BufferedReader r = new BufferedReader(new FileReader(file.uploadedFileName()));

		int cnt = 0;
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] arr = line.split("\\s");
			Edge e = graph.addEdge(graph.addVertex(arr[0]), graph.addVertex(arr[1]), label);
			if (arr.length == 3)
				e.addEvent(Long.parseLong(arr[2]));
			if (++cnt % 1000 == 0)
				Server.logger.debug("[" + file.name() + "] read lines " + cnt + " ... ");
		}
		Server.logger.debug("[" + file.name() + "] read lines " + cnt + " completed ");
		r.close();
	}
}
