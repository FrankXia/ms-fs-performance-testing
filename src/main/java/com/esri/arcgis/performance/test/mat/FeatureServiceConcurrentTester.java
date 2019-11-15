package com.esri.arcgis.performance.test.mat;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FeatureServiceConcurrentTester {

  public static void main(String[] args) {
    double width = 180.0;
    double height = 90.0;

    if (args.length >= 6) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      String groupByFieldName = args[4];
      String outStatisitcs = args[5];

      if (args.length >= 8) {
        width = Double.parseDouble(args[6]);
        height = Double.parseDouble(args[7]);
      }

      int timeoutInSeconds = 60;
      if (args.length > 8) {
        timeoutInSeconds = Integer.parseInt(args[8]);
      }

      concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, groupByFieldName, outStatisitcs, width, height, timeoutInSeconds);
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester <Services Url> <Service name> <Number of threads> <Number of concurrent calls (<=100)> <Group By field name> <Out Statistics> {<bounding box width (180)> <bounding box height (90)> <Timeout in seconds>}");
      System.out.println("Sample:");
      System.out.println("   java -cp  ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/ faa30m 4 4 dest  \"[" +
          " {\\\"statisticType\\\":\\\"avg\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"avg_speed\\\"}," +
          " {\\\"statisticType\\\":\\\"min\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"min_speed\\\"}," +
          " {\\\"statisticType\\\":\\\"max\\\",\\\"onStatisticField\\\":\\\"speed\\\",\\\"outStatisticFieldName\\\":\\\"max_speed\\\"} " +
          "]\"");

    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String groupByFieldName, String outStatisitcs, String boundingBox, int timeoutInSeconds) {
    Callable<Tuple> task = () -> {
      FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, false);
      return featureService.doGroupByStats("1=1", groupByFieldName, outStatisitcs, boundingBox, true);
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls, String groupByFieldName, String outStatistics, double width, double height, int timeoutInSeconds) {
    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    List<Callable<Tuple>> callables = new LinkedList<>();

    try {
      for (int index=0; index < numbConcurrentCalls; index++) {
        String boundingBox = Utils.getRandomBoundingBox(width, height);
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
