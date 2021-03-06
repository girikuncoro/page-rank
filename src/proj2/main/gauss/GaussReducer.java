package proj2.main.gauss;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import proj2.main.node.Node;
import proj2.main.node.TopologicalSort;
import proj2.main.util.Constants;

/**
 * Reducer class for Gauss-Seidel Computation Page Rank
 * In the end of process, reducer should emit at least:
 *   (1) Updated pageRank for every node of the block
 *	 (2) Compute residuals for overall termination test
 */
public class GaussReducer extends Reducer<Text, Text, Text, Text> {

	static private Map<String, Node> nodeMap = new HashMap<String, Node>();  // node stored with oldPageRank
	static private Map<String, Double> NPR = new HashMap<String, Double>();  // next PageRank value of Node v for v ∈ B
	static private Map<String, Set<String>> BE = new HashMap<String, Set<String>>();  // <u, v> ∈ BE, the Edges from Nodes in Block B
	static private Map<String, Double> BC = new HashMap<String, Double>();  // <u,v,R> ∈ BC, the Boundary Conditions

	Double currAvgResidual = Double.MAX_VALUE;
	Node lowestNode = null;

	public void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {

		resetDataStructure();

		String[] tokens;
		Iterator<Text> iter = values.iterator();
		while (iter.hasNext()) {
			tokens = iter.next().toString().split("\\s+");

			if (tokens[0].equals("PR")) {
				// the node structure with oldPageRank
				processNode(tokens);
			} else if (tokens[0].equals("BE")) {
				// edge in the block
				processBlockEdge(tokens);
			} else if (tokens[0].equals("BC")) {
				// incoming edge from outside of the block
				processBoundaryCond(tokens);
			} else {
				throw new IOException("Invalid input from the mapper, should be PR, BE, or BC");
			}
		}

		for(Entry<String, Set<String>> entry : BE.entrySet())
			nodeMap.get(entry.getKey()).setIndegreeWithinBlock(entry.getValue());
		
//		boolean flag = context.getCounter(Constants.BlockedCounterEnum.TOPO_FLAG).getValue() == 0l;
//		List<Node> sorted = TopologicalSort.sort(nodeMap, flag);
		
		// Diasble topological sort because it doesn't perform well
		List<Node> sorted = TopologicalSort.sort(nodeMap, false);

		// repeatedly updates PR[v] for every v ∈ B 
		// until "in-block residual" is below threshold or it reaches N iteration
		int iterNum = 0;
		do {
			currAvgResidual = iterateBlockOnce(sorted);
			iterNum++;
		} while (iterNum < Constants.INBLOCK_MAX_ITERATION && currAvgResidual > Constants.CONVERGENCE);

		// emit updated pageRank for every node
		// format: nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID
		for (Entry<String, Node> n : nodeMap.entrySet()) {
			Node node = n.getValue();
			String nodeIDPair = node.getNodeIDPair();
			Text outKey = new Text(nodeIDPair);
			Text outValue = new Text(NPR.get(nodeIDPair).toString() + " " + node.neighborsToString());

			// hadoop will concat key to value with spaces
			context.write(outKey, outValue);  
		}

		// total change computed by reduce task
		// should report the sum over all nodes v in its Block, divided by B
		double averageResidual = calculateAvgBlockResidual() * Constants.PRECISION_FACTOR;
		Counter counter = context.getCounter(Constants.BlockedCounterEnum.BLOCKED_RESIDUAL);
		counter.increment((long) averageResidual);

		// should report average number of iterations per block
		Counter iterCounter = context.getCounter(Constants.BlockedCounterEnum.N_ITERATION);
		iterCounter.increment((long) iterNum);

		// show page rank value for two lowest-numbered nodes
		System.out.println("Block " + lowestNode.getBlockID() + 
				" node " + lowestNode.getNodeID() + 
				" pageRank: " + NPR.get(lowestNode.getNodeIDPair()));

		String nextLowestNodeID = "" + (Integer.parseInt(lowestNode.getNodeID()) + 1);
		System.out.println("Block " + lowestNode.getBlockID() + 
				" node " + nextLowestNodeID + 
				" pageRank: " + NPR.get(nextLowestNodeID + "-" + lowestNode.getBlockID()));

	}

