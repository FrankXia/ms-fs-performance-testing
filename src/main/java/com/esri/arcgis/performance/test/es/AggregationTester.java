package com.esri.arcgis.performance.test.es;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This testing program is to test triangle/square/hexagon aggregation
 * results are what we expected from an ArcGIS Velocity cluster.
 */
public class AggregationTester {

    private CloseableHttpClient httpClient = null;
    private String servicesUrl = "http://localhost:9200/";
    private String serviceName = "safegraph_5milb";
    private String outputFolder = "./";

    public static void main(String[] args ) {
        testingAggregations(args);
//        compareAggregationResults();
    }

    // get testing results
    static void testingAggregations(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.es.AggregationTester <service name> <save output> {<output folder>}");
            System.exit(0);
        }
        String serviceName = args[0];
        boolean saveOutput = Boolean.parseBoolean(args[1]);
        String outputFolder = saveOutput? args[2] : null;

        AggregationTester aggregationTester = new AggregationTester();
        aggregationTester.createHttpClient();
        aggregationTester.serviceName = serviceName;
        if (saveOutput && outputFolder != null) {
            aggregationTester.outputFolder = outputFolder.endsWith("/") ? outputFolder : outputFolder +"/";
        }
        int minLod = 3;
        int maxLod = 18;

        for (int lod = minLod; lod < maxLod; lod++) {
            aggregationTester.compareTriangleGridAggregations(lod, saveOutput);
        }

        // to compare StringSquareGrid and LongSquareGrid, one needs to setup the square aggregation style lod more than 31.
        for (int lod = minLod; lod < maxLod; lod++) {
            aggregationTester.compareSquareGridAggregations(lod, saveOutput);
        }

