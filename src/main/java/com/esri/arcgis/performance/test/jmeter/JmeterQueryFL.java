package com.esri.arcgis.performance.test.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class JmeterQueryFL extends AbstractJavaSamplerClient {
    long testDurationInSeconds = 10;
    private JmeterQueryFLTest jmeterQueryFLTest = new JmeterQueryFLTest();

    public JmeterQueryFL() {
        super();
    }

    @Override
    public Arguments getDefaultParameters() {

        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("serviceURL", "");
        defaultParameters.addArgument("serviceName", "");
        defaultParameters.addArgument("queryParameters", "");
        return defaultParameters;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);

        String serviceURL = context.getParameter("serviceURL");
        String serviceName = context.getParameter("serviceName");
        String queryParameters = context.getParameter("queryParameters");

        jmeterQueryFLTest =
                new JmeterQueryFLTest(
                        serviceURL,
                        serviceName,
                        queryParameters);
    }

    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();

        result.sampleStart(); // start stopwatch
        try {
            // if producer failed to initialize -> report an error by throwing an exception
            // if (producer.errorMsg().nonEmpty()) {
            // System.out.println("producer.errorMsg().get()>>>>" + producer.errorMsg().get());
            // throw new Exception(producer.errorMsg().get());
            // }

            // producer.start();
            // sleep();
            // producer.stop();

            new Thread(jmeterQueryFLTest).start();

            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(true);
            // result.setResponseMessage("Successfully produced " + producer.featuresProduced() + " features." );
            result.setResponseCodeOK(); // 200 code

        } catch (Exception error) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + error);

            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            error.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString());
            result.setDataType(SampleResult.TEXT);
            result.setResponseCode("500");
        }

        return result;
    }

    private void sleep() {
        try {
            Thread.sleep(testDurationInSeconds * 1000);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
        // producer.stop();
        // producer = null;
    }
}
