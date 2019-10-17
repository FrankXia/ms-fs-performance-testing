package com.esri.arcgis.performance.test;

public class MapServiceTester {

  public static void main(String[] args) {
    testOneService(args);
    //testAll();
  }

  private static void testOneService(String[] args) {
    if (args == null || args.length < 2) {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.MapServiceTester <Service Url> <Service Name> {<Aggregation Style (square/pointyHexagon)> <optional bounding box>}");
      return;
    }
    String servicesUrl = args[0];
    String serviceName = args[1];
    String aggregationStyle = "square";
    if (args.length > 2) {
      aggregationStyle = args[2];
    }

    int limitMax = 5000000;
    int limitMin = 2000000;
    String boundingBox = args.length == 3 ? args [2] : GenerateBoundingBox.getBbox(servicesUrl, serviceName, limitMin, limitMax, 180, 90, 1).split("[|]")[0];
    testExportMap(servicesUrl, serviceName, boundingBox, aggregationStyle);
  }

  private static void testExportMap(String servicesUrl, String serviceName, String boundingBox, String aggregationStyle) {
    MapService mapService = new MapService(servicesUrl, serviceName, 100, aggregationStyle);
    mapService.exportMap(boundingBox, 4326);
  }

  private static void testAll() {
    String servicesUrl = "https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/";

    String[] serviceNames = new String[]{"faa10k", "faa100k", "faa1m", "faa3m", "faa5m", "faa10m", "faa30m", "faa300m"};
    String[] bboxes = new String[serviceNames.length];
    bboxes[0] = "-180,-90,180,90";            // 10000, features returned

    long start = System.currentTimeMillis();
    int limitMax = 10000;
    int limitMin = 9000;
    for (int i=1; i<serviceNames.length; i++) {
      bboxes[i] = GenerateBoundingBox.getBbox(servicesUrl, serviceNames[i], limitMin, limitMax, 180, 90, 1).split("[|]")[0];
    }
    System.out.println("Time to get bounding boxes => " + (System.currentTimeMillis() - start) + " ms");

    for (int index =0; index < serviceNames.length; index++) {
      String name = serviceNames[index];
      MapService mapService = new MapService(servicesUrl, name, 100, "square");
      mapService.exportMap(bboxes[index], 4326);
    }
  }
}
