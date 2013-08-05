package org.apache.hadoop.mapred.nativetask.kvtest;

import java.io.IOException;
import java.util.zip.CRC32;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.nativetask.testutil.BytesUtil;
import org.apache.hadoop.mapred.nativetask.testutil.TestConstrants;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class KVJob {
	public static final String INPUTPATH = "nativetask.kvtest.inputfile.path";
	public static final String OUTPUTPATH = "nativetask.kvtest.outputfile.path";
	Job job = null;

	public static class ValueMapper<KTYPE, VTYPE> extends
			Mapper<KTYPE, VTYPE, KTYPE, VTYPE> {
		public void map(KTYPE key, VTYPE value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value);
		}
	}

	public static class KVMReducer<KTYPE, VTYPE> extends
			Reducer<KTYPE, VTYPE, KTYPE, VTYPE> {
		public void reduce(KTYPE key, VTYPE value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value);
		}
	}

	public static class KVReducer<KTYPE, VTYPE> extends
			Reducer<KTYPE, VTYPE, KTYPE, VTYPE> {

		public void reduce(KTYPE key, Iterable<VTYPE> values, Context context)
				throws IOException, InterruptedException {
			long resultlong = 0;//8 bytes match BytesUtil.fromBytes function
			CRC32 crc32 = new CRC32();
			for (VTYPE val : values) {
				crc32.reset();
				crc32.update(BytesUtil.toBytes(val));
				resultlong += crc32.getValue();
			}
			VTYPE V = null;
			context.write(key, (VTYPE) BytesUtil.fromBytes(Bytes.toBytes(resultlong), V
					.getClass().getName()));
		}
	}

	public KVJob(String jobname, Configuration conf, Class<?> keyclass,
			Class<?> valueclass,String inputpath,String outputpath) throws Exception {
		// TODO Auto-generated constructor stub
		job = new Job(conf, jobname);
		job.setJarByClass(KVJob.class);
		job.setMapperClass(KVJob.ValueMapper.class);
		job.setOutputKeyClass(keyclass);
		job.setOutputValueClass(valueclass);

		if (conf.get(TestConstrants.NATIVETASK_KVTEST_CREATEFILE).equals("true")) {
			FileSystem fs = FileSystem.get(conf);
			fs.delete(new Path(inputpath));
			fs.close();

			TestInputFile testfile = new TestInputFile(Integer.valueOf(conf
					.get(TestConstrants.FILESIZE_KEY, "1000")),
					keyclass.getName(), valueclass.getName());
			testfile.createSequenceTestFile(inputpath);

		}
		job.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPath(job, new Path(inputpath));
		FileOutputFormat.setOutputPath(job, new Path(outputpath));
	}

	public void runJob() throws Exception {

		job.waitForCompletion(true);
	}
}
