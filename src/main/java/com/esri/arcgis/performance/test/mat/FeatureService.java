package com.esri.arcgis.performance.test.mat;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class FeatureService {

  private Random random = new Random();

  private CloseableHttpClient httpClient = null;
  private String serviceName;
  private String servicesUrl;
  private String keyspace = "esri_ds_data";

  // 36 parameters
  private String where = "";
  private String objectIds = "";
  private String time = "";
  private String geometry = "";
  private String geometryType = "esriGeometryEnvelope";
  private String geohash = "";
  private String inSR = "";
  private String spatialRel = "esriSpatialRelIntersects";
  private String distance = "";
  private String units = "esriSRUnit_Foot";
  private String relationParam = "";
  private String outFields = "";
  private boolean returnGeometry = true;
  private String maxAllowableOffset ="";
  private String geometryPrecision = "";
  private String outSR = "";
  private String gdbVersion = "";
  private boolean returnDistinctValues = false;
  private boolean returnIdsOnly = false;
  private boolean returnCountOnly = false;
  private boolean returnExtentOnly = false;
  private String orderByFields = "";
  private String groupByFieldsForStatistics = "";
  private String outStatistics = "";
  private boolean returnZ = false;
  private boolean returnM = false;
  private String multipatchOption = "";
  private String resultOffset = "";
  private int resultRecordCount = 10000;
  private String lod = "";
  private String lodType = "square";
  private String lodSR = "";
  private String timeInterval = "";
  private String timeUnit = "minutes";
  private boolean returnClusters = false;
  private boolean returnFullLodGrid = false;
  private String f = "json";

  private int timeoutInSeconds = 60;
  private String cookie = null;

  FeatureService(String servicesUrl, String serviceName, int timeoutInSeconds, boolean requireCookie) {
    this.serviceName = serviceName;
    this.servicesUrl = servicesUrl.endsWith("/") ? servicesUrl : servicesUrl + "/";
    this.outFields = "*";

    if (requireCookie) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader("./mat-access-cookie.txt"));
        String line = reader.readLine();
        if (line != null) cookie = line;
        else System.err.println("Error in reading required 'Cookie' string.");
      } catch (Exception ex) {
        System.err.println("Error in reading required 'Cookie' string.");
        ex.printStackTrace();
      }
    }

    this.timeoutInSeconds = timeoutInSeconds;
    createClient();
  }

  private void createClient() {
    try {
      if (cookie == null) {
        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
//        SSLContext sslContext = SSLContextBuilder
//                .create()
//                .loadTrustMaterial(new TrustSelfSignedStrategy())
//                .build();

        // setup a Trust Strategy that allows all certificates.
        //
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
          public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            return true;
          }
        }).build();


        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);


        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
        // PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(timeoutInSeconds * 1000);
        requestBuilder.setSocketTimeout(timeoutInSeconds * 1000);
        requestBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestBuilder.build())
                .setSSLContext(sslContext)
