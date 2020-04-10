package com.esri.arcgis.performance.test.mat;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ComputeStates {

    public static void main(String[] args) {
//        sum_performance_mac_only("/Users/frank/github/ms-fs-performance-testing/extents-10k/sendFromMac/");
//        sum_performance_mac_and_windows("/Users/frank/github/ms-fs-performance-testing/extents-10k/sendFromMacAndWindows/");

        sum_performance_mac_only("/Users/frank/github/ms-fs-performance-testing/results-data/dataset-1m/sendFromMac/");

        extractCrossStats("/Users/frank/github/ms-fs-performance-testing/results-data/dataset-1m/sendFromMac/");
    }

    private static void extractCrossStats(String root) {
        String[] numReturnFeatures = new String[]{"10 to 15", "100 to 120", "500 to 600", "1000 to 1200", "5000 to 5500", "9950 to 10000"};
        String[] folderNames = new String[]{
                "mat-performance-testing-10-15-features/", "mat-performance-testing-100-120-features/",
                "mat-performance-testing-500-600-features/", "mat-performance-testing-1000-1100-features/",
                "mat-performance-testing-5000-5500-features/", "mat-performance-testing-9950-10000-features/"
        };
        String[] sum_file_names = new String[]{
                "extent-10-15-features.csv", "extent-100-120-features.csv", "extent-500-600-features.csv",
                "extent-1000-1100-features.csv", "extent-5000-5500-features.csv", "extent-9950-10000-features.csv"
        };

        // summary same extents
        String sameExtentPrefix = "summary-same-";
        String randomExtentPrefix = "summary-random-";

        // one summary file per concurrent testing
        generateCrossStats(root, sameExtentPrefix, numReturnFeatures, folderNames, sum_file_names);
        generateCrossStats(root, randomExtentPrefix, numReturnFeatures, folderNames, sum_file_names);

        // one summary file all concurrent request testing
        generateCrossStats2(root, sameExtentPrefix, numReturnFeatures, folderNames, sum_file_names);
        generateCrossStats2(root, randomExtentPrefix, numReturnFeatures, folderNames, sum_file_names);
    }
    private static void generateCrossStats(String root, String extentPrefix, String[] numReturnFeatures, String[] folderNames, String[] sum_file_names) {
        String commentPrefix =  "# of concurrent requests - ";
        String fieldNames = "# of returned features, average (ms), error rate %";
        String crossSumStats = "crossSumStats/";

        int[] numThreads = new int[]{1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        try {
            for (int index = 0; index < numThreads.length; index++) {
                String outputFile = root + crossSumStats + extentPrefix + numThreads[index] + ".csv";
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                writer.write(commentPrefix + numThreads[index]);
                writer.newLine();
                writer.write(fieldNames);
                writer.newLine();

                for (int i = 0; i < numReturnFeatures.length; i++) {
                    String numReturnedFeatures = numReturnFeatures[i];
                    double average = 0.0;
                    double errorRate = 0.0;

                    String sumFileName = root + folderNames[i] + extentPrefix + sum_file_names[i];
                    System.out.println(sumFileName);
                    BufferedReader reader = new BufferedReader(new FileReader(sumFileName));
                    reader.readLine();
                    String aLine = reader.readLine();
                    while (aLine != null) {
                        String[] items = aLine.split(",");
                        int concurrentThreads = Integer.parseInt(items[0]);
                        if (concurrentThreads == numThreads[index]) {
                            average = Double.parseDouble(items[2]);
                            errorRate = Double.parseDouble(items[6]);
                            break;
                        }
                        aLine = reader.readLine();
                    }

                    writer.write(numReturnedFeatures + "," + average + "," + errorRate);
                    writer.newLine();
                }

                writer.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private static void generateCrossStats2(String root, String extentPrefix, String[] numReturnFeatures, String[] folderNames, String[] sum_file_names) {
        int[] numThreads = new int[]{1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        try {
            String avgFieldNames = "# of returned features";
            String errFieldNames = "# of returned features";
            String avgFieldNamesHtml = "<table><tr>";
            String errFieldNamesHtml = "<table><tr>";
            for (int i=0; i < numThreads.length; i++) {
                avgFieldNames = avgFieldNames + ", average_" + numThreads[i];
                errFieldNames = errFieldNames + ", errRate_" + numThreads[i];

                avgFieldNamesHtml = avgFieldNamesHtml + "<td>average_" + numThreads[i] + "</td>";
                errFieldNamesHtml = errFieldNamesHtml + "<td>errRate_" + numThreads[i] + "</td>";
            }
            avgFieldNamesHtml = avgFieldNamesHtml + "</tr>";
            errFieldNamesHtml = errFieldNamesHtml + "</tr>";

            String outputFileAvg = root + extentPrefix + "average.csv";
            String outputFileErr = root + extentPrefix + "err-rate.csv";

            BufferedWriter avgWriter = new BufferedWriter(new FileWriter(outputFileAvg));
            avgWriter.write(avgFieldNames);
            avgWriter.newLine();

            BufferedWriter errWriter = new BufferedWriter(new FileWriter(outputFileErr));
            errWriter.write(errFieldNames);
            errWriter.newLine();

            String outputFileAvg_html = root + extentPrefix + "average.txt";
            String outputFileErr_html = root + extentPrefix + "err-rate.txt";

            BufferedWriter avgWriterHtml = new BufferedWriter(new FileWriter(outputFileAvg_html));
            avgWriterHtml.write(avgFieldNamesHtml);
            avgWriterHtml.newLine();

            BufferedWriter errWriterHtml = new BufferedWriter(new FileWriter(outputFileErr_html));
            errWriterHtml.write(errFieldNamesHtml);
            errWriterHtml.newLine();

            for (int i = 0; i < numReturnFeatures.length; i++) {
                String numReturnedFeatures = numReturnFeatures[i];
                StringBuilder average = new StringBuilder();
                StringBuilder errorRate = new StringBuilder();

                StringBuilder averageHtml = new StringBuilder("<tr>");
                StringBuilder errorRateHtml = new StringBuilder("<tr>");
                averageHtml.append("<td>").append(numReturnedFeatures).append("</td>");
                errorRateHtml.append("<td>").append(numReturnedFeatures).append("</td>");

                for (int index = 0; index < numThreads.length; index++) {
                    String sumFileName = root + folderNames[i] + extentPrefix + sum_file_names[i];
                    System.out.println(sumFileName);
                    BufferedReader reader = new BufferedReader(new FileReader(sumFileName));
                    reader.readLine();
                    String aLine = reader.readLine();
                    String avg = "N/A";
                    String err = "N/A";
                    while (aLine != null) {
                        String[] items = aLine.split(",");
                        int concurrentThreads = Integer.parseInt(items[0]);
                        if (concurrentThreads == numThreads[index]) {
                            avg = items[2];
                            err = items[6];
                            break;
                        }
                        aLine = reader.readLine();
                    }
                    average.append(",").append(avg);
                    errorRate.append(",").append(err);

                    averageHtml.append("<td>").append(avg).append("</td>");
                    errorRateHtml.append("<td>").append(err).append("</td>");
                }
                averageHtml.append("</tr>");
                errorRateHtml.append("</tr>");

                avgWriter.write(numReturnedFeatures  + average.toString());
                avgWriter.newLine();
                errWriter.write(numReturnedFeatures + errorRate.toString());
                errWriter.newLine();

                avgWriterHtml.write(averageHtml.toString());
                avgWriterHtml.newLine();
                errWriterHtml.write(errorRateHtml.toString());
                errWriterHtml.newLine();
            }

            avgWriterHtml.write("</table>");
            errWriterHtml.write("</table>");

            avgWriterHtml.close();
            errWriterHtml.close();

            avgWriter.close();
            errWriter.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // one testing client
    private static void sum_performance_mac_only(String root)
    {
        String folderName_10_15 = "mat-performance-testing-10-15-features/";
        sum_10_15_features(root, folderName_10_15);

        String folderName_100_120 = "mat-performance-testing-100-120-features/";
        sum_100_120_features(root, folderName_100_120);

//        String folderName_100_120_2 = "mat-performance-testing-100-120-features-2/";
//        sum_100_120_features(root, folderName_100_120_2);
//
//        String folderName_100_120_3 = "mat-performance-testing-100-120-features-3/";
//        sum_100_120_features(root, folderName_100_120_3);
//
//        String folderName_100_120_4 = "mat-performance-testing-100-120-features-4/";
//        sum_100_120_features(root, folderName_100_120_4);

        String folderName_500_600 = "mat-performance-testing-500-600-features/";
        sum_500_600_features(root, folderName_500_600);

        String folderName_1000_1100 = "mat-performance-testing-1000-1100-features/";
        sum_1000_1100_features(root, folderName_1000_1100);

        String folderName_5000_5500 = "mat-performance-testing-5000-5500-features/";
        sum_5000_5500_features(root, folderName_5000_5500);

        String folderName_9950_10000 = "mat-performance-testing-9950-10000-features/";
        sum_9950_10000_features(root, folderName_9950_10000);
    }

    // two testing client
    private static void sum_performance_mac_and_windows(String root) {
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
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "10-15-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "10-15-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "10-15-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_100_120_features(String root, String folderName) {
        // summarize Mac side only with two testing client
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "100-120-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "100-120-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "100-120-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_500_600_features(String root, String folderName) {
        // summarize Mac side only with two testing client
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "500-600-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "500-600-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "500-600-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_1000_1100_features(String root, String folderName) {
        // summarize Mac side only with two testing client
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "1000-1100-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "1000-1100-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "1000-1100-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_5000_5500_features(String root, String folderName) {
        // summarize Mac side only with two testing client
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "5000-5500-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "5000-5500-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "5000-5500-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_9950_10000_features(String root, String folderName) {
        // summarize Mac side only with two testing client
//        String inputPostfix_a = "_a.txt";
//        String outputPostfix_a_mac = "9950-10000-features-a-mac";
//        sum_same_random_states(root, folderName, inputPostfix_a, outputPostfix_a_mac);
//
//        // summarize Windows side only with two testing client
//        String inputPostfix_b = "_b.txt";
//        String outputPostfix_b_win = "9950-10000-features-b-win";
//        sum_same_random_states(root, folderName, inputPostfix_b, outputPostfix_b_win);

        // summarize with data from both Mac and Windows clients (after testing results data copied from
        // windows machine
        String inputPostfix_a_b = ".txt";
        String outputPostfix_a_b = "9950-10000-features";
        sum_same_random_states(root, folderName, inputPostfix_a_b, outputPostfix_a_b);
    }

    private static void sum_same_random_states(String root, String folderName, String inputFilePostfix,  String outputPostfix) {
        summarizeRandomExtents(root, true, folderName, inputFilePostfix, outputPostfix);
        summarizeRandomExtents(root,false, folderName, inputFilePostfix, outputPostfix);

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
            writer.write("# of concurrent threads, # of requests, average, min, max, # of errors, error rate %");
        } else {
            writer.write("<table class=\"tg\">");
            writer.write("<tr><td># of concurrent threads</td><td> # of requests</td><td> average</td><td> min</td><td> max</td><td> # of errors</td><td>error rate %</td>");
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
