from collections import defaultdict


EDGES_FILE = "data/test.txt"
BLOCKS_FILE = "data/blocks.txt"
TOTAL_NODES = 685230


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


class Blocks(object):
    def __init__(self, input_file):
        self.map = []
        self.helper(input_file)

    def helper(self, input_file):
        curr = 0
        with open(input_file) as f:
            for total_nodes in f:
                curr += int(total_nodes)
                self.map.append(curr)

    def get_blockID(self, node_id):
        l, r = 0, len(self.map)-1
        while l <= r:
            mid = l + (r-l)/2
            if node_id >= self.map[mid]:
                l = mid + 1
            else:
                r = mid - 1
        return l


if __name__ == "__main__":
    edges = Edges(EDGES_FILE)
    blocks = Blocks(BLOCKS_FILE)
    page_rank = "{0:.9f}".format(1/float(TOTAL_NODES))
    f = open("foo.txt", "w")

    for nid in range(3):
        bid = blocks.get_blockID(nid)
        data = "{}-{} {}".format(nid, bid, page_rank)

        neighbors = edges.get_neighbors(nid)
        if neighbors:
            for n, b in zip(neighbors, map(blocks.get_blockID, neighbors)):
                data += " {}-{}".format(n, b)
        f.write("{}\n".format(data))
    f.close()


# nodes_map = init_map(EDGES)
# blocks = init_blocks(BLOCKS)
# print blocks
# print len(blocks)

edges = Edges(EDGES_FILE)
print edges.map

blocks = Blocks(BLOCKS_FILE)
print blocks.map




