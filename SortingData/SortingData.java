/**
 * @Purpose: This class will execute internal sort for single
 *           machine with data file size less than memory size
 * @Author: Zhao Xie
 * @Date: 9/1/2015
 * @File: SortingData.java
 */

import java.io.*;
import java.util.*;
import java.util.Scanner;

public class SortingData {
	private static final String FILENAME = "file";
	private static final int NUMOFFILES = 1;
	private static final int NUMSPERFILE = 150000000;
	//private static final int NUMSPERROW = 20;
	private static final long MAXFILESIZE = 2684354560L;//2.5G
	
	public static void main(String[] args) throws FileNotFoundException{
		long programStart = System.currentTimeMillis();
		int count = 0;
		long readTime = 0, sortTime = 0,writeTime = 0,programTime;
		
		try{
		  while(count<NUMOFFILES){
			  /* reader data from file into array */
			  long readStart = System.currentTimeMillis();
			  String newFileName = FILENAME + count;
			  //check file size
			  File f = new File(newFileName);
			  long fileSize = f.length();
			  if (fileSize<MAXFILESIZE){
				 int[] A = new int[NUMSPERFILE];
		         Scanner input = new Scanner(new FileReader(newFileName));
		         for(int i=0;i<NUMSPERFILE;i++){
		    	    A[i] = input.nextInt();
		         }
		         input.close();
		         readTime += System.currentTimeMillis() - readStart;
		         /* sort the array */
		         long sortStart = System.currentTimeMillis();
		         QuickSort qs = new QuickSort();
			     qs.quickSort(A);
			     sortTime += System.currentTimeMillis() - sortStart;
			     /* write back the file */
			     long writeStart = System.currentTimeMillis();
			     File myFile = new File(newFileName);
			     FileWriter write = new FileWriter(myFile);
			     write.write("");
			     for(int i=0;i<NUMSPERFILE;i++){
                    //if(i%NUMSPERROW==0) {write.append("\n");}
				      write.append(A[i] + "\n");
                 }
			     write.append("\n");
			     write.flush();
                 write.close();
                 writeTime += System.currentTimeMillis() - writeStart;
			  }else{
				  System.out.println("Please use External Sort!");
				  return;
			  }
			  count++;
		  }
		  programTime = System.currentTimeMillis() - programStart;
		  /* write a time log file */
		  FileWriter write = new FileWriter(new File("timeLog"));
		  write.write("\n" + "Program execution time is: "+ programTime + 
				      " ms;\nRead data time is: "+readTime +
		  		      " ms;\nSort data time is: " +sortTime +
		  		      " ms;\nWrite data back time is: "+writeTime+
		  		      " ms.\n\n");
		  write.flush();
	      write.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
