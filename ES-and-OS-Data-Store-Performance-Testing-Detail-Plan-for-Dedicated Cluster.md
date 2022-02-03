# Data Store Performance Testing Detail Plan

Frank Xia

The testing plan divides into three parts:

1. Loading simulated plane data from S3 buckets ([S3](https://s3.console.aws.amazon.com/s3/home?region=us-east-1)/[esriplanes](https://s3.console.aws.amazon.com/s3/buckets/esriplanes)/[lat88/](https://s3.console.aws.amazon.com/s3/buckets/esriplanes?prefix=lat88/))
2. Query Performance testing via MAT Feature Service REST API for each index
3. Aggregation Performance testing for each index:
   1. via Feature service REST API
   2. via Map Service REST API

## 1 -- Loading 3 different size of datasets/indices

The cluster opensearch03&#39;s Velocity build # is 2.4.0.1486, ES version is 7.9.3, OS version is 1.1.0 and we create 3 different sizes of datasets/indexes (100k/1million/10millions)

### Loading data with RATs

#### RATs (points): All RATs running times are estimated (since RATs metrics don&#39;t support elapsed time consuming a feed) by subtracting 1 min of Feed starting time and have aggregation styles of &quot;geohash&quot;, &quot;square&quot; and &quot;pointy hexagon/triangle&quot; (the default setting). All output layers have configured to set &quot;Enable editor tracking&quot; to false, have 6 months data retention and &quot;Do not export data&quot;.

| ES Index records(feed speed) | ES loading time (s) | OS loading time (s) | OS Index records (feed speed) |
| --- | --- | --- | --- |
| 99,999 (1000/s) | 105 (1.75 min) (*1) | 105 (1.75 min) (*1) | 99,999 (1000/s) |
| 999,999 (1000/s) | 1005 (16.75 min) (*1) | 1000 (16.67 min) (*1) | 1,000,000 (1000/s) |
| 1,000,000 (2000/s) | 503 (8.38 min) | 502 (8.37 min) | 1,000,000 (2000/s) |
| 1,000,000 (4000/s) | 252 (4.20 min) | 250 (4.17 min) | 1,000,000 (4000/s) |
| 980,485 (8000/s) | 132 (2.20 min) | 130 (2.17 min) | 986,728 (8000/s) |
| 975,647 (8000/s) | 127 (2.12 min) | 129 (2.15 min) | 984,810 (8000/s) |
| 918,384 (16000/s) | 70 (1.12 min) | 67 (1.12 min) | 936,205 (16000/s) |
| 10,051,509 (8,000/s) | 1265 (21.08 min) (*2) | 1285 (21.25 min) | 10,051,207(8,000/s) |
| 10,137,444 (16,000/s) | 676 (11.27 min) | 675 (11.25 min) | 10,054,358(16,000/s) |
| 10,105,947 (20,000/s) | 650 (10.83 min) | 540 ( 9.00 min) | 10,105,569(20,000/s) |
| 10,132,866 (24,000/s) | 705 (11.75 min) | 600 (10.00 min) | 10,117,000(24,000/s) |

Note (*): All RATs started before feeds start.
1. both 100k and 1 million cases used a single CSV file and run once, so we can see events got dropped in higher velocity.
2. the feed uses 1 million CSV file and run continuously.

#### RATs (points with 100m buffer -> polygon): All RATs running times are estimated

| ES Index records(feed speed) | ES loading time (s) | OS loading time (s) | OS Index records (feed speed) |
| --- | --- | --- | --- |
| 100,000 (1,000/s) | 105 (1.75 min) | 100 (1.67 min) | 99,820 (1,000/s) |
| 999,999 (1,000/s) | 1005 (16.75 min) | 1000 (16.67 min) | 999,999 (1,000/s) |
| 997,947 (2,000/s) | 510 (8.50 min) | 500 ( 8.33 min) | 995,775 (2,000/s) |
| 755,252 (4,000/s) | 475 (7.92 min) (*1) | 330 ( 5.50 min) | 875,954 (4,000/s) |
| 985,155 (4,000/s) | 310 (5.17 min) | 280 ( 4.67 min) | 989,559 (4,000/s) |
| 782,614 (8,000/s) | 320 (5.33 min) (*2) | 330 ( 5.50 min) | 843,449 (8,000/s) |
| 872,905 (8,000/s) | 315 (5.25 min) | 280 ( 4.67 min) | 956,571 (8,000/s) |
| 10,000,000 (4,000/s) | 2940 (49 min) | 2790 (46.50 min) | 10,032,247(4,000/s) |
| 10,061,212 (8,000/s) | 2880 (48 min) | 2795 (46.58 min) | 10,070,213(8,000/s) |

Note (*):
1. the 4k feed finished earlier than the ES output (didn't get the stopping time). This is the first try with 4k, next one got more events than the first one but less than 1 million from the source (CSV file).
2. the 8k feed finished within 3 min and the ES datastore received data until 5.33 min. The second try, the feed still finished within 3 min but the index got more events/data on the second try, still less than expected 1 million.

### Loading data with BATs

#### BATs (points):

| ES Index records | ES loading time (s) | OS loading time (s) | OS Index records |
| --- | --- | --- | --- |
|    100,000 |  55.2 (0.92 min) (*1) | 60.6 (1.01 min)           |    100,000 |
|  1,000,000 |  79.8 (1.33 min) (*2) | 88.8 (1.48 min)           |  1,000,000 |
| 10,015,270 | 284.4 (4.74 min) (*3) | 268.2 (4.47 min) (*3)(@1) |  4,129,034 |
|            |                       | 331.2 (5.52 min) (*5)(@2) | 10,000,000 |
|            |                       | 288.0 (4.8  min) (*3)(@4) | 10,015,270 |


#### BATs (points with 100m buffer -> polygon):

| ES Index records | ES loading time (s) | OS loading time (s) | OS Index records |
| --- | --- | --- | --- |
|    100,000 | 112.8 (1.88 min) (*1) | 144.6 (2.41 min) (*1) |   100,000 |
|            |                       | 125.4 (2.09 min) (*1) |   100,000 |
|  1,000,000 |   495 (8.25 min) (*2) | 501.6 (8.36 min) (*2) | 1,000,000 |
|            |                       | 542.4 (9.04 min) (*2)(@3) | 996,971 |
| 10,015,270 | 3701.4 (61.69 min) (*3) | N/A, failed to finish with "Medium plan" (*3) |   N/A |
| 10,015,270 | 3587.4 (59.79 min) (*4) | N/A, failed to finish with "Large plan" (*3)  |   N/A |
|            |                         | 3885 (64.75 min) (*3)(@5) with "Medium Plan"  | 10,290,957 |

Note (*) - CSV file sizes:
1. Loading from 2 CSV files (50k each) from a S3 bucket
2. Loading from 10 CSV files (100k each) from a S3 bucket
3. Loading from 10 CSV files (1 million each) from a S3 bucket
4. Loading from a 10m feature layer
5. Loading from 100 CSV files (100 k each) from a S3 bucket

Note (@): 
1. Failed multiple times due to OOM with the Medium Plan (loaded: 4,129,034). The S3 bucket folder contains 10 1-million CSV file.
2. Succeeded when loaded with 100 100k-CSV files , Loaded with &quot;Medium Plan&quot;
3. Failed once with error message that is similar to the 10m BAT case after buffered 996,971 points. 
4. After doubled the memory for 3 master nodes from 1G to 2G, the BAT ran successfully!
5. After doubled the memory for 3 master nodes from 1G to 2G, the BAT ran successfully though it added additional 14,980 records/features!

## 2 – Query Performance testing via MAT Feature Service REST API

First, generating 3,100 random extents for each individual indexes and each feature limit group (from 1000 – 1500, and/or 5000 - 5500, and/or 9500 – 10,000, total 9,000 extents). This set of extents will be used across 3 testing scenarios: 1) ES cluster, 2) OS cluster with same PVC from ES, and 3) OS cluster with PVC created via OS cluster itself. Secondly, for each index, we do performance testing in 3 testing cases: 1) single thread with 20 consecutive requests for getting individual features with each limit group above (get average time for querying features of 1000/5000/10000), 2) multi-thread querying testing with number of threads of 5/10/20/30/50 to get average time for each feature limit group. ( [(5+10+20+30+40+50) \* 20 = 3100]).

