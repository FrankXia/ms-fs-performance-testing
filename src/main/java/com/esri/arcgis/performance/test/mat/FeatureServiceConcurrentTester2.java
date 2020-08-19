package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceConcurrentTester2 {
  // java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester2 https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 10 500 device_id "[{\"statisticType\":\"avg\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"avgAccuracy\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"minAccuracy\"}]" ./safegraph3.txt

  public static void main(String[] args) {

    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      String boundingBoxFileName = args[4];
      int numSkips = Integer.parseInt(args[5]);

      String outputFileName = args[6];

      int timeoutInSeconds = 60;
      if (args.length >= 8) {
        timeoutInSeconds = Integer.parseInt(args[7]);
      }

      String groupByFieldName = null;
      String outStatistics = null;
      if (args.length >= 10) {
        groupByFieldName = args[8];
        outStatistics = args[9];
      }

      if (numThreads > 1) {
        concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, groupByFieldName, outStatistics, boundingBoxFileName, numSkips, timeoutInSeconds, outputFileName);
      } else {
        sequentialTesting(servicesUrl, serviceName, numCalls, groupByFieldName, outStatistics, boundingBoxFileName, numSkips, timeoutInSeconds, outputFileName);
      }
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester2 <Services_Url> <Service_Name> <Number_Of_Threads> <Number_Of_Concurrent_Calls> <Path to bounding box file> <Number of lines to skip>  <Output File Name> " +
              "{ <Timeout in seconds> <Group By field name> <Out Statistics>}");
      System.out.println("Sample:");
      System.out.println("   java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ " +
              "Safegraph3 5 50 ./docs/safegraph3.txt 0 ./docs/output3_5_50.txt device_id  \"[" +
              " {\\\"statisticType\\\":\\\"avg\\\",\\\"onStatisticField\\\":\\\"accuracy\\\",\\\"outStatisticFieldName\\\":\\\"avg_accuracy\\\"}," +
              " {\\\"statisticType\\\":\\\"min\\\",\\\"onStatisticField\\\":\\\"accuracy\\\",\\\"outStatisticFieldName\\\":\\\"min_accuracy\\\"}," +
              " {\\\"statisticType\\\":\\\"max\\\",\\\"onStatisticField\\\":\\\"accuracy\\\",\\\"outStatisticFieldName\\\":\\\"max_accuracy\\\"} " +
              "]\"");

    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String groupByFieldName, String outStatistics, String boundingBox, int timeoutInSeconds) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, false);
      if (groupByFieldName != null & outStatistics != null) {
        return featureService.doGroupByStats("1=1", groupByFieldName, outStatistics, boundingBox, true);
      } else {
        return featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", "4326", boundingBox, true);
      }
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls, String groupByFieldName,
                                        String outStatistics, String boundingBoxFileName, int numSkips, int timeoutInSeconds, String outputFileName) {

    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    List<Callable<Tuple>> callables = new LinkedList<>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(boundingBoxFileName));
      List<String> boundingBoxes = new LinkedList<>();
      String box = reader.readLine();
      while (box != null) {
        String[] boxAndFeatures = box.split("\\|");
        if (boxAndFeatures.length == 2 && numSkips <= 0) {
          boundingBoxes.add(boxAndFeatures[0]);
        }
        box = reader.readLine();
        numSkips--;
      }
      reader.close();
      numbConcurrentCalls = Math.min(boundingBoxes.size(), numbConcurrentCalls);

      for (int index=0; index < numbConcurrentCalls; index++) {
        String boundingBox = boundingBoxes.get(index);
        callables.add(createTask(servicesUrl, serviceName, groupByFieldName, outStatistics, boundingBox, timeoutInSeconds));
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
      } else {
        // or calculate on the fly
        calculateStats(results, numbConcurrentCalls);
      }

    }catch (Exception ex) {
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
  }


  private static void sequentialTesting(String servicesUrl, String serviceName, int numCalls, String groupByFieldName,
                                        String outStatistics, String boundingBoxFileName, int numSkips, int timeoutInSeconds, String outputFileName) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(boundingBoxFileName));
      List<String> boundingBoxes = new LinkedList<>();
      String box = reader.readLine();
      while (box != null) {
        String[] boxAndFeatures = box.split("\\|");
        if (boxAndFeatures.length == 2 && numSkips <= 0) {
          boundingBoxes.add(boxAndFeatures[0]);
        }
        box = reader.readLine();
        numSkips--;
      }
      reader.close();
      numCalls = Math.min(boundingBoxes.size(), numCalls);

      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
      writer.write("RequestTime, Features, ErrorCode");
      writer.newLine();

      for (int index = 0; index < numCalls; index++) {
        FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, true);
        String boundingBox = boundingBoxes.get(index);
        try {
          if (groupByFieldName != null & outStatistics != null) {
            Tuple tuple = featureService.doGroupByStats("1=1", groupByFieldName, outStatistics, boundingBox, true);
            writer.write(tuple.requestTime + "," + tuple.returnedFeatures + "," + tuple.errorCode);
            writer.newLine();
          } else {
            Tuple tuple = featureService.getFeaturesWithWhereClauseAndBoundingBox("1=1", "4326", boundingBox, true);
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
      times.add(tuple.requestTime);
      features.add(tuple.returnedFeatures);
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
  }
}
