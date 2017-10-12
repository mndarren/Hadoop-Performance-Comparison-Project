/**
 * @Purpose: This class will generate file(s) by which 
 *           integers can be generated with specific 
 *           numbers and specific files
 * @Author: Zhao Xie
 * @Date: 8/27/2015
 * @File: CreateData.java
 */
import java.io.*;
import java.util.*;

public class CreateData{
	private static final String FILENAME = "file"; //file name prefix
	private static final int NUMOFFILES = 1;  //number of files
	private static final long NUMSPERFILE = 100000000L;
	private static final int MAXVALUE = 100000000;
	
	public static void main(String[] args) {
		long writeStart = System.currentTimeMillis();
		int count = 0;
		try{
			/* produce data files */
			while(count < NUMOFFILES) {
				String newFileName = FILENAME + count;
			    File myFile = new File(newFileName);
				Random randomGenerator = new Random();
				FileWriter write = new FileWriter(myFile);
				write.write("");
				long n = NUMSPERFILE-1;
				for(long i=0; i<n; i++) {
				   write.append(randomGenerator.nextInt(MAXVALUE) + "\n");
                }
				//confirm no empty line
				write.append(randomGenerator.nextInt(MAXVALUE) + ""); 
				write.flush();
                write.close();
				count++;
				}
			long writeTime = System.currentTimeMillis() - writeStart;
			/* write a log file */
			FileWriter write = new FileWriter(new File("dataLog"));
			write.write("\n"+count + " file(s) produced;\n" + NUMSPERFILE 
					    + " integers per file;\n" + MAXVALUE 
					    + " is the max value;\n" + (new Date()).toString() 
					    + "write time = " + writeTime +"\n\n");
			write.flush();
            write.close();
		}catch(IOException e){
				e.printStackTrace();
		}
	}
} 
