package org.apache.hadoop.mapred.nativetask.compresstest;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;
import org.apache.hadoop.mapred.nativetask.testutil.TestConstrants;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CompressMapper {
	public static final String inputFile = "./compress/input.txt";
	public static final String outputFileDir = "./compress/output/";
	
	public static class TextCompressMapper extends Mapper<Text,Text,Text,Text>{

		@Override
		protected void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			context.write(key, value);
		}
	}
	public static Job getCompressJob(String jobname,Configuration conf){
		conf.addResource(TestConstrants.NATIVETASK_COMPRESS_PATH);
		Job job = null;
		try{
			job = new Job(conf,jobname+"-CompressMapperJob");
			job.setJarByClass(CompressMapper.class);
			job.setMapperClass(TextCompressMapper.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			Path outputpath = new Path(outputFileDir+jobname);
			// if output file exists ,delete it
			FileSystem hdfs = FileSystem.get(ScenarioConfiguration.envconf);
			if(hdfs.exists(outputpath)){
				hdfs.delete(outputpath);
			}
			hdfs.close();
			job.setInputFormatClass(SequenceFileInputFormat.class);
			SequenceFileInputFormat.addInputPath(job, new Path(inputFile));
			FileOutputFormat.setOutputPath(job, outputpath);
		}catch(Exception e){
			e.printStackTrace();
		}
		return job;
	}
}
