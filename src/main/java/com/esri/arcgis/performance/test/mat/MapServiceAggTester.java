package com.esri.arcgis.performance.test.mat;


public class MapServiceAggTester {
  public static void main(String[] args) {
    if (args.length >= 6) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numCalls = Integer.parseInt(args[2]);
      int width = Integer.parseInt(args[3]);
      int height = Integer.parseInt(args[4]);
      String aggStyle = args[5];

      int timeoutInSeconds = 60;
      if (args.length >= 7) {
        timeoutInSeconds = Integer.parseInt(args[6]);
      }

      int featureLimit = 1000;
      if (args.length >= 8) {
        featureLimit = Integer.parseInt(args[7]);
      }

      String aggregationStyle = "square";
      if (args.length >= 9) {
        aggregationStyle = args[8];
      }

      singleTesting(servicesUrl, serviceName, width, height, aggStyle, numCalls, timeoutInSeconds, aggregationStyle, featureLimit);

    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggTester " +
          "<Services Url> <Service name> <Number of calls> <Bounding box width> <Bounding box height> <Aggregation style>  {<Timeout in seconds: 60> <Feature Limit> <Aggregation Style (square/pointyHexagon)>}");
    }
  }

  private static void singleTesting(String servicesUrl, String serviceName, int width, int height, String aggStyle, int numCalls, int timeoutInSeconds, String aggregationStyle, int featureLimit) {
    MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);
    Double[] times = new Double[numCalls];
    for (int index=0; index < numCalls; index++) {
      String boundingBox = Utils.getRandomBoundingBox(width, height);
      Tuple tuple = mapService.exportMap(boundingBox, 4326, aggStyle);
      times[index] = tuple.requestTime * 1.0;
    }
    Utils.computeStats(times, numCalls);
  }

}