### Single Thread Testing Cases

#### Single Thread (1), Index size: 100,000, averaging time for 20 consecutive requests (the max values are showed here)

| Data Store (write/read) | Feature querying 1000 – 1500 (ms) | Feature querying 5000 - 5500 (ms) | Feature querying 9500 - 10000 (ms) |
| --- | --- | --- | --- |
| ES/ES | 995.4 | 1,881.5 | 2,856.6 |
| ES/OS | 783.2 | 1,504.2 | 2,336.6 |
| OS/OS | 651.3 | 1,425.1 | 1,771.0 |

#### Single Thread (2), Index size: 1 million, averaging time for 20 consecutive requests

| Data Store (write/read) | Feature querying 1000 – 1500 (ms) | Feature querying 5000 - 5500 (ms) | Feature querying 9500 - 10000 (ms) |
| --- | --- | --- | --- |
| ES/ES | 619.8 | 1656.5 | 2665.1 |
| ES/OS | 813.3 | 2335.8 | 2077.3 |
| OS/OS | 634.2 | 1,160.8 | 1,880.7 |

#### Single Thread (3), Index size: 10 milions, averaging time for 20 consecutive requests

| Data Store (write/read) | Feature querying 1000 – 1500 (ms) | Feature querying 5000 - 5500 (ms) | Feature querying 9500 - 10000 (ms) |
| --- | --- | --- | --- |
| ES/ES | 938.8 | 1423.7 | 2353.3 |
| ES/OS | 1126.5 | 1803.9 | 2050.6 |
| OS/OS | 673.8 | 1,353.0 | 2517.6 |

