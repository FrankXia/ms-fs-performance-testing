## 01-21-2022, 01-24-2022, with new memory settings (double the settings for Elasticsearch)

## OpenSearch 1.1.0 (write). BATs with different loading sizes without any analytics

| Dataset Size | source csv files | time (min) | coordinating_rejections | Comments |
| ------------ | -----------------| -----------| -------- | -------------- |
|  3,000,000   |  100k (5.5 MB)   |   2.4      |      1   | tested on 1/21 |
|  3,004,581   |  1m (109.5 MB)   |   2.2      |      0   | tested on 1/21 |
|  5,007,635   |  1m (109.5 MB)   |   2.6      |      0   | tested on 1/21 |
| 10,015,270   |  1m (109.5 MB)   |   4.8      |      0   | tested on 1/21 |
| ------ ----- | -----------------| -----------| -------- | ---------------------- |
|  5,008,080   |  5m (566.4 MB)   |   3.3      |      0   | tested on 1/24, failed |
| 15,022,905   |  1m (109.5 MB)   |   6.45     |      3   | tested on 1/24 (*3)    |  
| 20,030,540   |  1m (109.5 MB)   |   8.60     |      3   | tested on 1/24 (*3)    |  

## OpenSearch 1.1.0 (write). BATs with different loading sizes with 100 meter buffer

| Dataset Size | source csv files | time (min) | coordinating_rejections | Comments |
| ------------ | -----------------| -----------| -------- | ----------------------- |
|  3,004,581   |  1m (109.5 MB)   |   21.20    |      2   | tested on 1/21          |
|  5,283,322   |  1m (109.5 MB)   |   34.20    |      2   | tested on 1/21, 275,687 records added (*1) |
| 10,290,957   |  1m (109.5 MB)   |   64.75    |      2   | tested on 1/21,  14,980 records added (*2) |
| -----------  | -----------------| -----------| -------- | ----------------------- |
| 15,022,905   |  1m (109.5 MB)   |   90.00    |      0   |  tested on 1/24 (*3)    |
| 20,188,033   |  1m (109.5 MB)   |  120.00    |      4   |  tested on 1/24 (*3, *4), 157,653 records added |

*1 maybe caused by the 2 coordinating_rejections errors though in some other cases a few coordinating_rejections error didn't cause any drop or increase of records. 
there are 44 error messages of "Invalid Token". Tested on 1/21/2022

*2 maybe caused by the 2 coordinating_rejections errors though in some other cases a few coordinating_rejections error didn't cause any drop or increase of records. tested on 1/21/2022

*3 note that the 15/20 million testing cases were performed in the morning time while all the testing cases in 1/21 were performed in the late afternoon. It seems 
the morning cases are more stable with less rejection errors even though it loaded and buffered more features than those 
testing cases on 1/21. 

*4 one of the datastore master nodes lost after (or during the last step of) the job is done. The finishing time is estimated since the BAT log wasn't available
anymore after the master node lost connection to the data store cluster (the master pod is still visible from K8s).

## 01-26:01-28-2022, with new memory settings (double the settings for Elasticsearch)

## OpenSearch 1.2.4 (write). BATs with different loading sizes without any analytics

| Dataset Size | source csv files | time (min) | coordinating_rejections | Comments |
| ------------ | -----------------| -----------| -------- | -------------- |
| 10,015,270   |  1m (109.5 MB)   |   5.24     |      3   | tested on 1/26, no error from Spark Driver logs |
| 15,022,905   |  1m (109.5 MB)   |   6.66     |      2   | tested on 1/26, no error from Spark Driver logs |  
| 20,030,540   |  1m (109.5 MB)   |   9.30     |      8   | tested on 1/27, spark driver logs has 200MB errors  |  
| 20,030,540   |  1m (109.5 MB)   |   9.40     |      5   | tested on 1/27, no error from spark driver   |  
| 25,038,175   |  1m (109.5 MB)   |  10.58     |      6   | tested on 1/28, no error from spark driver   |  

