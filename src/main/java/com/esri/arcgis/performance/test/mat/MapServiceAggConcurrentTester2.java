package com.esri.arcgis.performance.test.mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MapServiceAggConcurrentTester2 {

  public static void main(String[] args) {
    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      int numThreads = Integer.parseInt(args[2]);
      int numCalls = Integer.parseInt(args[3]);
      String fileName = args[4];
      int lines2Skip = Integer.parseInt(args[5]);

      String outputFileName = args[6];

      String aggregationStyle = "square";
      if (args.length >= 8) {
        aggregationStyle = args[7];
      }
      int featureLimit = 1000;
      if (args.length >= 9) featureLimit = Integer.parseInt(args[8]);

      int timeoutInSeconds = 100;
      if (args.length >= 10) timeoutInSeconds = Integer.parseInt(args[9]);

      concurrentTesting(servicesUrl, serviceName, numThreads, numCalls, fileName, lines2Skip, timeoutInSeconds, aggregationStyle, featureLimit, outputFileName);
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceConcurrentTester " +
          "<Services Url> <Service name> <Number of threads> <Number of concurrent calls> <Path to bounding box file> <Number of lines to skip> <Output file name> {<Aggregation Style (square/pointyHexagon/flatTriangle> <Feature Limit> <Timeout in seconds: 100> }");
    }
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, int timeoutInSeconds, String aggregationStyle, long numFeatures, int featureLimit) {
    Callable<Tuple> task = () -> {
      MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);
      Tuple tuple = mapService.exportMap(boundingBox, 4326);
      tuple.returnedFeatures = numFeatures;
      return tuple;
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, int numbThreads, int numbConcurrentCalls,
                                        String bboxFile, int lines2Skip, int timeoutInSeconds, String aggregationStyle, int featureLimit, String outputFileName) {
    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    int port = 9000;
    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    long overallStart = System.currentTimeMillis();
    List<Callable<Tuple>> callables = new LinkedList<>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(bboxFile));
      String line = reader.readLine();

      while (line != null && lines2Skip > 0) {
        line = reader.readLine();
        lines2Skip--;
      }

      int lineRead = 0;
      while (line != null && lineRead < numbConcurrentCalls) {
        String[] bboxAndNumFeatures = line.split("[|]");
        if (bboxAndNumFeatures.length == 2) {
          String boundingBox = bboxAndNumFeatures[0];
          long numFeatures = Long.parseLong(bboxAndNumFeatures[1]);
          callables.add(createTask(servicesUrl, serviceName, boundingBox, timeoutInSeconds, aggregationStyle, numFeatures, featureLimit));
        }
        lineRead++;
        line = reader.readLine();
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

      System.out.println( ((double)(System.currentTimeMillis() - overallStart)) / (double)numbConcurrentCalls  );
    }
  }
}
