package proj2.main.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Node {
	private String nodeIDPair;
	private String nodeID;
	private String blockID;
	private Double pageRank;
	private int degree;
	private Double emittedPageRank = 0.0;
	private String[] neighbors;
	private int inDegree;  // degree for incoming edges


	public Node(String value) throws IOException {
		String[] tokens = value.toString().split("\\s+");
		if (tokens.length < 2) {
			throw new IOException("Missing node ID or PageRank in the input for node " + value);
		}
		
		nodeIDPair = tokens[0];
		nodeID = tokens[0].split("-")[0];
		blockID = tokens[0].split("-")[1];
		pageRank = new Double(tokens[1]);
		degree = tokens.length - 2;
		neighbors = Arrays.copyOfRange(tokens, 2, tokens.length);
		
		if (degree > 0) emittedPageRank = pageRank / degree;
	}
	
	// format: PR nodeID-blockID pageRank destNodeID-blockID destNodeID-blockID ...
	public Node(String[] tokensPR) {
		nodeIDPair = tokensPR[1];
		nodeID = tokensPR[1].split("-")[0];
		blockID = tokensPR[1].split("-")[1];
		pageRank = new Double(tokensPR[2]);
		degree = tokensPR.length - 3;
		neighbors = Arrays.copyOfRange(tokensPR, 3, tokensPR.length);
		
		if (degree > 0) emittedPageRank = pageRank / degree;
	}
	
	public Boolean hasEdges() {
		return degree > 0;
	}
	
	public static String getBlockID(String edge) {
		return edge.split("-")[1];
	}

	public String getNodeIDPair() {
		return nodeIDPair;
	}

	public void setNodeIDPair(String nodeIDPair) {
		this.nodeIDPair = nodeIDPair;
	}
	
	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getBlockID() {
		return blockID;
	}

	public void setBlockID(String blockID) {
		this.blockID = blockID;
	}

	public Double getPageRank() {
		return pageRank;
	}

	public void setPageRank(Double pageRank) {
		this.pageRank = pageRank;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public int getInDegree() {
		return inDegree;
	}

	public void setInDegree(int inDegree) {
		this.inDegree = inDegree;
	}

	public Double getEmittedPageRank() {
		return emittedPageRank;
	}

	public void setEmittedPageRank(Double emittedPageRank) {
		this.emittedPageRank = emittedPageRank;
	}

	public String[] getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(String[] neighbors) {
		this.neighbors = neighbors;
	}
	
	public ArrayList<String> getNeighborsList() {
		return new ArrayList<String>(Arrays.asList(neighbors));
	}
	
	public void setNeighborsList(ArrayList<String> neighbors) {
		this.neighbors = neighbors.toArray(this.neighbors);
	}
	
	public String neighborsToString() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < neighbors.length; i++) {
			res.append(neighbors[i]);
			if (i < neighbors.length - 1) {
				res.append(" ");
			}
		}
		return res.toString();
	}
}