        for (int lod = minLod; lod < maxLod; lod++) {
            aggregationTester.compareHexagonGridAggregations(lod, saveOutput);
        }
    }

    // compare testing results
    static void compareAggregationResults() {
        compareBeforeAfterFixResultsFor730();
        compareResults730With792();
    }

    private void compareHexagonGridAggregations(int lod, boolean save) {
        List<Bucket> stringBuckets = testStringHexagonGridAggregation(lod, save);
        List<Bucket> stringLongBuckets = testStringLongHexagonGridAggregation(lod, save);
        List<Bucket> longBuckets = testLongHexagonGridAggregation(lod, save);
        int identicalCount = 0;
        for (Bucket bucket1 : stringBuckets) {
            for (Bucket bucket2 : longBuckets ) {
                if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                    identicalCount++;
                }
            }
        }
        int identicalCount2 = 0;
        for (Bucket bucket1 : stringLongBuckets) {
            for (Bucket bucket2 : longBuckets ) {
                if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                    identicalCount2++;
                }
            }
        }
        int identicalCount3 = 0;
        for (Bucket bucket1 : stringLongBuckets) {
            for (Bucket bucket2 : stringBuckets ) {
                if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                    identicalCount3++;
                }
            }
        }
        boolean isIdentical = (identicalCount != 0 && identicalCount == stringBuckets.size());
        if  (isIdentical)
            System.out.println("String and long hexagon grid aggregations are identical? " + isIdentical);
        else
            System.err.println("String and long hexagon grid aggregations are identical? " + isIdentical);

        boolean isIdentical2 =  (identicalCount2 != 0 && identicalCount2 == stringLongBuckets.size());
        if  (isIdentical2)
            System.out.println("String-long and long hexagon grid aggregations are identical? " + isIdentical2);
        else
            System.err.println("String-long and long hexagon grid aggregations are identical? " + isIdentical2);

        boolean isIdentical3 = (identicalCount3 != 0 && identicalCount3 == stringLongBuckets.size());
        if  (isIdentical3)
            System.out.println("String-long and string hexagon grid aggregations are identical? " + isIdentical3);
        else
            System.err.println("String-long and string hexagon grid aggregations are identical? " + isIdentical3);

        System.out.println( stringBuckets.size() +  " --------------------------------------------------------------- " + lod);
    }
    private void compareTriangleGridAggregations(int lod, boolean save) {
        List<Bucket> stringBuckets = testStringTriangleGridAggregation(lod, save);
        List<Bucket> longBuckets = testLongTriangleGridAggregation(lod, save);
        int identicalCount = 0;
        for (Bucket bucket1 : stringBuckets) {
            for (Bucket bucket2 : longBuckets ) {
                if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                    identicalCount++;
                }
            }
        }
        boolean isIdentical = (identicalCount != 0 && identicalCount == stringBuckets.size());
        if  (isIdentical)
            System.out.println("String and long triangle grid aggregations are identical? " + isIdentical);
        else
            System.err.println("String and long triangle grid aggregations are identical? " + isIdentical);
        System.out.println( stringBuckets.size() +  " --------------------------------------------------------------- " + lod);
    }
    private void compareSquareGridAggregations(int lod, boolean save) {
        List<Bucket> stringBuckets = testStringSquareGridAggregation(lod, save);
        List<Bucket> longBuckets = testLongSquareGridAggregation(lod, save);
        int identicalCount = 0;
        for (Bucket bucket1 : stringBuckets) {
            for (Bucket bucket2 : longBuckets ) {
                if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                    identicalCount++;
                }
            }
        }
        boolean isIdentical = (identicalCount != 0 && identicalCount == stringBuckets.size());
        if  (isIdentical)
            System.out.println("String and long square grid aggregations are identical? " + isIdentical);
        else
            System.err.println("String and long square grid aggregations are identical? " + isIdentical);

        System.out.println( stringBuckets.size() +  " --------------------------------------------------------------- " + lod);
    }

    private List<Bucket>  testStringSquareGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"StringSquareGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.square_102100_s\"," +
                "                \"precision\": " + lod +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder + serviceName + "-StringSquareGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }
    private List<Bucket>  testLongSquareGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"LongSquareGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.square_102100_l\"," +
                "                \"precision\": " + lod +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder + serviceName + "-LongSquareGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }

    private List<Bucket>  testStringTriangleGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"StringTriangleGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.pointytriangle_102100_s\"," +
                "                \"precision\": " + lod +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder +  serviceName + "-StringTriangleGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }
    private List<Bucket>  testLongTriangleGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"LongTriangleGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.pointytriangle_102100_l\"," +
                "                \"precision\": " + lod +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder +  serviceName + "-LongTriangleGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }

    private List<Bucket>  testStringHexagonGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"StringHexagonGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.pointytriangle_102100_s\"," +
                "                \"precision\": " +  lod +
                "                ,\"style\": \"pointyHexagon\"" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder +  serviceName + "-StringHexagonGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }
    private List<Bucket>  testStringLongHexagonGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"StringLongHexagonGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.pointytriangle_102100_s\"," +
                "                \"precision\": " +  lod +
                "                ,\"style\": \"pointyHexagon\"" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder +  serviceName + "-StringLongHexagonGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }
    private List<Bucket>  testLongHexagonGridAggregation(int lod, boolean save) {
        String queryJson = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"stems\": {" +
                "            \"LongHexagonGrid\": {" +
                "               \"size\": 100000," +
                "                \"field\": \"---geo_hash---.pointytriangle_102100_l\"," +
                "                \"precision\": " + lod +
                "                ,\"style\": \"pointyHexagon\"" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        String fileName = save? outputFolder +  serviceName + "-LongHexagonGrid-" + lod + ".txt" : null;
        return executeRequest(queryJson, fileName);
    }

    private void createHttpClient() {
        int timeoutInSeconds = 60;
        try {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(timeoutInSeconds * 1000);
        requestBuilder.setSocketTimeout(timeoutInSeconds * 1000);
        requestBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestBuilder.build())
                .setConnectionManager(connectionManager)
                .disableAutomaticRetries()
                .build();
    } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<Bucket> executeRequest(String queryJson, String outputFileName) {
        CloseableHttpClient client =  httpClient;
        List<Bucket> bucketList = new LinkedList<>();
        try {
            String url = servicesUrl + serviceName + "/_search";
            StringEntity entity = new StringEntity(queryJson);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // use Apache HttpClient
            long start = System.currentTimeMillis();
            CloseableHttpResponse response = client.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Code : " + responseCode + ", time: " + (System.currentTimeMillis() - start) + " ms");

            if (responseCode == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                response.close();
                //System.out.println(result);
                JSONObject jsonObject = new JSONObject(result.toString());
                JSONObject aggregations = jsonObject.getJSONObject("aggregations").getJSONObject("stems");

                if (aggregations.getJSONArray("buckets") != null) {
                    JSONArray buckets = aggregations.getJSONArray("buckets");
                    System.out.println("# of buckets: " + buckets.length());
                    for (int i=0; i <  buckets.length(); i++) {
                        JSONObject jsonBucket = buckets.getJSONObject(i);
                        Bucket bucket = new Bucket();
                        bucket.key = jsonBucket.getString("key");
                        bucket.count = jsonBucket.getLong("doc_count");
                        bucketList.add(bucket);
                    }
                }

                //System.out.println("# of bucket -> " + bucketList.size());
                if (outputFileName != null) {
                    BufferedWriter writer = new BufferedWriter( new FileWriter(outputFileName));
                    writer.write(queryJson);
                    writer.newLine();
                    writer.write(result.toString());
                    writer.close();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bucketList;
    }

    //
    // compare before and after aggs results
    //

    private static void compareResults730With792() {
        String fix792Folder = "./fixed-es-792/";
        String fix730Folder = "./fixed-es-730/";
        String serviceName = "safegraph_50mil_bull";

        int minLod = 3;
        int maxLod = 18;

        for (int lod = minLod; lod < maxLod; lod++) {
            String stringTriangleGridAggFile730 = fix730Folder + serviceName + "-StringTriangleGrid-" + lod + ".txt";
            String stringTriangleGridAggFile792 = fix792Folder + serviceName + "-StringTriangleGrid-" + lod + ".txt";
            compareBeforeAndPostFixResults(stringTriangleGridAggFile730, stringTriangleGridAggFile792, lod, "triangle");
        }
        for (int lod = minLod; lod < maxLod; lod++) {
            String stringTriangleGridAggFile730 = fix730Folder + serviceName + "-StringHexagonGrid-" + lod + ".txt";
            String stringTriangleGridAggFile792 = fix792Folder + serviceName + "-StringHexagonGrid-" + lod + ".txt";
            compareBeforeAndPostFixResults(stringTriangleGridAggFile730, stringTriangleGridAggFile792, lod, "hexagon");
        }
    }
    private static void compareBeforeAfterFixResultsFor730() {
        String beforeFix730Folder = "./current-es-730/";
        String afterFix730Folder = "./fixed-es-730/";
        String serviceName = "safegraph_50mil_bull";

        int minLod = 3;
        int maxLod = 15;

        for (int lod = minLod; lod < maxLod; lod++) {
            String beforeStringTriangleGridAggFile = beforeFix730Folder + serviceName + "-StringTriangleGrid-" + lod + ".txt";
            String afterStringTriangleGridAggFile = afterFix730Folder + serviceName + "-StringTriangleGrid-" + lod + ".txt";
            compareBeforeAndPostFixResults(beforeStringTriangleGridAggFile, afterStringTriangleGridAggFile, lod, "triangle");
        }
        for (int lod = minLod; lod < maxLod; lod++) {
            // before applying fixes, the LongHexagonGrid aggregation returns correct results
            // while the StringHexagonGrid and StringLongHexagonGrid return wrong results.
            String beforeStringHexagonGridAggFile = beforeFix730Folder + serviceName + "-LongHexagonGrid-" + lod + ".txt";
            String afterStringHexagonGridAggFile = afterFix730Folder + serviceName + "-StringHexagonGrid-" + lod + ".txt";
            compareBeforeAndPostFixResults(beforeStringHexagonGridAggFile, afterStringHexagonGridAggFile, lod, "hexagon");
        }
    }
    private static void compareBeforeAndPostFixResults(String beforeFixFileName, String afterFixFileName, int lod, String style) {
        try {
            BufferedReader readerPre = new BufferedReader(new FileReader(beforeFixFileName));
            readerPre.readLine();
            BufferedReader readerAfter = new BufferedReader(new FileReader(afterFixFileName));
            readerAfter.readLine();

            List<Bucket> beforeBuckets = parseResults(readerPre.readLine());
            List<Bucket> afterBuckets = parseResults(readerAfter.readLine());

            int identicalCount = 0;
            for (Bucket bucket1 : beforeBuckets) {
                for (Bucket bucket2 : afterBuckets ) {
                    if (bucket1.key.equals(bucket2.key) && bucket1.count == bucket2.count) {
                        identicalCount++;
                    }
                }
            }
            System.out.println(style + " String grid aggregations are identical? " + (identicalCount != 0 && identicalCount == beforeBuckets.size()));
            System.out.println( beforeBuckets.size() +  " --------------------------------------------------------------- " + lod);



        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static List<Bucket> parseResults(String aggResults) {
        List<Bucket> bucketList = new LinkedList<>();

        JSONObject jsonObject = new JSONObject(aggResults);
        JSONObject aggregations = jsonObject.getJSONObject("aggregations").getJSONObject("stems");

        JSONArray buckets = aggregations.getJSONArray("buckets");
        System.out.println("# of buckets: " + buckets.length());
        for (int i=0; i <  buckets.length(); i++) {
            JSONObject jsonBucket = buckets.getJSONObject(i);
            Bucket bucket = new Bucket();
            bucket.key = jsonBucket.getString("key");
            bucket.count = jsonBucket.getLong("doc_count");
            bucketList.add(bucket);
        }

        return bucketList;
    }


    static class Bucket {
        String key;
        long count;
    }
}
