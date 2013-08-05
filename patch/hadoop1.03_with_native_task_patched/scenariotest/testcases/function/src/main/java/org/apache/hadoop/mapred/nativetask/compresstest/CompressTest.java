package org.apache.hadoop.mapred.nativetask.compresstest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.nativetask.kvtest.TestInputFile;
import org.apache.hadoop.mapred.nativetask.testutil.ResultVerifier;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Before;
import org.junit.Test;

public class CompressTest {

	@Test
	public void testSnappyCompress() throws Exception {
		Configuration conf = ScenarioConfiguration.getNativeConfiguration();
		conf.addResource("test-snappy-compress-conf");
		Job job = CompressMapper.getCompressJob("nativesnappy", conf);
		job.waitForCompletion(true);

		Configuration hadoopconf = ScenarioConfiguration
				.getCommonConfiguration();
		hadoopconf.addResource("test-bzip2-compress-conf");
		Job hadoopjob = CompressMapper.getCompressJob("hadoopsnappy",
				hadoopconf);
		hadoopjob.waitForCompletion(true);
		
		boolean compareRet = ResultVerifier.verify(CompressMapper.outputFileDir + "nativesnappy",
				CompressMapper.outputFileDir + "hadoopsnappy");
		assertEquals(
				"file compare result: if they are the same ,then return true",
				true, compareRet);
	}
	@Test
	public void testGzipCompress() throws Exception {
		Configuration conf = ScenarioConfiguration.getNativeConfiguration();
		conf.addResource("test-gzip-compress-conf");
		Job job = CompressMapper.getCompressJob("nativegzip", conf);
		job.waitForCompletion(true);

		Configuration hadoopconf = ScenarioConfiguration
				.getCommonConfiguration();
		hadoopconf.addResource("test-bzip2-compress-conf");
		Job hadoopjob = CompressMapper.getCompressJob("hadoopgzip", hadoopconf);
		hadoopjob.waitForCompletion(true);
		
		boolean compareRet = ResultVerifier.verify(CompressMapper.outputFileDir + "nativegzip",
				CompressMapper.outputFileDir + "hadoopgzip");
		assertEquals(
				"file compare result: if they are the same ,then return true",
				true, compareRet);
	}
	@Test
	public void testBzip2Compress() throws Exception {
		Configuration nativeconf = ScenarioConfiguration
				.getNativeConfiguration();
		nativeconf.addResource("test-bzip2-compress-conf");
		Job nativejob = CompressMapper
				.getCompressJob("nativebzip2", nativeconf);
		nativejob.waitForCompletion(true);

		Configuration hadoopconf = ScenarioConfiguration
				.getCommonConfiguration();
		hadoopconf.addResource("test-bzip2-compress-conf");
		Job hadoopjob = CompressMapper
				.getCompressJob("hadoopbzip2", hadoopconf);
		hadoopjob.waitForCompletion(true);

		boolean compareRet = ResultVerifier.verify(CompressMapper.outputFileDir + "nativebzip2",
				CompressMapper.outputFileDir + "hadoopbzip2");
		assertEquals(
				"file compare result: if they are the same ,then return true",
				true, compareRet);
	}

	@Before
	public void startUp() throws Exception {
		FileSystem fs = FileSystem.get(ScenarioConfiguration.envconf);
		if (!fs.exists(new Path(CompressMapper.inputFile))) {
			new TestInputFile(ScenarioConfiguration.getCommonConfiguration()
					.getInt("nativetask.compress.filesize", 100000),
					Text.class.getName(), Text.class.getName())
					.createSequenceTestFile(CompressMapper.inputFile);

		}
		fs.close();
	}
}
