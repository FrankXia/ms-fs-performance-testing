package com.esri.arcgis.performance.test.jmeter;

import com.esri.arcgis.performance.test.mat.FeatureLayerQueryTester;

public class JmeterQueryFLTest implements Runnable {
    String serviceURL;
    String serviceName;
    String queryParameters;

    public JmeterQueryFLTest() {
    }

    public JmeterQueryFLTest(
            String serviceURL,
            String serviceName,
            String queryParameters) {
        this.serviceURL = serviceURL;
        this.serviceName = serviceName;
        this.queryParameters = queryParameters;
    }

    @Override
    public void run() {
        String[] args =
                new String[]{
                        serviceURL,
                        serviceName,
                        queryParameters};

        try {
            FeatureLayerQueryTester.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

