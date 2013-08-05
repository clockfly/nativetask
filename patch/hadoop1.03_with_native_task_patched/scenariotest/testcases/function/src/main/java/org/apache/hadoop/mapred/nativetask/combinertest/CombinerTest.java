package org.apache.hadoop.mapred.nativetask.combinertest;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.mapred.nativetask.testutil.ResultVerifier;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;
import org.apache.hadoop.mapred.nativetask.testutil.TestConstrants;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Before;
import org.junit.Test;

public class CombinerTest {
	@Test
	public void testWordCountCombiner() {
		try {
			ScenarioConfiguration conf =new ScenarioConfiguration();
			conf.addcombinerConf();
			Job nativejob = CombinerJobFactory
					.getWordCountNativeJob("nativewordcount");
			Job normaljob = CombinerJobFactory
					.getWordCountNormalJob("normalwordcount");
			nativejob.waitForCompletion(true);
			normaljob.waitForCompletion(true);
			String nativeoutputpath = conf.get(
					TestConstrants.NATIVETASK_TEST_COMBINER_OUTPUTPATH,
					TestConstrants.NATIVETASK_TEST_COMBINER_OUTPUTPATH_DEFAULTV)
					+ "/nativewordcount";
			String hadoopoutputpath = conf.get(
					TestConstrants.NORMAL_TEST_COMBINER_OUTPUTPATH,
					TestConstrants.NORMAL_TEST_COMBINER_OUTPUTPATH_DEFAULTV)
					+ "/normalwordcount";
			assertEquals(true, ResultVerifier.verify(
					nativeoutputpath, 
					hadoopoutputpath)
					);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals("run exception",true,false);
		}
	}
}
