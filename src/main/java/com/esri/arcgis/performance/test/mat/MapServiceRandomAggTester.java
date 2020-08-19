package com.esri.arcgis.performance.test.mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MapServiceRandomAggTester {
  public static void main(String[] args) {
    if (args.length >= 6) {
      String servicesUrl = args[0];
      String serviceName = args[1];
      String extentFile = args[2];
      int numCalls = Integer.parseInt(args[3]);
      int numThreads = Integer.parseInt(args[4]);
      int startIndex = Integer.parseInt(args[5]);
      startIndex = Math.max(startIndex, 0);
      String aggStyle = "pointyHexagon";
      if (args.length >= 7) {
        aggStyle = args[6];
      }
      boolean selectExtentRandomly = true;
      if (args.length >= 8) {
        selectExtentRandomly = Boolean.parseBoolean(args[7]);
      }

      boolean showRequestUrl = false;
      if (args.length >= 9) {
        showRequestUrl = Boolean.parseBoolean(args[8]);
      }

      int timeoutInSeconds = 60;
      if (args.length >= 10) {
        timeoutInSeconds = Integer.parseInt(args[9]);
      }

      System.out.println("Testing started at " + (new Date()));
      if (numThreads <= 1) {
        startTesting(servicesUrl, serviceName, numCalls, extentFile, startIndex, timeoutInSeconds, aggStyle, showRequestUrl, selectExtentRandomly);
      } else {
        startConcurrentTesting(servicesUrl, serviceName, numCalls,numThreads, extentFile, startIndex, timeoutInSeconds, aggStyle, showRequestUrl, selectExtentRandomly, null);
      }
    } else {
      System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceRandomAggTester " +
          "<Services Url> <Service name> <Extent file> <Number of calls> <Number of threads> <Starting extent index> {<Aggregation style (pointyHexagon/geohash/square)> <Select extent randomly (true/false)> <Show requestUrl (true/false)> <Timeout in seconds: 60>} \n" +
              "Ex.  java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceRandomAggTester https://us-iotdev.arcgis.com/devlion/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/ safegraph5m safegraph5m.txt 100 0 1");
    }
  }

  private static void startTesting(String servicesUrl, String serviceName, int numCalls, String extentFile, int startIndex, int timeoutInSeconds, String aggregationStyle, boolean showRequestUrl, boolean selectRandomly) {
    int numExtents = numCalls;
    List<String> bboxes = new LinkedList<>();
    List<Integer> features = new LinkedList<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(extentFile));
      String record = reader.readLine();
      while (startIndex > 0 && record != null) {
        record = reader.readLine();
        startIndex--;
      }

      while (record!=null && numExtents > 0) {
        String[] splitRecord = record.split("[|]");
        bboxes.add(splitRecord[0]);
        features.add(Integer.parseInt(splitRecord[1]));
        record = reader.readLine();
        numExtents--;
      }
      reader.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    numCalls = Math.min(numCalls, bboxes.size());
    Random random = new Random();
    MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, 10000);
    List<Double> times = new LinkedList<>();
    List<Integer> numFeatures = new LinkedList<>();
    List<String> extents = new LinkedList<>();

    if (selectRandomly) {
      while (bboxes.size() > 0) {
        int index = random.nextInt(bboxes.size());
        String bbox = bboxes.get(index);
        try {
          Tuple tuple = mapService.exportMap(bbox, 4326, false, aggregationStyle, showRequestUrl);
          if (tuple.errorCode == 0) {
            times.add(tuple.requestTime * 1.0);
            numFeatures.add(features.get(index));
            extents.add(bbox);
          }
          features.remove(index);
          bboxes.remove(index);
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        if (bboxes.size() % 500 == 0)
          System.out.println("Records remaining -> " + bboxes.size());
      }
    } else {
      for (int index = 0; index < bboxes.size(); index++) {
        String bbox = bboxes.get(index);
        try {
          Tuple tuple = mapService.exportMap(bbox, 4326, false, aggregationStyle, showRequestUrl);
          if (tuple.errorCode == 0) {
            times.add(tuple.requestTime * 1.0);
            numFeatures.add(features.get(index));
            extents.add(bbox);
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        if (index % 500 == 0)
          System.out.println("Records processed -> " + index);
      }
    }

    Utils.computeStats(times, numFeatures, extents, aggregationStyle);
  }

  private static void startConcurrentTesting(String servicesUrl, String serviceName, int numCalls, int numThreads, String extentFile, int startIndex,
                                             int timeoutInSeconds, String aggregationStyle, boolean showRequestUrl, boolean selectRandomly, String outputFileName) {
    int numExtents = numCalls;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    DecimalFormat df = new DecimalFormat("#.#");
    df.setGroupingUsed(true);
    df.setGroupingSize(3);

    List<String> bboxes = new LinkedList<>();
    //List<Integer> features = new LinkedList<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(extentFile));
      String record = reader.readLine();
      while (startIndex > 0 && record != null) {
        record = reader.readLine();
        startIndex--;
      }

      while (record!=null && numExtents > 0) {
        String[] splitRecord = record.split("[|]");
        bboxes.add(splitRecord[0]);
        //features.add(Integer.parseInt(splitRecord[1]));
        record = reader.readLine();
        numExtents--;
      }
      reader.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    numCalls = Math.min(numCalls, bboxes.size());

    List<Callable<Tuple>> callables = new LinkedList<>();
    Random random = new Random();

    long start = System.currentTimeMillis();

    try {
      if (selectRandomly) {
        while (bboxes.size() > 0) {
          int index = random.nextInt(bboxes.size());
          String bbox = bboxes.get(index);
          callables.add(createTask(servicesUrl, serviceName, bbox, aggregationStyle, timeoutInSeconds, aggregationStyle));
          bboxes.remove(index);
        }
      } else {
        for (int index=0; index < bboxes.size(); index++) {
          String bbox = bboxes.get(index);
          callables.add(createTask(servicesUrl, serviceName, bbox, aggregationStyle, timeoutInSeconds, aggregationStyle));
        }
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

  private static Callable<Tuple> createTask(String servicesUrl, String serviceName, String boundingBox, String aggStyle, int timeoutInSeconds, String aggregationStyle) {
    Callable<Tuple> task = () -> {
      MapService mapService = new MapService(servicesUrl, serviceName, timeoutInSeconds, aggregationStyle, 10000);
      return mapService.exportMap(boundingBox, 4326, false, aggStyle, false);
    };
    return task;
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










