package org.apache.hadoop.mapred.nativetask.kvtest;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.VLongWritable;
import org.apache.hadoop.mapred.nativetask.testutil.BytesUtil;
import org.apache.hadoop.mapred.nativetask.testutil.ScenarioConfiguration;

public class TestInputFile {
	private byte[] databuf = null;
	private String keyClsName, valueClsName;
	private int filesize = 0;
	private int keyMaxBytesNum, keyMinBytesNum;
	private int valueMaxBytesNum, valueMinBytesNum;
	private SequenceFile.Writer writer = null;
	Random r = new Random();
	public static final int DATABUFSIZE = 1 << 22; // 4M

	private enum State {
		KEY, VALUE
	};

	public TestInputFile(int filesize, String keytype,
			String valuetype) throws Exception {
		this.filesize = filesize;
		this.databuf = new byte[DATABUFSIZE];
		this.keyClsName = keytype;
		this.valueClsName = valuetype;
		
		if (keytype.equals(BooleanWritable.class.getName())) {
			keyMaxBytesNum = 1;
			keyMinBytesNum = 1;
		} else if (keytype.equals(DoubleWritable.class.getName())) {
			keyMaxBytesNum = 8;
			keyMinBytesNum = 8;
		} else if (keytype.equals(LongWritable.class.getName())) {
			keyMaxBytesNum = 8;
			keyMinBytesNum = 8;
		} else if (keytype.equals(VLongWritable.class.getName())) {
			keyMaxBytesNum = 8;
			keyMinBytesNum = 8;
		} else if (keytype.equals(Text.class.getName())) {
			keyMaxBytesNum = 64;
			keyMinBytesNum = 1;
		} else if (keytype.equals(UTF8.class.getName())) {
			keyMaxBytesNum = 64;
			keyMinBytesNum = 1;
		} else if (keytype.equals(ImmutableBytesWritable.class.getName())) {
			keyMaxBytesNum = 64;
			keyMinBytesNum = 1;
		} else {
			keyMaxBytesNum = 4;
			keyMinBytesNum = 4;
		}
		if (valuetype.equals(BooleanWritable.class.getName())) {
			valueMaxBytesNum = 1;
			valueMinBytesNum = 1;
		} else if (valuetype.equals(DoubleWritable.class.getName())) {
			valueMaxBytesNum = 8;
			valueMinBytesNum = 8;
		} else if (valuetype.equals(LongWritable.class.getName())) {
			valueMaxBytesNum = 8;
			valueMinBytesNum = 8;
		} else if (valuetype.equals(VLongWritable.class.getName())) {
			valueMaxBytesNum = 8;
			valueMinBytesNum = 8;
		} else if (valuetype.equals(Text.class.getName())) {
			valueMaxBytesNum = 64;
			valueMinBytesNum = 1;
		} else if (valuetype.equals(UTF8.class.getName())) {
			valueMaxBytesNum = 64;
			valueMinBytesNum = 1;
		} else if (valuetype.equals(ImmutableBytesWritable.class.getName())) {
			valueMaxBytesNum = 64;
			valueMinBytesNum = 1;
		} else {
			valueMaxBytesNum = 4;
			valueMinBytesNum = 4;
		}
	}

	public void createSequenceTestFile(String filepath) throws Exception {
		System.out.println("create file "+filepath);
		System.out.println(keyClsName+" "+valueClsName);
		Class<?> tmpkeycls, tmpvaluecls;
		try {
			tmpkeycls = Class.forName(keyClsName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new Exception("key class not found: ", e);
		}
		try {
			tmpvaluecls = Class.forName(valueClsName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new Exception("key class not found: ", e);
		}
		try {
			FileSystem hdfs = FileSystem.get(ScenarioConfiguration.envconf);
			Path outputfilepath = new Path(filepath);
			writer = new SequenceFile.Writer(hdfs, ScenarioConfiguration.envconf, outputfilepath,
					tmpkeycls, tmpvaluecls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int tmpfilesize = this.filesize - DATABUFSIZE;
		while (tmpfilesize > 0) {
			r.nextBytes(databuf);
			flushBuf(DATABUFSIZE);
			tmpfilesize -= DATABUFSIZE;
		}
		
		r.nextBytes(databuf);
		
		flushBuf(tmpfilesize + DATABUFSIZE);
		
		if (writer != null)
			IOUtils.closeStream(writer);
		else
			throw new Exception("no writer to create sequenceTestFile!");
	}

	private void flushBuf(int buflen) throws Exception {
		// TODO Auto-generated method stub
		State state = State.KEY;
		Random r = new Random();
		int keybytesnum, valuebytesnum;
		keybytesnum = keyMaxBytesNum == keyMinBytesNum ? keyMaxBytesNum : (r
				.nextInt() % keyMaxBytesNum);
		keybytesnum = keybytesnum<0?-keybytesnum:keybytesnum;
		keybytesnum = keybytesnum >= keyMinBytesNum ? keybytesnum
				: (keybytesnum % (keyMaxBytesNum - keyMinBytesNum) + keyMinBytesNum);
		valuebytesnum = valueMaxBytesNum == valueMinBytesNum ? valueMaxBytesNum
				: (r.nextInt() % valueMaxBytesNum);
		valuebytesnum = valuebytesnum<0?-valuebytesnum:valuebytesnum;
		valuebytesnum = valuebytesnum >= valueMinBytesNum ? valuebytesnum
				: (valuebytesnum % (valueMaxBytesNum - valueMinBytesNum) + valueMinBytesNum);
		byte[] key = new byte[keybytesnum];
		byte[] value = new byte[valuebytesnum];
		for (int offset = 0; offset < buflen;) {
			if (state.equals(State.KEY)) {
				for (int i = 0; i < keybytesnum; i++)
					key[i] = databuf[i + offset];
				offset += keybytesnum;
				state = State.VALUE;
			} else {
				for (int i = 0; i < valuebytesnum; i++)
					value[i] = databuf[i + offset];
				offset += valuebytesnum;
				state = State.KEY;
				try {
					writer.append(BytesUtil.fromBytes(key, this.keyClsName),
							BytesUtil.fromBytes(value, this.valueClsName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new Exception("sequence file create failed", e);
				}
			}
		}
	}
}
