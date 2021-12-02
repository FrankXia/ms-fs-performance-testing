package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceWithRandomBBoxConcurrentAggTester {

  public static void main(String[] args) {

    if (args.length >= 9) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      String dataSizeString = args[2];
      int numThreads = Integer.parseInt(args[3]);
      int numConcurrentCalls = Integer.parseInt(args[4]);
      String boundingBoxFileFolder = args[5];
      int line2Skip = Integer.parseInt(args[6]);
      String aggStyle = args[7];
      String outputFileName = args[8];

      int lod = 10;
      if (args.length >= 10) lod = Integer.parseInt(args[9]);

      int timeoutInSeconds = 600;
      if (args.length >= 11) {
        timeoutInSeconds = Integer.parseInt(args[10]);
      }

      boolean returnCountOnly = false;
      if (numThreads > 1) {
        concurrentTesting(servicesUrl, serviceName, dataSizeString, numThreads, numConcurrentCalls, boundingBoxFileFolder, line2Skip, timeoutInSeconds, outputFileName, returnCountOnly, lod, aggStyle);
      }
    } else {
      System.out.println("Sample command: \n" +
              "java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_1m_bat_fl 1m 50 250 ./random_extents 430 geohash ./test_outputs/aggregation/1m_geohash_50thread_250req.txt");
    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, String boundingBoxSR, int timeoutInSeconds, boolean returnCountOnly, int lod, String lodSR, String aggStyle) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
      if (returnCountOnly) {
        return featureService.getCount("1=1", boundingBox, true);
      } else {
        return featureService.getAggregationWithWhereClauseAndBoundingBox("1=1", boundingBox, boundingBoxSR, lod, lodSR, aggStyle, false);
      }
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, String datasetSize, int numbThreads, int numConcurrentCalls, String boundingBoxFileFolder,
                                        int lines2Skip, int timeoutInSeconds, String outputFileName, boolean returnCountOnly, int lod, String aggStyle) {
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);
    List<Callable<Tuple>> callables = new LinkedList<>();

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
    if (aggStyle.toLowerCase().contains("triangle")) {
      if (lod > 9) lod = 9; //
    }
    if (aggStyle.toLowerCase().contains("hexagon")) {
      if (lod > 10) lod = 10; //
    }

    try {
      List<String> extents = Utils.getPreGeneratedRandomExtents(boundingBoxFileFolder, datasetSize, lines2Skip, numConcurrentCalls);
      if (extents.size() != numConcurrentCalls) throw new Exception("Not enough extents");

      for (int index=0; index < numConcurrentCalls; index++) {
        String aLine = extents.get(index);
        System.out.println(index + " => " +  aLine);
        String boundingBox = aLine.split("[|]")[0];
        callables.add(createTask(servicesUrl, serviceName, boundingBox, bboxSR, timeoutInSeconds, returnCountOnly, lod, lodSR, aggStyle));
      }

      Stream<Tuple> results =
          executor.invokeAll(callables)
              .stream()
              .map(future -> {
                try {
                  return future.get();
                } catch (Exception e) {
                  throw new IllegalStateException(e);
                }
              });

      System.out.println("Starting writing to the output file if there is one ... ");
      // output stats
      if (outputFileName != null) {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        writer.write("RequestTime, Features, ErrorCode");
        writer.newLine();
        results.forEach(
                tuple -> {
                  try {
                    writer.write(tuple.requestTime + "," + tuple.returnedFeatures + "," + tuple.errorCode);
                    writer.newLine();
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                });
        writer.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    try {
      System.out.println("attempt to shutdown executor");
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      System.err.println("tasks interrupted");
    }
    finally {
      if (!executor.isTerminated()) {
        System.err.println("cancel non-finished tasks");
      }
      executor.shutdownNow();
      System.out.println("shutdown finished");
    }

    System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime));
  }

}
