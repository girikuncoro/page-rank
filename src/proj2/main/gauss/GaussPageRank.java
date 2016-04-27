package proj2.main.gauss;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import proj2.main.util.Constants;

/**
 * Runner for Blocked Computation Page Rank
 * MapReduce jobs will be run until convergence
 * It shouldn't take than more than 7 passes to converge, faster than SimplePageRank
 */
public class GaussPageRank {

	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException{
		if (args.length != 2){
			throw new IllegalArgumentException("Please specify the input and output path"); 
		}

		String inputPath = args[0];
		String outputPath = args[1];
		
		int passNum = 0;
		double avgResidual = Double.MAX_VALUE;
		long totalIteration = 0;
		double avgIteration = 0.0;
		
		// run map reduce pass until convergence
		do {
			// create a new job, with unique jobname
			Configuration conf = new Configuration();
			Job job = Job.getInstance(conf, "BlockedPageRank " + passNum);

			// specify various job-specific parameters
			job.setJarByClass(GaussPageRank.class);
			job.setMapperClass(GaussMapper.class);
			job.setReducerClass(GaussReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			
			// use preprocessed input file in the first pass
			// but use output of previous job if not
			if (passNum == 0){
			    FileInputFormat.addInputPath(job, new Path(inputPath));
		    } else{
			    FileInputFormat.addInputPath(job, new Path(outputPath + "/iteration" + (passNum-1)));
		    }
			FileOutputFormat.setOutputPath(job, new Path(outputPath + "/iteration" + passNum));
			
			// wait current job to finish before starts next pass
			job.waitForCompletion(true);
			
		    avgResidual = (double)job.getCounters().findCounter(Constants.BlockedCounterEnum.BLOCKED_RESIDUAL).getValue() / Constants.PRECISION_FACTOR;
		    avgResidual /= Constants.BLOCK_NUM;
		    
		    totalIteration = job.getCounters().findCounter(Constants.BlockedCounterEnum.N_ITERATION).getValue();
		    avgIteration = totalIteration * 1.0 / Constants.BLOCK_NUM;
		    
		    // Iteration 0 average error 2.332958e+00
		    System.out.println("Iteration " + passNum + " average error " + avgResidual);
		    // Iteration 0 average iteration per block 4.222
		    System.out.println("Iteration " + passNum + " average iteration per block " + avgIteration);
		    passNum++;
			
		} while (avgResidual > Constants.CONVERGENCE);
	}
}
