package org.dfpl.chronograph.chronoweb;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubscriptionTest {

    static String HOST;
    static String EDGE_LABEL;
    static String KAIROS_PROGRAM_NAME;
    static String SOURCE_VERTEX_ID;
    static String SOURCE_TIME;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HOST = "http://localhost/chronoweb";
        EDGE_LABEL = "label";
        KAIROS_PROGRAM_NAME = "OutIsAfterReachability";
        SOURCE_VERTEX_ID = "a";
        SOURCE_TIME = "0";
    }

    @Test
    public void testOutIsAfterReachability() throws IOException {
        JsonObject expectedResult = new JsonObject()
                .put("time", 0).put("source", SOURCE_VERTEX_ID).put("program", KAIROS_PROGRAM_NAME).put("edgeLabel", EDGE_LABEL);

        // 1. Subscribe time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        String subsribeURL = String.format("%s/graph/%s/OutIsAfterReachability/%s/%s", HOST, SOURCE_TIME, EDGE_LABEL, SOURCE_VERTEX_ID);
        URL subscribeURL = new URL(subsribeURL);
        HttpURLConnection subscribeCon = (HttpURLConnection) subscribeURL.openConnection();
        subscribeCon.setRequestMethod("PUT");

        Assert.assertEquals(200, subscribeCon.getResponseCode());

        // 2. PUT a|label|b_1
        postEdgeEvent("a", "b", "1");

        // 3. GET time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        expectedResult.put("gamma", new JsonObject().put("a", 0).put("b", 1));
        getReachability(expectedResult);

        // 4. PUT b|label|c_5
        postEdgeEvent("b", "c", "5");

        // 5. GET time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        expectedResult.put("gamma", new JsonObject().put("a", 0).put("b", 1).put("c", 5));
        getReachability(expectedResult);

        // 6. PUT c|label|d_3
        postEdgeEvent("c", "d", "3");
        // 7. GET time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        expectedResult.put("gamma", new JsonObject().put("a", 0).put("b", 1).put("c", 5));
        getReachability(expectedResult);

        // 8. PUT b|label|c_3
        postEdgeEvent("b", "c", "3");
        // 9. GET time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        expectedResult.put("gamma", new JsonObject().put("a", 0).put("b", 1).put("c", 3));
        getReachability(expectedResult);

        // 10. DELETE b|label|c_3
        delEdgeEvent("b", "c", "3");
        // 11. GET time: 0, kairosProgram: OutIsAfterReachability, edgeLabel: label, source vertex: a
        expectedResult.put("gamma", new JsonObject().put("a", 0).put("b", 1).put("c", 5));
        getReachability(expectedResult);
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

    private static void delEdgeEvent(String from, String to, String time) throws IOException {
        String postURLString = String.format("%s/graph/%s|label|%s_%s", HOST, from, to, time);
        URL postURL = new URL(postURLString);
        HttpURLConnection postCon = (HttpURLConnection) postURL.openConnection();
        postCon.setRequestMethod("DELETE");
        postCon.setDoOutput(true);
        postCon.setRequestProperty("Content-Type", "application/json");
        postCon.setRequestProperty("Accept", "application/json");

        OutputStreamWriter osw = new OutputStreamWriter(postCon.getOutputStream());
        osw.write("{ \"k\": \"v\"}");
        osw.flush();
        osw.close();
        Assert.assertEquals(204, postCon.getResponseCode());

        postCon.disconnect();
    }

    private void getReachability(JsonObject expectedResult) throws IOException {
        String getURLString = String.format("%s/gammaTable/%s/OutIsAfterReachability/%s/%s", HOST, SOURCE_TIME, EDGE_LABEL, SOURCE_VERTEX_ID);

        URL getURL = new URL(getURLString);
        HttpURLConnection getCon = (HttpURLConnection) getURL.openConnection();
        getCon.setRequestMethod("GET");

        Assert.assertEquals(200, getCon.getResponseCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(getCon.getInputStream()));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = br.readLine()) != null)
            stringBuilder.append(line);

        br.close();
        JsonObject actualResult = new JsonObject(stringBuilder.toString());
        Assert.assertEquals(expectedResult, actualResult);
    }
}
