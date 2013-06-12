/*
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

#include "commons.h"
#include "util/StringUtil.h"
#include "MMapperHandler.h"
#include "NativeObjectFactory.h"
#include "MapOutputCollector.h"

namespace NativeTask {

MMapperHandler::MMapperHandler() :
    _config(NULL),
    _moc(NULL),
    _mapper(NULL),
    _partitioner(NULL),
    _combinerCreator(NULL),
    _numPartition(1),
    _dest(NULL),
    _remain(0) {
}

MMapperHandler::~MMapperHandler() {
  reset();
}

void MMapperHandler::reset() {
  _dest = NULL;
  _remain = 0;
  delete _mapper;
  _mapper = NULL;
  delete _moc;
  _moc = NULL;
  delete _partitioner;
  _partitioner = NULL;
  _combinerCreator = NULL;
}

void MMapperHandler::configure(Config & config) {
  _config = &config;

  // collector
  _numPartition = config.getInt("mapred.reduce.tasks", 1);
  if (_numPartition > 0) {
    // combiner
    const char * combinerClass = config.get("native.combiner.class");
    if (NULL != combinerClass) {
      _combinerCreator = NativeObjectFactory::GetObjectCreator(combinerClass);
      if (NULL == _combinerCreator) {
        THROW_EXCEPTION_EX(UnsupportException, "Combiner not found: %s", combinerClass);
      }
    }

    // partitioner
    const char * partitionerClass = config.get("native.partitioner.class");
    if (NULL != partitionerClass) {
      _partitioner
          = (Partitioner *) NativeObjectFactory::CreateObject(partitionerClass);
    }
    else {
      _partitioner
          = (Partitioner *) NativeObjectFactory::CreateDefaultObject(PartitionerType);
    }
    if (NULL == _partitioner) {
      THROW_EXCEPTION_EX(UnsupportException, "Partitioner not found: %s", partitionerClass);
    }
    _partitioner->configure(config);

    LOG("Native Mapper with MapOutputCollector");
    _moc = new MapOutputCollector(_numPartition);
    _moc->configure(config);
  }
  else {
    LOG("Native Mapper with java direct output collector");
  }

  // mapper
  const char * mapperClass = config.get("native.mapper.class");
  if (NULL != mapperClass) {
    _mapper = (Mapper *) NativeObjectFactory::CreateObject(mapperClass);
  }
  else {
    _mapper = (Mapper *) NativeObjectFactory::CreateDefaultObject(MapperType);
  }
  if (NULL == _mapper) {
    THROW_EXCEPTION_EX(UnsupportException, "Mapper not found: %s", mapperClass);
  }
  _mapper->configure(config);
  _mapper->setCollector(this);
}

void MMapperHandler::finish() {
  close();
  BatchHandler::finish();
  reset();
}

void MMapperHandler::handleInput(char * buff, uint32_t length) {

  while (length > 0) {
    if (unlikely(length<2*sizeof(uint32_t))) {
      THROW_EXCEPTION(IOException, "k/v length information incomplete");
    }
    uint32_t klength = ((uint32_t*) buff)[0];
    uint32_t vlength = *((uint32_t*) (buff + klength + sizeof(uint32_t) ));
    uint32_t kvlength = klength + vlength + 2 * sizeof(uint32_t);

    if (kvlength > length) {
        THROW_EXCEPTION(IOException, "k/v data incomplete");
    }

    if (kvlength <= length) {
      _mapper->map(buff + sizeof(uint32_t), klength, buff + klength + 2 * sizeof(uint32_t), vlength);
      buff += kvlength;
      length -= kvlength;
    }
  }
}

void MMapperHandler::collect(const void * key, uint32_t keyLen,
    const void * value, uint32_t valueLen, int partition) {
  if (NULL == _moc) {
    THROW_EXCEPTION(UnsupportException, "Collect with partition not support");
  }
  int result =_moc->put(key, keyLen, value, valueLen, partition);
  if (result==0) {
    return;
  }
  string spillpath = this->sendCommand("GetSpillPath");
  if (spillpath.length() == 0) {
    THROW_EXCEPTION(IOException, "Illegal(empty) spill files path");
  }
  vector<string> pathes;
  StringUtil::Split(spillpath, ";", pathes);
  _moc->mid_spill(pathes,"", _moc->getMapOutputSpec(), _combinerCreator);
  result =_moc->put(key, keyLen, value, valueLen, partition);
  if (0 != result) {
    // should not get here, cause _moc will throw Exceptions
    THROW_EXCEPTION(OutOfMemoryException, "key/value pair larger than io.sort.mb");
  }
}

void MMapperHandler::collect(const void * key, uint32_t keyLen,
                     const void * value, uint32_t valueLen) {
  if (NULL == _moc) {

    //flush output to make sure we have enough room to hold the key and value
    if (_ob.position + keyLen + valueLen + 2 * sizeof(uint32_t) > _ob.capacity) {
        flushOutput(_ob.position);
        _ob.position = 0;
    }

    putInt(keyLen);
    put((char *)key, keyLen);
    putInt(valueLen);
    put((char *)value, valueLen);
    return;
  }
  uint32_t partition = _partitioner->getPartition((const char *) key, keyLen,
      _numPartition);
  collect(key, keyLen, value, valueLen, partition);
}

void MMapperHandler::close() {
  _mapper->close();
  if (NULL == _moc) {
    return;
  }
  string outputpath = this->sendCommand("GetOutputPath");
  string indexpath = this->sendCommand("GetOutputIndexPath");
  if ((outputpath.length() == 0) || (indexpath.length() == 0)) {
    THROW_EXCEPTION(IOException, "Illegal(empty) map output file/index path");
  }
  vector<string> pathes;
  StringUtil::Split(outputpath, ";", pathes);
  _moc->final_merge_and_spill(pathes, indexpath, _moc->getMapOutputSpec(), _combinerCreator);
}

} // namespace NativeTask