## OpenSearch 1.2.4 (write). BATs with different loading sizes with 100 meter buffer

| Dataset Size | source csv files | time (min) | coordinating_rejections | Comments |
| ------------ | -----------------| -----------| -------- | ----------------------- |
| 10,015,270   |  1m (109.5 MB)   |   57.24    |      0   | tested on 1/26, no error log from Spark Driver, no record missing or added |
| 15,141,059   |  1m (109.5 MB)   |   89.18    |      1   | tested on 1/26, no error log from Spark Driver, 118,154 records added |
| 20,148,694   |  1m (109.5 MB)   |  123.85    |      1   | tested on 1/27, no error log from Spark Driver, 118,154 records added |
| 25,038,175   |  1m (109.5 MB)   |  153.54    |      1   | tested on 1/28, no error log from Spark Driver, no record missing or added |

Note: 

It is interesting to note that the testing of 25 million BATs, the one without any analytic tool encountered 6 (master nodes) "coordinating_rejections" errors 
while the BAT with 100 meter buffering operation only had 1 error and more amazingly it didn't add or lose any record while yesterdays' 15/20 million cases 
experienced the same amount of increased records (mostly due to rejection error and BAT/Spark re-run the partitioned small tasks).

## 02-07-2022, with multiple new memory settings for master nodes

All testing cases load data from 1-million CSV files (109.5 MB/each) from an Amazon S3 bucket. 
Tested on a data store cluster with OpenSearch 1.2.4. 

| Dataset Size | JVM Memory | Container Resource Limit  | Relative to defaults | time (min) | records loaded | Comments |
| ------------ | ---------- | ------------------------- | ---------------------| -----------| -------------- | -------- |
| 10,015,270   |   1024MB   |  2.00 Gi                  |        1.00          |     N/A    |    5,687,742   | fail to finish, missed 4,327,528 | 
| 10,015,270   |   1024MB   |  2.00 Gi                  |        1.00          |     N/A    |    9,599,185   | fail to finish, missed 416,085 | 
| 10,015,270   |   1280MB   |  2.25 Gi                  |        1.25          |     4.48   |   10,449,496   | finished but added 434,226  |
| 10,015,270   |   1280MB   |  2.25 Gi                  |        1.25          |     4.30   |   10,231,934   | finished but added 216,664  |
| 10,015,270   |   1536MB   |  3.00 Gi                  |        1.50          |     4.58   |   10,015,270   | finished with correct number of records  |
| 10,015,270   |   1536MB   |  3.00 Gi                  |        1.50          |     4.24   |   10,015,270   | finished with correct number of records  |
| 10,015,270   |   1536MB   |  3.00 Gi                  |        1.50          |     4.19   |   10,015,270   | finished with correct number of records  |
| 10,015,270   |   2048MB   |  4.00 Gi                  |        2.00          |     4.34   |   10,015,270   | finished with correct number of records  |
| -----------  | ---------- |
| 15,022,905   |   1536MB   |  3.00 Gi                  |        1.50          |     6.29   |   15,022,905   | finished with correct number of records |
| 15,022,905   |   1536MB   |  3.00 Gi                  |        1.50          |     6.23   |   15,022,905   | finished with correct number of records |
| 15,022,905   |   1536MB   |  3.00 Gi                  |        1.50          |     6.10   |   15,022,905   | finished with correct number of records |
| -----------  | ---------- |
| 20,030,540   |   1536MB   |  3.00 Gi                  |        1.50          |     8.00   |   20,030,540   | finished with correct number of records |
| 20,030,540   |   1536MB   |  3.00 Gi                  |        1.50          |     8.40   |   20,030,540   | finished with correct number of records, 13 coordinating_rejections |

Note: for the current datastore configuration with 3 master nodes, the case with 1.5 times more memory than defaults, it 
will need to add total 3Gi to the requested resources for its container.