//                .setSSLSocketFactory(connectionFactory)
                .setConnectionManager(connectionManager)
                .build();
      } else {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(timeoutInSeconds * 1000);
        requestBuilder.setSocketTimeout(timeoutInSeconds * 1000);
        requestBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestBuilder.build())
                .setConnectionManager(connectionManager)
                .build();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * @deprecated
   */
  private void createClient1() {
    String userName = System.getenv("A4IOT_USER");
    String password = System.getenv("A4IOT_PASSWORD");
    if (userName != null && password != null) {
      CredentialsProvider provider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials
              = new UsernamePasswordCredentials(userName, password);
      provider.setCredentials(AuthScope.ANY, credentials);

      RequestConfig.Builder requestBuilder = RequestConfig.custom();
      requestBuilder.setConnectTimeout(timeoutInSeconds * 1000);
      requestBuilder.setSocketTimeout(timeoutInSeconds * 1000);
      requestBuilder.setConnectionRequestTimeout(timeoutInSeconds * 1000);

//      HttpClientBuilder builder = HttpClientBuilder.create();
//      builder.setDefaultCredentialsProvider(provider);
//      builder.setDefaultRequestConfig(requestBuilder.build());
////    builder.disableAutomaticRetries();
//      httpClient = builder.build();

      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
      httpClient = HttpClients.custom()
              .setDefaultCredentialsProvider(provider)
              .setDefaultRequestConfig(requestBuilder.build())
              .setConnectionManager(connectionManager)
              .build();
    }
//    else {
//      System.out.println("ERROR: You must set 2 environment variables: A4IOT_USER and A4IOT_PASSWORD from accessing your A4IoT services. ");
//      System.out.println("       If your password contains some special characters, you need to add single quote in the beginning and the end of your password.");
//      System.exit(0);
//    }
    else {
      try {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            //System.out.println("getAcceptedIssuers =============");
            return null;
          }

          @Override
          public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //System.out.println("checkClientTrusted =============");
          }

          @Override
          public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //System.out.println("checkServerTrusted =============");
          }
        }}, new SecureRandom());

        httpClient = HttpClients
                .custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  Tuple getCount(String where) {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    return getCount();
  }

  Tuple getCount(String where, String boundingBox) {
    resetParameters2InitialValues();
    this.geometry = boundingBox;
    this.where = where == null? "" : where.trim();
    return getCount();
  }

  Tuple getCount() {
    this.returnCountOnly = true;
    long totalCount = 0L;
    int errorCode = 0;
    long start = System.currentTimeMillis();
    String queryParameters = composeGetRequestQueryParameters();
    String response = executeRequest(queryParameters, false);
    if (response != null) {
      System.out.println(response);
      if (response.trim().startsWith("{")) {
        JSONObject obj = new JSONObject(response);
        if (obj.optJSONObject("error") == null) {
          totalCount = obj.getLong("count");
        } else {
          errorCode = 3;
          System.out.print("Request getCount failed -> " + response);
        }
      } else {
        errorCode = 2;
        System.out.println("Error: getCount ==> " + response);
      }
    } else {
      errorCode = 1;
      System.out.println("Error: getCount ==> response is null.");
    }
    return new Tuple(System.currentTimeMillis() - start, totalCount, errorCode);
  }

  Tuple getFeaturesWithWhereClauseAndBoundingBoxAndTimeExtent(String where, String boundingBox, String timeExtent, boolean closeClient)  throws Exception {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    this.geometry = boundingBox;
    this.time = timeExtent;
    return getFeatures(false, closeClient);
  }

  Tuple getFeaturesWithWhereClauseAndBoundingBoxAndTimeExtentAndGroupBy(String where, String boundingBox, String timeExtent,
                                                                        int lod, int timeInterval, String timeUnits, boolean closeClient)  throws Exception {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    this.geometry = boundingBox;
    this.time = timeExtent;
    this.timeInterval = timeInterval + "";
    this.timeUnit = timeUnits;
    this.lod = lod + "";
    return getFeatures(true, closeClient);
  }

  Tuple getFeaturesWithWhereClauseAndRandomOffset(String where, boolean takeOffset, boolean closeClient)  throws Exception {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    // add random number of records skipped
    if (takeOffset) {
      int skip = random.nextInt(this.resultRecordCount / 2);
      this.resultOffset = skip + "";
    } else {
      this.resultOffset = "0";
    }
    return getFeatures(false, closeClient);
  }

  Tuple getFeaturesWithWhereClauseAndBoundingBox(String where, String boundingBox, boolean closeClient)  throws Exception{
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    this.geometry = boundingBox;
    return getFeatures(false, closeClient);
  }

  Tuple getFeaturesWithTimeExtent(String where, String timeString, boolean closeClient) throws Exception { // such as 1547480515000, 1547480615000
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    this.time = timeString;
    return getFeatures(false, closeClient);
  }

  Tuple getFeaturesWithWhereClause(String where, boolean closeClient) throws Exception {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    return getFeatures(false, closeClient);
  }

  Tuple doGroupByStats(String where, String groupByFdName, String outStats, String boundingBox, boolean closeClient) throws Exception {
    resetParameters2InitialValues();
    this.where = where == null? "" : where.trim();
    this.groupByFieldsForStatistics = groupByFdName;
    this.outStatistics = outStats;
    if (boundingBox != null) this.geometry = boundingBox;
    return getFeatures(false, closeClient);
  }

  private Tuple getFeatures(boolean isSpaceTime, boolean closeClient) throws Exception {
    long start = System.currentTimeMillis();
    String queryParameters = composeGetRequestQueryParameters();
    String response = executeRequest(queryParameters, closeClient);
    long numFeatures = 0;
    int errorCode = 0;
    if (response != null) {
      //System.out.println(response);
      if (response.trim().startsWith("{")) {
        JSONObject obj = new JSONObject(response);
        if (obj.optJSONObject("error") == null) {
          boolean exceededTransferLimit = obj.optBoolean("exceededTransferLimit");
          JSONArray features = null;
          JSONObject spaceTimeFeatures = null;
          if (isSpaceTime) {
            spaceTimeFeatures = obj.getJSONObject("spaceTimeFeatures");
            if (spaceTimeFeatures != null) {
              Map<String, Object> stFeatures = spaceTimeFeatures.toMap();
              for (String key : stFeatures.keySet()) {
                System.out.println(key + " -> " + stFeatures.get(key));
              }
            }
          } else {
            features = obj.getJSONArray("features");
            if (features.length() > 0) {
              // print a random feature
              int index = random.nextInt() % features.length();
              index = index < 0 ? (-1) * index : index;
              System.out.println(features.get(index));
            }
            numFeatures = features.length();
            System.out.println("# of features returned: " + features.length() + ", exceededTransferLimit: " + exceededTransferLimit + ", offset: " + this.resultOffset);
          }
        } else {
          errorCode = 3;
          System.out.print("Request getFeatures failed -> " + response);
        }
      } else {
        errorCode = 2;
        System.out.println("Error: getFeatures ==> " + response);
      }
    } else {
      errorCode = 1;
      System.out.println("Error: getFeatures ==> response is null.");
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

  private String composeGetRequestQueryParameters() {
    StringBuilder request = new StringBuilder();
    request.append("where=").append(URLEncoder.encode(where));
    request.append("&objectIds=").append(objectIds);
    request.append("&time=").append(URLEncoder.encode(time));
    request.append("&geometry=").append(geometry);
    request.append("&geometryType=").append(geometryType);
    request.append("&geohash=").append(geohash);
    request.append("&inSR=").append(inSR);
    request.append("&spatialRel=").append(spatialRel);
    request.append("&distance=").append(distance);
    request.append("&units=").append(units);
    request.append("&relationParam=").append(relationParam);
    request.append("&outFields=").append(outFields);
    request.append("&returnGeometry=").append(returnGeometry);
    request.append("&maxAllowableOffset=").append(maxAllowableOffset);
    request.append("&geometryPrecision=").append(geometryPrecision);
    request.append("&outSR=").append(outSR);
    request.append("&gdbVersion=").append(gdbVersion);
    request.append("&returnDistinctValues=").append(returnDistinctValues);
    request.append("&returnIdsOnly=").append(returnIdsOnly);
    request.append("&returnCountOnly=").append(returnCountOnly);
    request.append("&returnExtentOnly=").append(returnExtentOnly);
    request.append("&orderByFields=").append(orderByFields);
    request.append("&groupByFieldsForStatistics=").append(groupByFieldsForStatistics);
    request.append("&outStatistics=").append(URLEncoder.encode(outStatistics));
    request.append("&returnZ=").append(returnZ);
    request.append("&returnM=").append(returnM);
    request.append("&multipatchOption=").append(multipatchOption);
    request.append("&resultOffset=").append(resultOffset);
    request.append("&resultRecordCount=").append(resultRecordCount);
    request.append("&lod=").append(lod);
    request.append("&lodType=").append(lodType);
    request.append("&lodSR=").append(lodSR);
    request.append("&timeInterval=").append(timeInterval);
    request.append("&timeUnit=").append(timeUnit);
    request.append("&returnClusters=").append(returnClusters);
    request.append("&returnFullLodGrid=").append(returnFullLodGrid);
    request.append("&f=").append(f);

    return request.toString();
  }

  private void resetParameters2InitialValues() {
    this.where = "";
    this.objectIds = "";
    this.time = "";
    this.geometry = "";
    this.geometryType = "esriGeometryEnvelope";
    this.geohash = "";
    this.inSR = "";
    this.spatialRel = "esriSpatialRelIntersects";
    this.distance = "";
    this.units = "esriSRUnit_Foot";
    this.relationParam = "";
    this.outFields = "*";
    this.returnGeometry = true;
    this.maxAllowableOffset ="";
    this.geometryPrecision = "";
    this.outSR = "";
    this.gdbVersion = "";
    this.returnDistinctValues = false;
    this.returnIdsOnly = false;
    this.returnCountOnly = false;
    this.returnExtentOnly = false;
    this.orderByFields = "";
    this.groupByFieldsForStatistics = "";
    this.outStatistics = "";
    this.returnZ = false;
    this.returnM = false;
    this.multipatchOption = "";
    this.resultOffset = "";
    this.resultRecordCount = 10000;
    this.lod = "";
    this.lodType = "square";
    this.lodSR = "";
    this.timeInterval = "";
    this.timeUnit = "minutes";
    this.returnClusters = false;
    this.returnFullLodGrid = false;
    this.f = "json";
  }

  private String executeRequest(String queryParameters, boolean closeClient) {
    CloseableHttpClient client =  httpClient;
    //HttpClientBuilder.create().build();
    try {
      String url = servicesUrl + serviceName + "/FeatureServer/0/query?" + queryParameters;
      System.out.println(Thread.currentThread() + ": " +  url);
      //long start = System.currentTimeMillis();
      String result = Utils.executeHttpGET(client, url, closeClient, cookie);
      //System.out.println("======> Total request time: " + (System.currentTimeMillis() - start)  + " ms, service name: " + serviceName);
      return result;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  JSONObject getFieldStats(String fieldName) {
    String stats = "[" +
        "{\"statisticType\":\"min\",\"onStatisticField\":\"" + fieldName + "\",\"outStatisticFieldName\":\"min\"}," +
        "{\"statisticType\":\"max\",\"onStatisticField\":\"" + fieldName + "\",\"outStatisticFieldName\":\"max\"}]";
    this.outStatistics = stats;
    this.where = "1=1";
    this.resultRecordCount = 100000;
    //long start = System.currentTimeMillis();
    String queryParameters = composeGetRequestQueryParameters();
    String response = executeRequest(queryParameters, true);
    JSONObject jsonObject = new JSONObject(response);
    System.out.println(response);
    JSONObject f = jsonObject.getJSONArray("features").getJSONObject(0);
    return f.getJSONObject("attributes");
  }

  List<String> getFieldUniqueValues(String fieldName) {
    List<String> uniqueValues = new LinkedList<String>();

    //this.groupByFieldsForStatistics = fieldName;
    //this.outStatistics = "[{\"statisticType\":\"count\",\"onStatisticField\":\"" + fieldName + "\",\"outStatisticFieldName\":\"count\"}]";
    this.where = "1=1";
    this.returnDistinctValues = true;
    this.outFields = fieldName;
    this.resultRecordCount = 100000;
    this.orderByFields = fieldName;
    String queryParameters = composeGetRequestQueryParameters();
    String response = executeRequest(queryParameters, true);
    JSONObject jsonObject = new JSONObject(response);
    JSONArray features = jsonObject.getJSONArray("features");
    for (int i=0; i<features.length(); i++) {
      JSONObject f = features.getJSONObject(i);
      uniqueValues.add(f.getJSONObject("attributes").get(fieldName).toString());
    }
    System.out.println(response);
    return uniqueValues;
  }

}
