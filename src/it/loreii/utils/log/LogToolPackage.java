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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Closeables;

public class LogToolPackage {
	private static final String HEADER = "Class,Occurences\n";
	static BufferedWriter writer;
	static int counter = 0;
	static int loc = 0;
	static HashMap<String, Integer> occurenceMap = new HashMap<>();
	private static String namespace;
	
	public static void main(String[] args) throws IOException {

		if (args.length < 2) {
			
			System.err.println("grep from log file the occurences of logs per class containing the specified prefix\n"
					+ "ie com.example.x.y.z.A");
			System.err.println("use : java LogToolPackage com.example dir1 dir2 ..");
			
			System.exit(-1);
		}

		Long l = System.currentTimeMillis();
		try {
			Path result = Files.createTempFile(null, ".log");
			writer = Files.newBufferedWriter(result, Charset.forName("US-ASCII"));
			System.err.println("save to : " + result);
			// System.out.print(HEADER);
			writer.write(HEADER);
			namespace=args[0];
			for (int i = 1; i < args.length; ++i) {
				File f = new File(args[i]);
				iterate(f);
			}
			
			for (Entry<String, Integer> e : occurenceMap.entrySet()) {
				String s = String.format("%s,%d\n", e.getKey(),e.getValue());
				// System.out.print(s);
				writer.write(s, 0, s.length());
			}
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Closeables.close(writer, true);
		}

		System.err.println(System.currentTimeMillis() - l + "ms x " + counter + " files "+"lines "+loc);
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
			
			String[] lines = sb.toString().split("\\n");
			
			for (String l : lines) {
				analyze(l,occurenceMap);
				++loc;
			}
		
			inChannel.close();
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private static void analyze(String string, HashMap<String, Integer> occurenceMap) {
		if (string.contains(namespace)) {
			 Pattern pattern = Pattern.compile("("+namespace+".([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*)");
			    // in case you would like to ignore case sensitivity,
			    // you could use this statement:
			    // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
			    Matcher matcher = pattern.matcher(string);
			    // check all occurance
			    while (matcher.find()) {
			      String key=matcher.group();
			      Integer acc = occurenceMap.get(key);
			      acc=acc!=null?++acc:0;
			      occurenceMap.put(key, acc);
			    }
		}

	}

}
