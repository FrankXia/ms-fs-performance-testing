package com.esri.arcgis.performance.test.mat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public class FeatureLayerQueryTester {

    private static int timeoutInSeconds = 120;
    public static void main(String[] args)  throws Exception {
        String servicesUrl = args[0];
        String serviceName = args[1];
        String queryParams = args[2];
//        String where = args[2];
//        String objectIds = args[3];
//        String time = args[4];
//        String defaultSR = args[5];
//        String geometry = args[6];
//        String geometryType = args[7];
//        String geohash = args[8];
//        String inSr = args[9];
//        String outFields = args[10];
//        String returnGeometry = args[11];
//        String returnDistinctValues = args[12];
//        String returnIdsOnly = args[13];
//        String returnCountOnly = args[14];
//        String returnExtentOnly = args[15];
//        String orderByFields = args[16];
//        String groupByFieldsForStatistics = args[17];
//        String outStatistics = args[18];
//        String returnZ = args[19];
//        String returnM = args[20];
//        String multipatchOption = args[21];
//        String resultOffset = args[22];
//        String resultRecordCount = args[23];
//        String f = args[24];

        Map params = createParameterMap(queryParams);

        FeatureService featureService = new FeatureService(servicesUrl, serviceName, timeoutInSeconds, false);

        // query parameters
        featureService.setWhere(params.get("where").toString());
        featureService.setObjectIds(params.get("objectIds").toString());
        featureService.setTime(params.get("time").toString());
        featureService.setDefaultSR(params.get("defaultSR").toString());
        featureService.setGeometry(params.get("geometry").toString());
        featureService.setGeometryType(params.get("geometryType").toString());
        featureService.setGeohash(params.get("geohash").toString());
        featureService.setInSR(params.getOrDefault("inSr", "4326").toString());
        featureService.setOutFields(params.get("outFields").toString());
        featureService.setReturnGeometry(Boolean.parseBoolean(params.get("returnGeometry").toString()));
        featureService.setReturnDistinctValues(Boolean.parseBoolean(params.get("returnDistinctValues").toString()));
        featureService.setReturnIdsOnly(Boolean.parseBoolean(params.get("returnIdsOnly").toString()));
        featureService.setReturnCountOnly(Boolean.parseBoolean(params.get("returnCountOnly").toString()));
        featureService.setReturnExtentOnly(Boolean.parseBoolean(params.get("returnExtentOnly").toString()));
        featureService.setOrderByFields(params.get("orderByFields").toString());
        featureService.setGroupByFieldsForStatistics(params.get("groupByFieldsForStatistics").toString());
        featureService.setOutStatistics(params.get("outStatistics").toString());
        featureService.setReturnZ(Boolean.parseBoolean(params.get("returnZ").toString()));
        featureService.setReturnM(Boolean.parseBoolean(params.get("returnM").toString()));
        featureService.setMultipatchOption(params.get("multipatchOption").toString());
        featureService.setResultOffset(params.get("resultOffset").toString());
        featureService.setResultRecordCount(Integer.parseInt(params.get("resultRecordCount").toString()));
        featureService.setF(params.get("f").toString());

        Boolean returnExtentOnly = Boolean.parseBoolean(params.get("returnExtentOnly").toString());

        if (returnExtentOnly) {
            featureService.getExtent(false);
        } else {
            featureService.getFeatures(false, false);
        }

    }

    private static Map createParameterMap(String queryParams) {
        String[] params = queryParams.split("&");
        Map<String, String> map = new java.util.HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equalsIgnoreCase("where")) {
                    try {
                        value = URLDecoder.decode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                map.put(keyValue[0], value);
            } else {
                map.put(keyValue[0], "");
            }
        }
        return map;
    }


}
