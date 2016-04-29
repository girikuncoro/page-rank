# CS5300 Projec 2: Fast Convergence PageRank in MapReduce

### Giri Kuncoro (gk256), Yihui Fu (yf263), Shibo Zang(sz428)


## 1. Overall Structure
In this project, we implemented the following versions of computing PageRank values:

* Simple Computation of PageRank.
* Jacobi Blocked Computation of PageRank.
* Gauss-Seidel Computation of PageRank
* ==Random Block Partition==

## 2. Preprocessing

### 2.1 Filtering Edges
The netID used to compute rejectMin and rejectLimit is gk256 with the reversed value being 0.652. The rejectMin is 0.5868 and the rejectLimit is 0.5968. In total, 7,524,304 edges are selected in the graph. The pre-filtered edges file is stored in our S3 bucket: [https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/processed_edges.txt](https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/processed_edges.txt). It's the same format as the given edges.txt, i.e, `src-node-number dest-node-number floating number`. 

The Python code used for filtering is in "preprocess/process_edges.py". To run it, `cd` to the directory where process_edges.py is, put `edges.txt` in the same directory, then run `python process_edges.py <input-file> <output-file>`.

### 2.2 Converting Edges to Nodes
Additionally, we processed the edge file into a node-centric one so that it can be an appropriate input for mapper. It's also stored in our S3 bucket: [https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/nodepairs.txt](https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/nodepairs.txt). The format of the node file is `nodeID-blockID pagerank-value neighborNodeID-blockID [...]`. Although some edges were rejected in the filtering, the nodes in these edges were still retained.

The python code for this is in "preprocess/process_nodes.py". To run it, `cd` to the directory where process_nodes.py is, put `blocks.txt` and the filtered edge file in the same directory, then run ` python process_nodes.py <edge-file> <block-file> <output-file>`.

==You may put both files under `data` directory.==

## 3. Simple Computation of PageRank

`src/proj2/main/simple` package:

* SimplePageRank.java
	* Sets the input and output path accordingly and runs 5 MapReduce passes. Initializes a MapReduce job for each MapReduce pass, and calculates per iteration average residual using Hadoop Counters. 
* SimpleMapper.java
	* For every input node, emits the node itself for computing residual, and emits the PageRank for every destination node.
* SimpleReducer.java
	* Updates and emits the PageRank value for a node based on the PageRank values of its immediate neighbors, using the formula `(1-d)/N + d * sum (<PRt(u)/degree(u)>)` where d = 0.85 and N = 685230. Additionally, adds the residual for this node |(PR<sup>t</sup>(u) - PR<sup>t+1</sup>(u))| / PR<sup>t+1</sup>(u) to a residual counter.

## 4. Jacobi Blocked Computation of PageRank

`src/proj2/main/blocked` package:

## 5. Gauss-Seidel Computation of PageRank

`src/proj2/main/gauss` package:

## 6. ==Random Block Partition==