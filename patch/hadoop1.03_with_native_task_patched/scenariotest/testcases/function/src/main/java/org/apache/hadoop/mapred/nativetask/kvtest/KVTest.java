package org.apache.hadoop.mapred.nativetask.kvtest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.nativetask.testutil.ResultVerifier;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;
import org.apache.hadoop.mapred.nativetask.testutil.TestConstrants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class KVTest {
	private static Class<?>[] keyclasses = null;
	private static Class<?>[] valueclasses = null;
	private static String[] keyclassNames = null;
	private static String[] valueclassNames = null;

	private static ScenarioConfiguration nativekvtestconf = null;
	private static ScenarioConfiguration hadoopkvtestconf = null;
	static {
		nativekvtestconf = new ScenarioConfiguration();
		nativekvtestconf.addNativeConf();
		nativekvtestconf.addKVTestConf();
		hadoopkvtestconf = new ScenarioConfiguration();
		hadoopkvtestconf.addKVTestConf();
	}

	@Parameters(name = "key:{0}\nvalue:{1}")
	public static Iterable<Class<?>[]> data() {
		String valueclassesStr = nativekvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_VALUECLASSES);
		System.out.println(valueclassesStr);
		valueclassNames = valueclassesStr.replaceAll("\\s", "").split(";");// delete
																			// " "
		ArrayList<Class<?>> tmpvalueclasses = new ArrayList<Class<?>>();
		for (int i = 0; i < valueclassNames.length; i++) {
			try {
				if(valueclassNames[i].equals(""))
					continue;
				tmpvalueclasses.add(Class.forName(valueclassNames[i]));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		valueclasses = tmpvalueclasses.toArray(new Class[tmpvalueclasses.size()]);
		String keyclassesStr = nativekvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_KEYCLASSES);
		System.out.println(keyclassesStr);
		keyclassNames = keyclassesStr.replaceAll("\\s", "").split(";");// delete
													// " "
		ArrayList<Class<?>> tmpkeyclasses = new ArrayList<Class<?>>();
		for (int i = 0; i < keyclassNames.length; i++) {
			try {
				if(keyclassNames[i].equals(""))
					continue;
				tmpkeyclasses.add(Class.forName(keyclassNames[i]));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		keyclasses = tmpkeyclasses.toArray(new Class[tmpkeyclasses.size()]);
		Class<?>[][] kvgroup = new Class<?>[keyclassNames.length
				* valueclassNames.length][2];
		for (int i = 0; i < keyclassNames.length; i++) {
			int tmpindex = i * valueclassNames.length;
			for (int j = 0; j < valueclassNames.length; j++) {
				kvgroup[tmpindex + j][0] = keyclasses[i];
				kvgroup[tmpindex + j][1] = valueclasses[j];
			}
		}
		return Arrays.asList(kvgroup);
	}

	private Class<?> keyclass;
	private Class<?> valueclass;

	public KVTest(Class<?> keyclass, Class<?> valueclass) {
		this.keyclass = keyclass;
		this.valueclass = valueclass;

	}

	@Test
	public void testKVCompability() {

		try {
			String nativeoutput = this.runNativeTest(
					"Test:" + keyclass.getSimpleName() + "--"
							+ valueclass.getSimpleName(), keyclass, valueclass);
			String normaloutput = this.runNormalTest(
					"Test:" + keyclass.getSimpleName() + "--"
							+ valueclass.getSimpleName(), keyclass, valueclass);
			boolean compareRet = ResultVerifier.verify(normaloutput,
					nativeoutput);
			assertEquals(
					"file compare result: if they are the same ,then return true",
					true, compareRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			assertEquals("test run exception:", null, e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			assertEquals("test run exception:", null, e);
		}
	}

	@Before
	public void startUp() {

	}

	private String runNativeTest(String jobname, Class<?> keyclass,
			Class<?> valueclass) throws IOException {
		String inputpath = nativekvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_INPUTDIR)
				+ "/"
				+ keyclass.getName() + "/" + valueclass.getName();
		String outputpath = nativekvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_OUTPUTDIR)
				+ "/"
				+ keyclass.getName() + "/" + valueclass.getName();
		//if output file exists ,then delete it
		FileSystem fs = FileSystem.get(nativekvtestconf);
		fs.delete(new Path(outputpath));
		fs.close();
		nativekvtestconf.set(TestConstrants.NATIVETASK_KVTEST_CREATEFILE,
				"true");
		try {
			KVJob keyJob = new KVJob(jobname, nativekvtestconf, keyclass,
					valueclass,inputpath,outputpath);
			keyJob.runJob();
		} catch (Exception e) {
			return "native testcase run time error.";
		}
		return outputpath;
	}

	private String runNormalTest(String jobname, Class<?> keyclass,
			Class<?> valueclass) throws IOException {
		String inputpath = hadoopkvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_INPUTDIR)
				+ "/"
				+ keyclass.getName() + "/" + valueclass.getName();
		String outputpath = hadoopkvtestconf
				.get(TestConstrants.NATIVETASK_KVTEST_NORMAL_OUTPUTDIR)
				+ "/"
				+ keyclass.getName() + "/" + valueclass.getName();
		//if output file exists ,then delete it
		FileSystem fs = FileSystem.get(hadoopkvtestconf);
		fs.delete(new Path(outputpath));
		fs.close();
		hadoopkvtestconf.set(TestConstrants.NATIVETASK_KVTEST_CREATEFILE,
				"false");
		try {
			KVJob keyJob = new KVJob(jobname, hadoopkvtestconf, keyclass,
					valueclass, inputpath, outputpath);
			keyJob.runJob();
		} catch (Exception e) {
			return "normal testcase run time error.";
		}
		return outputpath;
	}
}