## 02-08/02-09-2022, with new memory settings for master nodes

All testing cases load data from 1-million CSV files (109.5 MB/each) from an Amazon S3 bucket.
Tested on a data store cluster with OpenSearch 1.2.4.

| Dataset Size | records loaded | time (min) | Container Resource Limit  | JVM Memory | Relative to defaults  |  Comments |
| ------------ | -------------- | ---------- | --------------------------| -----------| --------------------- | --------- |
| - the same BAT ran twice ---  | ---------- |
| 10,015,270   |  10,015,270    |   5.40     |         2.00 Gi           |  1536 MB   |        1.50           | finished but with over 200MB error logs in Spark Driver |
| 10,015,270   |  10,015,270    |   5.00     |         2.00 Gi           |  1536 MB   |        1.50           | finished but with over 200MB error logs in Spark Driver |
| - the same BAT ran twice --   | ------     |
| 10,015,270   |  10,015,270    |   4.70     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records | 
| 10,015,270   |  10,015,270    |   4.00     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |
| - the same BAT ran twice --   |  ------    |
| 10,015,270   |  10,015,270    |   4.50     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records | 
| 10,015,270   |  10,015,270    |   4.70     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |
| - the same BAT ran twice --   | ------     |
| 15,022,905   |  15,022,905    |   6.50     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records | 
| 15,022,905   |  15,022,905    |   6.30     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |
| -- the same BAT ran 3 times   | ------     |
| 20,030,540   |  28,628,490    |   8.00     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records, but Spark Driver pod restarted after running 3 min or so, so extra 8,597,950 records loaded |
| 20,030,540   |  20,030,540    |   8.20     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |
| 20,030,540   |  20,030,540    |   8.90     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |
| -- the same BAT ran twice --  | ------     |
| 25,038,175   |  25,331,042    |   9.70     |         2.00 Gi           |  1536 MB   |        1.50           | finished but added extra 292,867 records, and 6 coordinating_rejections |
| 25,038,175   |  25,038,175    |   9.70     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records and 6 coordinating_rejections |

## 02-09-2022, with new memory settings for master nodes, add 100m buffer and output polygons

| Dataset Size | records loaded | time (min) | Container Resource Limit  | JVM Memory | Relative to defaults  |  Comments |
| ------------ | -------------- | ---------- | --------------------------| -----------| --------------------- | --------- |
| 10,015,270   |  10,290,956    |  56.37     |         2.00 Gi           |  1536 MB   |        1.50           | finished but added extra 275,686 records/polygons and 6 coordinating_rejections | 
| 15,022,905   |  15,771,367    |  82.49     |         2.00 Gi           |  1536 MB   |        1.50           | finished but added extra 748,642 records/polygons and 11 coordinating_rejections | 



#### Set OPENSEARCH_JAVA_OPTS to 1.75Gi failed

