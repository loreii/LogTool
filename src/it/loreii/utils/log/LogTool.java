package it.loreii.utils.log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.Closeables;

public class LogTool {
	private static final String HEADER = "File,sys.out,LOC,sys.err,trace,debug,log,info,error\n";
	static BufferedWriter writer;
	static int counter = 0;
	public static void main(String[] args) throws IOException {

		if (args.length < 1) {
			System.err.println("use : java LogTool dir1 dir2 ..");
			System.exit(-1);
		}
		
		Long l = System.currentTimeMillis();
		try {
			Path result = Files.createTempFile(null, ".log");
			writer = Files.newBufferedWriter(result, Charset.forName("US-ASCII"));
			System.err.println("save to : "+result);
			//System.out.print(HEADER);
			writer.write(HEADER);
			
			for (int i = 0; i < args.length; ++i) {
				File f = new File(args[i]);
				iterate(f);
			}
			writer.flush();
		
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			Closeables.close(writer,true);
		}
		
		System.err.println(System.currentTimeMillis()-l+"ms x "+counter+" files");
	}

	private static void iterate(File f) throws FileNotFoundException {
		if (f.isDirectory()) {
			File[] list = f.listFiles();
			for (File c : list) {
				iterate(c);
			}
		} else {
			handleSourceFile(f);
		}
	}

	private static void handleSourceFile(File f) {
		try {
			if (!f.getName().endsWith("java")) {
				return;
			}
			++counter;
			FileInputStream fis = new FileInputStream(f);
			FileChannel inChannel = fis.getChannel();
			long fileSize = inChannel.size();
			ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
			inChannel.read(buffer);
			// buffer.rewind();
			buffer.flip();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < fileSize; i++) {
				sb.append((char) buffer.get());
			}
			int[] levels = { 0, 0, 0, 0, 0, 0, 0 };
			String[] lines = sb.toString().split("\\n");
			int loc=0;
			for (String l : lines) {
				matchLevel(l, "System.out", levels, 0);
				matchLevel(l, "System.err", levels, 1);
				matchLevel(l, ".trace", levels, 2);
				matchLevel(l, ".debug", levels, 3);
				matchLevel(l, ".info", levels, 4);
				matchLevel(l, ".warn", levels, 5);
				matchLevel(l, ".error", levels, 6);
				++loc;
			}
			String s = String.format("%s,%d,%d,%d,%d,%d,%d,%d,%d\n", f.getAbsolutePath(),loc, levels[0], levels[1], levels[2],
					levels[3], levels[4], levels[5], levels[6]);
			//System.out.print(s);
			writer.write(s, 0, s.length());
			inChannel.close();
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void matchLevel(String string, String gate, int[] levels, int i) {
		if (string.contains(gate)) {
			levels[i]++;
		}

	}

}
