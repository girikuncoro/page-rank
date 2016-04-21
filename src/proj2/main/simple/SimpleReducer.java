package proj2.main.simple;

import java.io.IOException;
import java.text.DecimalFormat;
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
		String ID = "";
		while (iter.hasNext()){
			tokens = iter.next().toString().split("\\s+");
			// Format: pageRank
			if (tokens.length == 1 && !tokens[0].equals("prevPR")){
				newPageRank += Double.parseDouble(tokens[0]);
			}else{
				// Format: prevPR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID......
				oldPageRank = Double.parseDouble(tokens[2]);
				ID = tokens[1];
				for (int i = 3; i < tokens.length; i++){
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
		// Format: "nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID......"
//		context.write(key, new Text(ID + " " + newPageRank.toString() + " " + outEdges.toString()));
		Text newValue = new Text(newPageRank.toString() + " " + outEdges.toString());
		context.write(new Text(ID), newValue);
		
		double residual = Math.abs(oldPageRank - newPageRank) / newPageRank; 
		DecimalFormat df = new DecimalFormat("#0.0000");
		df.format(residual);
		Counter counter = context.getCounter(Constants.SimpleCounterEnum.SIMPLE_RESIDUAL);
		counter.increment((long) residual);
	}
}
