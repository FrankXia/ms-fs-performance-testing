package com.esri.arcgis.performance.test.mat;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
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
    request.setHeader("Cookie", cookie);

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
