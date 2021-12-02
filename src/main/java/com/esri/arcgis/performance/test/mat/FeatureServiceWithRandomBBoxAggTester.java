package com.esri.arcgis.performance.test.mat;

import java.text.DecimalFormat;
import java.util.List;


public class FeatureServiceWithRandomBBoxAggTester {

  private  static DecimalFormat df1 = new DecimalFormat("#.#");
  private  static DecimalFormat df0 = new DecimalFormat("#");

  public static void main(String[] args) throws Exception {
    if (args.length < 7) {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxAggTester" +
          " <Services Url> <Service name> <Dataset Size in String> <Bounding box file folder> <Number of tests (divisible by 5)> <Number of lines to skip> <Aggregation Style (square/pointyHexagon/pointyTriangle)> {<LOD> <Timeout in seconds: 120>}");
      System.out.println("Sample command: ");
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxAggTester https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 ./safegraph.txt 100");
    } else {
      String servicesUrl = args[0];
      String table = args[1];
      String dataSizeString = args[2];
      int numTests = Integer.parseInt(args[3]);
      String bboxFileFolder = args[4];
      int lines2Skip = Integer.parseInt(args[5]);
      String aggregationStyle = args[6];

      int lod = 10;
      if (args.length > 7) lod = Integer.parseInt(args[7]);

      int timeoutInSeconds = 120;
      if (args.length > 8) timeoutInSeconds = Integer.parseInt(args[8]);

      testGetFeaturesWithBoundingBox(servicesUrl, table, dataSizeString, bboxFileFolder, numTests, timeoutInSeconds, lines2Skip, aggregationStyle, lod);
    }

  }

  private static void testGetFeaturesWithBoundingBox(String servicesUrl, String tableName, String datasetSize, String path2BBoxFiles,
                                                     int numbTests, int timeoutInSeconds, int lines2Skip, String aggStyle, int lod) throws Exception {
    df0.setGroupingUsed(true);
    df0.setGroupingSize(3);
    df1.setGroupingUsed(true);
    df1.setGroupingSize(3);

    FeatureService featureService = new FeatureService(servicesUrl, tableName, timeoutInSeconds, true);

    List<String> extents = Utils.getPreGeneratedRandomExtents(path2BBoxFiles, datasetSize, lines2Skip, numbTests);
    if (extents.size() != numbTests) throw new Exception("Not enough extents");

    double totalFeatures = 0;
    Double[] features = new Double[numbTests];
    Double[] times = new Double[numbTests];
    String lodSR = "102100";
    String bboxSR = "4326";

    // when lod > 3 for geohash, the # of buckets will exceed 10k limit of DataStore
    // when lod > 9 for square, the # of buckets will exceed 10k limit of DataStore
    // when lod > 8 for triangle, the # of buckets will exceed 10k limit of DataStore
    // when lod > 10 for hexagon, the # of buckets will exceed 10k limit of DataStore

    if (aggStyle.equalsIgnoreCase("geohash")) {
      lodSR = "4326";
      if (lod > 3) lod = 3; //
    }
    if (aggStyle.equalsIgnoreCase("square")) {
      if (lod > 9) lod = 9; //
    }
    if (aggStyle.contains("triangle")) {
      if (lod > 8) lod = 8; //
    }
    if (aggStyle.contains("hexagon")) {
      if (lod > 10) lod = 10; //
    }

    for (int index = 0; index  < numbTests; index++) {
      long start = System.currentTimeMillis();
      String aLine = extents.get(index);
      System.out.println(index + " => " +  aLine);
      String boundingBox = aLine.split("[|]")[0];
      featureService.getAggregationWithWhereClauseAndBoundingBox("1=1", boundingBox, bboxSR, lod, lodSR, aggStyle,false);
      times[index] = (System.currentTimeMillis() - start) * 1.0;
      features[index] = Double.parseDouble(aLine.split("[|]")[1]);
      totalFeatures += features[index];
    }

    String avgFeatures = df0.format(totalFeatures/numbTests);
    System.out.println("| avg features | avg (ms) | min (ms) | max (ms) | std_dev (ms) | avg (fs) | min (fs) | max (fs) | std dev (fs) | ");
    System.out.print("| " + avgFeatures + " | " + Utils.computeStats(times, numbTests, 1));
    System.out.println(" | " + Utils.computeStats(features, numbTests, 0) + " |");
  }
}
