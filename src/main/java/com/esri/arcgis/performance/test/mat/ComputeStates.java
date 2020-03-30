package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComputeStates {

    public static void main(String[] args) {
    }

    private static void sum_10k_performance()
    {
        String root = "/Users/frank/github/ms-fs-performance-testing/extents-10k/";
        sum_10_15_features(root);
        sum_100_120_features(root);
        sum_500_600_features(root);
    }

    private static void sum_500_600_features(String root)
    {
        String folderName = "mat-performance-testing-500-600-features/";
        String outputPostfix = "500-600-features";
        summarizeRandomExtents(root, true, folderName, outputPostfix);
        summarizeRandomExtents(root,false, folderName, outputPostfix);

        summarizeSameExtents(root, true, folderName, outputPostfix);
        summarizeSameExtents(root, false, folderName, outputPostfix);
    }

    private static void sum_10_15_features(String root)
    {
        String folderName = "mat-performance-testing-10-15-features/";
        String outputPostfix = "10-15-features";
        summarizeRandomExtents(root, true, folderName, outputPostfix);
        summarizeRandomExtents(root, false, folderName, outputPostfix);

        summarizeSameExtents(root, true, folderName, outputPostfix);
        summarizeSameExtents(root, false, folderName, outputPostfix);
    }

    private static void sum_100_120_features(String root)
    {
        String folderName = "mat-performance-testing-100-120-features/";
        String outputPostfix = "100-120-features";
        summarizeRandomExtents(root, true, folderName, outputPostfix);
        summarizeRandomExtents(root, false, folderName, outputPostfix);

        summarizeSameExtents(root, true, folderName, outputPostfix);
        summarizeSameExtents(root, false, folderName, outputPostfix);
    }

    private static void summarizeSameExtents(String root, boolean asCSV, String folderName, String outputFilePostfix)
    {
        try {
            String fileName = "summary-same-extent-" + outputFilePostfix + ".csv";
            if (!asCSV) fileName = "summary-same-extent-" + outputFilePostfix + ".txt";
            computeStates("same_extent_", root + folderName, root + fileName, asCSV);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void summarizeRandomExtents(String root, boolean asCSV, String folderName, String outputFilePostfix)
    {
        try {
            String fileName = "summary-random-extent-" + outputFilePostfix + ".csv";
            if (!asCSV) fileName = "summary-random-extent-" +  outputFilePostfix + ".txt";
            computeStates("random_extent_", root + folderName, root + fileName, asCSV);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void computeStates(String filePrefix, String folder, String outputFile, boolean asCSV) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        if (asCSV) {
            writer.write("# of concurrent threads, # of requests, average, min, max, # of errors, error rate");
        } else {
            writer.write("<table class=\"tg\">");
            writer.write("<tr><td># of concurrent threads</td><td> # of requests</td><td> average</td><td> min</td><td> max</td><td> # of errors</td><td>error rate</td>");
        }
        writer.newLine();

        File dataFolder = new File(folder);
        String[] fileNames = dataFolder.list();

        List<Tuple> fileList = new LinkedList<>();
        for (String fileName: fileNames) {
            if (fileName.startsWith(filePrefix)) {
                int order = Integer.parseInt(fileName.substring(filePrefix.length()).split("-")[0]);
                Tuple tuple = new Tuple(fileName, order);
                fileList.add(tuple);
            }
        }
        Tuple[] orderedFileNames = fileList.toArray(new Tuple[fileList.size()]);
        Arrays.sort(orderedFileNames);
        for (Tuple tuple : orderedFileNames) {
            String fileName = tuple.fileName;
            if (fileName.startsWith(filePrefix)) {
                BufferedReader reader = new BufferedReader(new FileReader(folder + "/" + fileName));
                reader.readLine();
                String aLine = reader.readLine();
                int validCount = 0;
                int totalRequests = 0;
                int errorCount = 0;
                double totalTime = 0.0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                while (aLine != null) {
                    String[] parsedLine = aLine.split(",");
                    if (parsedLine.length == 3) {
                        validCount++;
                        double time = Double.parseDouble(parsedLine[0]);
                        totalTime += time;
                        if (time > max) max = time;
                        if (time < min) min = time;
                        int errorCode = Integer.parseInt(parsedLine[2]);
                        if (errorCode == 2) errorCount++;
                    }
                    totalRequests++;
                    aLine = reader.readLine();
                }
                reader.close();

                int errors = totalRequests - validCount;
                double errorRate = (double)errorCount / (double)totalRequests * 100;
                double average = totalTime / totalRequests;
                int numThreads = totalRequests / 10;

                DecimalFormat decimalFormat = new DecimalFormat("###.00");
                if (asCSV)
                    writer.write(numThreads+"," + totalRequests + "," +decimalFormat.format(average) +"," + min + "," + max +"," + errorCount + "," +  decimalFormat.format(errorRate));
                else {
                    writer.write("<tr>");
                    writer.write("<td>" + numThreads + "</td><td>" + totalRequests + "</td><td>" + decimalFormat.format(average) + "</td><td>" + min + "</td><td>" + max + "</td><td>" + errorCount + "</td><td>" + decimalFormat.format(errorRate)  + "</td>");
                    writer.write("</tr>");
                }
                writer.newLine();
            }
        }

        if (!asCSV)  writer.write("</table>");
        writer.close();
    }


    static class Tuple implements Comparable<Tuple> {
        String fileName;
        Integer order;

        public Tuple(String fileName, int order) {
            this.order = order;
            this.fileName = fileName;
        }

        public int compareTo(Tuple tuple) {
            return this.order - tuple.order;
        }
    }
}