### Multi-Threads Testing Cases

### Returning 1000 – 1500 features

#### Multi-threads (1), Index size: 100,000, averaging time for (20 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1597.5 | 1617.3 | 1766.1 | 2900 | 2137.1 | 3413.7 |
| ES/OS | 1167.5 | 1239.3 | 1257.3 | 1292.7 | 2492.3 | 1780.6 |
| OS/OS | 1275.8 | 1203.3 | 1212 | 1259.6 | 1340.2 | 1628.7 |

#### Multi-threads (2), Index size: 1 million, averaging time for (20 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1348.4 | 1476.6 | 1546.4 | 1890.6 | 2139.3 | 2162.3 |
| ES/OS | 1648.7 | 1603.3 | 1624.8 | 2378.3 | 1818.5 | 1774.6 |
| OS/OS | 1493.3 | 1661.2 | 1599.5 | 1626.2 | 1688.7 | 2280.1 |

#### Multi-threads (3), Index size: 10 millions, averaging time for (20 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1420.4 | 1512.9 | 1716.6 | 2248.4 | 2155.2 | 2200.1 |
| ES/OS | 1421.9 | 1376.5 | 1412.7 | 1445.4 | 1506.1 | 1559 |
| OS/OS | 1418.6 | 1464.9 | 2051.2 | 1787.6 | 2290.7 | 2542.9 |

### Returning 5000 – 5500 features

#### Multi-threads (1), Index size: 100,000, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2843 | 2757.9 | 3058.9 | 4236.3 | 5637.3 | 7015.3 |
| ES/OS | 2263.1 | 3277.7 | 4706.9 | 4080.6 | 8254.3 | 9166.7 |
| OS/OS | 2399.8 | 2343.3 | 3544.7 | 4550.6 | 5289.6 | 6634.6 |

#### Multi-threads (2), Index size: 1 million, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2855.6 | 3719.9 | 3240.7 | 4323.7 | 5646.8 | 7024.8 |
| ES/OS | 3104.7 | 2399.1 | 2706.3 | 6012.1 | 6142 | 9529 |
| OS/OS | 2362.8 | 2339.4 | 2711.9 | 4009.8 | 5331.5 | 7072.9 |

#### Multi-threads (3), Index size: 10 millions, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2777 | 2856.5 | 3188.7 | 4221.5 | 5576.6 | 9450 |
| ES/OS | 2266.8 | 2335.2 | 4984.5 | 6894.5 | 5254.9 | 13125.2 |
| OS/OS | 2500.5 | 3134 | 2762.3 | 5189 | 6681.7 | 7628.2 |

