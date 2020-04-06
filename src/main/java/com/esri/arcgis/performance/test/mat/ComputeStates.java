package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComputeStates {

    public static void main(String[] args) {
        sum_10k_performance_mac_only();
//        sum_10k_performance_mac_and_windows();
    }

    // one testing client
    private static void sum_10k_performance_mac_only()
    {
        String root = "/Users/frank/github/ms-fs-performance-testing/extents-10k/sendFromMac/";

//        String folderName_10_15 = "mat-performance-testing-10-15-features/";
//        sum_10_15_features(root, folderName_10_15);
//
//        String folderName_100_120_2 = "mat-performance-testing-100-120-features-2/";
//        sum_100_120_features(root, folderName_100_120_2);
//
//        String folderName_100_120_3 = "mat-performance-testing-100-120-features-3/";
//        sum_100_120_features(root, folderName_100_120_3);

        String folderName_100_120_4 = "mat-performance-testing-100-120-features-4/";
        sum_100_120_features(root, folderName_100_120_4);

//        String folderName_500_600 = "mat-performance-testing-500-600-features/";
//        sum_500_600_features(root, folderName_500_600);
    }

    // two testing client
    private static void sum_10k_performance_mac_and_windows() {
        String root = "/Users/frank/github/ms-fs-performance-testing/extents-10k/sendFromMacAndWindows/";

//        String folderName_10_15 = "mat-performance-testing-10-15-features/";
//        sum_10_15_features(root, folderName_10_15);

        String folderName_100_120_2 = "mat-performance-testing-100-120-features-2/";
        sum_100_120_features(root, folderName_100_120_2);

//        String folderName_100_120 = "mat-performance-testing-100-120-features/";
//        sum_100_120_features(root, folderName_100_120);

//        String folderName_500_600 = "mat-performance-testing-500-600-features/";
//        sum_500_600_features(root, folderName_500_600);
    }

    private static void sum_10_15_features(String root, String folderName) {
        // summarize Mac side only with two testing client
        String inputPostfix_a = "_a.txt";
        String outputPostfix_a_mac = "10-15-features-a-mac";
        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);

        // summarize Windows side only with two testing client
        String inputPostfix_b = "_b.txt";
        String outputPostfix_b_win = "10-15-features-b-win";
        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "10-5-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_100_120_features(String root, String folderName) {
        // summarize Mac side only with two testing client
        String inputPostfix_a = "_a.txt";
        String outputPostfix_a_mac = "100-120-features-a-mac";
        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);

        // summarize Windows side only with two testing client
        String inputPostfix_b = "_b.txt";
        String outputPostfix_b_win = "100-120-features-b-win";
        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "100-120-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_500_600_features(String root, String folderName) {
        // summarize Mac side only with two testing client
        String inputPostfix_a = "_a.txt";
        String outputPostfix_a_mac = "500-600-features-a-mac";
        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);

        // summarize Windows side only with two testing client
        String inputPostfix_b = "_b.txt";
        String outputPostfix_b_win = "500-600-features-b-win";
        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "500-600-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_same_random_states(String root, String folderName, String inputFilePostfix,  String outputPostfix) {
//        summarizeRandomExtents(root, true, folderName, inputFilePostfix, outputPostfix);
//        summarizeRandomExtents(root,false, folderName, inputFilePostfix, outputPostfix);

        summarizeSameExtents(root, true, folderName, inputFilePostfix, outputPostfix);
        summarizeSameExtents(root, false, folderName, inputFilePostfix, outputPostfix);
    }

    private static void summarizeSameExtents(String root, boolean asCSV, String folderName, String inputFilePostfix, String outputFilePostfix)
    {
        try {
            String fileName = "summary-same-extent-" + outputFilePostfix + ".csv";
            if (!asCSV) fileName = "summary-same-extent-" + outputFilePostfix + ".txt";
            computeStates("same_extent_", inputFilePostfix, root + folderName, root + folderName + fileName, asCSV);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void summarizeRandomExtents(String root, boolean asCSV, String folderName, String inputFilePostfix, String outputFilePostfix)
    {
        try {
            String fileName = "summary-random-extent-" + outputFilePostfix + ".csv";
            if (!asCSV) fileName = "summary-random-extent-" +  outputFilePostfix + ".txt";
            computeStates("random_extent_", inputFilePostfix, root + folderName, root + folderName + fileName, asCSV);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void computeStates(String filePrefix, String filePostfix, String folder, String outputFile, boolean asCSV) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        if (asCSV) {
            writer.write("# of concurrent threads, # of requests, average, min, max, # of errors, error rate");
        } else {
            writer.write("<table class=\"tg\">");
            writer.write("<tr><td># of concurrent threads</td><td> # of requests</td><td> average</td><td> min</td><td> max</td><td> # of errors</td><td>error rate</td>");
        }
        writer.newLine();

        File dataFolder = new File(folder);
        String[] allFileNames = dataFolder.list();

        List<Tuple> fileList = new LinkedList<>();
        for (String fileName: allFileNames) {
            if (fileName.startsWith(filePrefix) && fileName.endsWith(filePostfix)) {
                int order = Integer.parseInt(fileName.substring(filePrefix.length()).split("-")[0]);
                Tuple tuple = null;
                for (Tuple t: fileList) {
                    if (t.order == order) {
                        tuple = t;
                        break;
                    }
                }
                if (tuple == null) {
                    tuple = new Tuple(order);
                    fileList.add(tuple);
                }
                tuple.fileNames.add(fileName);
            }
        }
        Tuple[] orderedFileNames = fileList.toArray(new Tuple[fileList.size()]);
        Arrays.sort(orderedFileNames);
        for (Tuple tuple : orderedFileNames) {
            List<String> fileNames = tuple.fileNames;
            int validCount = 0;
            int totalRequests = 0;
            int errorCount = 0;
            double totalTime = 0.0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (String fileName : fileNames) {
                BufferedReader reader = new BufferedReader(new FileReader(folder + "/" + fileName));
                reader.readLine();
                String aLine = reader.readLine();
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
            }
            int errors = totalRequests - validCount;
            double errorRate = (double) errorCount / (double) totalRequests * 100;
            double average = totalTime / totalRequests;
            int numThreads = totalRequests / 10;

            DecimalFormat decimalFormat = new DecimalFormat("###.00");
            if (asCSV)
                writer.write(numThreads + "," + totalRequests + "," + decimalFormat.format(average) + "," + min + "," + max + "," + errorCount + "," + decimalFormat.format(errorRate));
            else {
                writer.write("<tr>");
                writer.write("<td>" + numThreads + "</td><td>" + totalRequests + "</td><td>" + decimalFormat.format(average) + "</td><td>" + min + "</td><td>" + max + "</td><td>" + errorCount + "</td><td>" + decimalFormat.format(errorRate) + "</td>");
                writer.write("</tr>");
            }
            writer.newLine();
        }

        if (!asCSV)  writer.write("</table>");
        writer.close();
    }


    static class Tuple implements Comparable<Tuple> {
        List<String> fileNames = new LinkedList<>();
        Integer order;

        public Tuple(int order) {
            this.order = order;
        }

        public int compareTo(Tuple tuple) {
            return this.order - tuple.order;
        }
    }
}
