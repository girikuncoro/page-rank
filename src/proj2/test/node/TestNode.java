package proj2.test.node;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import proj2.main.node.Node;

public class TestNode {
	
	@Test
	public void testNodeConstructor() throws IOException {
		String validMapperInput = "0-0 0.000001459 303743-30 303746-30 303747-30";
		Node node = new Node(validMapperInput);
		
		assertEquals("0", node.getBlockID());
		assertEquals("0-0", node.getNodeIDPair());
		assertEquals(new Double(0.000001459), node.getPageRank());
		assertEquals(3, node.getDegree());
		assertEquals(new Double(0.000001459/3.0), node.getEmittedPageRank());
		assertEquals(true, node.hasEdges());
		
		String[] neighbors = {"303743-30", "303746-30", "303747-30"};
		for (int i = 0; i < node.getDegree(); i++) {
			assertEquals(neighbors[i], node.getNeighbors()[i]);
		}
		
		assertEquals("303743-30 303746-30 303747-30", node.neighborsToString());
	}

	@Test
	public void testNodeWithoutEdges() throws IOException {
		String validMapperInput = "0-0 0.000001459";
		Node node = new Node(validMapperInput);
		
		assertEquals(new Double(0.000001459), node.getPageRank());
		assertEquals(0, node.getDegree());
		assertEquals(new Double(0.0), node.getEmittedPageRank());
		assertEquals(false, node.hasEdges());
	}
	
	@Test(expected = IOException.class)
	public void testInvalidInput() throws IOException {
		String invalidMapperInput = "0-0";
		Node node = new Node(invalidMapperInput);
	}
}
