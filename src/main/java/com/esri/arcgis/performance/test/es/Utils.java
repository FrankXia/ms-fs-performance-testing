package com.esri.arcgis.performance.test.es;


import java.io.*;

public class Utils {
    static int increment = 0;

    public static void main (String[] args) {
        if (args.length >= 2) {
            removeInvalidBoundingBoxes(args[0], args[1]);
        } else {
            System.out.println("Usage: java -cp -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.es.Utils <Input Bounding Box File> <Output Bounding Box File>");
        }
    }

    static void removeInvalidBoundingBoxes(String inputFile, String outputFile) {
        try {
            int badCount = 0;
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter( new FileWriter(outputFile));

            String line = reader.readLine();
            while (line != null) {
                String[] envelopeAndFeatures = line.split("[|]");
                if (envelopeAndFeatures.length == 2) {
                    String[] envelope = envelopeAndFeatures[0].split(",");
                    if (envelope.length == 4) {
                        double topLeftX = Double.parseDouble(envelope[0]);
                        double topLeftY = Double.parseDouble(envelope[3]);
                        double bottomRightX = Double.parseDouble(envelope[2]);
                        double bottomRightY = Double.parseDouble(envelope[1]);
                        if (topLeftX > bottomRightX || topLeftY < bottomRightY) {
                            System.out.println( (badCount++) + " ==> Bad envelope: " + envelopeAndFeatures[0]);
                        } else {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                }
                line = reader.readLine();
            }

            reader.close();
            writer.close();

            System.out.println("Total bad envelopes: " + badCount);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
