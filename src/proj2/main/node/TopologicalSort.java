package proj2.main.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
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
	
	public static List<Node> sort2(Map<String, Node> nodeMap) {
		List<Node> appending = new LinkedList<Node>();
		List<Node> prepending = new LinkedList<Node>();
		
		Map<Integer, Set<Node>> indegreeMap = getInvertedIndegrees(nodeMap);
		Map<Integer, Set<Node>> outdegreeMap = getInvertedOutdegrees(nodeMap);
		Map<Integer, Set<Node>> differentialMap = getInvertedDifferentials(nodeMap);
		
		int maxDiff = Collections.max(differentialMap.keySet());
		
		while(indegreeMap.size() != 0 || outdegreeMap.size() != 0) {
			// Source nodes
			while(indegreeMap.get(0).size() != 0) {
				Set<Node> newSources = null;
				for(Iterator<Node> it = indegreeMap.get(0).iterator(); it.hasNext(); ) {
					Node node = it.next();
					appending.add(node);
					
					outdegreeMap.get(node.getOutdegreeWithinBlock().size()).remove(node);
					removeFromDifferentialMap(node, differentialMap);
					newSources = removeOutdegreeNeighbors(nodeMap, node, indegreeMap, differentialMap);
					it.remove();
				}
				if(newSources != null) indegreeMap.get(0).addAll(newSources);
			}
			
			// Sink nodes
			while(outdegreeMap.get(0).size() != 0) {
				Set<Node> newSinks = null;
				for(Iterator<Node> it = outdegreeMap.get(0).iterator(); it.hasNext(); ) {
					Node node = it.next();
					prepending.add(0, node);
					
					indegreeMap.get(node.getIndegreeWithinBlock().size()).remove(node);
					removeFromDifferentialMap(node, differentialMap);
					newSinks = removeIndegreeNeighbors(nodeMap, node, outdegreeMap, differentialMap);
					it.remove();
				}
				if(newSinks != null) outdegreeMap.get(0).addAll(newSinks);
			}
			
			// Find the biggest differential node
			if(differentialMap.size() == 0) break;
			
			if(!differentialMap.containsKey(maxDiff))
				maxDiff = Collections.max(differentialMap.keySet());
			Node special = differentialMap.get(maxDiff).iterator().next();
			
			removeFromDifferentialMap(special, differentialMap);
			Set<Node> newSources = removeOutdegreeNeighbors(nodeMap, special, indegreeMap, differentialMap);
			if(newSources != null) indegreeMap.get(0).addAll(newSources);
			Set<Node> newSinks = removeIndegreeNeighbors(nodeMap, special, outdegreeMap, differentialMap);
			if(newSinks != null) outdegreeMap.get(0).addAll(newSinks);
			
			appending.add(special);
		}
		
		appending.addAll(prepending);
		
		return appending;
	}
	
	private static void removeFromDifferentialMap(Node node, Map<Integer, Set<Node>> differentialMap) {
		int nodeDiff = node.getOutdegreeWithinBlock().size() - node.getIndegreeWithinBlock().size();
		differentialMap.get(nodeDiff).remove(node);
		if(differentialMap.get(nodeDiff).size() == 0) differentialMap.remove(nodeDiff);
	}
	
	private static void addToDifferentialMap(Node node, int diff, Map<Integer, Set<Node>> differentialMap) {
		int oldDiff = node.getOutdegreeWithinBlock().size() - node.getIndegreeWithinBlock().size();
		if(!differentialMap.containsKey(oldDiff+diff)) differentialMap.put(oldDiff+diff, new HashSet<Node>());
		differentialMap.get(oldDiff+diff).add(node);
	}
	
	private static Set<Node> removeOutdegreeNeighbors(Map<String, Node> nodeMap, Node node, Map<Integer, Set<Node>> indegreeMap, Map<Integer, Set<Node>> differentialMap) {
		Set<Node> result = new HashSet<Node>();
		
		for(String n : node.getOutdegreeWithinBlock()) {
			Node neighbor = nodeMap.get(n);
			
			// Permanently remove outgoing edges
			removeFromDifferentialMap(neighbor, differentialMap);
			indegreeMap.get(neighbor.getIndegreeWithinBlock().size()).remove(neighbor);
			
			neighbor.removeFromIndegreeWithinBlock(node.getNodeIDPair());
			
			if(neighbor.getIndegreeWithinBlock().size() == 0) result.add(neighbor);
			else {
				if(!indegreeMap.containsKey(neighbor.getIndegreeWithinBlock().size()))
					indegreeMap.put(neighbor.getIndegreeWithinBlock().size(), new HashSet<Node>());
				indegreeMap.get(neighbor.getIndegreeWithinBlock().size()).add(neighbor);
			}
			
			addToDifferentialMap(neighbor, 0, differentialMap);
		}
		
		return result;
	}
	
	private static Set<Node> removeIndegreeNeighbors(Map<String, Node> nodeMap, Node node, Map<Integer, Set<Node>> outdegreeMap, Map<Integer, Set<Node>> differentialMap) {
		Set<Node> result = new HashSet<Node>();
		
		for(String n : node.getIndegreeWithinBlock()) {
			Node neighbor = nodeMap.get(n);
			
			// Permanently remove incoming edges
			removeFromDifferentialMap(neighbor, differentialMap);
			outdegreeMap.get(neighbor.getOutdegreeWithinBlock().size()).remove(neighbor);
			
			neighbor.removeFromOutdegreeWithinBlock(node.getNodeIDPair());
			
			if(neighbor.getOutdegreeWithinBlock().size() == 0) result.add(neighbor);
			else {
				if(!outdegreeMap.containsKey(neighbor.getOutdegreeWithinBlock().size()))
					outdegreeMap.put(neighbor.getOutdegreeWithinBlock().size(), new HashSet<Node>());
				outdegreeMap.get(neighbor.getOutdegreeWithinBlock().size()).add(neighbor);
			}
			
			addToDifferentialMap(neighbor, 0, differentialMap);
		}
		
		return result;
	}
	
	private static Map<Integer, Set<Node>> getInvertedIndegrees(Map<String, Node> nodeMap) {
		Map<Integer, Set<Node>> result = new HashMap<Integer, Set<Node>>();
		for(Entry<String, Node> entry : nodeMap.entrySet()) {
			int val = entry.getValue().getIndegreeWithinBlock().size();
			if(!result.containsKey(val)) result.put(val, new HashSet<Node>());
			result.get(val).add(entry.getValue());
		}
		
		if(result.get(0) == null) result.put(0, new HashSet<Node>());
		return result;
	}
	
	private static Map<Integer, Set<Node>> getInvertedOutdegrees(Map<String, Node> nodeMap) {
		Map<Integer, Set<Node>> result = new HashMap<Integer, Set<Node>>();
		for(Entry<String, Node> entry : nodeMap.entrySet()) {
			int val = entry.getValue().getOutdegreeWithinBlock().size();
			if(!result.containsKey(val)) result.put(val, new HashSet<Node>());
			result.get(val).add(entry.getValue());
		}
		
		if(result.get(0) == null) result.put(0, new HashSet<Node>());
		return result;
	}
	
	private static Map<Integer, Set<Node>> getInvertedDifferentials(Map<String, Node> nodeMap) {
		Map<Integer, Set<Node>> result = new HashMap<Integer, Set<Node>>();
		for(Entry<String, Node> entry : nodeMap.entrySet()) {
			Node node = entry.getValue();
			int val = node.getOutdegreeWithinBlock().size() - node.getIndegreeWithinBlock().size();
			if(!result.containsKey(val)) result.put(val, new HashSet<Node>());
			result.get(val).add(node);
		}
		
		return result;
	}
}
