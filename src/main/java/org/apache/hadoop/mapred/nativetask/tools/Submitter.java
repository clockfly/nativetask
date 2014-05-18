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

package org.apache.hadoop.mapred.nativetask.tools;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.mapred.nativetask.Constants;
import org.apache.hadoop.mapred.nativetask.NativeMapTaskDelegator;
import org.apache.hadoop.mapred.nativetask.NativeReduceTaskDelegator;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;

public class Submitter extends Configured implements Tool {
  protected static final Log LOG = LogFactory.getLog(Submitter.class);

  public Submitter() {
    this(new Configuration());
  }

  public Submitter(Configuration conf) {
    setConf(conf);
  }

  /**
   * Set the configuration, if it doesn't already have a value for the given key.
   * 
   * @param conf
   *          the configuration to modify
   * @param key
   *          the key to set
   * @param value
   *          the new "default" value to set
   */
  private static void setIfUnset(JobConf conf, String key, String value) {
    if (conf.get(key) == null) {
      conf.set(key, value);
    }
  }

  public static void setupNativeJob(JobConf conf) throws IOException {
    final String textClassname = Text.class.getName();
    setIfUnset(conf, MRJobConfig.MAP_OUTPUT_KEY_CLASS, textClassname);
    setIfUnset(conf, MRJobConfig.MAP_OUTPUT_VALUE_CLASS, textClassname);
    setIfUnset(conf, MRJobConfig.OUTPUT_KEY_CLASS, textClassname);
    setIfUnset(conf, MRJobConfig.OUTPUT_VALUE_CLASS, textClassname);
    conf.setBoolean(Constants.NATIVE_TASK_ENABLED, true);
    if ("JAVA".equals(conf.get(Constants.NATIVE_MAPPER_CLASS))) {
      conf.setMapperClass(IdentityMapper.class);
    } else {
      conf.set(Constants.MAPRED_MAPTASK_DELEGATOR_CLASS, NativeMapTaskDelegator.class.getCanonicalName());
    }
    if ("JAVA".equals(conf.get(Constants.NATIVE_REDUCER_CLASS))) {
      conf.setReducerClass(IdentityReducer.class);
    } else {
      conf.set(Constants.MAPRED_REDUCETASK_DELEGATOR_CLASS, NativeReduceTaskDelegator.class.getCanonicalName());
    }
    if (conf.getJobName() == null || conf.getJobName().length() == 0) {
      conf.setJobName("Native-" + conf.get(Constants.NATIVE_MAPPER_CLASS) + "-"
          + conf.get(Constants.NATIVE_REDUCER_CLASS));
    }
  }

  /**
   * Submit a job to the map/reduce cluster. All of the necessary modifications to the job to run under pipes are made
   * to the configuration.
   * 
   * @param conf
   *          the job to submit to the cluster (MODIFIED)
   * @throws IOException
   */
  public static RunningJob runJob(JobConf conf) throws IOException {
    setupNativeJob(conf);
    return JobClient.runJob(conf);
  }

  /**
   * A command line parser for the CLI-based Pipes job submitter.
   */
  static class CommandLineParser {
    private final Options options = new Options();

    @SuppressWarnings("static-access")
    void addOption(String longName, boolean required, String description, String paramName) {
      final Option option = OptionBuilder.withArgName(paramName).hasArgs(1).withDescription(description)
          .isRequired(required).create(longName);
      options.addOption(option);
    }

    @SuppressWarnings("static-access")
    void addArgument(String name, boolean required, String description) {
      final Option option = OptionBuilder.withArgName(name).hasArgs(1).withDescription(description)
          .isRequired(required).create();
      options.addOption(option);

    }

    Parser createParser() {
      final Parser result = new BasicParser();
      return result;
    }

    void printUsage() {
      System.out.println("bin/hadoop -jar nativetask.jar");
      System.out.println("  [-input <path>]         // Input directory");
      System.out.println("  [-output <path>]        // Output directory");
      System.out.println("  [-lib <name=path>,..    // user native libraries");
      System.out.println("  [-inputformat <class>]  // InputFormat class");
      System.out.println("  [-outputformat <class>] // OutputFormat class");
      System.out.println("  [-mapper <class|JAVA>]  // native Mapper class, JAVA if you want java IdentityMapper");
      System.out.println("                          // default NativeTask.Mapper (IndentityMapper)");
      System.out.println("  [-reducer <class|JAVA>] // native Reducer class, JAVA if you want java IdentityReducer");
      System.out.println("                          // default NativeTask.Mapper (IndentityReducer)");
      System.out.println("  [-partitioner <class>]  // native Partitioner class");
      System.out.println("  [-combiner <class>]     // native Combiner class");
      System.out.println("  [-reader <class>]       // native RecordReader class");
      System.out.println("                          // default NativeTask.LineRecordReader");
      System.out.println("  [-writer <class>]       // native RecordWrtier class");
      System.out.println("                          // default NativeTask.LineRecordWriter");
      System.out.println("  [-maps <num>]           // number of maps, just a hint");
      System.out.println("  [-reduces <num>]        // number of reduces, default 1");
      System.out.println("  [-jobconf <n1=v1>[,n2=v2]...] // Add or override a JobConf property.");
      System.out.println();
      GenericOptionsParser.printGenericCommandUsage(System.out);
    }
  }

