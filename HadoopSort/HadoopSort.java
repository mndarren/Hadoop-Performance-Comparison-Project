/**
 * @Purpose: Sorting integers in Hadoop cluster for paper lab experiment
 * @Author:Zhao Xie
 * @Time:9/30/2015
 * @File:HadoopSort.java
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class HadoopSort {
    /**
     * @Purpose: Decompose input data, and map them into DFS.
     *           input key/value type is LongWritable/Text, output key/value type
     *           is IntWritable/Text. Mapper reads value as Text, and the input value
     *           will be the output key, which is IntWritable type. Actually we don't
     *           input key and output value.
     * @problem: Fixed one space printed per line;
	 *           still too slow, try to read directly IntWritable for input value on map
     */
	public static class TokenizerMapper
       extends Mapper<LongWritable, Text, IntWritable, NullWritable>{
    private final static NullWritable one = NullWritable.get();
    private IntWritable num = new IntWritable();

    public void map(LongWritable key, Text value, Context context
                    ) throws IOException, InterruptedException {
        String line = value.toString();  //every time read one line from data file
		//if(line.trim().length() != 0){  //avoid empty line lead to error
		   int temp = Integer.parseInt(line); //convert String to int
		   num.set(temp);   //only accept int, not Integer type
           context.write(num, one); //always data in hadoop is key/value pair format
		//}
    }
  }
  /**
   * @Purpose: Combine the output of mapper into result as expected
   *           input and output key/value type are same. Facts prove that
   *           input value can be IntWritable or Text, both of which work.
   *
   */
  public static class IntSumReducer
       extends Reducer<IntWritable,NullWritable,IntWritable,NullWritable> {
    private final static NullWritable result = NullWritable.get();

    public void reduce(IntWritable key, Iterable<NullWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      //int sum = 0;
      for (NullWritable val : values) {
        //sum += val.get();
		context.write(key, result);
      }
      // cannot use for(int i;i<sum;i++) loop to control the write operation, not work
      // cannot use for(Text val : values) second time to do that, not work
    }
  }

  public static void main(String[] args) throws Exception {
    long startTime = System.currentTimeMillis();
	long stopTime;
    FileWriter write = new FileWriter(new File("HSTimeLog"));
    Configuration conf = new Configuration();
	conf.setBoolean(Job.MAP_OUTPUT_COMPRESS,true);
	conf.setClass(Job.MAP_OUTPUT_COMPRESS_CODEC,GzipCodec.class,CompressionCodec.class);
    Job job = Job.getInstance(conf, "hadoop sort");
	boolean status;
    try{
		
        job.setJarByClass(HadoopSort.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
	    job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(IntWritable.class);
	    //job.setOutputValueClass(Text.class); 
	    //the method sets the output value for mapper and reduser
	    //job.setMapOutputKeyClass(IntWritable.class); can only be sued to set Map
	    //job.setMapOutputValueClass(IntWritable.class);
	    
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
  } catch(Exception e){
	  write.write("\nError Happened. msg: " + e.getMessage() + "\n\n");
  }finally{
	  status = job.waitForCompletion(true);
	  stopTime = System.currentTimeMillis();
	  long totalTime = stopTime - startTime;
	  write.write(  "\n" + "start time: " + startTime + "\n" +
	                "\n" + "stop time: " + stopTime + "\n" +
	                "\n" + "Program execution time is: "+ totalTime + " ms;\n\n");
	  write.close();
  }
	System.exit(status?0:1);
 }
}

