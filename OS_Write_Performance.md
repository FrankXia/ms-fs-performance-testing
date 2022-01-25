#### 01-21-2022, 01-24-2022

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