  private static <InterfaceType> Class<? extends InterfaceType> getClass(CommandLine cl, String key, JobConf conf,
      Class<InterfaceType> cls) throws ClassNotFoundException {
    return conf.getClassByName(cl.getOptionValue(key)).asSubclass(cls);
  }

  @Override
  public int run(String[] args) throws Exception {
    final CommandLineParser cli = new CommandLineParser();
    if (args.length == 0) {
      cli.printUsage();
      return 1;
    }

    cli.addOption("input", true, "input path to the maps", "path");
    cli.addOption("output", true, "output path from the reduces", "path");
    cli.addOption("lib", false, "extra native library used", "path");
    cli.addOption("inputformat", false, "java classname of InputFormat", "class");
    cli.addOption("outputformat", false, "java classname of OutputFormat", "class");
    cli.addOption("mapper", false, "native Mapper class", "class");
    cli.addOption("reducer", false, "native Reducer class", "class");
    cli.addOption("partitioner", false, "native Partitioner class", "class");
    cli.addOption("combiner", false, "native Combiner class", "class");
    cli.addOption("reader", false, "native RecordReader class", "class");
    cli.addOption("writer", false, "native RecordWriter class", "class");
    cli.addOption("maps", false, "number of maps(just hint)", "num");
    cli.addOption("reduces", false, "number of reduces", "num");
    cli.addOption("jobconf", false, "\"n1=v1,n2=v2,..\" (Deprecated) Optional. Add or override a JobConf property.",
        "key=val");
    final Parser parser = cli.createParser();
    try {

      final JobConf job = new JobConf(getConf());

      final GenericOptionsParser genericParser = new GenericOptionsParser(job, args);

      setConf(job);

      final CommandLine results = parser.parse(cli.options, genericParser.getRemainingArgs());

      if (results.hasOption("input")) {
        FileInputFormat.setInputPaths(job, results.getOptionValue("input"));
      }
      if (results.hasOption("output")) {
        FileOutputFormat.setOutputPath(job, new Path(results.getOptionValue("output")));
      }
      if (results.hasOption("mapper")) {
        job.set(Constants.NATIVE_MAPPER_CLASS, results.getOptionValue("mapper"));
      }
      if (results.hasOption("reducer")) {
        job.set(Constants.NATIVE_REDUCER_CLASS, results.getOptionValue("reducer"));
      }
      if (results.hasOption("partitioner")) {
        job.set(Constants.NATIVE_PARTITIONER_CLASS, results.getOptionValue("partitioner"));
      }
      if (results.hasOption("combiner")) {
        job.set(Constants.NATIVE_COMBINER_CLASS, results.getOptionValue("combiner"));
      }
      if (results.hasOption("reader")) {
        job.set(Constants.NATIVE_RECORDREADER_CLASS, results.getOptionValue("reader"));
      }
      if (results.hasOption("writer")) {
        job.set(Constants.NATIVE_RECORDWRITER_CLASS, results.getOptionValue("writer"));
      }
      if (results.hasOption("maps")) {
        final int numMapTasks = Integer.parseInt(results.getOptionValue("maps"));
        job.setNumReduceTasks(numMapTasks);
      }
      if (results.hasOption("reduces")) {
        final int numReduceTasks = Integer.parseInt(results.getOptionValue("reduces"));
        job.setNumReduceTasks(numReduceTasks);
      }
      if (results.hasOption("lib")) {
        job.set(Constants.NATIVE_CLASS_LIBRARY, results.getOptionValue("lib"));
      }
      if (results.hasOption("inputformat")) {
        job.setInputFormat(getClass(results, "inputformat", job, InputFormat.class));
      }
      if (results.hasOption("outputformat")) {
        job.setOutputFormat(getClass(results, "outputformat", job, OutputFormat.class));
      }
      if (results.hasOption("jobconf")) {
        LOG.warn("-jobconf option is deprecated, please use -D instead.");
        final String options = results.getOptionValue("jobconf");
        final StringTokenizer tokenizer = new StringTokenizer(options, ",");
        while (tokenizer.hasMoreTokens()) {
          final String keyVal = tokenizer.nextToken().trim();
          final String[] keyValSplit = keyVal.split("=", 2);
          job.set(keyValSplit[0], keyValSplit[1]);
        }
      }
      runJob(job);
      return 0;
    } catch (final ParseException pe) {
      LOG.error("Error : " + pe);
      cli.printUsage();
      return 1;
    }
  }

  /**
   * Submit a pipes job based on the command line arguments.
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    if (args.length > 0 && args[0].equals("terasort")) {
      final String[] rest = new String[args.length - 1];
      System.arraycopy(args, 1, rest, 0, rest.length);
      final int exitCode = new TeraSort().run(rest);
      System.exit(exitCode);
    } else {
      final int exitCode = new Submitter().run(args);
      System.exit(exitCode);
    }
  }
}
