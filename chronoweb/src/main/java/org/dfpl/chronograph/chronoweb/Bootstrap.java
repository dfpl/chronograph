package org.dfpl.chronograph.chronoweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import io.vertx.core.json.JsonObject;

public class Bootstrap {

	public static void setLogger() {
		Configurator.setRootLevel(Level.OFF);
		Configurator.setLevel(Server.class, Level.DEBUG);
		Server.logger = LogManager.getLogger(Server.class);
	}

	public static String readFile(String fileName) throws IOException {

		BufferedReader r = new BufferedReader(new FileReader(fileName));
		String content = "";
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			content += line + "\n";
		}
		r.close();
		return content;
	}

	public static String readFile(InputStream stream) throws IOException {

		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		String content = "";
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			content += line + "\n";
		}
		r.close();
		return content;
	}

	public static void setConfiguration(String[] args) {
		try {
			Server.configuration = new JsonObject(readFile(args[0]));
		} catch (Exception e) {
			if (e instanceof IOException)
				System.out.println("args[0] not found. try internal configuration.json");
			else
				System.out.println("args[0] has a syntax problem. try internal configuration.json");
			try {
				Server.configuration = new JsonObject(
						readFile(Bootstrap.class.getResourceAsStream("/configuration.json")));
				System.out.println("Chronoweb is running as developer mode (IDE)");
			} catch (Exception e1) {
				try {
					Server.configuration = new JsonObject(
							readFile(Bootstrap.class.getResourceAsStream("/resources/configuration.json")));
					System.out.println("Chronoweb is running as user mode (Runnable Jar)");
				} catch (Exception e2) {
					System.out.println("No configuration found. System Terminated");
					System.exit(1);
				}
			}
		}
	}

	public static void setPort() {
		try {
			Server.port = Server.configuration.getInteger("port");
		} catch (Exception e) {
			System.out.println("Invalid port. System Terminated.");
			System.exit(1);
		}
	}

	public static void setBackend() {
		try {
			String type = Server.configuration.getString("data_storage_type");
			if (type.equals("memory")) {
				Server.backendType = "memory";
				Server.connectionString = Server.configuration.getString("db_connection_string");
				System.out.println("backendType:memory");
			} else if (type.equals("persistent")) {
				Server.backendType = "persistent";
				Server.dbName = Server.configuration.getString("db_name");
				Server.connectionString = Server.configuration.getString("db_connection_string");
				System.out.println("backendType:persistent");
				System.out.println("dbName:" + Server.dbName);
				System.out.println("connectionString:" + Server.connectionString);
			} else {
				System.out.println("Invalid data_storage_type. System Terminated.");
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Invalid configuration. System Terminated." + e.getMessage());
			System.exit(1);
		}
	}

	public static void setNumberOfCore() {
		try {
			Server.numberOfVerticles = Server.configuration.getInteger("number_of_verticles");
		} catch (Exception e) {
			Server.numberOfVerticles = Runtime.getRuntime().availableProcessors() + 1;
			System.out.println("Number of Verticles is set to # cores + 1");
		}
	}

	public static void initializeGammaTable() {
		try {
			Server.gammaBaseDirectory = Server.configuration.getString("gamma_base_directory");
			File base = new File(Server.gammaBaseDirectory);
			if (!base.isDirectory()) {
				System.out.println("Invalid gamma base directory ( " + Server.gammaBaseDirectory + " ). System Terminated.");
				System.exit(1);
			}
			org.apache.commons.io.FileUtils.cleanDirectory(base);
		} catch (Exception e) {
			System.out.println("Invalid gamma base directory. System Terminated." + e.getMessage());
			System.exit(1);
		}
		Server.gammaDBName = Server.configuration.getString("gamma_database");
		System.out.println("gammaDBName:" + Server.gammaDBName);

	}

	public static void bootstrap(String[] args) {
		setLogger();
		setConfiguration(args);
		setPort();
		setBackend();
		setNumberOfCore();
		initializeGammaTable();
	}
}
