package com.esri.arcgis.performance.test.es;

import com.esri.arcgis.performance.test.mat.Tuple;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;

public class ElasticsearchQuery {
    private CloseableHttpClient httpClient = null;
    private String searchUrl;
    private int timeoutInSeconds = 60;

    public ElasticsearchQuery(String servicesUrl, String serviceName) {
        this.searchUrl = (servicesUrl.endsWith("/") ? servicesUrl : servicesUrl + "/") +  serviceName + "/_search";
        createClient();
    }

    private void createClient() {
        // cut it from browser

        RequestConfig.Builder requestBuilder = RequestConfig.custom();
//        requestBuilder.setConnectTimeout(timeoutInSeconds * 1000);
//        requestBuilder.setSocketTimeout(timeoutInSeconds * 1000);
//        requestBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestBuilder.build())
                .setConnectionManager(connectionManager)
                .build();
    }

    Tuple getFeatures(String boundingBox, int timeoutInSeconds, boolean closeClient, BufferedWriter writer) throws Exception {
        long start = System.currentTimeMillis();
        int errorCode = 0;
        int numFeatures = 10000;
        String[] envelope = boundingBox.split(",");
        if (envelope.length != 4) throw new Exception("Invalid bounding box!");

        double topLeftX = Double.parseDouble(envelope[0]);
        double topLeftY = Double.parseDouble(envelope[3]);
        double bottomRightX = Double.parseDouble(envelope[2]);
        double bottomRightY = Double.parseDouble(envelope[1]);

        String json = searchSafeGraphBuilder(topLeftX, topLeftY, bottomRightX, bottomRightY);

        // output curl commands
        if (writer != null) {
            if (topLeftX > bottomRightX || topLeftY < bottomRightY) {
                System.out.println(boundingBox);
            } else {
                writer.write("curl -X POST -H \"Content-Type: application/json\" http://localhost:9200/safegraph/_search -d '" + json.replaceAll(" ", "") + "' > ./curl_standard_out/output-" + (Utils.increment++) + ".txt");
                writer.newLine();
            }
        } else {
            StringEntity entity = new StringEntity(json);
            HttpPost httpPost = new HttpPost(searchUrl);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Code : " + responseCode);

            if (responseCode == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                response.close();
            } else if (responseCode == 503)
                errorCode = 2;
            else
                errorCode = 1;
        }

        if (closeClient) {
            try {
                httpClient.close();
            } finally {
                httpClient.close();
            }
        }

        return new Tuple(System.currentTimeMillis() - start, numFeatures, errorCode);
    }

    private String searchSafeGraphBuilder(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
        return "{" +
                "  \"from\": 0," +
                "  \"size\": 10000," +
                "  \"query\": {" +
                "    \"match_all\": {" +
                "      \"boost\": 1" +
                "    }" +
                "  }," +
                "  \"post_filter\": {" +
                "    \"geo_bounding_box\": {" +
                "      \"Geometry\": {" +
                "        \"top_left\": [" +
                topLeftX + "," +
                topLeftY +
                "        ]," +
                "        \"bottom_right\": [" +
                bottomRightX + "," +
                bottomRightY +
                "        ]" +
                "      }," +
                "      \"validation_method\": \"STRICT\"," +
                "      \"type\": \"MEMORY\"," +
                "      \"ignore_unmapped\": false," +
                "      \"boost\": 1" +
                "    }" +
                "  }," +
                "  \"_source\": {" +
                "    \"includes\": [" +
                "      \"Geometry\"," +
                "      \"objectid\"," +
                "      \"globalid\"," +
                "      \"col_1\"," +
                "      \"col_2\"," +
                "      \"col_3\"," +
                "      \"col_4\"," +
                "      \"col_5\"," +
                "      \"col_6\"," +
                "      \"col_7\"," +
                "      \"Geometry\"" +
                "    ]," +
                "    \"excludes\": []" +
                "  }" +
                "}";
    }
}
