package proj2.main.simple;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import proj2.main.util.Constants;

public class SimplePageRank {

	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException{
		if (args.length != 2){
			throw new IllegalArgumentException("Please specify the input and output path"); 
		}
		
		String inputPath = args[0];
		String outputPath = args[1];
		
		for (int i = 0; i < Constants.SIMPLE_MP_PASS_NUM; i++){
			// create a new job
			Configuration conf = new Configuration();
		    Job job = Job.getInstance(conf, "PageRank " + i);
		    
		    // Specify various job-specific parameters
		    job.setJarByClass(SimplePageRank.class);
		    job.setMapperClass(SimpleMapper.class);
		    job.setReducerClass(SimpleReducer.class);
		    job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(Text.class);

		    if (i == 0){
			    FileInputFormat.addInputPath(job, new Path(inputPath));
		    }else{
			    FileInputFormat.addInputPath(job, new Path(outputPath + "iteration" + (i - 1)));
		    }
		    FileOutputFormat.setOutputPath(job, new Path(outputPath + "/iteration" + i));
		    
		    job.waitForCompletion(true);
		    double residual = (double) job.getCounters().findCounter(Constants.SimpleCounterEnum.SIMPLE_RESIDUAL).getValue();
		    residual /= Constants.NODE_NUM;
		    //Iteration 0 avg error 2.332958e+00
		    System.out.println("Iteration " + i + " average error " + residual);
		}
	}
}