### Returning 9500 – 10,000 features

#### Multi-threads (1), Index size: 100,000, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 3614.4 | 3463.6 | 5414.3 | 9186.8 | 11973.7 | 14506.5 |
| ES/OS | 3051.7 | 7837.7 | 6056.8 | 11121.3 | 12502.7 | 15881.3 |
| OS/OS | 3074.2 | 3386.9 | 5041.9 | 7504.5 | 10783.7 | 13850.9 |

#### Multi-threads (2), Index size: 1 million, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 3543.8 | 3511.2 | 12290.6 | 14810.2 | 12302.8 | 13469.6 |
| ES/OS | 4739.7 | 4090.8 | 10460.7 | 8269.7 | 9891.2 | 13052.2 |
| OS/OS | 3127.3 | 3209.8 | 5658.6 | 7454.2 | 9886.7 | 12366.4 |

#### Multi-threads (3), Index size: 10 millions, averaging time for (20 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 4429.1 | 4997.6 | 7453 | 9595.5 | 11937.6 | 22444.2 |
| ES/OS | 2947.5 | 4191.8 | 5918.9 | 8315.5 | 9731.6 | 12189.4 |
| OS/OS | 3096.9 | 3402.6 | 5443.2 | 7903.9 | 9822 | 13061.8 |

## 3 – Aggregation Performance via MAT Feature Service REST API

Aggregation Performance testing via Feature Service REST API for each index created above. Experimenting with each index to find out a good bounding box size (with/height) for aggregation testing so that the number of buckets for each request will not exceed the default limit of 10,000 items. Then generating 3,100 random extents for each individual indexes. Secondly, for each index, we do performance testing in 3 testing cases:

1. Single thread with 20 consecutive requests for each aggregation style (GeoHash/Square/flatTriangle/pointyTriangle/FlatHexagon/pointyHexagon)
2. Multi-thread aggregation testing with number of threads of 5/10/20/30/50 to get average time for each aggregation style (geohash/square/triangle/hexagon). For each thread testing case, we use (5 x threads) extents from 5 random extent groups, 5 extents from each group.

### Single Thread Testing Cases

#### Single-thread (1), Index size: 100,000, aggregation average time for 20 consecutive requests (use average over 2 or more tests)

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 715.2 | 654.8 | 1283.3 | 961.5 |
| ES/OS | 896.4 | 1107.7 | 1650.9 | 1764.1 |
| OS/OS | 881.7 | 1234 | 1456.4 | 1402.1 |

#### Single-thread (2), Index size: 1 million, aggregation average time for 20 consecutive requests

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 613.5 | 1193.3 | 1549.2 | 1530.6 |
| ES/OS | 849.6 | 1270 | 1685 | 1406.9 |
| OS/OS | 885.2 | 1086.2 | 1537.7 | 1351.4 |

#### Single-thread (3), Index size: 10 millions, aggregation average time for 20 consecutive requests

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 903.8 | 1231 | 1529.1 | 2063 |
| ES/OS | 893 | 1154.7 | 1593.8 | 1790.1 |
| OS/OS | 936.1 | 1092.1 | 1588.4 | 2008.9 |

### Multi-Threads Testing Cases

### Aggregation Style: Geohash

#### Multi-threads (1), Index size: 100,000, GeoHash, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2246.8 | 1237.4 | 1836.3 | 1988.3 | 2504.7 | 3129.5 |
| ES/OS | 2014.7 | 1350.9 | 1614.2 | 4866.1 | 2531.6 | 3732.7 |
| OS/OS | 1557.8 | 1302.3 | 3335.9 | 4805.7 | 5760.5 | 3200.7 |

#### Multi-threads (2), Index size: 1 million, GeoHash, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2866.8 | 1366.6 | 5448.3 | 2011.6 | 2695.5 | 3186.4 |
| ES/OS | 1724.8 | 1507.4 | 1573.3 | 2011.2 | 2532.3 | 3167.9 |
| OS/OS | 2218.6 | 2254.9 | 2420.8 | 2423.1 | 2259.8 | 5902.8 |

