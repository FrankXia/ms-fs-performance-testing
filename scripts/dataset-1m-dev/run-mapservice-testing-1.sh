#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 15 150 15 10 pointyHexagon ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-map-service-testing/export-map-15-concurrent-150-total.txt
#sleep 180
#
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 30 3000 30 20 pointyHexagon ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-map-service-testing/export-map-30-concurrent-300-total_30-20.txt
#sleep 180

#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 30 10000 30 20 pointyHexagon ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-map-service-testing/export-map-30-concurrent-10000-total_30-20.txt
#sleep 180

java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 31 10000 30 20 pointyHexagon ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-map-service-testing/export-map-31-concurrent-10000-total_30-20.txt
sleep 180