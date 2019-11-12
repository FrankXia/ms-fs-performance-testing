package com.esri.arcgis.performance.test.es;

import com.esri.arcgis.performance.test.mat.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ESConcurrentTester {
    private int timeoutInSeconds = 60;

    // java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.es.ESConcurrentTester http://localhost:9200/ safegraph 2 2 ./docs_kube1/safegraph1.txt 0 ./output1_es_2_2.txt

    public static void main(String[] args) {
        if (args.length >= 7) {
            String esServerUrl = args[0];
            String indexName = args[1];
            int numThreads = Integer.parseInt(args[2]);
            int numConcurrentCalls = Integer.parseInt(args[3]);
            String boundingBoxFile = args[4];
            int numSkips = Integer.parseInt(args[5]);
            String outputFileName = args[6];
            ESConcurrentTester esConcurrentTester = new ESConcurrentTester();
            esConcurrentTester.testWithEnvelopes(esServerUrl, indexName, outputFileName, boundingBoxFile, numThreads, numConcurrentCalls, numSkips);
        } else {
            System.out.println("Usage: java -cp ./ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.es.ESConcurrentTester <ES Server Url> <Index Name> <Number_Of_Threads> <Number_Of_Concurrent_Calls> <Path to bounding box file> <Number of lines to skip>  <Output File Name> ");
        }
    }


    private Callable<Tuple> createTask(String serviceUrl, String serviceName, String boundingBox, int timeoutInSeconds, boolean closeClient, BufferedWriter writer) throws Exception {
        Callable<Tuple> task = () -> {
            ElasticsearchQuery elasticsearchQuery = new ElasticsearchQuery(serviceUrl, serviceName);
            return elasticsearchQuery.getFeatures(boundingBox, timeoutInSeconds, closeClient, writer);
        };
        return task;
    }

    private void testWithEnvelopes(String servicesUrl, String serviceName, String outputFileName,
            String boundingBoxFileName, int numbThreads, int numbConcurrentCalls, int numSkips) {

        ExecutorService executor = Executors.newFixedThreadPool(numbThreads);

        try {
            BufferedWriter curl_writer = null; // new BufferedWriter(new FileWriter("./curl-commands.sh"));

            BufferedReader reader = new BufferedReader(new FileReader(boundingBoxFileName));
            List<String> boundingBoxes = new LinkedList<>();
            String box = reader.readLine();
            while (box != null) {
                String[] boxAndFeatures = box.split("\\|");
                if (boxAndFeatures.length == 2 && numSkips <= 0) {
                    boundingBoxes.add(boxAndFeatures[0]);
                }
                box = reader.readLine();
                numSkips--;
            }
            reader.close();
            numbConcurrentCalls = Math.min(boundingBoxes.size(), numbConcurrentCalls);

            List<Callable<Tuple>> callables = new LinkedList<>();

            for (int index=0; index < numbConcurrentCalls; index++) {
                String boundingBox = boundingBoxes.get(index);
                callables.add(createTask(servicesUrl, serviceName, boundingBox, timeoutInSeconds,true, curl_writer));
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
            }

            if (curl_writer != null) curl_writer.close();

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
    }
}
