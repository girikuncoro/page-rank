package proj2.main.gauss;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import proj2.main.node.Node;


/**
 * Mapper class for Gauss-Seidel Computation Page Rank
 * The mapper should emit 3 things to reducer:
 *   (1) PR[v] = current PageRank value of Node v for v ∈ B
 *	 (2) BE = { <u, v> | u ∈ B ∧ u → v } = the Edges from Nodes in Block B
 *   (3) BC = { <u, v, R> | u ∉ B ∧ v ∈ B ∧ u → v ∧ R = PR(u)/deg(u) } = the Boundary Conditions
 */
public class GaussMapper extends Mapper<LongWritable, Text, Text, Text> {
	
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {
		
		Node node = new Node(value.toString());
		
		// emit the node structure, PR[v] to compute the residual in reducer
		// format: PR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID ...
		Text mapKey = new Text(node.getBlockID());
		context.write(mapKey, new Text("PR " + value));
		
		if (node.hasEdges()) {
			// emit for every outgoing edge
			// check if in the same block (BE/Block Edge) or different (BC/Boundary Condition)
			for (String m : node.getNeighbors()) {
				String outgoingBlock = Node.getBlockID(m);
				mapKey = new Text(outgoingBlock);
				
				if (outgoingBlock.equals(node.getBlockID())) {
					// neighbor is in the same block as the node, no need to emit pageRank
					// format: BE nodeID-blockID destNodeID-blockID
					context.write(mapKey, new Text("BE " + node.getNodeIDPair() + " " + m));
				} else {
					// boundary condition, outgoing edge is in another block
					// pageRank will be used by the other block
					// format: BC nodeID-blockID emitPageRank destNodeID-blockID
					context.write(mapKey, new Text("BC " + node.getNodeIDPair() + " " + node.getEmittedPageRank() + " " + m));
				}
			}
		}
	}
}
