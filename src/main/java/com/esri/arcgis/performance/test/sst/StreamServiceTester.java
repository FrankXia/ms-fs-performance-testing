package com.esri.arcgis.performance.test.sst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;


public class StreamServiceTester implements ReconnectListener {
    private final static NumberFormat nf = NumberFormat.getInstance();
    private final List<WebSocketClientWrapper> clientList = new LinkedList<>();
    private final String serviceUrl;
    private int totalFailedConnections = 0;
    private final int timeoutInSeconds;
    private long requestInterval = 1000L;
    private final int numConcurrentConnections;

    public static void main(String[] args) throws Exception {
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        if (args.length == 0) {
            System.out.println("Usage: java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.sst.StreamServiceTester <Stream Service Url> {<Number of concurrent connections> <Duration in minutes> <Request interval (ms)>}");
            System.exit(0);
        }

//        String streamServiceUrl = "wss://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/streams/arcgis/ws/services/ais_josn_1_sst/StreamServer/assribe?token=bumsTmEzXyRsxeRGHqtdDYCUhOfpPXK4CKD-5z0RxDHTehyDJQxHVPgJjpm2Su798R1Pn-3H4bbFqiIJInoaAaBFutQ98dndwraahs8CuNC7mtsnZf9t9MIcQAnkY9jKYaQA9RgJOFyLYHcIcidibGSi92jMV-9dJQYiVuUonGpQLKgca01q3K3FTrT3eX5A7N1R-qmNkvEO6JJX5ABJluTPRoKmqfrNAmXtyNWzImg.";
//        String streamServiceUrl = "wss://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/streams/arcgis/ws/services/ais_josn_250_sst/StreamServer/subscribe?token=bumsTmEzXyRsxeRGHqtdDYCUhOfpPXK4CKD-5z0RxDHTehyDJQxHVPgJjpm2Su798R1Pn-3H4bbFqiIJInoaAaBFutQ98dndwraahs8CuNC7mtsnZf9t9MIcQAnkY9jKYaQA9RgJOFyLYHcIcidibGSi92jMV-9dJQYiVuUonGpQLKgca01q3K3FTrT3eX5A7N1R-qmNkvEO6JJX5ABJluTPRoKmqfrNAmXtyNWzImg.";
        String streamServiceUrl = "wss://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/streams/arcgis/ws/services/ais_json_250_sst/StreamServer/";

        streamServiceUrl = args[0];
        if (!args[0].endsWith("/")) streamServiceUrl += "/";
        streamServiceUrl += "subscribe?token=";

        int numConcurrentConnections = 40;
        if (args.length >= 2) {
            numConcurrentConnections = Integer.parseInt(args[1]);
        }

        int testingDuration = 5; // 5 minutes
        if (args.length >= 3) {
            testingDuration = Integer.parseInt(args[2]);
        }

        // milli-seconds
        long requestInterval = 1000;
        if (args.length >= 4)  {
            requestInterval = Integer.parseInt(args[3]);
        }

        // read token from disk
        String token = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("sst-access-token.txt"));
            String line = reader.readLine();
            if (line != null) token = line;
            else System.err.println("Error in reading required 'token' string.");
        } catch (Exception ex) {
            System.err.println("Error in reading required 'token' string.");
            ex.printStackTrace();
            System.exit(0);
        }

        if (token != null) {
            streamServiceUrl += token;
        } else {
            System.err.println("required 'token' is null.");
            System.exit(0);
        }

        StreamServiceTester streamServiceTester = new StreamServiceTester(streamServiceUrl, numConcurrentConnections, requestInterval, testingDuration);
        streamServiceTester.startStreaming();
    }

    public StreamServiceTester(String streamServiceUrl, int numConcurrentConnections,
                               long requestInterval, int testingDuration) {
        serviceUrl = streamServiceUrl;
        timeoutInSeconds = testingDuration * 60;
        this.requestInterval = requestInterval;
        this.numConcurrentConnections = numConcurrentConnections;
    }

    private void startStreaming()  throws InterruptedException {
        System.out.println("# of requests: " + numConcurrentConnections);

        // terminating testing Timer
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("starting shutdown .... ");
                System.out.println("===============================");
                int totalFailed = 0;

                System.out.println("Thread Id, Total Msg, Total Bytes");
                double sumOfThroughput = 0;
                double sumOfRequestByteRate = 0;
                double minThroughput = 0;
                double maxThroughput = 0;
                for (WebSocketClientWrapper client : clientList) {
                    Tuple tuple = client.close();
                    System.out.println(tuple.threadId + ", " +  tuple.messageTotal +", " + tuple.messageTotalBytes);
                    if (tuple.messageTotalBytes == 0 || tuple.messageTotal == 0)
                        totalFailed++;
                    else {
                        double currentThroughput = (tuple.messageTotalBytes / timeoutInSeconds) / 1000;
                        double currentRequesteDataRate =  tuple.messageTotalBytes / tuple.messageTotal;
                        if (minThroughput == 0 || minThroughput > currentThroughput) minThroughput = currentThroughput;
                        if (maxThroughput == 0 || maxThroughput < currentThroughput) maxThroughput = currentThroughput;
                        sumOfRequestByteRate += currentRequesteDataRate;
                        sumOfThroughput+=currentThroughput;
                    }
                }
                System.out.println("-------------------------------");
                System.out.println("min throughput: " + minThroughput + "kb/s");
                System.out.println("max throughput: " + maxThroughput + "kb/s");
                System.out.println("avg throughput: " + sumOfThroughput / (clientList.size() - totalFailed) + "kb/s");
                System.out.println("avg request rate: " + sumOfRequestByteRate/ (clientList.size() - totalFailed) + "bytes/s");
                System.out.println("total connections: " + clientList.size() + ", failed: " + totalFailed + ", rate: " + nf.format((totalFailed * 1.0/clientList.size()) * 100) + "%");
                System.out.println("=============================== total failed connections: " + totalFailedConnections);
                System.exit(0);
            }
        };
        Timer timer = new Timer("Timer");
        timer.schedule(timerTask,  timeoutInSeconds * 1000); // milli-seconds

        for (int i=0; i<numConcurrentConnections; i++) {
            WebSocketClientWrapper webSocketClientWrapper = new WebSocketClientWrapper(serviceUrl, requestInterval);
            webSocketClientWrapper.setReconnectListener(this);
            webSocketClientWrapper.setTimeout(timeoutInSeconds);
            webSocketClientWrapper.connect();
            clientList.add(webSocketClientWrapper);
            Thread.sleep(requestInterval); // wait little bit before sending out another request
        }
        System.out.println("# of request sent: " + clientList.size());
    }

    @Override
    public void reconnect(long threadId) {
        totalFailedConnections++;
        System.out.println("trying to reconnect ... " + threadId + ", " + (new Date()));
        synchronized (clientList) {
            WebSocketClientWrapper clientWrapper = null;
            int validConnections = 0;
            for (int i = 0; i < clientList.size(); i++) {
                if (clientList.get(i) == null) continue;
                if (!clientList.get(i).isConnectFailed()) validConnections++;
                if (clientList.get(i).getThreadId() == threadId) {
                    clientWrapper = clientList.get(i);
                }
            }
            if (clientWrapper != null) {
                clientList.remove(clientWrapper);
                WebSocketClientWrapper webSocketClientWrapper = new WebSocketClientWrapper(serviceUrl, requestInterval);
                webSocketClientWrapper.setReconnectListener(this);
                webSocketClientWrapper.setTimeout(timeoutInSeconds);
                webSocketClientWrapper.connect();
                clientList.add(webSocketClientWrapper);
                System.out.println("        reconnected -> " + threadId + ", valid connections: " + validConnections);
            }
        }
    }
}
