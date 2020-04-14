# run script in ./scripts/dataset-1m
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester2 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 1 10 ./extent-9950-10000-features-14000.txt 0 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-9950-10000-features/export-map-1-concurrent-10-total-square.txt square

sleep 5
