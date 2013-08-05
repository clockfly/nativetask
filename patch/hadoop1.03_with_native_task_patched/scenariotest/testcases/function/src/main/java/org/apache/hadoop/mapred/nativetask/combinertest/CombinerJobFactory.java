package org.apache.hadoop.mapred.nativetask.combinertest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.nativetask.combinertest.WordCount.IntSumReducer;
import org.apache.hadoop.mapred.nativetask.combinertest.WordCount.TokenizerMapper;
import org.apache.hadoop.mapred.nativetask.kvtest.TestInputFile;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;
import org.apache.hadoop.mapred.nativetask.testutil.TestConstrants;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CombinerJobFactory {

	public static Job getWordCountNativeJob(String jobname) throws Exception {
		Configuration conf = ScenarioConfiguration.getNativeConfiguration();
		conf.addResource(TestConstrants.COMBINER_CONF_PATH);
		String inputpath = conf.get(
				TestConstrants.NATIVETASK_TEST_COMBINER_INPUTPATH_KEY,
				TestConstrants.NATIVETASK_TEST_COMBINER_INPUTPATH_DEFAULTV)
				+ "/wordcount";
		String outputpath = conf.get(
				TestConstrants.NATIVETASK_TEST_COMBINER_OUTPUTPATH,
				TestConstrants.NATIVETASK_TEST_COMBINER_OUTPUTPATH_DEFAULTV)
				+ "/" + jobname;
		conf.set("fileoutputpath", outputpath);
		FileSystem fs = FileSystem.get(conf);
		// no such file ,then create it
		if (!fs.exists(new Path(inputpath))) {
			new TestInputFile(conf.getInt(
					"nativetask.combiner.wordcount.filesize", 10000),
					Text.class.getName(), Text.class.getName())
					.createSequenceTestFile(inputpath);

		}
		if (fs.exists(new Path(outputpath))) {
			fs.delete(new Path(outputpath));
		}
		fs.close();
		Job job = new Job(conf, jobname);
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPath(job, new Path(inputpath));
		FileOutputFormat.setOutputPath(job, new Path(outputpath));
		return job;
	}

	public static Job getWordCountNormalJob(String jobname) throws Exception {
		Configuration conf = ScenarioConfiguration.getCommonConfiguration();
		conf.addResource(TestConstrants.COMBINER_CONF_PATH);
		Job job = new Job(conf, jobname);
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat
				.addInputPath(
						job,
						new Path(
								conf.get(
										TestConstrants.NATIVETASK_TEST_COMBINER_INPUTPATH_KEY,
										TestConstrants.NATIVETASK_TEST_COMBINER_INPUTPATH_DEFAULTV)
										+ "/wordcount"));

		String outputpath = conf.get(
				TestConstrants.NORMAL_TEST_COMBINER_OUTPUTPATH,
				TestConstrants.NORMAL_TEST_COMBINER_OUTPUTPATH_DEFAULTV)
				+ "/" + jobname;
		FileOutputFormat.setOutputPath(job, new Path(outputpath));
		conf.set("fileoutputpath", outputpath);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(new Path(outputpath))) {
			fs.delete(new Path(outputpath));
		}
		fs.close();
		return job;
	}

	public static Job getCombinerTestNativeJob(String jobname,
			Class<?> mapperCls, Class<?> combinerCls, Class<?> reducerCls) {
		return null;
	}
}
