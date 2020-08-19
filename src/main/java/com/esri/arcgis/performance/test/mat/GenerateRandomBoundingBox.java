package com.esri.arcgis.performance.test.mat;

import com.esri.core.geometry.Envelope2D;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class GenerateRandomBoundingBox {

  public static void main(String[] args) {
    getBoundingBoxWith10kFeatures(args);
  }

  private static void getBoundingBoxWith10kFeatures(String[] args) {
    if (args == null || args.length < 3) {
      System.out.println("Usage: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.GenerateRandomBoundingBox " +
          "<Services Url> <Service Name> <Output File> { <# of bounding boxes: 100> <Min # Features: 1000>}\n" +
              "Ex: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.GenerateRandomBoundingBox https://us-iotdev.arcgis.com/devlion/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/ safegraph5m safegraph5m.txt 100 1000"
              );
      return;
    }

    String servicesUrl = args[0];
    String serviceName = args[1];
    String fileName = "./" + args[2];

    int numBBoxes = 10;
    int minFeatures = 1000;
    if (args.length > 3) numBBoxes = Integer.parseInt(args[3]);
    if (args.length > 4) minFeatures = Integer.parseInt(args[4]);

    try {

      Envelope2D maxExtent = getMaxExtent(servicesUrl, serviceName);
      System.out.println(maxExtent);

      // expand the envelope 10% on each side
      maxExtent.xmin = maxExtent.xmin - (maxExtent.xmax - maxExtent.xmin) * 0.1;
      maxExtent.xmax = maxExtent.xmax + (maxExtent.xmax - maxExtent.xmin) * 0.1;
      maxExtent.ymin = maxExtent.ymin - (maxExtent.ymax - maxExtent.ymin) * 0.1;
      maxExtent.ymax = maxExtent.ymax + (maxExtent.ymax - maxExtent.ymin) * 0.1;


      int validCount = 0;
      while (validCount < numBBoxes) {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        String boundingBox = getBbox(servicesUrl, serviceName, maxExtent , minFeatures);
        writer.append(boundingBox);
        writer. newLine();
        writer.close();
        validCount++;
        System.out.println("Generated =============> " + validCount);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static Envelope2D getMaxExtent(String servicesUrl, String serviceName) {
    MapService mapService = new MapService(servicesUrl, serviceName, 100, "square", 100);
    return mapService.getMaxExtent();
  }

  private static final Random random = new Random();

  static Envelope2D getRandomBox(Envelope2D maxExtent) {
    double randomX = random.nextDouble();
    double randomY = random.nextDouble();
    double xmin = maxExtent.xmin + randomX * (maxExtent.xmax - maxExtent.xmin);
    double ymin = maxExtent.ymin + randomY * (maxExtent.ymax - maxExtent.ymin);
    randomX = random.nextDouble();
    randomY = random.nextDouble();
    double width = randomX * (maxExtent.xmax - maxExtent.xmin);
    double height = randomY * (maxExtent.ymax - maxExtent.ymin);
    return new Envelope2D(xmin, ymin, xmin + width, ymin + height);
  }

  static String getBbox(String servicesUrl, String serviceName, Envelope2D maxExtent,  int minFeatures) {
    String bbox;
    long numFeatures;

    while (true) {
      Envelope2D randomBox = getRandomBox(maxExtent);
      bbox = randomBox.xmin +"," + randomBox.ymin + "," + randomBox.xmax +"," + randomBox.ymax ;
      MapService mapService = new MapService(servicesUrl, serviceName, 100, "square", 100);
      numFeatures = mapService.getCount("1=1", bbox).returnedFeatures;
      if (numFeatures >= minFeatures)
        return  bbox + "|" + numFeatures;
    }
  }

}
