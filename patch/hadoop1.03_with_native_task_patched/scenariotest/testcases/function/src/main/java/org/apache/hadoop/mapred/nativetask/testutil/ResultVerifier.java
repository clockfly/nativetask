package org.apache.hadoop.mapred.nativetask.testutil;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

public class ResultVerifier {
	/**
	 * verify the result
	 * 
	 * @param sample
	 *            :nativetask output
	 * @param source
	 *            :yuanwenjian
	 * @throws Exception
	 */
	public static boolean verify(String sample, String source) throws Exception {
		FSDataInputStream sourcein = null;
		FSDataInputStream samplein = null;

		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path hdfssource = new Path(source);
		Path[] sourcepaths = FileUtil.stat2Paths(fs.listStatus(hdfssource));

		Path hdfssample = new Path(sample);
		Path[] samplepaths = FileUtil.stat2Paths(fs.listStatus(hdfssample));
		if (sourcepaths == null)
			throw new Exception("source file can not be found");
		if (samplepaths == null)
			throw new Exception("sample file can not be found");
		if (sourcepaths.length != samplepaths.length)
			return false;
		for (int i = 0; i < sourcepaths.length; i++) {
			Path sourcepath = sourcepaths[i];
			// op result file start with "part-r" like part-r-00000

			if (!sourcepath.getName().startsWith("part-r"))
				continue;
			Path samplepath = null;
			for (int j = 0; j < samplepaths.length; j++) {
				if (samplepaths[i].getName().equals(sourcepath.getName())) {
					samplepath = samplepaths[i];
					break;
				}
			}
			if (samplepath == null)
				throw new Exception("cound not found file "
						+ samplepaths[0].getParent() + "/"
						+ sourcepath.getName()
						+ " , as sourcepaths has such file");

			// compare
			try {
				if (fs.exists(sourcepath) && fs.exists(samplepath)) {
					sourcein = fs.open(sourcepath);
					samplein = fs.open(samplepath);
				} else {
					System.err.println("result file not found:" + sourcepath
							+ " or " + samplepath);
					return false;
				}

				CRC32 sourcecrc, samplecrc;
				samplecrc = new CRC32();
				sourcecrc = new CRC32();
				byte[] bufin = new byte[1 << 16];
				int readnum = 0;
				while (samplein.available() > 0) {
					readnum = samplein.read(bufin);
					samplecrc.update(bufin, 0, readnum);
				}
				while (sourcein.available() > 0) {
					readnum = sourcein.read(bufin);
					sourcecrc.update(bufin, 0, readnum);
				}

				if (samplecrc.getValue() == sourcecrc.getValue())
					;
				else {
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new Exception("verify exception :", e);
			} finally {

				try {
					if (samplein != null)
						samplein.close();
					if (sourcein != null)
						sourcein.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return true;
	}

	public static void main(String[] args) {
	}
}
