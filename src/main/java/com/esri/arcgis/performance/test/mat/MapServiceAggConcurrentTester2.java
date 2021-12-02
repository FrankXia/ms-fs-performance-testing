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

public class MapServiceAggConcurrentTester2 {
  private  static DecimalFormat df1 = new DecimalFormat("#.#");
  private  static DecimalFormat df0 = new DecimalFormat("#");

  public static void main(String[] args) throws Exception {
    if (args.length >= 7) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      String datasetSize = args[2];
      int numThreads = Integer.parseInt(args[3]);
      int numCalls = Integer.parseInt(args[4]);
      String bboxFileFolder = args[5];
      int lines2Skip = Integer.parseInt(args[6]);

      String aggregationStyle = "square";
      if (args.length >= 8) {
        aggregationStyle = args[7];
      }
      String outputFileName = "";
      if (args.length >= 9 )  {
        outputFileName = args[8];
      }

      int featureLimit = 1000;
      if (args.length >= 10) featureLimit = Integer.parseInt(args[9]);

      int timeoutInSeconds = 100;
      if (args.length >= 11) timeoutInSeconds = Integer.parseInt(args[10]);

      if (numThreads > 1) {
        concurrentTesting(servicesUrl, serviceName, datasetSize, numThreads, numCalls, bboxFileFolder, lines2Skip, timeoutInSeconds, aggregationStyle, featureLimit, outputFileName);
      } else {
        singleThreadTesting(servicesUrl, serviceName, numCalls, featureLimit,  bboxFileFolder, lines2Skip, timeoutInSeconds, aggregationStyle);
      }
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester2 " +
          "<Services Url> <Service name> <Dataset size in String> <Number of threads> <Number of concurrent calls> <Path to bounding box files> <Number of lines to skip> <Aggregation Style (square/pointyHexagon/pointyTriangle)> {<Output file name> <Feature Limit> <Timeout in seconds: 100> }");
    }
  }

  private static void singleThreadTesting(String servicesUrl, String serviceName, int numbTests, int featureLimit,
                                          String bboxFileName, int lines2Skip, int timeoutInSeconds, String aggregationStyle) throws IOException {
    df0.setGroupingUsed(true);
    df0.setGroupingSize(3);
    df1.setGroupingUsed(true);
    df1.setGroupingSize(3);

    MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);
    BufferedReader reader = new BufferedReader(new FileReader(bboxFileName));
    int startIndex = lines2Skip;
    if (startIndex < 0) startIndex = -1 * startIndex;
    while (startIndex > 0) {
      reader.readLine();
      startIndex--;
    }

    double totalFeatures = 0;
    Double[] features = new Double[numbTests];
    Double[] times = new Double[numbTests];
    for (int index = 0; index  < numbTests; index++) {
      long start = System.currentTimeMillis();
      String aLine = reader.readLine();
      System.out.println(index + " => " +  aLine);
      String boundingBox = aLine.split("[|]")[0];
      long numFeatures = Long.parseLong(aLine.split("[|]")[1]);
      Tuple tuple = mapService.exportMap(boundingBox, 4326, true);
      tuple.returnedFeatures = numFeatures;
      times[index] = (System.currentTimeMillis() - start) * 1.0;
      features[index] = numFeatures * 1.0;
      totalFeatures += features[index];
    }

    String avgFeatures = df0.format(totalFeatures/numbTests);
    System.out.println("| avg features | avg (ms) | min (ms) | max (ms) | std_dev (ms) | avg (fs) | min (fs) | max (fs) | std dev (fs) | ");
    System.out.print("| " + avgFeatures + " | " + Utils.computeStats(times, numbTests, 1));
    System.out.println(" | " + Utils.computeStats(features, numbTests, 0) + " |");
  }

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, int timeoutInSeconds, String aggregationStyle, long numFeatures, int featureLimit) {
    Callable<Tuple> task = () -> {
      MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, featureLimit);
      Tuple tuple = mapService.exportMap(boundingBox, 4326, false);
      tuple.returnedFeatures = numFeatures;
      return tuple;
    };
    return task;
  }

  private static void concurrentTesting(String servicesUrl, String serviceName, String datasetSize, int numbThreads, int numbConcurrentCalls,
                                        String bboxFileFolder, int lines2Skip, int timeoutInSeconds, String aggregationStyle, int featureLimit, String outputFileName) {
    ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    long overallStart = System.currentTimeMillis();
    List<Callable<Tuple>> callables = new LinkedList<>();

    try {

      List<String> extents = Utils.getPreGeneratedRandomExtents(bboxFileFolder, datasetSize, lines2Skip, numbConcurrentCalls);
      if (extents.size() != numbConcurrentCalls) throw new Exception("Not enough extents");

      int lineRead = 0;
      while (lineRead < numbConcurrentCalls) {
        String line = extents.get(lineRead);
        String[] bboxAndNumFeatures = line.split("[|]");
        if (bboxAndNumFeatures.length == 2) {
          String boundingBox = bboxAndNumFeatures[0];
          long numFeatures = Long.parseLong(bboxAndNumFeatures[1]);
          callables.add(createTask(servicesUrl, serviceName, boundingBox, timeoutInSeconds, aggregationStyle, numFeatures, featureLimit));
        }
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
