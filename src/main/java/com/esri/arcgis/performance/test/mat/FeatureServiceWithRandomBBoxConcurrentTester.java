package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceWithRandomBBoxConcurrentTester {
  // java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentTester https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 10 500 device_id "[{\"statisticType\":\"avg\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"avgAccuracy\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"minAccuracy\"}]" ./safegraph3.txt

  public static void main(String[] args) {

    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numConcurrentCalls = Integer.parseInt(args[3]);
      String outputFileName = args[4];
      String boundingBoxFileName = args[5];
      int startingIndex = Integer.parseInt(args[6]);

      String outSR = "4326";
      if (args.length >= 8) {
        outSR = args[7];
      }

      int timeoutInSeconds = 600;
      if (args.length >= 9) {
        timeoutInSeconds = Integer.parseInt(args[8]);
      }

      boolean returnCountOnly = false;
      if (args.length >= 10) {
        returnCountOnly = Boolean.parseBoolean(args[9]);
      }

      if (numThreads > 1) {
        concurrentTesting(servicesUrl, serviceName, numThreads, numConcurrentCalls, boundingBoxFileName, startingIndex, timeoutInSeconds, outputFileName, returnCountOnly, outSR);
      } else {
        sequentialTesting(servicesUrl, serviceName, numConcurrentCalls, boundingBoxFileName, outSR, timeoutInSeconds, outputFileName,true, returnCountOnly);
      }
    } else {
      System.out.println(
              "Usage: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentTester " +
                      "<Services_Url> <Service_Name> <# of Threads> <Total Number of Concurrent Requests> <Output File Name> <Bounding Box File> <Starting Index in Bounding Box File>" +
              "{ <Output SR ID> <Timeout in seconds> <Return Count Only>}");
      System.out.println("Sample:");
      System.out.println("   " +
              "java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentTester https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ " +
              "safegraph 5 100 ./same_extent_concurrent-5.txt ./safegraph_extents_1k.txt 0");
      System.out.println("  " +
              "java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentTester https://us-iotdev.arcgis.com/opensearch01/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/" +
              " planes_10millions_fl 5 100 ./planes_10m_fl_querying_1k_5thread_100req.txt ./10m_extents_1k_features.txt 0 ");

    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, int timeoutInSeconds, boolean returnCountOnly, String outSR) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
      if (returnCountOnly) {
        return featureService.getCount("1=1", boundingBox, true);
      } else {
        return featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", outSR, boundingBox, true);
      }
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numConcurrentCalls, String boundingBoxFileName,
                                        int startingIndex, int timeoutInSeconds, String outputFileName, boolean returnCountOnly, String outSR) {
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);
    List<Callable<Tuple>> callables = new LinkedList<>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(boundingBoxFileName));
      // skip the number of entries before startingIndex
      if (startingIndex < 0) startingIndex = 0;
      while (startingIndex > 0) {
        reader.readLine();
        startingIndex--;
      }

      for (int index=0; index < numConcurrentCalls; index++) {
        String aLine = reader.readLine();
        System.out.println(index + " => " +  aLine);
        String boundingBox = aLine.split("[|]")[0];
        callables.add(createTask(servicesUrl, serviceName, boundingBox, timeoutInSeconds, returnCountOnly, outSR));
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

  private static void sequentialTesting(String servicesUrl, String serviceName, int numCalls, String boundingBox, String outSR, int timeoutInSeconds, String outputFileName, boolean closeClient, boolean returnCountOnly) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
      writer.write("RequestTime, Features, ErrorCode");
      writer.newLine();

      for (int index = 0; index < numCalls; index++) {
        FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
        try {
          if (returnCountOnly) {
            Tuple tuple = featureService.getCount("1=1", boundingBox, true);
            writer.write(tuple.requestTime + "," + tuple.returnedFeatures + "," + tuple.errorCode);
            writer.newLine();
          } else {
            Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", outSR, boundingBox, closeClient);
            writer.write(tuple.requestTime + "," + tuple.returnedFeatures + "," + tuple.errorCode);
            writer.newLine();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static void calculateStats(Stream<Tuple> results, int numbConcurrentCalls) {

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    final List<Long> times = new LinkedList<>();
    final List<Long> features = new LinkedList<>();
    results.forEach( tuple -> {
      if (tuple.errorCode == 0) {
        times.add(tuple.requestTime);
        features.add(tuple.returnedFeatures);
      }
    });

    double timeTotal = 0;
    double featureTotal = 0;
    long minTime = times.get(0);
    long maxTime = times.get(0);
    long minFeatures = features.get(0);
    long maxFeatures = features.get(0);

    double squaredTimes = 0.0;
    double squaredFeatures = 0.0;

    for (int i=0; i<times.size(); i++) {
      timeTotal += times.get(i);
      featureTotal += features.get(i);
      if (times.get(i) < minTime) minTime = times.get(i);
      if (times.get(i) > maxTime) maxTime = times.get(i);
      if (features.get(i) < minFeatures) minFeatures = features.get(i);
      if (features.get(i) > maxFeatures) maxFeatures = features.get(i);

      squaredTimes += times.get(i) * times.get(i);
      squaredFeatures += features.get(i) * features.get(i);
    }
//      long totalFinal = results.reduce(0L, (total, i) -> total + i);
//      System.out.println( (double)totalFinal / (double)callables.size());

    double avgTime = timeTotal / times.size();
    double avgFeatures = featureTotal / features.size();
    double stdDevTimes = Math.sqrt( (squaredTimes - times.size() * avgTime * avgTime) / (times.size() - 1) );
    double stdDevFeatures = Math.sqrt( (squaredFeatures - features.size() * avgFeatures * avgFeatures) / (features.size() - 1) );
    System.out.println( "Time -> average, min, max, and standard deviation over " + times.size() +  " requests: | " +  df.format(avgTime) + " | " + df.format(minTime) + " | " + df.format(maxTime)  + " | " + df.format(stdDevTimes) + " | ");
    System.out.println( "Features -> average, min, max, and standard deviation over " + features.size() +  " requests: | " +  df.format(avgFeatures) + " | " + df.format(minFeatures)  + " | " + df.format(maxFeatures) + " | " + df.format(stdDevFeatures) + " | ");
    System.out.println("Failed calls: " + (numbConcurrentCalls - features.size()));
  }
}
