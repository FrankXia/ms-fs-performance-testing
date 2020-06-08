package com.esri.arcgis.performance.test.sst;

public class Tuple {
    long threadId;
    int messageTotal;
    long messageTotalBytes;

    public Tuple(long threadId, long messageTotalBytes,  int messageTotal) {
        this.messageTotal = messageTotal;
        this.messageTotalBytes = messageTotalBytes;
        this.threadId = threadId;
    }
}
