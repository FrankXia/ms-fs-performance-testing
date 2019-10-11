# ms-fs-performance-testing
This is Map/Feature service performance testing repo.

Here is the instructions for running the testing locally with SSH tunnel to a DSE cluster in Amazon/Azure Clouds

1. Get your map/feature service url from the MAT Pod such as https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/space_stations/FeatureServer

2. Clone and build this testing project from https://github.com/FrankXia/ms-fs-performance-testing

3. Run FeatureServiceTest from command window such as 

    `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester 3`
    
    where the number 3 is the testing case number (range from 0 to 8)
    
4. Run concurrent feature service testing from command window such as 

    `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester a1 faa30m 3 3 dest  "[ {\"statisticType\":\"avg\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"avg_speed\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"min_speed\"}, {\"statisticType\":\"max\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"max_speed\"} ]"`
    
5.  Run concurrent map service testing from command window such as
 
 `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester a1 faa300m 30 30 faa300m.txt 81`

