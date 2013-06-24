/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef MMAPTASKHANDLER_H_
#define MMAPTASKHANDLER_H_


#include "NativeTask.h"
#include "BatchHandler.h"

namespace NativeTask {

class MapOutputCollector;

/**
 * Handle the whole map task, from input split to writer/mapoutput
 * First call setup to setup all sub parts,
 * then call command("run") to run the whole task
 */
class MMapTaskHandler :
    public BatchHandler,
    public Collector {
private:
  uint32_t _numPartition;
  Config * _config;
  RecordReader * _reader;
  Mapper * _mapper;
  Partitioner * _partitioner;
  ObjectCreatorFunc _combinerCreator;
  MapOutputCollector * _moc;
  RecordWriter * _writer;

  // counters
  Counter * _mapInputRecords;
  Counter * _mapInputBytes;
  Counter * _mapOutputRecords;
  Counter * _mapOutputBytes;
public:
  MMapTaskHandler();
  virtual ~MMapTaskHandler();

  virtual void configure(Config & config);
  virtual string command(const string & cmd);

  // Collector methods
  virtual void collect(const void * key, uint32_t keyLen, const void * value,
      uint32_t valueLen, int partition);
  virtual void collect(const void * key, uint32_t keyLen, const void * value,
      uint32_t valueLen);
private:
  void initCounters();
  void close();
  void reset();
};

} // namespace NativeTask

#endif /* MMAPTASKHANDLER_H_ */
