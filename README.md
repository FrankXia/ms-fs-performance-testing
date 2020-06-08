# ms-fs-performance-testing
This is Map/Feature service performance testing repo.

Here is the instructions for running the testing locally with SSH tunnel to a DSE cluster in Amazon/Azure Clouds

1. Get your map/feature service url from the MAT Pod such as https://us-iotdev.arcgis.com/fx1010d/maps/arcgis/rest/services/space_stations/FeatureServer

2. Clone and build this testing project from https://github.com/FrankXia/ms-fs-performance-testing

### performance testing 

1. Run FeatureServiceTest from command window such as 

    `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester 3`
    
    where the number 3 is the testing case number (range from 0 to 8)
    
2. Run concurrent feature service testing from command window such as 

    `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester a1 faa30m 3 3 dest  "[ {\"statisticType\":\"avg\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"avg_speed\"}, {\"statisticType\":\"min\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"min_speed\"}, {\"statisticType\":\"max\",\"onStatisticField\":\"speed\",\"outStatisticFieldName\":\"max_speed\"} ]"`
    
3.  Run concurrent map service testing from command window such as
 
 `java -cp  ./target/ms-fs-performance-test-1.0.jar com.esri.arcgis.performance.test.FeatureServiceTester a1 faa300m 30 30 faa300m.txt 81`

### MAT autoscaler testing 

1. Get Cookie from Browser and save it into the directory of the project (`ms-fs-performance-testing`) as `mat-access-cookie.txt`

2. Run the following command to generate a set of bounding boxes with a given range of returning features, 

`java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.GenerateBoundingBox https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new testing-bb.txt 10 20 10 2000,2100`

where `https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services` is the REST Service URL, 
`1mil_RandomPoints_042120_new` the feature service name

 `testing-bounding-boxes.txt` the bounding box output file 

 `10` the number of bounding boxes to be generated 

 `20` the starting search box width in decimal degree

 `10` the starting search box height in decimal degree

 `2000,2100` the returning feature range
 
 The output bounding box file should look like the following sample, 
 
 `-147.15576625291237,56.608759999790806,-137.13119482434095,69.26114095217176|2098`
 
  `-0.8109543272265398,47.637300059237845,9.033331387059174,60.618252440190226|2081`
 
  `128.39693950222454,64.74677977740751,138.24735220063724,77.62773215835989|2091`
  
  where the second part is the number of features within the bounding box
  
3. Testing with the same bounding box with different number of concurrent threads (loads), sample command:
  
  `java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1a https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 70 50000 ./same_extent_70-concurrent-50000-total-2k.txt -147.15576625291237,56.608759999790806,-137.13119482434095,69.26114095217176`
  
  where 
  
  `https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services` the REST service ending point
  
  `1mil_RandomPoints_042120_new1` the feature service name
  
  `70` the number of concurrent threads
  
  `50000` the total number of testing requests
  
  `./same_extent_70-concurrent-50000-total-2k.txt` the output file  
  
  `-147.15576625291237,56.608759999790806,-137.13119482434095,69.26114095217176` the bounding box that returns 2098 (see above sample) faetures
  
4. Testing with random extents/bounding boxes with different number of concurrent threads (loads), sample command:

`java -cp  ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.FeatureServiceConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 10 100 ./testing-bb.txt 0 ./random-extent-10-concurrent-100-total.txt`

where 

`https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services`, the feature service ending point

`1mil_RandomPoints_042120_new` the feature service name

`10` the number of concurrent threads

`100` the total number of requests

`./testing-bb.txt` the bounding box file (see above)

`0` the number of records to be skipped 

`./random-extent-10-concurrent-100-total.txt` the output file
 
5. Testing with map service with different number of concurrent threads (loads), sample command: 

`java -cp ./target/ms-fs-performance-test-1.0-jar-with-dependencies.jar com.esri.arcgis.performance.test.mat.MapServiceAggConcurrentTester1 https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services 1mil_RandomPoints_042120_new 10 100 30 20 pointyHexagon`

where 

`https://us-iotdev.arcgis.com/dev0420a3/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services`, the REST service ending point

`1mil_RandomPoints_042120_new` the map service name

`10` the number of concurrent threads

`100` the total number of requests

`30` the export map extent's width

`20` the export map extent's height

`pointyHexagon` the aggregation style
