package proj2.test.node;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
		node0.setIndegreeWithinBlock(new HashSet<String>());
		nodes.add(node1);
		node1.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "1-0" })));
		nodes.add(node2);
		node2.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "1-0", "2-0" })));
		nodes.add(node3);
		node3.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "3-0" })));

		Map<String, Node> nodeMap = new HashMap<String, Node>();

		for (Node n : nodes) {
			nodeMap.put(n.getNodeIDPair(), n);
		}

		List<Node> res = TopologicalSort.sort(nodeMap);
		List<Node> expectedRes = new ArrayList<Node>();
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

	@Test
	public void testSort2CyclicGraph() throws IOException {
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node node0 = new Node("1-0 0.01 2-0 3-0");
		Node node1 = new Node("2-0 0.01 3-0");
		Node node2 = new Node("3-0 0.01 4-0");
		Node node3 = new Node("4-0 0.01 1-0");

		nodes.add(node0);
		node0.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "4-0" })));
		nodes.add(node1);
		node1.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "1-0" })));
		nodes.add(node2);
		node2.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "1-0", "2-0" })));
		nodes.add(node3);
		node3.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "3-0" })));

		Map<String, Node> nodeMap = new HashMap<String, Node>();

		for (Node n : nodes) {
			nodeMap.put(n.getNodeIDPair(), n);
		}

		List<Node> res = TopologicalSort.sort(nodeMap);
		ArrayList<Node> expectedRes = new ArrayList<Node>();
		expectedRes.add(node0);
		expectedRes.add(node1);
		expectedRes.add(node2);
		expectedRes.add(node3);

		assertEquals(4, res.size());

		System.out.println("Sorted result from least indegree");
		for (int i = 0; i < 4; i++) {
			System.out.print(expectedRes.get(i).getNodeID());
			System.out.println(" " + res.get(i).getNodeID());
		}
	}
	
	@Test
	public void testSort2CyclicGraph2() throws IOException {
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node node0 = new Node("1-0 0.01 2-0");
		Node node1 = new Node("2-0 0.01 3-0 8-0");
		Node node2 = new Node("3-0 0.01 4-0 5-0 8-0");
		Node node3 = new Node("4-0 0.01 2-0 6-0");
		Node node4 = new Node("5-0 0.01 6-0");
		Node node5 = new Node("6-0 0.01 7-0");
		Node node6 = new Node("7-0 0.01 4-0 1-0");
		Node node7 = new Node("8-0 0.01 9-0 10-0");
		Node node8 = new Node("9-0 0.01 7-0");
		Node node9 = new Node("10-0 0.01 1-0");

		nodes.add(node0);
		node0.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "7-0", "10-0" })));
		nodes.add(node1);
		node1.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "1-0", "4-0" })));
		nodes.add(node2);
		node2.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "2-0" })));
		nodes.add(node3);
		node3.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "3-0", "7-0" })));
		nodes.add(node4);
		node4.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "3-0" })));
		nodes.add(node5);
		node5.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "4-0", "5-0" })));
		nodes.add(node6);
		node6.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "6-0", "9-0" })));
		nodes.add(node7);
		node7.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "2-0", "3-0" })));
		nodes.add(node8);
		node8.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "8-0" })));
		nodes.add(node9);
		node9.setIndegreeWithinBlock(new HashSet<String>(Arrays.asList(new String[] { "8-0" })));

		Map<String, Node> nodeMap = new HashMap<String, Node>();

		for (Node n : nodes) {
			nodeMap.put(n.getNodeIDPair(), n);
		}

		List<Node> res = TopologicalSort.sort(nodeMap);
//		ArrayList<Node> expectedRes = new ArrayList<Node>();
//		expectedRes.add(node0);
//		expectedRes.add(node1);
//		expectedRes.add(node2);
//		expectedRes.add(node3);

//		assertEquals(4, res.size());

		System.out.println("Sorted result from least indegree");
//		for (int i = 0; i < 10; i++) {
//			System.out.print(expectedRes.get(i).getNodeID());
		System.out.println(res.toString());
//			System.out.println(" " + res.get(i).getNodeID());
	}
}
