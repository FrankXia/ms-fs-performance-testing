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

public class MapServiceAggConcurrentTester1 {

  public static void main(String[] args) {
    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      int width = Integer.parseInt(args[4]);
      int height = Integer.parseInt(args[5]);
      String aggStyle = args[6];

      String outputFile = null;
      if (args.length >= 8) {
        outputFile = args[7];
      }

      int featureLimit = 1000;
      if (args.length >= 9) {
        featureLimit = Integer.parseInt(args[8]);
      }

      int timeoutInSeconds = 60;
      if (args.length >= 10) {
        timeoutInSeconds = Integer.parseInt(args[9]);
      }

      if (numCalls == 1) {
        singleTesting(servicesUrl, serviceName, width, height, aggStyle, timeoutInSeconds, aggStyle, featureLimit);
      } else {
        concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, width, height, aggStyle, timeoutInSeconds, aggStyle, outputFile, featureLimit);
      }
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 " +
          "<Services Url> <Service name> <Number of threads> <Number of concurrent calls> <Bounding box width> <Bounding box height> <Aggregation style (square/pointyHexagon)> {<Output File> <Feature Limit> <Timeout in seconds: 60>}");
    }
  }

  private static void singleTesting(String servicesUrl, String serviceName, int width, int height, String aggStyle, int timeoutInSeconds, String aggregationStyle, int featureLimit) {
    String boundingBox = Utils.getRandomBoundingBox(width, height);
    MapService mapService = new MapService(servicesUrl, serviceName,timeoutInSeconds, aggregationStyle, featureLimit);
    Tuple tuple = mapService.exportMap(boundingBox, 4326, aggStyle);
    System.out.println( "Time -> " + tuple.requestTime);
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, String aggStyle, int timeoutInSeconds, String aggregationStyle, int featureLimit) {
    Callable<Tuple> task = () -> {
      MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);
      return mapService.exportMap(boundingBox, 4326, aggStyle);
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls, int width, int height,
                                        String aggStyle, int timeoutInSeconds, String aggregationStyle, String outputFileName, int featureLimit) {
    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    List<Callable<Tuple>> callables = new LinkedList<>();

    long start = System.currentTimeMillis();
    try {

      int lineRead = 0;
      while (lineRead < numbConcurrentCalls) {
        String boundingBox = Utils.getRandomBoundingBox(width, height);
        callables.add(createTask(servicesUrl, serviceName, boundingBox, aggStyle, timeoutInSeconds, aggregationStyle, featureLimit));
        lineRead++;
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
        writer.write("RequestTime, ErrorCode");
        writer.newLine();
        results.forEach(
                tuple -> {
                  try {
                    writer.write(tuple.requestTime  + "," + tuple.errorCode);
                    writer.newLine();
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                });
        writer.close();
      } else {
        doStats(results);
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

    System.out.println("Total time: " + (System.currentTimeMillis() - start) + " ms");
  }

  private static void doStats(Stream<Tuple> results) {
    DecimalFormat df = new DecimalFormat("#.#");

    final List<Long> times = new LinkedList<>();
    results.forEach( t -> {
      times.add(t.requestTime);
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

  }

}
