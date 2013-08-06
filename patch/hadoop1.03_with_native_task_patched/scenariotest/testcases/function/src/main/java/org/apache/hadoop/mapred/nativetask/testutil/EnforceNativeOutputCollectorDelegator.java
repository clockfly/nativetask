package org.apache.hadoop.mapred.nativetask.testutil;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.Task;
import org.apache.hadoop.mapred.TaskUmbilicalProtocol;
import org.apache.hadoop.mapred.Task.TaskReporter;
import org.apache.hadoop.mapred.nativetask.NativeMapOutputCollectorDelegator;

public class EnforceNativeOutputCollectorDelegator<K, V> extends
		NativeMapOutputCollectorDelegator<K, V> {
	private boolean nativetaskloaded = false;

	public void init(TaskUmbilicalProtocol umbilical, TaskReporter reporter,
			Configuration conf, Task task) throws Exception {
		try {
			super.init(umbilical, reporter, conf, task);
			nativetaskloaded = true;
		} catch (Exception e) {
			nativetaskloaded = false;
			System.err.println("load nativetask lib failed");
		}
	}

	public void collect(K key, V value, int partition) throws IOException,
			InterruptedException {
		if (this.nativetaskloaded) {
			super.collect(key, value, partition);
		} else {
			// nothing to do.
			System.err
					.println("could not execute nativetask. nothing to do in collect step.");
		}
	}
}
