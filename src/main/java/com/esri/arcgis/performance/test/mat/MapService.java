package com.esri.arcgis.performance.test.mat;

import com.esri.core.geometry.Envelope2D;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

public class MapService {

  private int dpi = 96;
  private boolean transparent = true;
  private String format = "png";
  private String sizeString = "750,500";
  private String bbox = "10,10,50,50";
  private int bboxSR = 4326;
  private int imageSR = 102100;
  private String f = "image";
  private String dynamicLayers = "";

  private int featureLimit = 1000;
  private String aggregationStyle = "pointyHexagon";

  private CloseableHttpClient httpClient;
  private String serviceName;
  private String servicesUrl;
  private int timeoutInSeconds = 60; // seconds
  private String cookieOrToken = "";
  private String cookieFile = null; // "./mat-access-cookie.txt";
  private String tokenFile = "./velocity_access_token.txt";
  private boolean useCookie = false;

  public MapService(String servicesUrl, String serviceName, int timeoutInSeconds, String aggregationStyle, int featureLimit) {
    this.servicesUrl = servicesUrl.endsWith("/")? servicesUrl : servicesUrl + "/";
    this.serviceName = serviceName;
    this.aggregationStyle = aggregationStyle;
    this.featureLimit = featureLimit;
    this.timeoutInSeconds = timeoutInSeconds;

    useCookie = cookieFile != null;
    try {
      String cookieOrTokenFile = cookieFile==null?tokenFile:cookieFile;
      File cookieFile  = new File(cookieOrTokenFile);
      long currentTimeMinus24Hours = 24 * 60 * 60 * 1000;
      if (cookieFile.isFile() && ( (new Date()).getTime()  - cookieFile.lastModified() > currentTimeMinus24Hours) ) {
        System.err.println("The required 'Cookie' file, " + cookieFile.getAbsolutePath() + " is more than 24 hour old in MapService.");
        System.exit(0);
      }
      BufferedReader reader = new BufferedReader(new FileReader(cookieOrTokenFile));
      String line = reader.readLine();
      if (line != null) {
          cookieOrToken = line;
      }
      else System.err.println("Error in reading required 'Cookie' string.");
    } catch (Exception ex) {
      System.err.println("Error in reading required 'Cookie' string.");
      ex.printStackTrace();
    }

    createClient();
  }

  private void createClient() {
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

//    HttpClientBuilder builder = HttpClientBuilder.create();
//    builder.setDefaultRequestConfig(requestBuilder.build());
////    builder.disableAutomaticRetries();
//    httpClient = builder.build();

      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
      httpClient = HttpClients.custom()
              .setDefaultCredentialsProvider(provider)
              .setDefaultRequestConfig(requestBuilder.build())
              .setConnectionManager(connectionManager)
              .disableAutomaticRetries()
              .build();
    } else {
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

    this.timeoutInSeconds = timeoutInSeconds;
  }

  public Tuple exportMap(String bbox, int bboxSR, boolean showRequestUrl) {
    this.bbox = bbox;
    this.bboxSR = bboxSR;
    CloseableHttpClient client =  httpClient;
    String url = servicesUrl + serviceName + "/MapServer/export?" + getParameters();
    return Utils.executeHttpGETRequest(client, url, false, useCookie?cookieOrToken:null, showRequestUrl);
  }

  private String getParameters() {
    this.dynamicLayers = createDynamicLayers(featureLimit, serviceName);

    StringBuilder queryParameters = new StringBuilder();
    queryParameters.append("dpi=").append(dpi);
    queryParameters.append("&transparent=").append(transparent);
    queryParameters.append("&format=").append(format);
    queryParameters.append("&layers=show%3A0");
    // queryParameters.append("&dynamicLayers=").append(URLEncoder.encode(dynamicLayers));
    queryParameters.append("&bbox=").append(URLEncoder.encode(bbox));
    queryParameters.append("&bboxSR=").append(bboxSR);
    queryParameters.append("&imageSR=").append(imageSR);
    queryParameters.append("&size=").append(URLEncoder.encode(sizeString));
    queryParameters.append("&f=").append(f);
    if (!useCookie) {
      queryParameters.append("&").append(cookieOrToken);
    }
    return  queryParameters.toString();
  }

  public Tuple exportMap(String bbox, int bboxSR, String aggregationStyle, boolean showRequestUrl) {
    this.bbox = bbox;
    this.bboxSR = bboxSR;
    this.aggregationStyle = aggregationStyle;

    String url = servicesUrl + serviceName + "/MapServer/export?" + getParameters();
    return Utils.executeHttpGETRequest(httpClient, url, true, useCookie?cookieOrToken:null, showRequestUrl);
  }

  public Tuple exportMap(String bbox, int bboxSR, boolean closeClient, String aggregationStyle, boolean showRequestUrl) {
    this.bbox = bbox;
    this.bboxSR = bboxSR;
    this.aggregationStyle = aggregationStyle;

    String url = servicesUrl + serviceName + "/MapServer/export?" + getParameters();
    return Utils.executeHttpGETRequest(httpClient, url, closeClient, useCookie?cookieOrToken:null, showRequestUrl);
  }

  private String createDynamicLayers(int featureLimit, String layerName) {
    String template = "[{\"id\":0,\"name\":\"" + layerName + "\",\"source\":{\"type\":\"mapLayer\",\"mapLayerId\":0},\"drawingInfo\":" +
        "{\"renderer\":{\"type\":\"aggregation\",\"style\":\"Grid\",\"featureThreshold\":" + featureLimit +
        ",\"lodOffset\":0,\"minBinSizeInPixels\":25,\"fullLodGrid\":false,\"labels\":" +
        "{\"color\":[0,0,0,255],\"font\":\"Arial\",\"size\":12,\"style\":\"PLAIN\",\"format\":\"###.#KMB\"},\"fieldStatistic\":null," +
        "\"binRenderer\":{\"type\":\"Continuous\",\"minColor\":[255,0,0,0],\"maxColor\":[255,0,0,255],\"minOutlineColor\":[0,0,0,100]," +
        "\"maxOutlineColor\":[0,0,0,100],\"minOutlineWidth\":0.5,\"maxOutlineWidth\":0.5,\"minValue\":null,\"maxValue\":null,\"minSize\":100,\"maxSize\":100,\"normalizeByBinArea\":false}," +
        "\"geoHashStyle\":{\"style\":\"" + aggregationStyle + "\"," +
        "\"sr\":\"102100\"},\"featureRenderer\":{\"type\":\"simple\",\"symbol\":{\"type\":\"esriSMS\",\"style\":\"esriSMScircle\"," +
        "\"color\":[158,202,225,150],\"size\":12,\"angle\":0,\"xoffset\":0,\"yoffset\":0,\"outline\":{\"color\":[0,0,0,255],\"width\":1}}," +
        "\"label\":\"\",\"description\":\"\",\"rotationType\":\"\",\"rotationExpression\":\"\"}}},\"minScale\":0,\"maxScale\":0}]";
    return template;
  }


  Tuple getCount(String where, String boundingBox) {
    FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
    return featureService.getCount(where, boundingBox, true);
  }

  Envelope2D getMaxExtent() {
    FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
    return featureService.getExtent(true);
  }

}
