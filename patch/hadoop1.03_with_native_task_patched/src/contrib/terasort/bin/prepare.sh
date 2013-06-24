#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

echo "========== preparing terasort data=========="
# configure
DIR=`cd $bin/../; pwd`
. "${DIR}/../bin/hibench-config.sh"
. "${DIR}/conf/configure.sh"

# path check
$HADOOP_HOME/bin/hadoop dfs -rmr $INPUT_HDFS

# Generate the terasort data
$HADOOP_HOME/bin/hadoop jar $HADOOP_HOME/hadoop-examples*.jar teragen \
    -libjars $HADOOP_HOME/lib/hadoop-snappy-0.0.1-SNAPSHOT.jar \
    -D io.sort.mb=1200 \
    -D mapred.output.compress=true \
    -D mapred.compress.map.output=true \
    -D mapred.output.compression.type=BLOCK \
    -D mapred.map.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec \
    -D mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec \
    -D mapred.map.tasks=60 \
    $DATASIZE $INPUT_HDFS

$HADOOP_HOME/bin/hadoop dfs -rmr $INPUT_HDFS/_*
