package proj2.main.simple;

import java.io.IOException;
import java.util.Iterator;

import proj2.main.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

public class SimpleReducer extends Reducer<Text, Text, Text, Text> {
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		Double newPageRank = new Double(0.0);
		Double oldPageRank = new Double(0.0);
		String[] tokens;
		Iterator<Text> iter = values.iterator();
		StringBuilder outEdges = new StringBuilder();
		
		while (iter.hasNext()){
			tokens = iter.next().toString().split("\\s+");
			// Format: pageRank
			if (tokens.length == 1){
				newPageRank += Double.parseDouble(tokens[0]);
			}else{
				// Format: nodeID-blockID pageRank (destNodeID-blockID destNodeID-blockID......)
				oldPageRank = Double.parseDouble(tokens[1]);
				for (int i = 2; i < tokens.length; i++){
					outEdges.append(tokens[i]);
					if (i < tokens.length - 1)
						outEdges.append(" ");
				}
			}
		}
		
		//compute new page rank: (1-d)/N + d * sum (<PRt(u)/degree(u)>)
		newPageRank = Constants.DAMPING_FACTOR * newPageRank + 
				(1 - Constants.DAMPING_FACTOR) / Constants.NODE_NUM;
		
		// same format as the input for mapper
		// Format: "nodeID-blockID, pageRank (destNodeID-blockID destNodeID-blockID......)"
		Text newValue = new Text(newPageRank.toString() + " " + outEdges.toString());
		context.write(key, newValue);
		
		double residual = Math.abs(oldPageRank - newPageRank) / newPageRank * Constants.PRECISION_FACTOR;
		Counter residualCounter = context.getCounter(Constants.SimpleCounterEnum.SIMPLE_RESIDUAL);
		residualCounter.increment((long) residual);
	}
}
