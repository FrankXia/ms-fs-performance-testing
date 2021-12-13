java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 5 25 ./random_extents 10 geohash ./test_outputs/aggregation/10m_geohash_5thread_25req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 5 25 ./random_extents 15 square ./test_outputs/aggregation/10m_square_5thread_25req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 5 25 ./random_extents 20 pointyTriangle ./test_outputs/aggregation/10m_triangle_5thread_25req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 5 25 ./random_extents 25 pointyHexagon ./test_outputs/aggregation/10m_hexagon_5thread_25req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 10 50 ./random_extents 30 geohash ./test_outputs/aggregation/10m_geohash_10thread_50req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 10 50 ./random_extents 40 square ./test_outputs/aggregation/10m_square_10thread_50req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 10 50 ./random_extents 50 pointyTriangle ./test_outputs/aggregation/10m_triangle_10thread_50req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 10 50 ./random_extents 60 pointyHexagon ./test_outputs/aggregation/10m_hexagon_10thread_50req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 20 100 ./random_extents 70 geohash ./test_outputs/aggregation/10m_geohash_20thread_100req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 20 100 ./random_extents 90 square ./test_outputs/aggregation/10m_square_20thread_100req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 20 100 ./random_extents 110 pointyTriangle ./test_outputs/aggregation/10m_triangle_20thread_100req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 20 100 ./random_extents 130 pointyHexagon ./test_outputs/aggregation/10m_hexagon_20thread_100req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 30 150 ./random_extents 150 geohash ./test_outputs/aggregation/10m_geohash_30thread_150req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 30 150 ./random_extents 180 square ./test_outputs/aggregation/10m_square_30thread_150req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 30 150 ./random_extents 210 pointyTriangle ./test_outputs/aggregation/10m_triangle_30thread_150req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 30 150 ./random_extents 240 pointyHexagon ./test_outputs/aggregation/10m_hexagon_30thread_150req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 40 200 ./random_extents 270 geohash ./test_outputs/aggregation/10m_geohash_40thread_200req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 40 200 ./random_extents 310 square ./test_outputs/aggregation/10m_square_40thread_200req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 40 200 ./random_extents 350 pointyTriangle ./test_outputs/aggregation/10m_triangle_40thread_200req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 40 200 ./random_extents 390 pointyHexagon ./test_outputs/aggregation/10m_hexagon_40thread_200req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 50 250 ./random_extents 430 geohash ./test_outputs/aggregation/10m_geohash_50thread_250req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 50 250 ./random_extents 480 square ./test_outputs/aggregation/10m_square_50thread_250req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 50 250 ./random_extents 530 pointyTriangle ./test_outputs/aggregation/10m_triangle_50thread_250req.txt
sleep 2
java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar  com.esri.arcgis.performance.test.mat.FeatureServiceWithRandomBBoxConcurrentAggTester https://us-iotdev.arcgis.com/opensearch03/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services planes_10m_bat_fl 10m 50 250 ./random_extents 580 pointyHexagon ./test_outputs/aggregation/10m_hexagon_50thread_250req.txt