Set OPENSEARCH_JAVA_OPTS to 1.75Gi (with resource limit of 2Gi) failed to restart the 3 master node' pods 
The 3 master nodes' status change over 8 minutes.
(base) FXia4:~ frank$ kubectl get pods 
NAME                                             READY   STATUS             RESTARTS   AGE
datastore-elasticsearch-master-0                 1/1     Running            2          157m
datastore-elasticsearch-master-1                 1/1     Running            2          157m
datastore-elasticsearch-master-2                 0/1     Terminating        2          157m
---- 
datastore-elasticsearch-master-0                 1/1     Running            2          157m
datastore-elasticsearch-master-1                 1/1     Running            2          157m
datastore-elasticsearch-master-2                 0/1     Init:1/3           0          35s
---- 
datastore-elasticsearch-master-0                 0/1     OOMKilled          2          159m
datastore-elasticsearch-master-1                 0/1     OOMKilled          2          95s
datastore-elasticsearch-master-2                 0/1     CrashLoopBackOff   3          2m24s
---- 
datastore-elasticsearch-master-0                 1/1     Running            3          160m
datastore-elasticsearch-master-1                 1/1     Running            3          110s
datastore-elasticsearch-master-2                 0/1     CrashLoopBackOff   3          2m39s
----
datastore-elasticsearch-master-0                 1/1     Running            3          160m
datastore-elasticsearch-master-1                 0/1     CrashLoopBackOff   3          2m14s
datastore-elasticsearch-master-2                 1/1     Running            4          3m3s
---- 
datastore-elasticsearch-master-0                 1/1     Running            3          160m
datastore-elasticsearch-master-1                 0/1     CrashLoopBackOff   3          2m36s
datastore-elasticsearch-master-2                 0/1     CrashLoopBackOff   4          3m25s
---- 
datastore-elasticsearch-master-0                 1/1     Running            3          162m
datastore-elasticsearch-master-1                 0/1     OOMKilled          5          4m30s
datastore-elasticsearch-master-2                 0/1     CrashLoopBackOff   5          5m19s
----
datastore-elasticsearch-master-0                 1/1     Running            3          164m
datastore-elasticsearch-master-1                 0/1     CrashLoopBackOff   5          6m12s
datastore-elasticsearch-master-2                 0/1     CrashLoopBackOff   5          7m1s
----
atastore-elasticsearch-master-0                  1/1     Running            3          165m
datastore-elasticsearch-master-1                 0/1     CrashLoopBackOff   5          6m57s
datastore-elasticsearch-master-2                 0/1     OOMKilled          6          7m46s



## 02-10-2022, with new memory settings for master nodes and ArcGIS Velocity version of 3.1.0.1575

All testing cases load data from 1-million CSV files (109.5 MB/each) from an Amazon S3 bucket.
Tested on a data store cluster with OpenSearch 1.2.4.

| Dataset Size | records loaded | time (min) | Container Resource Limit  | JVM Memory | Relative to defaults  |  Comments |
| ------------ | -------------- | ---------- | --------------------------| -----------| --------------------- | --------- |
| - the same BAT ran twice ---  | ---------- |
| 10,015,270   |  10,015,270    |   5.70     |         2.00 Gi           |  1536 MB   |        1.50           | finished with "coordinating_rejections": 5 |
| 10,015,270   |  10,015,270    |   4.60     |         2.00 Gi           |  1536 MB   |        1.50           | finished with "coordinating_rejections": 2 |
| - the same BAT ran twice --   | ------     |
| 15,022,905   |  15,022,905    |   7.43     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records | 
| 15,022,905   |  15,022,905    |   7.40     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records but with over 200MB log from Spark Driver pod |
| -- the same BAT ran 2 times   | ------     |
| 20,030,540   |  20,106,744    |   8.90     |         2.00 Gi           |  1536 MB   |        1.50           | finished but so extra 76,204 records loaded |
| 20,030,540   |  20,030,540    |   8.59     |         2.00 Gi           |  1536 MB   |        1.50           | finished with correct number of records |

## 02-11-2022, with new memory settings for master nodes, add 100m buffer and output polygons

| Dataset Size | records loaded | time (min) | Container Resource Limit  | JVM Memory | Relative to defaults  |  Comments |
| ------------ | -------------- | ---------- | --------------------------| -----------| --------------------- | --------- |
| 10,015,270   |  10,487,902    |  62.53     |         2.00 Gi           |  1536 MB   |        1.50           | finished but added extra 472,632 records/polygons and 5 coordinating_rejections | 
| 15,022,905   |  24,133,128    |    N/A     |         2.00 Gi           |  1536 MB   |        1.50           | with multiple restarts with errors and added much more records | 
| 15,022,905   |  15,731,950    |  96.17     |         2.00 Gi           |  1536 MB   |        1.50           | finished without errors and added 709,045 records, and has 4 coordinating_rejections | 



