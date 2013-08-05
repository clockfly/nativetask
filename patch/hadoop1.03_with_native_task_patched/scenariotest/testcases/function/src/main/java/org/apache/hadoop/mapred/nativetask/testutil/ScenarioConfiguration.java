package org.apache.hadoop.mapred.nativetask.testutil;

import org.apache.hadoop.conf.Configuration;

public class ScenarioConfiguration extends Configuration{
	public ScenarioConfiguration(){
		super(commonconf);
	}
	public void addcompressConf(){
		this.addResource(TestConstrants.COMPRESS_CONF_PATH);
	}
	public  void addcombinerConf(){
		this.addResource(TestConstrants.COMBINER_CONF_PATH);
	}
	public void addKVTestConf(){
		this.addResource(TestConstrants.KVTEST_CONF_PATH);
	}
	public void addNativeConf(){
		this.set(TestConstrants.NATIVETASK_COLLECTOR_DELEGATOR,
				TestConstrants.NATIVETASK_COLLECTOR_DELEGATOR_CLASS);
	}
	
	public static final Configuration envconf = new Configuration();
	private static Configuration commonconf = null;
	private static Configuration nativeconf = null;
	static {
		commonconf = new Configuration();
		commonconf.addResource("common_conf.xml");
		nativeconf = new Configuration(commonconf);
		nativeconf.set(TestConstrants.NATIVETASK_COLLECTOR_DELEGATOR,
				TestConstrants.NATIVETASK_COLLECTOR_DELEGATOR_CLASS);
	}
	public static Configuration getCommonConfiguration(){
		return commonconf;
	}
	public static Configuration getNativeConfiguration() {
		return nativeconf;
	}
}
