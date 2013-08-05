package org.apache.hadoop.mapred.nativetask.testutil;

public class TestConstrants {
	//conf path
	public static final String COMPRESS_CONF_PATH = "compress-conf.xml";
	public static final String COMBINER_CONF_PATH = "test-combiner-conf.xml";
	public static final String KVTEST_CONF_PATH = "kvtest-conf.xml";
	
	
	public static final String NATIVETASK_KVTEST_INPUTDIR = "nativetask.kvtest.inputdir";
	public static final String NATIVETASK_KVTEST_OUTPUTDIR = "nativetask.kvtest.outputdir";
	public static final String NATIVETASK_KVTEST_NORMAL_OUTPUTDIR = "normal.kvtest.outputdir";
	public static final String NATIVETASK_KVTEST_CREATEFILE = "nativetask.kvtest.createfile";
	public static final String NATIVETASK_KVTEST_FILE_RECORDNUM = "nativetask.kvtest.file.recordnum";
	public static final String NATIVETASK_KVTEST_KEYCLASSES = "nativetask.kvtest.keyclasses";
	public static final String NATIVETASK_KVTEST_VALUECLASSES = "nativetask.kvtest.valueclasses";
	public static final String NATIVETASK_COLLECTOR_DELEGATOR = "mapreduce.map.output.collector.delegator.class";
	public static final String NATIVETASK_COLLECTOR_DELEGATOR_CLASS = "org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator";
	
	public static final String NATIVETASK_COMPRESS_PATH = "test-compress-conf.xml";
	
	public static final String NATIVETASK_TEST_COMBINER_INPUTPATH_KEY = "nativetask.combinertest.inputpath";
	public static final String NATIVETASK_TEST_COMBINER_INPUTPATH_DEFAULTV = "./combinertest/input";
	public static final String NATIVETASK_TEST_COMBINER_OUTPUTPATH = "nativetask.combinertest.outputdir";
	public static final String NATIVETASK_TEST_COMBINER_OUTPUTPATH_DEFAULTV = "./combinertest/output/native";
	public static final String NORMAL_TEST_COMBINER_OUTPUTPATH = "normal.combinertest.outputdir";
	public static final String NORMAL_TEST_COMBINER_OUTPUTPATH_DEFAULTV = "./combinertest/output/normal";
	
	public static final String common_conf_path="common_conf.xml";
	
	public static final String FILESIZE_KEY = "kvtest.file.size";
}
