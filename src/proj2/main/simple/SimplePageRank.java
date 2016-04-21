package proj2.main.simple;

import java.io.IOException;

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
		    Job job = Job.getInstance();
		    
		    // Specify various job-specific parameters
		    job.setJarByClass(SimplePageRank.class);
		    job.setJobName("Simple Page Rank");	
		    
		    job.setMapperClass(SimpleMapper.class);
		    job.setReducerClass(SimpleReducer.class);
		    
		    job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(Text.class);

		    FileInputFormat.addInputPath(job, new Path(inputPath));
		    FileOutputFormat.setOutputPath(job, new Path(outputPath));
		    job.waitForCompletion(true);
		}
	}
}
