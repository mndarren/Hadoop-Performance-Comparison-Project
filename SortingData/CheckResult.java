/**
 * @Purpose This class will check if the sorted file is correct.
 *          Check the file name before executing.
 * @Author Zhao Xie
 * @Date 9/19/2015
 * @File CheckResult.java
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CheckResult {
     private static BufferedReader br;
     private static String inputFile = "file0";

	public static void main(String[] args) throws IOException {
    	 long startCheck = System.currentTimeMillis();
    	 br = new BufferedReader(new FileReader(inputFile));
    	 String line = br.readLine();
    	 int small=0, big;
    	 int numCount = 0;
    	 
    	 while(line!=null){
    		 if(line.trim().length() == 0) continue;
    		 big = Integer.parseInt(line);
    		 if(big<small){
    			 System.out.println("The result of sorting is error!");
    			 return;
    		 }
    		 small = big;
    		 line = br.readLine();
    		 numCount++;
    	 }
    	 br.close();
    	 System.out.println("Correct!");
    	 System.out.println("nums = "+numCount);
    	 long time = System.currentTimeMillis() - startCheck;
    	 System.out.println("Time used = "+time);
     }
}
