package proj2.main.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TopologicalSort {
	
	public static List<Node> sort(Map<String, Node> nodeMap) {
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
