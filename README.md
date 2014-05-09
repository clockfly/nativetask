#Introduction
NativeTask is a native engine inside Hadoop MapReduce(MR) Task written in 
C++ and focuses on task performance optimization, 
while leaving the scheduling and communication job to 
the MR framework.

NativeTask could be used in two modes:     
1. native MapOutputCollector mode      
2. full native mode      

For the first mode, there is little user work needed other than turning on a option and users could run their Java MapReduce job transparently. For the second mode, users will need to write MapReduce jobs in C/C++.

**NativeTask feature list**:      
1. transparently support existing MRv1 and MRv2 apps   
2. support most common key types and all values    
3. support Java combiner    
4. support Lz4 / Snappy / Gzip      
5. support CRC32 and CRC32C (hardware checksum)   
6. support Hive / Mahout / Pig     
7. support MR ove HBase    
8. support non-sort Map    
9. support hash join     


----

##Motivation 
We found MapReduce slow for the following reasons:
* IO bound with Compression/Decompression overhead  
* Inefficient Scheduling/Shuffle/Merge      
* Inefficient memory management    
* Suboptimal sorting    
* Inefficient Serialization & Deserialization    
* Inflexible programming paradigm   
* Java limitations

NativeTask solves the above issues and is faster because:
* Use optimized Compression/Decompression codec
* High efficient memory management
* Highly optimized sorting
* Use hardware optimization when neccessary
* Avoid Java runtime side-effects

----

##Performance overview

Here is the diagram of NativeTask Performance improvement (native MapOutputCollector mode) against Hadoop original.

![native MapOutputCollector mode](https://lh6.googleusercontent.com/-Cj1ojoRjKxk/U2w2LFGLz3I/AAAAAAAAC14/XnstsiUhPKA/w959-h558-no/hibench.PNG)

NativeTask is 2x faster further in full native mode.    

![full native mode](https://lh6.googleusercontent.com/-Ssxz-tjd7F0/U2w2LDPC_oI/AAAAAAAAC18/JmYow_CKFYs/w822-h400-no/fullnative.PNG)

----

##How to use

### Native MapOutputCollector mode
In MRv1, please set `mapreduce.map.output.collector.delegator.class=org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator` in JobConf. For example, to run Pi with native MapOutputCollector

```bash
hadoop jar hadoop-examples.jar pi -D mapreduce.map.output.collector.delegator.class=org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator 10 10
```

MRv2 supports pluggable MapOutputCollector. Set `mapreduce.job.map.output.collector.class=org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator` in JobConf. Now the Pi example could be run with native MapOutputCollector as

```bash
hadoop jar hadoop-mapreduce-examples.jar pi -D mapreduce.job.map.output.collector.class=org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator 10 10
```

In both MRv1 and MRv2, please check the task log, if there is 
```bash
INFO org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator: Native output collector can be successfully enabled! 
```
Then NativeTask is successfully enabled.

### Full native mode

----

##Related work
[MAPREDUCE-2841](https://issues.apache.org/jira/browse/MAPREDUCE-2841) discusses about some initial experiment in "task level native optimization" while our implementation comes with far more advanced features (e.g. more key types support, Java combiner support) and has been used and verified in production environment.   

----

