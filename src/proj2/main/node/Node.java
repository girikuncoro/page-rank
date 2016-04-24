package proj2.main.node;

import java.util.Arrays;

public class Node {
	private String nodeIDPair;
	private String blockID;
	private Double pageRank;
	private int degree;
	private Double emittedPageRank = 0.0;
	private String[] edgeList;
	
	public Node(String value) {
		String[] tokens = value.toString().split("\\s+");
		nodeIDPair = tokens[0];
		blockID = tokens[0].split("-")[1];
		pageRank = new Double(tokens[1]);
		degree = tokens.length - 2;
		edgeList = Arrays.copyOfRange(tokens, 2, tokens.length);
		
		if (degree > 0) emittedPageRank = pageRank / degree;
	}

	public String getNodeIDPair() {
		return nodeIDPair;
	}

	public void setNodeIDPair(String nodeIDPair) {
		this.nodeIDPair = nodeIDPair;
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

	public Double getEmittedPageRank() {
		return emittedPageRank;
	}

	public void setEmittedPageRank(Double emittedPageRank) {
		this.emittedPageRank = emittedPageRank;
	}

	public String[] getEdgeList() {
		return edgeList;
	}

	public void setEdgeList(String[] edgeList) {
		this.edgeList = edgeList;
	}
	
	
}
