package com.esri.arcgis.performance.test.mat;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class FeatureServiceWithoutStatsTester {

  private static SimpleDateFormat simpleDateFormat;

  private static int timeoutInSeconds = 120;
  private static int numRuns = 20;
  private static double percentage = 0.2;

  public static void main(String[] args)  throws Exception{
    String pattern = "yyyy-MM-dd HH:mm:ss";
    simpleDateFormat = new SimpleDateFormat(pattern);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    int numParameters = args.length;
    if (numParameters >= 3) {
      testVariousRequestsWithoutStats(args);
    } else {
      testVariousRequestsWithoutStats(new String[0]);
    }
  }


  private static void testVariousRequestsWithoutStats(String[] args)  throws Exception{

    if (args.length < 3) {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester " +
          "<Services Url> <Service name> <Option code: 0 -> 8> {<Number of runs = 20> <Timeout in seconds = 120> <Bounding box width> and/or <Time extent relative to the maximum time extent in percentage = 5>}");
      System.out.println("Code stands for: ");
      System.out.println("0 -> get total counts for all services ");
      System.out.println("1 -> all:  1=1, limit=10,000 ");
      System.out.println("2 -> attribute range:  speed < x and speed > y, limit=10,000 for all services ");
      System.out.println("3 -> attribute group:  flightId IN ('1234', '5678'), limit=10,000 for all services ");
      System.out.println("4 -> spatial extent:  geometry INSIDE bounding box, limit=10,000 for all services ");
      System.out.println("5 -> not supported yet, spatial polygon:  geometry INSIDE state boundary, limit=10,000 for all services ");
      System.out.println("6 -> temporal extent:  time > t1 and time < t2, limit=10,000 for all services ");
      System.out.println("7 -> spatiotemporal extent:  geometry INSIDE bounding box AND time > t1 and time < t2, limit=10,000 for all services ");
      System.out.println("8 -> spatiotemporal extent with attribute group:  flightId IN ('1234', '5678') AND geometry INSIDE bounding box AND time > t1 and time < t2, limit=10,000 for all services ");
      System.out.println("9 -> spatiotemporal extent with attribute group:  flightId IN ('1234', '5678') AND geometry INSIDE bounding box AND time > t1 and time < t2 AND GroupBy One hour limit=10,000 for all services ");

      System.out.println("Samples: ");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 0");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 1");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 2 " +
          "20 120 speed 5 (20 -> runs, 120 -> timeout, speed -> plane speed field name, 5 -> the percentage of the speed range) ");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 3 " +
          "20 120 plane_id false (20 -> runs, 120 -> timeout, plane_id -> unique id field name, false -> is String field)");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 4 " +
          "20 120 25 (20 -> runs, 120 -> timeout, 25 -> bounding box width");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 5");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 6 " +
          "20 120 ts 10 (20 -> runs, 120 -> timeout, ts -> time field name, 5 -> time window percentage)");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 7 " +
          "20 120 35 ts 5 (20 -> runs, 120 -> timeout, 35 -> bounding box width, ts -> time field name, 5 -> time window percentage)");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 8 " +
          "20 120 60 ts 5 orig true (20 -> runs, 120 -> timeout, 60 -> bounding box width, ts -> time field name, 5 -> time window percentage, orig -> unique value field name, true -> is it a string field)");
      System.out.println("java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithoutStatsTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa10m 9 " +
          "20 120 60 ts 5 orig true 3 1 hours (20 -> runs, 120 -> timeout, 60 -> bounding box width, ts -> time field name, 5 -> time window percentage, orig -> unique value field name, " +
          "true -> is it a string field, 3 -> Lod, 1 -> time interval, hours -> time unit)");

    } else {
      String servicesUrl = args[0];
      String[] tableNames = new String[]{args[1]}; // new String[]{"faa10k", "faa100k", "faa1m", "faa3m", "faa5m", "faa10m", "faa30m", "faa300m"};
      String codes = args[2];
      if (args.length > 3) numRuns = Integer.parseInt(args[3]);
      if (args.length > 4) timeoutInSeconds = Integer.parseInt(args[4]);

      if (codes.equals("0")) testTotalCountForAll(servicesUrl, tableNames);
      if (codes.equals("1")) testGetFeaturesForAll(servicesUrl, tableNames);
      if (codes.equals("2")) {
        String fieldName = "speed";
        if (args.length > 5) fieldName = args[5];
        if (args.length > 6) percentage = Double.parseDouble(args[6]) / 100.0;
        testGetFeaturesWithSpeedRange(servicesUrl, tableNames, fieldName);
      }
      if (codes.equals("3")) {
        String uniqueFieldName = "plane_id";
        boolean isUniqueFieldAStringField = false;
        if (args.length > 5) uniqueFieldName = args[5];
        if (args.length > 6) isUniqueFieldAStringField = Boolean.parseBoolean(args[6]);
        testGetFeaturesWithSQLIn(servicesUrl, tableNames, uniqueFieldName, isUniqueFieldAStringField);
      }
      if (codes.equals("4")) {
        double boundingBoxWidth = 25;
        if (args.length > 5) boundingBoxWidth = Double.parseDouble(args[5]);
        testGetFeaturesWithBoundingBox(servicesUrl, tableNames, boundingBoxWidth, "4326");
      }
      if (codes.equals("5")) System.out.println("To be implemented!");

      if (codes.equals("6")) {
        String timeFieldName = "ts";
        if (args.length > 5) timeFieldName = args[5];
        if (args.length > 6) percentage = Double.parseDouble(args[6]) / 100.0;
        testGetFeaturesWithTimeExtent(servicesUrl, tableNames, timeFieldName);
      }
      if (codes.equals("7")) {
        double boundingBoxWidth = 35;
        if (args.length > 5) boundingBoxWidth = Double.parseDouble(args[5]);

        String fieldName = "ts";
        if (args.length > 6) fieldName = args[6];

        if (args.length > 7) percentage = Double.parseDouble(args[7]) / 100.0;

        testGetFeaturesWithBoundingBoxAndTimeExtent(servicesUrl, tableNames, boundingBoxWidth, fieldName);
      }
      // if bounding box too small, the table/service may return 0 feature.
      if (codes.equals("8")) {
        double boundingBoxWidth = 60;
        if (args.length > 5) boundingBoxWidth = Double.parseDouble(args[5]);

        String timeFieldName = "ts";
        if (args.length > 6) timeFieldName = args[6];

        if (args.length > 7) percentage = Double.parseDouble(args[7]) / 100.0;

        String fieldName = "orig";
        if (args.length > 8) fieldName = args[8];

        boolean isStringField = true;
        if (args.length > 9) isStringField = Boolean.parseBoolean(args[9]);

        testGFeaturesWithBoundingBoxAndTimeExtentAndSQLIN(servicesUrl, tableNames, boundingBoxWidth, fieldName, isStringField, timeFieldName);
      }

      if (codes.equals("9")) {
        double boundingBoxWidth = 60;
        if (args.length > 5) boundingBoxWidth = Double.parseDouble(args[5]);

        String timeFieldName = "ts";
        if (args.length > 6) timeFieldName = args[6];

        if (args.length > 7) percentage = Double.parseDouble(args[7]) / 100.0;

        String fieldName = "orig";
        if (args.length > 8) fieldName = args[8];

        boolean isStringField = true;
        if (args.length > 9) isStringField = Boolean.parseBoolean(args[9]);

        int lod = 3;
        if (args.length > 10) lod = Integer.parseInt(args[10]);

        int timeInterval = 1;
        if (args.length > 11) timeInterval = Integer.parseInt(args[11]);

        String timeUnits = "hours";
        if (args.length > 12) timeUnits = args[12];

        testGFeaturesWithBoundingBoxAndTimeExtentAndSQLIN_GroupBy(servicesUrl, tableNames, boundingBoxWidth, fieldName, isStringField, timeFieldName, lod, timeInterval, timeUnits);
      }
    }
  }

  private static void testGFeaturesWithBoundingBoxAndTimeExtentAndSQLIN_GroupBy(
      String servicesUrl, String[] tableNames, double boundingBoxWidth, String uniqueFieldName, boolean isStringField,
      String timeStampFieldName, int lod, int timeInterval, String timeUnits) {
    System.out.println("======== get features from each service with a 10 degree random bounding box and time extent and IN parameter with Group By ========= ");
    String boundingBox = Utils.getRandomBoundingBox(boundingBoxWidth, boundingBoxWidth/2);
    Random random = new Random();

    try {
      Double[] stats = new Double[numRuns];
      for (String table : tableNames) {
        FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
        String mTimestamp = getTimeExtent(featureService, timeStampFieldName, random);
        List<String> uniqueValues = featureService.getFieldUniqueValues(uniqueFieldName);
        for (int i=0; i<numRuns; i++) {
          String where = getTwoUniqueValuesForIN(uniqueValues, uniqueFieldName, isStringField);
          Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBoxAndTimeExtentAndGroupBy(where, boundingBox, mTimestamp, lod, timeInterval, timeUnits, false);
          stats[i] = tuple.requestTime * 1.0;
        }
      }
      Utils.computeStats(stats, numRuns * tableNames.length, 1);
    }catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  private static void testGFeaturesWithBoundingBoxAndTimeExtentAndSQLIN(String servicesUrl, String[] tableNames, double boundingBoxWidth, String uniqueFieldName, boolean isStringField, String timeStampFieldName) {
    System.out.println("======== get features from each service with a 10 degree random bounding box and time extent and IN parameter ========= ");
    String boundingBox = Utils.getRandomBoundingBox(boundingBoxWidth, boundingBoxWidth/2);
    Random random = new Random();

    try {
      Double[] stats = new Double[numRuns];
      for (String table : tableNames) {
        FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
        String mTimestamp = getTimeExtent(featureService, timeStampFieldName, random);
        List<String> uniqueValues = featureService.getFieldUniqueValues(uniqueFieldName);

        for (int i=0; i<numRuns; i++) {
          String where = getTwoUniqueValuesForIN(uniqueValues, uniqueFieldName, isStringField);
          Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBoxAndTimeExtent(where, boundingBox, mTimestamp, false);
          stats[i] = tuple.requestTime * 1.0;
        }
      }
      Utils.computeStats(stats, numRuns * tableNames.length, 1);
    }catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void testGetFeaturesWithBoundingBoxAndTimeExtent(String servicesUrl, String[] tableNames, double boundingBoxWidth, String timeStampFieldName) {
    System.out.println("======== get features from each service with a 10 degree random bounding box and time extent ========= ");
    String boundingBox = Utils.getRandomBoundingBox(boundingBoxWidth, boundingBoxWidth/2);
    Random random = new Random();

    try {
      Double[] stats = new Double[numRuns];
      for (String table : tableNames) {
        FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
        String mTimestamp = getTimeExtent(featureService, timeStampFieldName, random);
        for (int i=0; i<numRuns; i++) {

          Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBoxAndTimeExtent("1=1", boundingBox, mTimestamp, false);
          stats[i] = tuple.requestTime * 1.0;
        }
      }
      Utils.computeStats(stats, numRuns * tableNames.length, 1);
    }catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void testGetFeaturesWithSQLIn(String servicesUrl, String[] tableNames, String uniqueFieldName, boolean isStringField) {
    System.out.println("======== get features from each service with sSQL IN (xxx,xxx) ========= ");
    try {
      Double[] stats = new Double[numRuns];

      for (String table : tableNames) {
        FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
        List<String> uniqueValues = featureService.getFieldUniqueValues(uniqueFieldName);


        for (int i=0; i<numRuns; i++) {
          String where = getTwoUniqueValuesForIN(uniqueValues, uniqueFieldName, isStringField);
          Tuple tuple = featureService.getFeaturesWithWhereClause(where, false);
          stats[i] = tuple.requestTime * 1.0;
        }
      }
      Utils.computeStats(stats, numRuns * tableNames.length, 1);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void testGetFeaturesWithTimeExtent(String servicesUrl, String[] tableNames, String timeStampFieldName) {
    System.out.println("======== get features from each service with time filter ========= ");
    String pattern = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Random random = new Random();
    try {
      Double[] stats = new Double[numRuns];
      for (String table : tableNames) {
        FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
        String mTimestamp = getTimeExtent(featureService, timeStampFieldName, random);
        for (int i=0; i<numRuns; i++) {

          Tuple tuple = featureService.getFeaturesWithTimeExtent("1=1", mTimestamp, false);
          stats[i] = tuple.requestTime * 1.0;
        }
      }
      Utils.computeStats(stats, numRuns * tableNames.length, 1);
    }catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void testGetFeaturesWithSpeedRange(String servicesUrl, String[] tableNames, String speedFieldName)  throws Exception {
    System.out.println("======== get features from each service with speed range ========= ");
    testWithStatsAsWhereClause(speedFieldName, servicesUrl, tableNames);
  }

  private static void testGetFeaturesWithBoundingBox(String servicesUrl, String[] tableNames, double boundingBoxWidth, String outSR) throws Exception {
    System.out.println("======== get features from each service with a 10 degree random bounding box ========= ");
    Double[] stats = new Double[numRuns];
    String boundingBox = Utils.getRandomBoundingBox(boundingBoxWidth, boundingBoxWidth/2);
    for (String table: tableNames) {
      FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
      for (int i=0; i<numRuns; i++) {

        Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", outSR, boundingBox, false);
        stats[i] = tuple.requestTime * 1.0;
      }
    }
    Utils.computeStats(stats, numRuns * tableNames.length, 1);
  }
  
  private static void testGetFeaturesForAll(String servicesUrl, String[] tableNames)  throws Exception{
    System.out.println("======== get features from each service with a random offset ========= ");
    Double[] stats = new Double[numRuns];
    for (String table: tableNames) {
      FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
      boolean useOffset =  !table.contains("10k");
      for (int i=0; i<numRuns; i++) {

        Tuple tuple = featureService.getFeaturesWithWhereClauseAndRandomOffset("1=1", useOffset, false);
        stats[i] = tuple.requestTime * 1.0;
      }
    }
    Utils.computeStats(stats, numRuns * tableNames.length, 1);
  }
  
  private static void testTotalCountForAll(String servicesUrl, String[] tableNames) {
    System.out.println("======== get total count for each service ========= ");
    Double[] stats = new Double[numRuns];
    for (String table: tableNames) {
      FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
      for (int i=0; i<numRuns; i++) {
        Tuple tuple = featureService.getCount("1=1", true);
        stats[i] = tuple.requestTime * 1.0;
      }
    }
    Utils.computeStats(stats, numRuns * tableNames.length, 1);
  }

  private static void testWithStatsAsWhereClause(String fieldName, String servicesUrl, String[] tableNames)  throws Exception{
    Random random = new Random();

    Double[] runStats = new Double[numRuns];
    for (String table: tableNames) {
      FeatureService featureService = new FeatureService(servicesUrl, table, timeoutInSeconds, false);
      JSONObject stats = featureService.getFieldStats(fieldName);
      double min = stats.getDouble("min");
      double max = stats.getDouble("max");
      double width = max - min;

      for (int i=0; i<numRuns; i++) {
        double starting = getStartingPoint(percentage , random);
        String where = fieldName + " > " + (min + width * starting) + " AND " + fieldName + " < " + (min + width * (starting + percentage));
        Tuple tuple = featureService.getFeaturesWithWhereClause(where, false);
        runStats[i] = tuple.requestTime * 1.0;
      }
    }
    Utils.computeStats(runStats, numRuns * tableNames.length, 1);
  }

  private static double getStartingPoint(double percentage, Random random) {
    if (percentage == 1.0) {
     return 0.0;
    } else {
      double starting = random.nextDouble();
      if (starting < 0) starting = starting * (-1.0);

      while ((1.0 - starting) < percentage) {
        starting = random.nextDouble();
        if (starting < 0) starting = starting * (-1.0);
      }
      return starting;
    }
  }

  private static String getTimeExtent(FeatureService featureService, String fieldName, Random random) throws Exception {
    JSONObject stats = featureService.getFieldStats(fieldName);
//    String minTimestamp = stats.getString("min").replace("T", " ").replace("Z", "");
//    String maxTimestamp = stats.getString("max").replace("T", " ").replace("Z", "");
//    long min = simpleDateFormat.parse(minTimestamp).getTime();
//    long max = simpleDateFormat.parse(maxTimestamp).getTime();

    double min = stats.getDouble("min");
    double max = stats.getDouble("max");

    double width = max - min;
    double starting = getStartingPoint(percentage, random);

    return ((long)min + (long)(width * starting)) + ","+ ((long)min + (long)(width * (starting + percentage)));
  }

  private static String getTwoUniqueValuesForIN(List<String> uniqueValues, String fieldName, boolean isStringField) throws Exception {
    Random random = new Random();

    if (uniqueValues.size() == 0) throw new Exception("No unique values found!");
    if (uniqueValues.size() == 1) throw new Exception("Only have one value.");
    String uniqueValue1 = uniqueValues.get(0);
    String uniqueValue2 = uniqueValues.get(1);
    int totalCount = uniqueValues.size();

    if (totalCount > 2) {
      int index = random.nextInt() % totalCount;
      index = index < 0 ? (-1) * index : index;
      uniqueValue1 = uniqueValues.get(index);

      index = random.nextInt() % totalCount;
      index = index < 0 ? (-1) * index : index;
      while (uniqueValues.get(index).equals(uniqueValue1)) {
        index = random.nextInt() % totalCount;
        index = index < 0 ? (-1) * index : index;
      }
      uniqueValue2 = uniqueValues.get(index);
    }

    if (isStringField) {
      uniqueValue1 = "'" + uniqueValue1.replaceAll("'", "''") + "'";
      uniqueValue2 = "'" + uniqueValue2.replaceAll("'", "''") + "'";
    }

    String where = fieldName + " IN (" + uniqueValue1 + "," + uniqueValue2 + ")";
    return where;
  }
}
