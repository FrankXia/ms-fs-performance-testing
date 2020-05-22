## 2 pods to 1
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 70 10000 ./extent-9950-10000-features-14000.txt 0 ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-testing-9950-10000-features-2-pods-a/random_extent_70-concurrent-10000-total.txt
sleep 180


#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 1 10 ./extent-9950-10000-features-14000.txt 0 ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-testing-9950-10000-features-2-pods-a/random_extent_1-concurrent-10-total-a.txt
#sleep 180
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 20 1000 ./extent-9950-10000-features-14000.txt 0 ../../results-data/dataset-1m-dev/sendFromMac/mat-performance-testing-9950-10000-features-2-pods-a/random_extent_20-concurrent-1000-total.txt
#sleep 180