	public void resetDataStructure() {
		nodeMap.clear();
		NPR.clear();
		BE.clear();
		BC.clear();
		currAvgResidual = Double.MAX_VALUE;
		lowestNode = null;
	}

	/**
	 * Process the node and populate data structure
	 * format: PR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID ...
	 * @param splitted tokens
	 */
	public void processNode(String[] tokens) {
		Node node = new Node(tokens);
		nodeMap.put(node.getNodeIDPair(), node);  // oldPageRank stored here
		NPR.put(node.getNodeIDPair(), node.getPageRank());  // pageRank here will be updated in iteration

		// keep track the lowest numbered node in this block
		if(lowestNode == null || lowestNode.getNodeID().compareTo(node.getNodeID()) > 0) {
			lowestNode = node; 
		} 
	}

	/**
	 * Process the in-block neighbors coming to this node
	 * all incoming edges should be collected
	 * format: BE nodeID-blockID destNodeID-blockID
	 * @param splitted tokens
	 */
	public void processBlockEdge(String[] tokens) {
		Set<String> incomingEdges = new HashSet<String>();
		String nodeIDPair = tokens[1];
		String destIDPair = tokens[2];

		if (BE.containsKey(destIDPair)) {
			incomingEdges = BE.get(destIDPair);
		}

		incomingEdges.add(nodeIDPair);
		BE.put(destIDPair, incomingEdges);
	}

	/**
	 * Process the boundary condition, sum up total pageRank coming from outside the block
	 * the incoming pageRank is already divided over degree
	 * format: BC nodeID-blockID emitPageRank destNodeID-blockID
	 * @param splitted tokens
	 */
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

	/**
	 * Updates PR[v] for every v ∈ B to reflect a single iteration of the PageRank defining equation
	 * compute the “in-Block residual,” the residual error for the last call to IterateBlockOnce
	 * 
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
	 * Instead of updating PR in the end as above pseudocode, Gauss-Seidel needs to get 
	 * most updated PR as possible, so change all PR to NPR and update within one for loop
	 * 
	 * v: all nodes in block B; u: all neighbors coming to v
	 * BE contains u inside block B
	 * BC contains u outside block B 
	 * @return average residual (total changes/total nodes in B) for current iteration
	 */
	public double iterateBlockOnce(List<Node> sorted) {
		String nodeIDPair;
		Double residual = new Double(0.0);

//		for (Entry<String, Node> n : nodeMap.entrySet()) {
		for (Node n : sorted) {
//			nodeIDPair = n.getKey();
			nodeIDPair = n.getNodeIDPair();
			Double prevPR = NPR.get(nodeIDPair);
			Double newPR = new Double(0.0);

			if (BE.containsKey(nodeIDPair)) {
				for (String incomingIDPair : BE.get(nodeIDPair)) {
					Node incomingNode = nodeMap.get(incomingIDPair);
					newPR += NPR.get(incomingIDPair) / incomingNode.getDegree();
				}
			}

			if (BC.containsKey(nodeIDPair)) {
				// BC already contains summed pageRank from outside block, no need to iterate
				newPR += BC.get(nodeIDPair);
			}

			newPR = Constants.DAMPING_FACTOR * newPR + 
					(1 - Constants.DAMPING_FACTOR) / Constants.NODE_NUM;
			NPR.put(nodeIDPair, newPR);

			residual += Math.abs(prevPR - newPR) / newPR;
		}

		return residual / nodeMap.size();
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
			res += Math.abs(node.getPageRank() - NPR.get(nodeIDPair)) / NPR.get(nodeIDPair);  // (PRstart - PRend) / PRend
		}

		return res / nodeMap.size();
	}
}
