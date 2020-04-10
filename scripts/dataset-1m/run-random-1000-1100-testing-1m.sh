## 1st round
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 1 10 ./extent-1000-1100-features-25000.txt 0 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_1-concurrent-10-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 5 50 ./extent-1000-1100-features-25000.txt 10 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_5-concurrent-50-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 10 100 ./extent-1000-1100-features-25000.txt 60 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_10-concurrent-100-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 15 150 ./extent-1000-1100-features-25000.txt 160 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_15-concurrent-150-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 20 200 ./extent-1000-1100-features-25000.txt 310 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_20-concurrent-200-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 25 250 ./extent-1000-1100-features-25000.txt 510 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_25-concurrent-250-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 30 300 ./extent-1000-1100-features-25000.txt 760 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_30-concurrent-300-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 35 350 ./extent-1000-1100-features-25000.txt 1060 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_35-concurrent-350-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 40 400 ./extent-1000-1100-features-25000.txt 1410 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_40-concurrent-400-total.txt
sleep 5
java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 45 450 ./extent-1000-1100-features-25000.txt 1810 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_45-concurrent-450-total.txt
sleep 5


#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 50 500 ./extent-1000-1100-features-25000.txt 2260 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_50-concurrent-500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 55 550 ./extent-1000-1100-features-25000.txt 2760 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_55-concurrent-550-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 60 600 ./extent-1000-1100-features-25000.txt 3310 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_60-concurrent-600-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 65 650 ./extent-1000-1100-features-25000.txt 3910 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_65-concurrent-650-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 70 700 ./extent-1000-1100-features-25000.txt 4560 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_70-concurrent-700-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 75 750 ./extent-1000-1100-features-25000.txt 5260 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_75-concurrent-750-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 80 800 ./extent-1000-1100-features-25000.txt 6010 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_80-concurrent-800-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 85 850 ./extent-1000-1100-features-25000.txt 6810 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_85-concurrent-850-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 90 900 ./extent-1000-1100-features-25000.txt 7660 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_90-concurrent-900-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 95 950 ./extent-1000-1100-features-25000.txt 8560 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_95-concurrent-950-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 100 1000 ./extent-1000-1100-features-25000.txt 9510 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_100-concurrent-1000-total.txt



#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 150 1500 ./extent-1000-1100-features-25000.txt 10510 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_150-concurrent-1500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 200 2000 ./extent-1000-1100-features-25000.txt 12010 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_200-concurrent-2000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 250 2500 ./extent-1000-1100-features-25000.txt 14010 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_250-concurrent-2500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 300 3000 ./extent-1000-1100-features-25000.txt 16510 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_300-concurrent-3000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 350 3500 ./extent-1000-1100-features-25000.txt 19510 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_350-concurrent-3500-total.txt
#sleep 5

### second round
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 400 4000 ./extent-1000-1100-features-25000.txt 0 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_400-concurrent-4000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 450 4500 ./extent-1000-1100-features-25000.txt 4000 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_450-concurrent-4500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 500 5000 ./extent-1000-1100-features-25000.txt 8500 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_500-concurrent-5000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 550 5500 ./extent-1000-1100-features-25000.txt 13500 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_550-concurrent-5500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 600 6000 ./extent-1000-1100-features-25000.txt 19000 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_600-concurrent-6000-total.txt


#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 400 4000 ./extent-1000-1100-features-25000.txt 23000 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_400-concurrent-4000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 450 4500 ./extent-1000-1100-features-25000.txt 27000 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_450-concurrent-4500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 500 5000 ./extent-1000-1100-features-25000.txt 31500 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_500-concurrent-5000-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 550 5500 ./extent-1000-1100-features-25000.txt 36500 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_550-concurrent-5500-total.txt
#sleep 5
#java -cp  ../../target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/fx0323/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services RandomGlobalPoints_4326_1mil 600 6000 ./extent-1000-1100-features-25000.txt 42000 ../../results-data/dataset-1m/sendFromMac/mat-performance-testing-1000-1100-features/random_extent_600-concurrent-6000-total.txt
