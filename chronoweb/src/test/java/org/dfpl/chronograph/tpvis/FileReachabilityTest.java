package org.dfpl.chronograph.tpvis;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.concurrent.TimeUnit;

public class FileReachabilityTest {
    static String HOST;
    static String EDGE_LABEL;
    static String KAIROS_PROGRAM_NAME;
    static String SOURCE_VERTEX_ID;
    static String SOURCE_TIME;
    static String GRAPH_FILE;

    @BeforeClass
    public static void setUpBeforeClass() {
        HOST = "http://localhost/chronoweb";
        EDGE_LABEL = "label";
        KAIROS_PROGRAM_NAME = "OutIsAfterReachability";
        SOURCE_VERTEX_ID = "8";
        SOURCE_TIME = "0";
        GRAPH_FILE = "D:\\tpvis\\datasets\\CollegeMsg.txt";
    }

    @Test
    public void testIsOutAfterReachability() throws IOException, InterruptedException {

        // 1. Subscribe to source vertex event
        subscribe();
        // 2. Parse CollegeMsg file
        BufferedReader graphReader = new BufferedReader(new FileReader(GRAPH_FILE));
        int lineRead = 0;

        while (true) {
            String graphLine = graphReader.readLine();
            if (graphLine == null)
                break;

            String[] edgeEventArray = graphLine.split("\\s");

            String from = edgeEventArray[0];
            String to = edgeEventArray[1];
            String time = edgeEventArray[2];

            postEdgeEvent(from, to, time);

            TimeUnit.SECONDS.sleep(2);

            if (++lineRead % 10000 == 0)
                System.out.println(lineRead + " lines read...");
        }

        graphReader.close();
    }

    private static void subscribe() throws IOException {
        JsonObject expectedResult = new JsonObject()
                .put("time", 0).put("source", SOURCE_VERTEX_ID).put("program", KAIROS_PROGRAM_NAME).put("edgeLabel", EDGE_LABEL);

        String subsribeURL = String.format("%s/graph/%s/OutIsAfterReachability/%s/%s", HOST, SOURCE_TIME, EDGE_LABEL, SOURCE_VERTEX_ID);
        URL subscribeURL = new URL(subsribeURL);
        HttpURLConnection subscribeCon = (HttpURLConnection) subscribeURL.openConnection();
        subscribeCon.setRequestMethod("PUT");

        Assert.assertEquals(200, subscribeCon.getResponseCode());
    }

    private static void postEdgeEvent(String from, String to, String time) throws IOException {
        String postURLString = String.format("%s/graph/%s|label|%s_%s", HOST, from, to, time);
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
        Assert.assertEquals(200, postCon.getResponseCode());

        postCon.disconnect();
    }
}
