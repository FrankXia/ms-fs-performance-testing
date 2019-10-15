package com.esri.arcgis.performance.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class GenerateBoundingBox {

  public static void main(String[] args) {
    getBoundingBoxWith10kFeatures(args);
  }

  private static double MAX_W = 360;
  private static double MAX_H = 180;
  private static double MIN_X = -180;
  private static double MIN_Y = -90;

  private static void getBoundingBoxWith10kFeatures(String[] args) {
    if (args == null || args.length < 3) {
      System.out.println("Usage: java -cp ./ms-ms-performance-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.GenerateBoundingBox " +
          "<Services Url> <Service Name> <Output File> { <# of bounding boxes: 259> <width: 180> <height: 90> <Return range: 9900,10000> <Min X,Y> <Max X,Y>}\n" +
              "Ex: java -cp ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.GenerateBoundingBox https://us-iotdev.arcgis.com/fx1014a/maps/arcgis/rest/services/ Safegraph3 safegraph3.txt 1 30 20 100000,1000000 -125,25 65,50");
      return;
    }
    int featureLimitMax = 10000;
    int featureLimitMin = 9900;

    String servicesUrl = args[0];
    String name = args[1];
    String fileName = "./" + args[2];

    int numBBoxes = 259;
    double width = 180;
    double height = 90;
    if (args.length > 3) numBBoxes = Integer.parseInt(args[3]);
    if (args.length > 4) width = Double.parseDouble(args[4]);
    if (args.length > 5) height = Double.parseDouble(args[5]);
    if (args.length > 6) {
      String[] limits = args[6].split(",");
      if (limits.length == 2) {
        featureLimitMin = Integer.parseInt(limits[0]);
        featureLimitMax = Integer.parseInt(limits[1]);
      }
    }
    if (args.length > 7) {
      String[] minxy = args[7].split(",");
      if (minxy.length == 2) {
        MIN_X = Double.parseDouble(minxy[0]);
        MIN_Y = Double.parseDouble(minxy[1]);
      }
    }
    if (args.length > 8) {
      String[] maxxy = args[8].split(",");
      if (maxxy.length == 2) {
        double maxx = Double.parseDouble(maxxy[0]);
        if (maxx > MIN_X) MAX_W = maxx - MIN_X;
        double maxy = Double.parseDouble(maxxy[1]);
        if (maxy > MIN_Y) MAX_H = maxy - MIN_Y;
      }
    }

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

      int validCount = 0;
      while (validCount < numBBoxes) {
        String boundingBox = getBbox(servicesUrl, name, featureLimitMin, featureLimitMax, width, height, validCount);
        writer.append(boundingBox);
        writer. newLine();
        validCount++;
        System.out.println("Generated =============> " + validCount);
      }

      writer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  static class MinXY {
    double minx;
    double miny;

    public MinXY(double minx, double miny) {
      this.minx = minx;
      this.miny = miny;
    }
  }

  private static Random random = new Random();
  static MinXY getBbox(double width, double height) {
    double randomX = random.nextDouble();
    double randomY = random.nextDouble();
    double minx = MIN_X + randomX * (MAX_W - width);
    double miny = MIN_Y + randomY * (MAX_H - height);
    return new MinXY(minx, miny);
  }

  static String getBbox(String servicesUrl, String serviceName, int featureLimitMin, int featureLimitMax, double initWidth, double initHeight,  int count) {
    String bbox;
    long numFeatures;
    while (true) {
      MinXY minXY = getBbox(initWidth, initHeight);
      double minx = minXY.minx;
      double miny = minXY.miny;

      double maxx = minx + initWidth;
      double maxy = miny + initHeight;

      double width = maxx - minx;
      double height = maxy - miny;

      bbox = minx +"," + miny + "," +maxx+","+maxy;
      MapService mapService = new MapService(servicesUrl, serviceName, 100);
      numFeatures = mapService.getCount("1=1", bbox).returnedFeatures;
      bbox = bbox + "|" + numFeatures;

//      int limitRange = featureLimitMax - featureLimitMin;
      long delta = featureLimitMax - numFeatures;
      int loopCount = 0;

//      while (Math.abs(delta) > limitRange || delta < 0) {
      while  (numFeatures < featureLimitMin || numFeatures > featureLimitMax) {
        double percent = (double) delta / (double) featureLimitMax;
        System.out.println("# of features: " + numFeatures + ", delta: " + delta + ", loop count: " + loopCount + ", percentage: " + percent + ", bbox: " + bbox);
        if (Math.abs(percent) >= 1) {
          percent = percent > 0 ? 1/2.0 : -1/2.0;
          width = width + width * percent;
          height = height + height * percent;
        } else {
          if (loopCount % 2 == 0)
            width = width + width * percent;
          else
            height = height + height * percent;
        }
        loopCount++;
        maxx = (minx + width) % 180.0;
        maxy = (miny + height) % 90.0;
        bbox = minx + "," + miny + "," + maxx + "," + maxy;
        numFeatures = mapService.getCount("1=1", bbox).returnedFeatures;
        delta = featureLimitMax - numFeatures;
        bbox = bbox  + "|" + numFeatures;

        if (loopCount > 50) {
          bbox = null;
          break;
        }
      }
      if (bbox != null) break;
    }
    System.out.println( count + " ---------------------------------> " + numFeatures + " " + bbox);
    return bbox;
  }

}