#### Multi-threads (3), Index size: 10 millions, GeoHash, averaging time for (5 x threads) requests

| Data Store | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2177.9 | 2324.3 | 2148.7 | 3333.2 | 3989 | 4820.4 |
| ES/OS | 2026.1 | 1718.6 | 2025.1 | 2933.4 | 3714 | 4539.1 |
| OS/OS | 2052.3 | 1684.9 | 4115.2 | 3143.8 | 5573.6 | 5105.8 |

### Aggregation Style: Square

#### Multi-threads (1), Index size: 100,000, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1634.5 | 1851.1 | 2467.8 | 3593.4 | 4718.6 | 14273.2 |
| ES/OS | 2412.5 | 2080.8 | 2522.8 | 3928.1 | 4733.4 | 5876.6 |
| OS/OS | 2017.7 | 3757.4 | 4531.2 | 4105.6 | 5378.5 | 5958.7 |

#### Multi-threads (2), Index size: 1 million, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2553.6 | 1901.1 | 2797.5 | 3855.2 | 4860.3 | 6002.1 |
| ES/OS | 2159.8 | 1825.6 | 2479.1 | 10199.8 | 4780.7 | 5933 |
| OS/OS | 3160.7 | 5531.4 | 2602.5 | 3672.1 | 4822.9 | 6078.7 |

#### Multi-threads (3), Index size: 10 millions, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2428 | 2535 | 4049.6 | 14422.6 | 5334.8 | 6256.6 |
| ES/OS | 2307.1 | 2622.2 | 2804.2 | 3862.9 | 5085 | 6249 |
| OS/OS | 2402.5 | 2414.9 | 3957 | 7557.9 | 6916.7 | 6216.4 |

### Aggregation Style: Triangle

#### Multi-threads (1), Index size: 100,000, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 4886.4 | 10454 | 3222.2 | 13872.7 | 6255.1 | 14926.6 |
| ES/OS | 2162.3 | 2333.2 | 7087.6 | 4766.2 | 6239.6 | 7788 |
| OS/OS | 1916.5 | 4767.4 | 6115.6 | 6367.2 | 9675.6 | 8993.8 |

#### Multi-threads (2), Index size: 1 million, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 6093.4 | 2168.4 | 6601.4 | 4740.2 | 6250.1 | 10846.7 |
| ES/OS | 2133.3 | 2224.2 | 3497.1 | 4743.7 | 6348 | 7911.4 |
| OS/OS | 4256.4 | 2652.1 | 5982.9 | 10985.7 | 6354.1 | 10267.6 |

#### Multi-threads (3), Index size: 10 millions, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 5258 | 3077.2 | 3402.4 | 11188 | 6507.2 | 12617.5 |
| ES/OS | 2532.9 | 2813.8 | 3353.6 | 4917 | 6520 | 8093.5 |
| OS/OS | 2298.6 | 2542.3 | 4256 | 5030.4 | 6446.9 | 13027.6 |

### Aggregation Style: Hexagon

#### Multi-threads (1), Index size: 100,000, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1789.1 | 8932.1 | 3628.1 | 7724.3 | 10014.3 | 9558 |
| ES/OS | 2511.1 | 2361.9 | 3660.1 | 5392.9 | 7131.3 | 8907 |
| OS/OS | 1866.8 | 2496.9 | 3734.7 | 5444.7 | 9444.5 | 11853.8 |

#### Multi-threads (2), Index size: 1 million, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 2241.2 | 2378.1 | 3755.1 | 8776.7 | 7191.8 | 9012.9 |
| ES/OS | 2483.9 | 2459.3 | 3655.9 | 5565.9 | 7119.4 | 8943.7 |
| OS/OS | 3336 | 3481.7 | 4873.1 | 5494.1 | 9356.5 | 13224.3 |

#### Multi-threads (3), Index size: 10 millions, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 3621.2 | 7640.1 | 6543.4 | 8674.6 | 14196.3 | 17833.2 |
| ES/OS | 3560.6 | 8851.5 | 6112.2 | 8699.3 | 11212.1 | 13926.7 |
| OS/OS | 2933.4 | 3761.7 | 7222.7 | 10952.6 | 14196.3 | 14382.1 |

