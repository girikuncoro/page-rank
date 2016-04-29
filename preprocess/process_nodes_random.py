"""
Create nodeID-blockID pairs for each node, with init page rank
    and list of neighbors. Total 685230 nodes.
Usage:
    python process_nodes.py <edge-file> <output-file>
"""
import sys
from collections import defaultdict


TOTAL_NODES = 685230
TOTAL_BLOCKS = 68

"""
Populate dictionary of node with neighbors
"""
class Edges(object):
    def __init__(self, input_file):
        self.map = defaultdict(list)
        self.helper(input_file)

    def helper(self, input_file):
        with open(input_file) as f:
            for line in f:
                node, neighbor, val = line.split()
                node, neighbor = int(node), int(neighbor)
                self.map[node].append(neighbor)

    def get_neighbors(self, node_id):
        return self.map[node_id]

"""
Uniformly distribute the nodes to blocks
"""
def getRandomBlockIDofNode(node_id):
    return node_id % TOTAL_BLOCKS

"""
Main arguments to run the script in command line
"""
if __name__ == "__main__":
    edge_file = sys.argv[1]
    out_file = sys.argv[2]

    print "Begin processing data..."
    edges = Edges(edge_file)
    page_rank = "{0:.9f}".format(1/float(TOTAL_NODES))
    f = open(out_file, "w")

    print "Writing node pairs into file..."
    for nid in range(TOTAL_NODES):
        bid = getRandomBlockIDofNode(nid)
        data = "{}-{} {}".format(nid, bid, page_rank)

        neighbors = edges.get_neighbors(nid)
        if neighbors:
            for n, b in zip(neighbors, map(getRandomBlockIDofNode, neighbors)):
                data += " {}-{}".format(n, b)
        f.write("{}\n".format(data))
    f.close()

    print "Successfully written {} nodes into {}".format(TOTAL_NODES, out_file)
