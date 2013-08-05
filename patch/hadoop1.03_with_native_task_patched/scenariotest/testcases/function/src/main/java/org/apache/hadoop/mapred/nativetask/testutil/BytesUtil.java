package org.apache.hadoop.mapred.nativetask.testutil;

import java.util.Random;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.VLongWritable;

public class BytesUtil {
	public static Random r = new Random();
	public static Object fromBytes(byte[] bytes, String className) {
		if (className.equals(IntWritable.class.getName())) {
			return new IntWritable(Bytes.toInt(bytes));
		} else if (className.equals(FloatWritable.class.getName())) {
			r.setSeed(Bytes.toLong(bytes));
			return new FloatWritable(r.nextFloat());
		} else if (className.equals(DoubleWritable.class.getName())) {
			r.setSeed(Bytes.toLong(bytes));
			return new DoubleWritable(r.nextDouble());
		} else if (className.equals(LongWritable.class.getName())) {
			return new LongWritable(Bytes.toLong(bytes));
		} else if (className.equals(VIntWritable.class.getName())) {
			return new VIntWritable(Bytes.toInt(bytes));
		} else if (className.equals(VLongWritable.class.getName())) {
			return new VLongWritable(Bytes.toLong(bytes));
		} else if (className.equals(BooleanWritable.class.getName())) {
			return new BooleanWritable(bytes[0] % 2 == 1 ? true : false);
		} else if (className.equals(Text.class.getName())) {
			return new Text(Bytes.toString(bytes));
		} else if (className.equals(ByteWritable.class.getName())) {
			return new ByteWritable(bytes.length > 0 ? bytes[0] : 0);
		} else if (className.equals(BytesWritable.class.getName())) {
			return new BytesWritable(bytes);
		} else if (className.equals(ImmutableBytesWritable.class.getName())) {
			return new ImmutableBytesWritable(bytes);
		} else if (className.equals(UTF8.class.getName())) {
			return new UTF8(Bytes.toString(bytes));
		} else
			return null;
	}
	public static<VTYPE> byte[] toBytes(VTYPE obj){
		String className = obj.getClass().getName();
		if (className.equals(IntWritable.class.getName())) {
			return Bytes.toBytes(((IntWritable)obj).get());
		} else if (className.equals(FloatWritable.class.getName())) {
			return Bytes.toBytes(((FloatWritable)obj).get());
		} else if (className.equals(DoubleWritable.class.getName())) {
			return Bytes.toBytes(((DoubleWritable)obj).get());
		} else if (className.equals(LongWritable.class.getName())) {
			return Bytes.toBytes(((LongWritable)obj).get());
		} else if (className.equals(VIntWritable.class.getName())) {
			return Bytes.toBytes(((VIntWritable)obj).get());
		} else if (className.equals(VLongWritable.class.getName())) {
			return Bytes.toBytes(((VLongWritable)obj).get());
		} else if (className.equals(BooleanWritable.class.getName())) {
			return Bytes.toBytes(((BooleanWritable)obj).get());
		} else if (className.equals(Text.class.getName())) {
			return Bytes.toBytes(((Text)obj).toString());
		} else if (className.equals(ByteWritable.class.getName())) {
			return Bytes.toBytes(((ByteWritable)obj).get());
		} else if (className.equals(BytesWritable.class.getName())) {
			return ((BytesWritable)obj).getBytes();
		} else if (className.equals(ImmutableBytesWritable.class.getName())) {
			return ((ImmutableBytesWritable)obj).get();
		} else if (className.equals(UTF8.class.getName())) {
			return ((UTF8)obj).getBytes();
		} else
			return new byte[0];
	}
}
