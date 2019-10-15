package com.esri.arcgis.performance.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceConcurrentTester2 {
  // java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.FeatureServiceConcurrentTester2 https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 10 500 device_id "[{\"statisticType\":\"avg\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"avgAccuracy\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"accuracy\",\"outStatisticFieldName\":\"minAccuracy\"}]" ./safegraph3.txt

  public static void main(String[] args) {

    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      String groupByFieldName = args[4];
      String outStatistics = args[5];
      String boundingBoxFileName = args[6];
      int numSkips = 0;
      if (args.length >= 8) {
        numSkips = Integer.parseInt(args[1]);
      }
      int timeoutInSeconds = 60;
      if (args.length >= 9) {
        timeoutInSeconds = Integer.parseInt(args[8]);
      }

      concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, groupByFieldName, outStatistics, boundingBoxFileName, numSkips, timeoutInSeconds);
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.FeatureServiceConcurrentTester2 <Services Url> <Service name> <Number of threads> <Number of concurrent calls (<=100)> <Group By field name> <Out Statistics> <Path to bounding box file> {<Number of lines to skip> <Timeout in seconds>}");
      System.out.println("Sample:");
      System.out.println("   java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.FeatureServiceConcurrentTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa30m 4 4 dest  \"[" +
          " {\\\"statisticType\\\":\\\"avg\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"avg_speed\\\"}," +
          " {\\\"statisticType\\\":\\\"min\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"min_speed\\\"}," +
          " {\\\"statisticType\\\":\\\"max\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"max_speed\\\"} " +
          "]\" ./safegraph3.txt");

    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String groupByFieldName, String outStatistics, String boundingBox, int timeoutInSeconds) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds);
      return featureService.doGroupByStats("1=1", groupByFieldName, outStatistics, boundingBox);
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls, String groupByFieldName, String outStatistics, String boundingBoxFileName, int numSkips, int timeoutInSeconds) {
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
}
