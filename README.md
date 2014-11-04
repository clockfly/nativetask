**Update**

The nativetask is merged to hadoop trunk(3.0). For now, "**The Transparent Collector Mode**" is included, the "**Native Runtime Mode**" is not included. 

#What is NativeTask?
NativeTask is a **performance oriented** native engine for Hadoop MapReduce.

NativeTask can be used transparently as a replacement of in-efficient Map Output Collector , or as a full native runtime which support native mapper and reducer written in C++. Please check [wiki](https://github.com/intel-hadoop/nativetask/wiki) and this paper for details [*NativeTask: A Hadoop Compatible Framework for High Performance*](http://prof.ict.ac.cn/bpoe2013/downloads/papers/S7201_5910.pdf).


Some early discussions of NativeTask can be found at [MAPREDUCE-2841](https://issues.apache.org/jira/browse/MAPREDUCE-2841).


#What is the benefit?

**1. Superior Performance**

For CPU intensive job like WordCount, we can provides **2.6x** performance boost transparently, or **5x** performance boost when running as full native runtime.
![native MapOutputCollector mode](https://raw.githubusercontent.com/intel-hadoop/nativetask/master/doc/800px-Native-task-performance-overview.png)

**2. Compatibility and Transparency**

NativeTask can be transparently enabled in MRv1 and MRv2, requiring no code/binary change for existing MapReduce jobs. If certain required feature has not been supported yet, NativeTask will **automatically fallback** to default implementation.

**3. Feature Complete**

NativeTask is feature complete, it supports:
  * Most key types and all value types(subclass of Writable). For a comprehensive list of supported keys, please check the Wiki Page.
  * Platforms like HBase/Hive/Pig/Mahout. 
  * Compression codec like Lz4/Snappy/Gzip.
  * Java/Native combiner.
  * Hardware checksumming CRC32C.
  * Non-sorting MapReduce paradigm when sorting is not required.

**4. Full Extensibility**

Developers are allowed to extend NativeTask to support more key types, and to replace building blocks of NativeTask with a more efficient implementation dynamically without re-compilation of the source code.

#How to use NativeTask?

NativeTask can works in two modes,

**1. Transparent Collector Mode.** In this mode, NativeTask works as transparent replacement of current in-efficient Map Output Collector, with zero changes required from user side. 

**2. Native Runtime Mode** In this mode, NativeTask works as a dedicated native runtime to support native mapper and native reducer written in C++. 

Here is the steps to enable NativeTask in transparent collector mode:

1. clone NativeTask repository
  
  ```bash
  git clone https://github.com/intel-hadoop/nativetask.git
  ```

2. Checkout the right source branch

  To build NativeTask for hadoop1.2.1, 

  ```bash
  git checkout hadoop-1.0
  ```

  To build NativeTask for Hadoop2.2.0, 

  ```bash
  git checkout master
  ```

3. patch Hadoop (${HADOOP_ROOTDIR} points to the root directory of Hadoop codebase)

  **Note:** Please make sure you checked out the hadoop 2.2.0 version(for example: git checkout release-2.2.0). Other version should probably works(after changing the pom.xml to make it point to new version), but has not been tested.  

  **Note:** Please make sure you are using bash shell to run these commands.

  **Note:** It is recommended to use autoconf 2.69 and automake 1.13.4 or newer, as people have experienced problems with some older versions (autoconf 2.63 and automake 1.11.1)

  ```bash
  cd nativetask
  cp patch/hadoop-2.patch ${HADOOP_ROOTDIR}/
  cd ${HADOOP_ROOTDIR}
  patch -p0 < hadoop-2.patch
  ```

4. build NativeTask with Hadoop

  **Note:** The build scripts has only been tested on CentOS 6 64Bit platform. Other platforms has not been verified.
  
  **Note:** Prior building, please follow https://github.com/apache/hadoop-common/blob/trunk/BUILDING.txt to install dependancies.
  

  ```bash
  cd nativetask
  cp -r . ${HADOOP_ROOTDIR}/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-nativetask
  cd ${HADOOP_ROOTDIR}
  mvn install -DskipTests -Pnative
  ```

5. install NativeTask 

  ```bash
  cd ${HADOOP_ROOTDIR}/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-nativetask/target
  cp hadoop-mapreduce-client-nativetask-2.2.0.jar /usr/lib/hadoop-mapreduce/
  cp native/target/usr/local/lib/libnativetask.so /usr/lib/hadoop/lib/native/
  ```

6. run MapReduce Pi example with native output collector
  
  ```bash
  hadoop jar hadoop-mapreduce-examples.jar pi -Dmapreduce.job.map.output.collector.class=org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator 10 10
  ```

7. check the task log and NativeTask is successfully enabled if you see the following log
  
  ```bash
  INFO org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator: Native output collector can be successfully enabled! 
  ```

Please check wiki for how to run MRv1 over NativeTask and HBase, Hive, Pig and Mahout support

## Contributors
* [Binglin Chang](https://github.com/decster)     
* [Yang Dong](https://github.com/GarfiedYang)    
* [Sean Zhong](https://github.com/clockfly)    
* [Manu Zhang](https://github.com/manuzhang)    
* [Zhongliang Zhu](https://github.com/zoken)    
* [Vincent Wang](https://github.com/huafengw)     
* [Yan Dong](https://github.com/sproblvem)
* [Cheng Lian](https://github.com/liancheng)
* [Xusen Yin](https://github.com/yinxusen)
* [Fangqin Dai](https://github.com/soulmachine)
* [Jiang Weihua](https://github.com/whjiang) 
* Gansha Wu
* [Avik Dey](https://github.com/adey)

## Contacts
For questions and support, please contact 
* [Sean Zhong](https://github.com/clockfly) (xiang.zhong@intel.com)
* [Manu Zhang](https://github.com/manuzhang) (tianlun.zhang@intel.com)
* [Jiang Weihua](https://github.com/whjiang) (weihua.jiang@intel.com)

## Further information
For further documents, please check the [Wiki](https://github.com/intel-hadoop/nativetask/wiki) Page.
