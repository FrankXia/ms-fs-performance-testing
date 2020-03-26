package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComputeStates {
    static String root = "/Users/frank/github/ms-fs-performance-testing/";

    public static void main(String[] args) {
        //sum_10_15_features();
        sum_100_120_features();
    }

    private static void sum_10_15_features()
    {
        String folderName = "mat-performance-testing-10_15-features/";
        String outputPostfix = "10-15-features";
        summarizeRandomExtents(true, folderName, outputPostfix);
        summarizeRandomExtents(false, folderName, outputPostfix);

        summarizeSameExtents(true, folderName, outputPostfix);
        summarizeSameExtents(false, folderName, outputPostfix);
    }

    private static void sum_100_120_features()
    {
        String folderName = "mat-performance-testing-100-120-features/";
        String outputPostfix = "100-120-features";
        summarizeRandomExtents(true, folderName, outputPostfix);
        summarizeRandomExtents(false, folderName, outputPostfix);

        summarizeSameExtents(true, folderName, outputPostfix);
        summarizeSameExtents(false, folderName, outputPostfix);
    }

    private static void summarizeSameExtents(boolean asCSV, String folderName, String outputFilePostfix)
    {
        try {
            String fileName = "summary-same-extent-" + outputFilePostfix + ".csv";
            if (!asCSV) fileName = "summary-same-extent-" + outputFilePostfix + ".txt";
            computeStates("same_extent_", root + folderName, root + fileName, asCSV);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void summarizeRandomExtents(boolean asCSV, String folderName, String outputFilePostfix)
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
            writer.write("# of concurrent threads, # of requests, average, min, max, # of errors");
        } else {
            writer.write("<table class=\"tg\">");
            writer.write("<tr><td># of concurrent threads</td><td> # of requests</td><td> average</td><td> min</td><td> max</td><td> # of errors</td>");
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
                double average = totalTime / totalRequests;
                int numThreads = totalRequests / 10;

                if (asCSV)
                    writer.write(numThreads+"," + totalRequests + "," + average +"," + min + "," + max +"," + errorCount);
                else {
                    writer.write("<tr>");
                    writer.write("<td>" +numThreads+"</td><td>" + totalRequests + "</td><td>" + average +"</td><td>" + min + "</td><td>" + max +"</td><td>" + errorCount + "</td>");
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
