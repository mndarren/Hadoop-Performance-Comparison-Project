/**
 * @Purpose This file will execute external sort for single
 *          machine. It contains two classes, and the main 
 *          class is the ExternalSort.
 * @Author Zhao Xie
 * @Time 9/19/2015
 * @File ExternalSort.java
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @Goal: The external merge sort program will be used
 *        when the data size is larger than memory size.
 * @steps: To divide the file into small files;
 *         Sorting the data in the each small file;
 *         Merge the sorted small files.
 */
public class ExternalSort {
	private static long readTime; //record read IO time
	private static long sortTime; //record sort time
	private static long writeTime; //record write IO time
	/**
	 * @Purpose try to find the best temporary file size;
	 *          if too small, creating too many files; if too big, 
	 *          using too much memory.
	 * @param filetobesorted, the original file
	 * @return best block size
	 */
	public static long estimateBestSizeOfBlocks(File originalFile) {
		long sizeOfFile = originalFile.length(); //in bytes
		final int MAXTEMPFILES = 512;
		long blockSize = sizeOfFile / MAXTEMPFILES; //minimum size
		long freeMem = Runtime.getRuntime().freeMemory();//in bytes
		if (blockSize < freeMem / 2)
			blockSize = freeMem / 2;  //to avoid block size too small
		else {
			if (blockSize >= freeMem)
				System.err.println("We expect to run out of memory. ");
		}
		return blockSize;
	}
    /**
     * @Purpose to divide the original file into small files and sort them
     * @param file, some flat files
     * @param cmp, comparator about integer
     * @return a list of temporary files
     * @throws IOException
     */
	public static List<File> sortInBatch(File file, Comparator<Integer> cmp)
			throws IOException {
		List<File> files = new ArrayList<File>();
		BufferedReader fbr = new BufferedReader(new FileReader(file));
		long blockSize = estimateBestSizeOfBlocks(file);// in bytes
		try {
			List<Integer> tmplist = new ArrayList<Integer>();
			String line = "";
			try {
				while (line != null ) {
					long startRead = System.currentTimeMillis(); //start read
					long currentblocksize = 0;// in bytes
					while (currentblocksize < blockSize && 
							((line = fbr.readLine()) != null)) {
						if(line.trim().length() == 0) continue; //avoid space
						tmplist.add(Integer.parseInt(line)); //read string type
						currentblocksize += line.length(); //2B/char+40B overhead
					}
					//accumulate the read time
					readTime += System.currentTimeMillis() - startRead;
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			} catch (EOFException oef) {
				if (tmplist.size() > 0) {
					files.add(sortAndSave(tmplist, cmp));
					tmplist.clear();
				}
			}
		} finally {
			fbr.close();
			//delete the original file to free enough space to save the output
			file.delete();
			System.out.println("The original file is deleted now.");
		}
		return files;
	}
    /**
     * @Purpose to sort small file and save it as temporary file
     * @param tmplist, a list of integer
     * @param cmp, comparator for integer
     * @return sorted file
     * @throws IOException
     */
	public static File sortAndSave(List<Integer> tmplist,Comparator<Integer> cmp)
			      throws IOException {
		Collections.sort(tmplist, cmp); //quick sort is used in java library
		long startWrite = System.currentTimeMillis(); //record write start time
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile");
		newtmpfile.deleteOnExit();
		BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));
		try {
			for (int r : tmplist) {
				fbw.write(String.valueOf(r));
				fbw.write("\n");
			}
		} finally {
			fbw.close();
		}
		writeTime += System.currentTimeMillis() - startWrite;
		return newtmpfile;
	}
    /**
     * @Purpose to merge sorted files into output file, 
     *          all read and write operations are as string type,
     *          but sort data as integer type
     * @param files
     * @param outputfile
     * @param cmp
     * @return number of lines sorted
     * @throws IOException
     */
	public static int mergeSortedFiles(List<File> files, File outputfile,
			                 final Comparator<Integer> cmp) throws IOException {
		System.out.println("Start merging......");
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(
				11, new Comparator<BinaryFileBuffer>() {
					public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
						return cmp.compare(i.peek(), j.peek());
					}
				}); //priority queue is to find the minimum value
		for (File f : files) {
			BinaryFileBuffer bfb = new BinaryFileBuffer(f);
			pq.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
		int rowcounter = 0;
		try {
			while (pq.size() > 0) {
				BinaryFileBuffer bfb = pq.poll();
				Integer r = bfb.pop();
				if (r != null) {
					long startWrite = System.currentTimeMillis(); 
					fbw.write(String.valueOf(r));
					fbw.write("\n");
					writeTime += System.currentTimeMillis() - startWrite;
					++rowcounter;
					if (bfb.empty()) {
						bfb.fbr.close();
						bfb.originalfile.delete();// don't need it anymore
					} else {
						pq.add(bfb); // add it back
					}
				}
			}
		} finally {
			fbw.close();
			for (BinaryFileBuffer bfb : pq)
				bfb.close();
		}
		return rowcounter;
	}

	public static void main(String[] args) throws IOException {
		long startProgram = System.currentTimeMillis();
		String inputfile = "file0";
		String outputfile = inputfile + "output";
		Comparator<Integer> comparator = new Comparator<Integer>() {
			public int compare(Integer r1, Integer r2) {
				return r1.compareTo(r2);
			}
		};
		List<File> l = sortInBatch(new File(inputfile), comparator);
		mergeSortedFiles(l, new File(outputfile), comparator);
		long programTime = System.currentTimeMillis()-startProgram;
		System.out.println("program execute time = " + programTime);
		sortTime = programTime - readTime - writeTime;
		/* write a time log file */
		  FileWriter write = new FileWriter(new File("timeLog"));
		  write.write("\n" + "Program execution time is: "+ programTime + 
				      " ms;\nRead data time is: "+readTime +
		  		      " ms;\nSort data time is: " +sortTime +
		  		      " ms;\nWrite data back time is: "+writeTime+" ms.\n\n");
		  write.flush();
	      write.close();
	}
	/**
	 * @Purpose this inside class is to support the mergeSortedFiles method,
	 *          including a buffer, a file and a buffered reader
	 */
	static class BinaryFileBuffer {
		public  int BUFFERSIZE = 2048;
		public BufferedReader fbr;
		public File originalfile;
		private String cache;
		private boolean empty;

		public BinaryFileBuffer(File f) throws IOException {
			originalfile = f;
			fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
			reload();
		}

		public boolean empty() {
			return empty;
		}
        /* reload data from file to buffer and update the empty variable */
		private void reload() throws IOException {
			try {
				long startRead = System.currentTimeMillis();
				if ((this.cache = fbr.readLine()) == null) {
					empty = true;
					cache = null;
				} else {
					empty = false;
				}
				readTime += System.currentTimeMillis() - startRead;
			} catch (EOFException oef) {
				empty = true;
				cache = null;
			}
		}

		public void close() throws IOException {
			fbr.close();
		}

		public Integer peek() {
			if (empty())
				return null;
			if (cache == null || (cache != null && cache.trim().length() == 0))
				return null;
			return Integer.parseInt(cache);
		}

		public Integer pop() throws IOException {
			Integer answer = peek();
			reload();
			return answer;
		}
	}
}
