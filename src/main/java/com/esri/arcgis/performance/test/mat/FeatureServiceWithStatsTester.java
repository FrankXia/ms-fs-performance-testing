package com.esri.arcgis.performance.test.mat;

import java.text.SimpleDateFormat;

public class FeatureServiceWithStatsTester {

  private static SimpleDateFormat simpleDateFormat;

  private static int timeoutInSeconds = 120;

  public static void main(String[] args) {

    int numParameters = args.length;
    try {
      if (numParameters >= 4) {
        testVariousRequestsWithStats(args);
      } else {
        testVariousRequestsWithStats(new String[0]);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void testVariousRequestsWithStats(String[] args)  throws Exception {
    if (args.length < 6) {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithStatsTester <Services Url> <Service name> <Group By field name> <Number of runs> <Timeout in seconds> <Out statistics> {<Bounding Box>}");
      System.out.println("Sample:");
      System.out.println("   java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa30m dest 20 120 \"[" +
          "{\\\"statisticType\\\":\\\"avg\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"avg_speed\\\"}," +
          "{\\\"statisticType\\\":\\\"min\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"min_speed\\\"}," +
          "{\\\"statisticType\\\":\\\"max\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"max_speed\\\"}" +
          "]\"");
    } else {
      String servicesUrl = args[0];
      String serviceName = args[1];
      String groupbyFdName = args[2];
      int numRuns = Integer.parseInt(args[3]);
      if (numRuns <= 0) numRuns = 20;
      timeoutInSeconds = Integer.parseInt(args[4]);
      String outStats = args[5];
      System.out.println(outStats);

      Double[] stats = new Double[numRuns];
      for (int i=0; i<numRuns; i++) {
        String boundingBox = null;
        if (args.length == 7){
          double width = Double.parseDouble(args[6]);
          boundingBox = Utils.getRandomBoundingBox(width, width);
        }
        FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, false);
        Tuple tuple = featureService.doGroupByStats("1=1", groupbyFdName, outStats, boundingBox, false);
        stats[i] = tuple.requestTime * 1.0;
      }

      Utils.computeStats(stats, numRuns, 1);
    }
  }
}
