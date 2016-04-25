package proj2.main.blocked;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import proj2.main.node.Node;
import proj2.main.util.Constants;

/**
 * Reducer class for Blocked Computation Page Rank
 * In the end of process, reducer should emit at least:
 *   (1) Updated pageRank for every node of the block
 *	 (2) Compute residuals for overall termination test
 */
public class BlockedReducer extends Reducer<Text, Text, Text, Text> {
	
	private Map<String, Node> nodeMap = new HashMap<String, Node>();  // node stored with oldPageRank
	private Map<String, Double> NPR = new HashMap<String, Double>();  // next PageRank value of Node v for v ∈ B
	private Map<String, ArrayList<String>> BE = new HashMap<String, ArrayList<String>>();  // <u, v> ∈ BE, the Edges from Nodes in Block B
	private Map<String, Double> BC = new HashMap<String, Double>();  // <u,v,R> ∈ BC, the Boundary Conditions
	
	Double currResidual = Double.MAX_VALUE;
	
	public void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		
		resetDataStructure();
		
		String[] tokens;
		Iterator<Text> iter = values.iterator();
		while (iter.hasNext()) {
			tokens = iter.next().toString().split("\\s+");
			
			if (tokens[0] == "PR") {
				// the node structure with oldPageRank
				processNode(tokens);
			} else if (tokens[0] == "BE") {
				// edge in the block
				processBlockEdge(tokens);
			} else if (tokens[0] == "BC") {
				// incoming edge from outside of the block
				processBoundaryCond(tokens);
			} else {
				throw new IOException("Invalid input from the mapper, should be PR, BE, or BC");
			}
		}
		
		// repeatedly updates PR[v] for every v ∈ B 
		// until "in-block residual" is below threshold or it reaches N iteration
		int iterNum = 0;
		do {
			currResidual = iterateBlockOnce();
			iterNum++;
		} while (iterNum < Constants.INBLOCK_MAX_ITERATION && currResidual > Constants.CONVERGENCE);
		
		
		// emit updated pageRank for every node
		// format: nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID
		for (Entry<String, Node> n : nodeMap.entrySet()) {
			Node node = n.getValue();
			String nodeIDPair = node.getNodeIDPair();
			Text outKey = new Text(nodeIDPair);
			Text outValue = new Text(NPR.get(nodeIDPair).toString() + " " + node.neighborsToString());
			
			// hadoop will concat key to value with spaces
			context.write(outKey, outValue);  
			
			// TODO: output the first/last two nodes for each block
		}
		
		// total change computed by reduce task
		// should report the sum over all nodes v in its Block, divided by B
		double averageResidual = calculateAvgBlockResidual() * Constants.PRECISION_FACTOR;
		Counter counter = context.getCounter(Constants.BlockedCounterEnum.BLOCKED_RESIDUAL);
		counter.increment((long) averageResidual);
	}
	
	// format: PR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID ...
	public void processNode(String[] tokens) {
		Node node = new Node(tokens);
		nodeMap.put(node.getNodeIDPair(), node);  // oldPageRank stored here
		NPR.put(node.getNodeIDPair(), node.getPageRank());  // pageRank here will be updated in iteration
		
		// TODO: keep track of last/first nodeID in this block
	}
	
	// format: BE nodeID-blockID destNodeID-blockID
	public void processBlockEdge(String[] tokens) {
		// all incoming edges should be collected
		ArrayList<String> incomingEdges = new ArrayList<String>();
		String nodeIDPair = tokens[1];
		String destIDPair = tokens[2];
		
		if (BE.containsKey(destIDPair)) {
			incomingEdges = BE.get(destIDPair);
		}
		
		incomingEdges.add(nodeIDPair);
		BE.put(destIDPair, incomingEdges);
	}
	
	// format: BC nodeID-blockID emitPageRank destNodeID-blockID
	public void processBoundaryCond(String[] tokens) {
		Double currPR = new Double(0.0);
		Double emitPR = Double.parseDouble(tokens[2]);  // v ∧ R = PR(u)/deg(u)
		String destIDPair = tokens[3];
		
		if (BC.containsKey(destIDPair)) {
			currPR = BC.get(destIDPair);
		}
		
		currPR += emitPR;
		BC.put(destIDPair, currPR);
	}
	
	public void resetDataStructure() {
		nodeMap.clear();
		NPR.clear();
		BE.clear();
		BC.clear();
		currResidual = Double.MAX_VALUE;
	}
	
	/**
	 * TODO:
	 * void IterateBlockOnce(B) {
		    for( v ∈ B ) { NPR[v] = 0; }
		    for( v ∈ B ) {
		        for( u where <u, v> ∈ BE ) {
		            NPR[v] += PR[u] / deg(u);
		        }
		        for( u, R where <u,v,R> ∈ BC ) {
		            NPR[v] += R;
		        }
		        NPR[v] = d*NPR[v] + (1-d)/N;
		    }
		    for( v ∈ B ) { PR[v] = NPR[v]; }
		}
	 */
	public double iterateBlockOnce() {
		return new Double(0.0);
	}
	
	/**
	 * Compute average residual of this block
	 * Residual for one node is |(PRstart(v) - PRend(v))| / PRend(v)
	 * @return totalResidual/totalNodes  
	 */
	public double calculateAvgBlockResidual() {
		Double res = new Double(0.0);
		
		for (Entry<String, Node> n : nodeMap.entrySet()) {
			String nodeIDPair = n.getKey();
			Node node = n.getValue();
			res += Math.abs(node.getPageRank() - NPR.get(nodeIDPair)) / NPR.get(nodeIDPair);
		}
		
		return res / nodeMap.size();
	}
}
