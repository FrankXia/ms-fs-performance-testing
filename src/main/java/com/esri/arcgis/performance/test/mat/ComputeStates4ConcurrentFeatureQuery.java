package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComputeStates4ConcurrentFeatureQuery {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.ComputeStates4ConcurrentFeatureQuery " +
                    "<Testing out file folder> <query/aggregation>");
            System.out.println("Sample command: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.ComputeStates4ConcurrentFeatureQuery " +
                    "/Users/frank/github/ms-fs-performance-testing/test_outputs/query query");
        } else {
            try {
                String folderName = args[0];
                String queryOrAgg = args[1];
                computeStats(folderName, queryOrAgg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private  static DecimalFormat df0 = new DecimalFormat("#");
    static void computeStats(String folderName, String queryOrAgg) throws IOException {
        File dataFolder = new File(folderName);
        String[] allFileNames = dataFolder.list();
        if (allFileNames == null || allFileNames.length == 0) return;
        Arrays.sort(allFileNames);

        for (String fileName: allFileNames) {
            String datasetSize = "";
            String numThreads = "";
            String requests = "";
            String aggStyle = "";
            if (fileName.endsWith(".txt")) {
                String[] items = fileName.split("[_]");
                if (queryOrAgg.equalsIgnoreCase("query")) {
                    String indexSize = items[1];
                    String returnFeatures = items[4];
                    String threads = items[5];
                    String numRequests = items[6];
                    if (indexSize.equals("100k")) datasetSize = "100,000";
                    if (indexSize.equals("1m")) datasetSize = "1 million";
                    if (indexSize.equals("10m")) datasetSize = "10 millions";
                    numThreads = threads.substring(0, threads.indexOf("thread"));
                    requests = numRequests.substring(0, numRequests.indexOf("req"));
                    //System.out.println(fileName + " => " + datasetSize + ", " + numThreads + ", " + requests + ", " + returnFeatures);
                }else{
                    String indexSize = items[0];
                    aggStyle = items[1];
                    String threads = items[2];
                    String numRequests = items[3];
                    if (indexSize.equals("100k")) datasetSize = "100,000";
                    if (indexSize.equals("1m")) datasetSize = "1 million";
                    if (indexSize.equals("10m")) datasetSize = "10 millions";
                    numThreads = threads.substring(0, threads.indexOf("thread"));
                    requests = numRequests.substring(0, numRequests.indexOf("req"));
                    //System.out.println(fileName + " => " + datasetSize + ", " + numThreads + ", " + requests + ", " + aggStyle);
                }

                BufferedReader reader = new BufferedReader(new FileReader(folderName + "/" + fileName));
                int validCount = 0;
                int totalCount = 0;
                int validTotalFeatures = 0;
                List<Double> validFeatures = new LinkedList<Double>();
                List<Double> validReqTimes = new LinkedList<Double>();

                String line = reader.readLine();
                // skip first line
                line = reader.readLine();
                while (line != null) {
                    String[] entries = line.split(",");
                    totalCount += 1;
                    if (entries.length == 3) {
                        int errorCode = Integer.parseInt(entries[2]);
                        if (errorCode == 0) {
                            validCount += 1;
                            validReqTimes.add(Double.parseDouble(entries[0]));
                            validFeatures.add(Double.parseDouble(entries[1]));
                            validTotalFeatures += Integer.parseInt(entries[1]);
                        }
                    }
                    line = reader.readLine();
                }
                int errorCount = totalCount - validCount;
                System.out.print("| " + datasetSize + "  ");
                if (!aggStyle.equals("")) {
                    if (aggStyle.toLowerCase().contains("triangle")) aggStyle = "pointy triangle ";
                    if (aggStyle.toLowerCase().contains("hexagon")) aggStyle = "pointy hexagon  ";
                    if (aggStyle.equalsIgnoreCase("square")) aggStyle = "square          ";
                    if (aggStyle.equalsIgnoreCase("geohash")) aggStyle = "geohash         ";
                    System.out.print("| " + aggStyle);
                }
                System.out.print("|      " + numThreads + "     ");
                System.out.print("| " + df0.format(validTotalFeatures / validCount));
                System.out.print(" | " + Utils.computeStats(validReqTimes.toArray(new Double[0]), validCount, 1));
                System.out.print(" | " + Utils.computeStats(validFeatures.toArray(new Double[0]), validCount, 0));
                System.out.println(" | " + totalCount + " | " + errorCount + " |");
//                System.out.println();
            }
        }
    }

    static String getIndexSize(String[] items) {
        for (String item : items) {
            if (item.endsWith("m")) {
                return item.substring(0, item.indexOf("m"));
            }
        }
        return "";
    }
    static String getReturnNumberFeaturesSize(String[] items) {
        for (String item : items) {
            if (item.endsWith("k")) {
                return item.substring(0, item.indexOf("k"));
            }
        }
        return "";
    }
    static String getNumberThreadsSize(String[] items) {
        for (String item : items) {
            if (item.endsWith("thread")) {
                return item.substring(0, item.indexOf("thread"));
            }
        }
        return "";
    }
    static String getNumberRequestsSize(String[] items) {
        for (String item : items) {
            if (item.endsWith("req")) {
                return item.substring(0, item.indexOf("req"));
            }
        }
        return "";
    }
}
