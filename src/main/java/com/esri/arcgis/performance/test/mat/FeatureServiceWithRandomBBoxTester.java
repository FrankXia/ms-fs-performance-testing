package com.esri.arcgis.performance.test.mat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class FeatureServiceWithRandomBBoxTester {

  public static void main(String[] args) throws Exception {
    if (args.length < 4) {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxTester" +
          " <Services Url> <Service name> <Bounding box file name> <Number of tests> {<Timeout in seconds: 120> <Starting entry> }");
      System.out.println("Sample: ");
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxTester https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 ./safegraph.txt 100");
    } else {
      String servicesUrl = args[0];
      String table = args[1];
      String fileName = args[2];
      int numTests = Integer.parseInt(args[3]);
      int timeoutInSeconds = 120;
      if (args.length > 4) timeoutInSeconds = Integer.parseInt(args[4]);
      int startingEntry = -1;
      if (args.length > 5) startingEntry = Integer.parseInt(args[5]);
      testGetFeaturesWithBoundingBox(servicesUrl, table, fileName, numTests, timeoutInSeconds, startingEntry);
    }

  }

  private static void testGetFeaturesWithBoundingBox(String servicesUrl, String tableName, String boundingBoxFileName,
                                                     int numbTests, int timeoutInSeconds, int startingEntry) throws Exception {
    System.out.println("======== get features from each service with a random bounding box that contains less than 10k features ========= bbox file => " + boundingBoxFileName + ", # of tests => " + numbTests + ", timeout => " + timeoutInSeconds);
    FeatureService featureService = new FeatureService(servicesUrl, tableName, timeoutInSeconds, true);
    BufferedReader reader = new BufferedReader(new FileReader(boundingBoxFileName));
    int modNumber = (numbTests < 150) ? 100 : (250 - numbTests);
    int startIndex = (int) (new Random().nextDouble() * modNumber);
    if (startingEntry >= 0) startIndex = startingEntry;
    if (startIndex < 0) startIndex = -1 * startIndex;
    while (startIndex > 0) {
      reader.readLine();
      startIndex--;
    }

    Double[] data = new Double[numbTests];
    for (int index = 0; index  < numbTests; index++) {
      long start = System.currentTimeMillis();
      String aLine = reader.readLine();
      System.out.println(index + " => " +  aLine);
      String boundingBox = aLine.split("[|]")[0];
      featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", "4326", boundingBox, false);
      data[index] = (System.currentTimeMillis() - start) * 1.0;
    }
    Utils.computeStats(data, numbTests);
  }
}
