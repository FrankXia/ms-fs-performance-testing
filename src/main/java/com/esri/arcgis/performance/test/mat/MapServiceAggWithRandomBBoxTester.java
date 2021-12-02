package com.esri.arcgis.performance.test.mat;

import java.text.DecimalFormat;
import java.util.List;

public class MapServiceAggWithRandomBBoxTester {
  private  static DecimalFormat df1 = new DecimalFormat("#.#");
  private  static DecimalFormat df0 = new DecimalFormat("#");

  public static void main(String[] args) throws Exception {
    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      String datasetSize = args[2];
      int numCalls = Integer.parseInt(args[3]);
      String path2BBoxFiles = args[4];
      int lines2Skip = Integer.parseInt(args[5]);
      String aggregationStyle = args[6];

      int featureLimit = 1000;
      int timeoutInSeconds = 120;
      singleThreadTesting(servicesUrl, serviceName, numCalls, featureLimit, datasetSize, path2BBoxFiles, lines2Skip, timeoutInSeconds, aggregationStyle);

    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggWithRandomBBoxTester " +
          "<Services Url> <Service name> <Dataset Size in String> <Number of calls (divisible by 5)> <Path to bounding box files> <Number of lines to skip> <Aggregation Style (square/pointyHexagon/pointyTriangle)> ");
    }
  }

  private static void singleThreadTesting(String servicesUrl, String serviceName, int numbTests, int featureLimit, String datasetSize,
                                          String path2BBoxFiles, int lines2Skip, int timeoutInSeconds, String aggregationStyle) throws Exception {
    df0.setGroupingUsed(true);
    df0.setGroupingSize(3);
    df1.setGroupingUsed(true);
    df1.setGroupingSize(3);

    MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);

    double totalFeatures = 0;
    Double[] features = new Double[numbTests];
    Double[] times = new Double[numbTests];

    List<String> extents = Utils.getPreGeneratedRandomExtents(path2BBoxFiles, datasetSize, lines2Skip, numbTests);
    if (extents.size() != numbTests) throw new Exception("Not enough extents");

    for (int index = 0; index  < numbTests; index++) {
      long start = System.currentTimeMillis();
      String aLine = extents.get(index);
      System.out.println(index + " => " +  aLine);
      String boundingBox = aLine.split("[|]")[0];
      long numFeatures = Long.parseLong(aLine.split("[|]")[1]);
      Tuple tuple = mapService.exportMap(boundingBox, 4326, false);
      tuple.returnedFeatures = numFeatures;
      times[index] = (System.currentTimeMillis() - start) * 1.0;
      features[index] = numFeatures * 1.0;
      totalFeatures += features[index];
    }

    String avgFeatures = df0.format(totalFeatures/numbTests);
    System.out.println("| avg features | avg (ms) | min (ms) | max (ms) | std_dev (ms) | avg (fs) | min (fs) | max (fs) | std dev (fs) | ");
    System.out.print("| " + avgFeatures + " | " + Utils.computeStats(times, numbTests, 1));
    System.out.println(" | " + Utils.computeStats(features, numbTests, 0) + " |");
  }
}