## 4 – Aggregation Performance via MAT Map Service REST API

Aggregation Performance testing via Map Service REST API (ExportImage) for each index created above.

1. Single thread with 20 consecutive requests for each aggregation style (GeoHash/Square/flatTriangle/pointyTriangle/FlatHexagon/pointyHexagon)
2. Multi-thread aggregation testing with number of threads of 5/10/20/30/50 to get average time for each aggregation style (geohash/square/triangle/hexagon). For each thread testing case, we use (5 x threads) extents from 5 random extent groups, 5 extents from each group.

### Single Thread Testing Cases

#### Single-thread (1), Index size: 100,000, aggregation average time for 20 consecutive requests

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 811.8 | 639 | 645.3 | 579.5 |
| OS/ES | 588.2 | 501.2 | 584.1 | 502.8 |
| OS/OS | 572.3 | 497.5 | 503.6 | 447.2 |

#### Single-thread (2), Index size: 1 million, aggregation average time for 20 consecutive requests

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 701.3 | 665.8 | 667.8 | 682 |
| OS/ES | 596.9 | 579.6 | 590.4 | 584.3 |
| OS/OS | 539.2 | 556.2 | 590.1 | 558.7 |

#### Single-thread (3), Index size: 10 millions, aggregation average time for 20 consecutive requests

| Data Store (write/read) | GeoHash | Square | Pointy Triangle | Pointy Hexagon |
| --- | --- | --- | --- | --- |
| ES/ES | 1224.0 | 1,249.2 | 1,305.6 | 1,234.4 |
| ES/OS | 951.9 | 948.8 | 953.3 | 1009.9 |
| OS/OS | 892.0 | 900.3 | 799.7 | 940.3 |

### Multi-Threads Testing Cases

### Aggregation Style: Geohash

#### Multi-threads (1), Index size: 100,000, GeoHash, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1083.6 | 868.9 | 973.6 | 973.3 | 1084.7 | 1290.2 |
| ES/OS | 1387.8 | 858.6 | 956.7 | 973.7 | 1020.3 | 1230.5 |
| OS/OS | 1161.9 | 958 | 1212.3 | 1190.1 | 1273.9 | 1644 |

#### Multi-threads (2), Index size: 1 million, GeoHash, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1027.4 | 954.7 | 1038.6 | 1116.2 | 1340.9 | 1681.4 |
| ES/OS | 1140 | 987 | 1114.3 | 1143.8 | 1365 | 1597.9 |
| OS/OS | 1326.4 | 1190.6 | 1419.8 | 1648.8 | 1402.5 | 1721.4 |

#### Multi-threads (3), Index size: 10 millions, GeoHash, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1860.9 | 2480.9 | 4545.9 | 6612.1 | 8751.8 | 10822.6 |
| ES/OS | 2003.3 | 2517.7 | 4477.5 | 6497.4 | 8622.1 | 10713.6 |
| OS/OS | 1896.8 | 2549.1 | 4793.2 | 6961.4 | 9283.8 | 11517.6 |

### Aggregation Style: Square

#### Multi-threads (1), Index size: 100,000, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 844.3 | 876.2 | 1014.6 | 1057.7 | 1066 | 1331.6 |
| ES/OS | 829.3 | 867.9 | 978.2 | 987.1 | 1030.4 | 1201.2 |
| OS/OS | 934.3 | 993.6 | 1144.5 | 1069.6 | 1380.2 | 1584.2 |

#### Multi-threads (2), Index size: 1 million, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 938.9 | 958.5 | 1066.2 | 1164.3 | 1361.4 | 1663 |
| ES/OS | 983.2 | 977.4 | 1050.3 | 1130.4 | 1315.7 | 1601.5 |
| OS/OS | 1329.2 | 1178.2 | 1447.6 | 1336.9 | 1457.7 | 1687.5 |

