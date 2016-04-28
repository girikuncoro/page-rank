package proj2.main.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;

import proj2.main.util.Constants;

public class TopologicalSort {
	
//	public static ArrayList<Node> sort(Map<String, Node> nodeMap) {
//		Queue<Node> q = new LinkedList<Node>();
//		ArrayList<Node> res = new ArrayList<Node>();
//		
//		for (Entry<String, Node> e : nodeMap.entrySet()) {
//			Node node = e.getValue();
//			
//			if (node.getDegree() == 0) {
//				q.add(node);
//				res.add(node);
//			}
//		}
//		
//		while (!q.isEmpty()) {
//			Node n = q.poll();
//			
//			for (String neighID : n.getNeighbors()) {
//				Node neighbor = nodeMap.get(neighID);
//				neighbor.setDegree(neighbor.getDegree()-1);
//				if (neighbor.getDegree() == 0) {
//					q.add(neighbor);
//					res.add(neighbor);
//				}
//			}
//		}
//		
//		return res;
//	}
	
	public static ArrayList<Node> sort(Map<String, Node> nodeMap) {
		ArrayList<Node> res = new ArrayList<Node>();
		HashSet<Node> noIncoming = new HashSet<Node>();
		int minInDegree = Integer.MAX_VALUE;
		
		for (Entry<String, Node> e : nodeMap.entrySet()) {
			Node node = e.getValue();
			if (node.getInDegree() < minInDegree) {
				minInDegree = node.getInDegree();
			}
		}
		
		for (Entry<String, Node> e : nodeMap.entrySet()) {
			Node node = e.getValue();
			if (node.getInDegree() == minInDegree) {
				noIncoming.add(node);
			}
		}
		
		while (!noIncoming.isEmpty()) {
			// remove node from hashSet
			Node n = noIncoming.iterator().next();
			noIncoming.remove(n);
			res.add(n);
			
			for (Iterator<String> it=n.getNeighborsList().iterator(); it.hasNext();) {
				String neighIDPair = it.next();
				Node m = nodeMap.get(neighIDPair);
				it.remove();  
				
				// remove m from n
				ArrayList<String> nNeigh = n.getNeighborsList();
				nNeigh.remove(m.getNodeIDPair());
				n.setNeighborsList(nNeigh);
				n.setInDegree(n.getInDegree()-1);
				
				// remove n from m
				ArrayList<String> mNeigh = m.getNeighborsList();
				mNeigh.remove(n.getNodeIDPair());
				m.setNeighborsList(mNeigh);
				m.setInDegree(m.getInDegree()-1);
				
				// if m has no other incoming edges, then insert to noIncoming set
				if (m.getInDegree() == 0) {
					noIncoming.add(m);
				}
			}
		}
		
		return res;
	}
}
