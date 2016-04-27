package proj2.main.simple;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/**
 * A Mapper that distributes the page rank value of the source node to all its destination node 
 */
public class SimpleMapper extends Mapper<LongWritable, Text, Text, Text>{

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		String[] tokens = value.toString().split("\\s+");
		
		// value format: "nodeID-blockID pageRank (destNodeID-blockID destNodeID-blockID......)"
		if (tokens.length >= 2){
			Text ID = new Text(tokens[0]);
			int degree = tokens.length - 2;
			Double pageRank = new Double(tokens[1]);
			
			// produce the page rank of this node itself, so that we can compute the residual
			// in the reducer and retain the outgoing edges. 
			// Format: prevPR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID......
			context.write(ID, new Text("prevPR " + value));
			
			// emit the distributed page rank for every outgoing destination node
			// Format: pageRank
			if (degree > 0){
				Double emittedPageRank = pageRank / degree;
				for (int i = 2; i < tokens.length; i++){
					context.write(new Text(tokens[i]), new Text(emittedPageRank.toString()));
				}
			}
		}else{
			System.out.println("Missing node ID or PageRank in the input for node " + value);
		}
	}
}
