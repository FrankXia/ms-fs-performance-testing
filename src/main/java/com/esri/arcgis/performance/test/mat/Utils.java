package com.esri.arcgis.performance.test.mat;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Utils {

  private static int executeMapExportRequestCount = 0;

  private  static DecimalFormat df = new DecimalFormat("#.#");

  public static long executeHttpGETRequest(CloseableHttpClient client, String url, String serviceName, boolean closeClient, String cookie) {

    long start = System.currentTimeMillis();
    try {
      System.out.println(url);
      String response = executeHttpGET(client, url, closeClient, cookie);
      if (response.equals("503")) return -1;

      System.out.println("======> Total request time: " + (System.currentTimeMillis() - start)  + " ms, service name: " + serviceName +  ", " + (executeMapExportRequestCount++));
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    }

    return (System.currentTimeMillis() - start);
  }

  static String executeHttpGET(CloseableHttpClient client, String url, boolean closeClient, String cookie) throws Exception {
    HttpGet request = new HttpGet(url);
    if (cookie != null) {
      System.out.println("set cookie!");
      request.setHeader("Cookie", cookie);
    }

    CloseableHttpResponse response = client.execute(request);
    int responseCode = response.getStatusLine().getStatusCode();
    System.out.println("Response Code : " + responseCode);

    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    response.close();

    if (closeClient) {
      try {
        client.close();
      } finally {
        client.close();
      }
    }

    if (responseCode == 503)
      return "503";
    else
      return result.toString();
  }

  static int requestCount = 0;
  static String executeHttpsSimpleGET(String url, String cookie) throws Exception {
    URL myUrl = new URL(url);
    HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    conn.setRequestProperty("Accept-Language", "en-US");
    conn.setRequestProperty("Cookie", "_fbp=fb.1.1574182819524.1334966074; _gcl_au=1.1.1583812242.1574182820; sat_track=true; esri_gdpr=true; esri_whatsnew=%7B%22newMapViewer_announced%22%3Atrue%7D; ago_shield_token=home_X6h7ruI8opPKl; iv=150b6ed2-f666-432e-bc88-8e52063067ad; adcloud={%22_les_v%22:%22y%2Carcgis.com%2C1575478995%22}; __utma=193530365.620749356.1574266613.1575476699.1575558190.5; __utmz=193530365.1575558190.5.5.utmcsr=bing|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); AAMC_esri_0=REGION%7C9%7CAMSYNCSOP%7C%7CAMSYNCS%7C; s_dslv=1576548275425; check=true; AMCVS_ED8D65E655FAC7797F000101%40AdobeOrg=1; mbox=PC#e5b6613b63e644c4ae790c1eeb6fa36f.28_3#1638721880|session#a1ecea8ebd3f40f4b87325a3120380a7#1580247722; AMCV_ED8D65E655FAC7797F000101%40AdobeOrg=1075005958%7CMCIDTS%7C18290%7CMCMID%7C72152614587821069242522320305180930904%7CMCAAMLH-1578327298%7C9%7CMCAAMB-1580245907%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1580253108s%7CNONE%7CMCAID%7CNONE%7CvVersion%7C4.4.1; esri_aopc=DAEpLLOUevBakC-pnKti7S27wS5shA1QvFtwU_QthqPwg9YUHAVjZq9qOxn__w0xcTPOgBzsJqjCfHYZ3P2YRi5-biMhetEmkHc9V2A2dECXiHoSOhNGY1hQHLZsGNyMZ6d5ru7_GV1b8Pu-qtwqKWu14mHgcm2sX_poTctjWbQJPO5tJ_llYVVpZCF0f-RvdE-dlRhYraDyhIE_ULoG1fsNetziNtHBMvAbQb_v80ed8dtdJkczptfNgMgfPLSo2r-kxZPCb0ZIzu-JiEm7Vkz1nupOiutCkGlQdIiIE3If0wTXAXgTZx_k879_eadbwGQGdKxj2JjhZGJziBpsbZ57bTvHNwN1axUIW-jxdpzKo7n2scElrYT0K5u1W3OSb84gTJLEA-0d6EdT9X2l4Uds49JUnRHUXiKib22w9k845-rnMCoZL0RD92TDEAF3PoQTtWHrBAXuwtPgYVfsqg4xJNL_ZGygdWtRcB0UAkWMHuZKe3yZXXQS_462khUi9sqJFly5OnNBZ0zlU15TZkEe9mVC6h7NnggYGOwbz6o9jYnlT0X-NI68MJdUHVLyHk8ah6pqNRHh-56U0PWOYkFa3IDlfb8CgaAR9rQhECSk_qO9NPlrRWcDJylL-hpxL7vFtpcbDPRfTrvihdh_f2zqgJ4hLQopTEbORi9hRpT0Mr1BneeMvcil_RhTqI8o; esri_auth=%7B%22email%22%3A%22fxia_a4iot%22%2C%22token%22%3A%22L2pRhQN7KHoeiomrXzJFM85mh5o89k8VoFwWWKlq1_7nl0GKAdEF9YMZcxHU6g_CEc6B7gfgskh39WMUz5-jZ09nfFBn_cT1p8Pl-GMO4RHwswzZzrVYzsGR8RhZi-98PaprPBmQpCqH5qqfK-o01rPkzVjhQ5XghM6w1FtaauO9Y4jMerm5tD6HM4i9VCPuQhJ3GrPm4g71KM7wrKdUwWYGG3W3ks-ownJviJjEwJE.%22%2C%22expires%22%3A1582761099100%2C%22allSSL%22%3Atrue%2C%22persistent%22%3Afalse%2C%22created%22%3A1581551979100%2C%22culture%22%3A%22en-us%22%2C%22region%22%3A%22WO%22%2C%22accountId%22%3A%22cqvgkJ9ZrNkn9BCU%22%2C%22role%22%3A%22account_publisher%22%2C%22urlKey%22%3A%22a4iot%22%2C%22customBaseUrl%22%3A%22mapsdevext.arcgis.com%22%7D");

    requestCount++;
    System.err.println("request count => " + requestCount +", " + System.currentTimeMillis());

    InputStream is = conn.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = br.readLine()) != null) {
      result.append(line);
    }
    br.close();

    int responseCode = conn.getResponseCode();
    if (responseCode == 503)
      return "503";
    else
      return result.toString();
  }

  private static Random random = new Random();
  static String getRandomBoundingBox(double width, double height) {
    double MAX_W = 360;
    double MAX_H = 180;
    double MIN_X = -180;
    double MIN_Y = -90;

    double randomX = random.nextDouble();
    double randomY = random.nextDouble();
    double minx = MIN_X + randomX * (MAX_W - width);
    double miny = MIN_Y + randomY * (MAX_H - height);
    String bbox = minx +"," + miny + "," + (minx + width) + "," + (miny + height);
    System.out.println(bbox);
    return bbox;
  }

  public static long doHttpUrlConnectionAction(String desiredUrl, int timeoutInSeconds, String serviceName, String queryParameters)
  {
    URL url = null;
    BufferedReader reader = null;
    StringBuilder stringBuilder;
    long start = System.currentTimeMillis();

    try
    {
      // create the HttpURLConnection
      url = new URL(desiredUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // just want to do an HTTP GET here
      connection.setRequestMethod("POST");

      // uncomment this if you want to write output to this url
      byte[] contents = queryParameters.getBytes();
      connection.setFixedLengthStreamingMode(contents.length);
      connection.setDoOutput(true);

      // give it 15 seconds to respond
      connection.setConnectTimeout(timeoutInSeconds * 1000);
      connection.setReadTimeout(timeoutInSeconds * 1000);

      connection.connect();
      connection.getOutputStream().write(contents);

      // read the output from the server
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      stringBuilder = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line + "\n");
      }
//      return stringBuilder.toString();

      System.out.println("======> Total request time: " + (System.currentTimeMillis() - start)  + " ms, service name: " + serviceName +  ", " + (executeMapExportRequestCount++));

      return System.currentTimeMillis() - start;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      //throw e;
      return -1;
    }
    finally
    {
      // close the reader; this can throw an exception too, so
      // wrap it in another try/catch block.
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException ioe)
        {
          ioe.printStackTrace();
        }
      }
    }
  }


  static void computeStats(Double[] data, int numberRequest) {
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    Arrays.sort(data);
    double sum = 0;
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;

    double squaredValue = 0.0;
    for (int i= 0; i < data.length; i++) {
      double stat = data[i];
      sum += stat;
      if (stat < min) min = stat;
      if (stat > max) max = stat;
      squaredValue += stat * stat;

      System.out.println(stat);
    }

    double avg = sum / numberRequest;
    double std_dev = Math.sqrt( (squaredValue - numberRequest * avg * avg) / (numberRequest - 1) );

    System.out.println("Total data points: " + numberRequest);
    System.out.println("Average, min, max and std_dev: | " + df.format(avg) +  " | " + df.format(min) + " | " + df.format(max) + " | " + df.format(std_dev) + " |");
  }

  public static RequestConfig requestConfigWithTimeout(int timeoutInMilliseconds) {
    return RequestConfig.copy(RequestConfig.DEFAULT)
        .setSocketTimeout(timeoutInMilliseconds)
        .setConnectTimeout(timeoutInMilliseconds)
        .setConnectionRequestTimeout(timeoutInMilliseconds)
        .build();
  }
}
