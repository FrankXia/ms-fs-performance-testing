package com.esri.arcgis.performance.test.mat;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MapServiceAggConcurrentTester {

  public static void main(String[] args) {
    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      int width = Integer.parseInt(args[4]);
      int height = Integer.parseInt(args[5]);
      String aggStyle = args[6];

      int timeoutInSeconds = 60;
      if (args.length > 7) {
        timeoutInSeconds = Integer.parseInt(args[7]);
      }
      String aggregationStyle = "square";
      if (args.length > 8) {
        aggregationStyle = args[7];
      }

      if (numCalls == 1) {
        singleTesting(servicesUrl, serviceName, width, height, aggStyle, timeoutInSeconds, aggregationStyle);
      } else {
        concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, width, height, aggStyle, timeoutInSeconds, aggregationStyle);
      }
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester " +
          "<Services Url> <Service name> <Number of threads> <Number of concurrent calls (<=100)> <Bounding box width> <Bounding box height> <Aggregation style (square/pointyHexagon)> {<Timeout in seconds: 60>}");
    }
  }

  private static void singleTesting(String servicesUrl, String serviceName, int width, int height, String aggStyle, int timeoutInSeconds, String aggregationStyle) {
    String boundingBox = Utils.getRandomBoundingBox(width, height);
    MapService mapService = new MapService(servicesUrl, serviceName,timeoutInSeconds, aggregationStyle);
    long time = mapService.exportMap(boundingBox, 4326, aggStyle);
    System.out.println( "Time -> " + time);
  }

  private static Callable<Long> createTask(String servicesUrl, String serviceName, String boundingBox, String aggStyle, int timeoutInSeconds, String aggregationStyle) {
    Callable<Long> task = () -> {
      MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle);
      return mapService.exportMap(boundingBox, 4326, aggStyle);
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls, int width, int height, String aggStyle, int timeoutInSeconds, String aggregationStyle) {
    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    List<Callable<Long>> callables = new LinkedList<>();

    long start = System.currentTimeMillis();
    try {

      int lineRead = 0;
      while (lineRead < numbConcurrentCalls) {
        String boundingBox = Utils.getRandomBoundingBox(width, height);
        callables.add(createTask(servicesUrl, serviceName, boundingBox, aggStyle, timeoutInSeconds, aggregationStyle));
        lineRead++;
      }

      Stream<Long> results =
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
      results.forEach( t -> {
        times.add(t);
      });

      double timeTotal = 0;
      long minTime = times.get(0);
      long maxTime = times.get(0);
      double squaredTimes = 0.0;

      for (int i=0; i<times.size(); i++) {
        timeTotal += times.get(i);
        if (times.get(i) < minTime) minTime = times.get(i);
        if (times.get(i) > maxTime) maxTime = times.get(i);
        squaredTimes += times.get(i) * times.get(i);
      }

      double avgTime = timeTotal / times.size();
      double stdDevTimes = Math.sqrt( (squaredTimes - times.size() * avgTime * avgTime) / (times.size() - 1) );
      System.out.println( "Time -> average, min, max, and standard deviation over " + times.size() +  " requests: | " +  df.format(avgTime) + " | " + df.format(minTime) + " | " + df.format(maxTime)  + " | " + df.format(stdDevTimes) + " | ");


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

    System.out.println("Total time: " + (System.currentTimeMillis() - start) + " ms");
  }

}