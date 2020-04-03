package com.esri.arcgis.performance.test.mat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceConcurrentTester1a {
  // java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester2 https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 10 500 device_id "[{\"statisticType\":\"avg\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"avgAccuracy\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"minAccuracy\"}]" ./safegraph3.txt

  public static void main(String[] args) {

    if (args.length >= 6) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numConcurrentCalls = Integer.parseInt(args[3]);
      String outputFileName = args[4];
      String boundingBox = args[5];

      int timeoutInSeconds = 600;
      if (args.length >= 7) {
        timeoutInSeconds = Integer.parseInt(args[6]);
      }

      boolean returnCountOnly = false;
      concurrentTesting(servicesUrl, serviceName, numThreads, numConcurrentCalls, boundingBox, timeoutInSeconds, outputFileName, returnCountOnly);
    } else {
      System.out.println("Usage: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1a <Services_Url> <Service_Name> <# of Threads> <Number_Of_Concurrent Requests> <Output File Name> <Bounding Box>" +
              "{ <Return Count Only> <Timeout in seconds>}");
      System.out.println("Sample:");
      System.out.println("   java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1a https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ " +
              "safegraph 5 ./same_extent_concurrent-5.txt");

    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, int timeoutInSeconds, boolean returnCountOnly) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
      if (returnCountOnly) {
        return featureService.getCount("1=1", boundingBox, true);
      } else {
        return featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", boundingBox, true);
      }
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numConcurrentCalls, String boundingBox, int timeoutInSeconds, String outputFileName, boolean returnCountOnly) {
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    List<Callable<Tuple>> callables = new LinkedList<>();

    try {
      for (int index=0; index < numConcurrentCalls; index++) {
        callables.add(createTask(servicesUrl, serviceName, boundingBox, timeoutInSeconds, returnCountOnly));
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