#### Multi-threads (3), Index size: 10 millions, Square, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1797.2 | 2384.5 | 4479.8 | 6660.4 | 6662.6 | 10704.3 |
| ES/OS | 1865.6 | 2393.4 | 4397.4 | 6565.9 | 8519.3 | 10710.4 |
| OS/OS | 1865 | 2564.7 | 4815.1 | 7043.4 | 9190.4 | 11426.7 |

### Aggregation Style: Triangle

#### Multi-threads (1), Index size: 100,000, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 849.8 | 896.3 | 958.8 | 950.3 | 1068.9 | 1322.3 |
| ES/OS | 848.8 | 897.1 | 939.1 | 960.2 | 1051.8 | 1232.8 |
| OS/OS | 913 | 918.8 | 1101.1 | 1060.2 | 1496.5 | 1507 |

#### Multi-threads (2), Index size: 1 million, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 918 | 1016.2 | 1006.8 | 1093.8 | 1354.3 | 1633.7 |
| ES/OS | 945.7 | 987.2 | 1043.6 | 1117.3 | 1299.5 | 1595.2 |
| OS/OS | 1210.4 | 1169.1 | 1443.9 | 1305.5 | 1427.1 | 1699.6 |

#### Multi-threads (3), Index size: 10 millions, Pointy Triangle, averaging time for (5 x threads) requests

| Data Store (write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1660.2 | 2742.7 | 4512.8 | 6681.7 | 8770.2 | 14863.1 |
| ES/OS | 1824.4 | 2416.7 | 4510.8 | 6574.9 | 8688.2 | 10861.9 |
| OS/OS | 1698 | 2594 | 4763.4 | 6962.1 | 9258.4 | 11481.2 |

### Aggregation Style: Hexagon

#### Multi-threads (1), Index size: 100,000, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 858.1 | 868.1 | 953.3 | 980 | 1033.6 | 1331.4 |
| ES/OS | 817.6 | 847.5 | 983.7 | 1010 | 1019.2 | 1219.2 |
| OS/OS | 929.2 | 1014.7 | 1157.3 | 1098.3 | 1414.3 | 1416.7 |

#### Multi-threads (2), Index size: 1 million, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store Type(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 989.3 | 965.7 | 1028.1 | 1160.4 | 1347.9 | 1610 |
| ES/OS | 964.6 | 981.5 | 1044.1 | 1157.3 | 1314.4 | 1577.6 |
| OS/OS | 1174.9 | 1165.1 | 1445.1 | 1339.3 | 1405.1 | 1685.1 |

#### Multi-threads (3), Index size: 10 millions, Pointy Hexagon, averaging time for (5 x threads) requests

| Data Store(write/read) | 5 threads | 10 threads | 20 threads | 30 threads | 40 threads | 50 threads |
| --- | --- | --- | --- | --- | --- | --- |
| ES/ES | 1777.8 | 2477 | 4495.7 | 6586.6 | 8774.7 | 10819.8 |
| ES/OS | 1793.2 | 2482.7 | 4487.2 | 6589.1 | 8643.5 | 10792.9 |
| OS/OS | 1886.8 | 2548.3 | 4780.4 | 6978.6 | 9236.5 | 11508.1 |

###  How aggregation random extents are generated?

Since Elasticsearch has a default limit of 10,000 for returning buckets, we are trying to generate random extents for each dataset (100k/1m/10m) that the total number of buckets for each aggregation will be less than the default limit. For each dataset, we have 5 groups, each with different number of returning raw features. Each aggregation style will be performed at a predefined level of detail (LOD) so that the number of aggregation buckets will not exceed the default limit. The predefined LODs for each aggregation style are estimated and there are some cases where the number of returning buckets could exceed the default limit of 10,000.

630 random extents are generated for each group for each dataset.

1. 100k dataset: returning features
   1. 5 – 10k
   2. 10 – 20k,
   3. 20 – 30k,
   4. 30 – 40k,
   5. 40 – 50k,
2. 1m dataset: returning features
   1. 10 – 20k,
   2. 50 – 100k,
   3. 200 - 300k,
   4. 300 - 400k,
   5. 400k – 500k,
3. 10m dataset: returning features
   1. 50 – 100k,
   2. 500k – 1m,
   3. 1m – 2m,
   4. 2m – 3m,
   5. 3m – 4m,
