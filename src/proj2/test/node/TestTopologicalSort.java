package proj2.test.node;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import proj2.main.node.Node;
import proj2.main.node.TopologicalSort;

public class TestTopologicalSort {

	@Test
	public void testSortAyclicGraph() throws IOException {
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node node0 = new Node("1-0 0.01 2-0 3-0");
		Node node1 = new Node("2-0 0.01 3-0");
		Node node2 = new Node("3-0 0.01 4-0");
		Node node3 = new Node("4-0 0.01");
		
		nodes.add(node0);
		node0.setInDegree(0);
		nodes.add(node1);
		node1.setInDegree(1);
		nodes.add(node2);
		node2.setInDegree(2);
		nodes.add(node3);
		node3.setInDegree(1);
		
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		
		for (Node n : nodes) {
			nodeMap.put(n.getNodeIDPair(), n);
			System.out.print("Node: " + n.getNodeID());
			System.out.println(" inDegree: " + n.getInDegree());
		}
		
		ArrayList<Node> res = TopologicalSort.sort(nodeMap);
		ArrayList<Node> expectedRes = new ArrayList<Node>();
		expectedRes.add(node0);
		expectedRes.add(node1);
		expectedRes.add(node2);
		expectedRes.add(node3);
		
		assertEquals(4, res.size());
		
		System.out.println("Sorted result from least indegree");
		for (int i = 0; i < 4; i++) {
			assertEquals(expectedRes.get(i), res.get(i));
		}
	}	
}
