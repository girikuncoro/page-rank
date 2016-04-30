# CS5300 Projec 2: Fast Convergence PageRank in MapReduce

### Giri Kuncoro (gk256), Yihui Fu (yf263), Shibo Zang(sz428)


## 1. Overall Structure
In this project, we implemented the following versions of computing PageRank values:

* Simple Computation of PageRank.
* Jacobi Blocked Computation of PageRank.
* **[Extra credit]** Gauss-Seidel Computation of PageRank
* **[Extra credit]** Random Block Partition

## 2. Preprocessing

### 2.1 Filtering Edges
The netID used to compute rejectMin and rejectLimit is gk256 with the reversed value being 0.652. The rejectMin is 0.5868 and the rejectLimit is 0.5968. In total, 7,524,304 edges are selected in the graph. The pre-filtered edges file is stored in our S3 bucket: [https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/processed_edges.txt](https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/processed_edges.txt). It's the same format as the given edges.txt, i.e, `src-node-number dest-node-number floating number`. 

The Python code used for filtering is in `preprocess/process_edges.py`. To run it, `cd` to the directory where process_edges.py is, put `edges.txt` in the same directory, then run `python process_edges.py <input-file> <output-file>`.

### 2.2 Converting Edges to Nodes
Additionally, we processed the edge file into a node-centric one so that it can be an appropriate input for mapper. It's also stored in our S3 bucket: [https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/nodepairs.txt](https://s3-us-west-2.amazonaws.com/edu-cornell-cs-cs5300s16-gk256/nodepairs.txt). The format of a node entry is `nodeID-blockID pagerank-value neighborNodeID-blockID [...]`. Although some edges were rejected in the filtering, the nodes in these edges were still retained.

The python code for this is in `preprocess/process_nodes.py`. To run it, `cd` to the directory where process_nodes.py is, put `blocks.txt` and the filtered edge file in the same directory, then run `python process_nodes.py <edge-file> <block-file> <output-file>`.

### 2.3 Random Block Partition

To compute a bad partition for the graph, we assigned a node to a block based on the value of `nodeID % totalBlocks` where totalBlcoks is 68. This ensures that blocks are uniformly sized.

The python code for this is in `preprocess/process_nodes_random.py`. To run it, `cd` to the directory where process_nodes_random.py is, put the filtered edge file in the same directory, then run `python process_nodes_random.py <edge-file> <block-file> <output-file>`.

## 3. Helper Classes

`src/proj2/main/node/Node.java`:

* Includes meta data about a node to make extracting information easier: nodeID-blockID, nodeID, blockID, currentPageRank, degree, emittedPageRank, and an array of destination nodes
* Not for simple computation

`src/proj2/main/util/Constants.java`:

* Defines constant values used in this project, including damping factor, total node number, total block number, pass number for simple computation, convergence threhold, and enumerations for Hadoop counters

## 4. Simple Computation of PageRank

`src/proj2/main/simple` package:

* SimplePageRank.java
	* Sets the input and output path accordingly
	* Runs 5 MapReduce passes 
	* Initializes a MapReduce job for each MapReduce pass
	* Computes per iteration average residual using a Hadoop Counter
* SimpleMapper.java
	* Emits < srcNodeID-blockID, node entry > for computing residual and recovering node structure
	* Emits < destNodeID-blockID, outgoingPageRank> for every outgoing edge
* SimpleReducer.java
	* Updates the PageRank value for a node based on the PageRank values of its immediate neighbors, using the formula `(1-d)/N + d * sum (<PRt(u)/degree(u)>)` where d = 0.85 and N = 685230
	* Emits < nodeID-blockID, updated PageRank and reconstructed adjancency list >
	* Adds the residual for this node |(PR<sup>t</sup>(u) - PR<sup>t+1</sup>(u))| / PR<sup>t+1</sup>(u) to a residual counter

==The average residual errors in each MapReduce pass are as the following:==

## 5. Jacobi Blocked Computation of PageRank

`src/proj2/main/blocked` package:

* BlockedPageRank.java
	* Sets the input and output path accordingly
	* Runs MapReduce passes until reaching convergence where the average relative residual error is below 0.001
	* Computes average relative residual and average number of iterations per block for each reduce task by using two Hadoop Counters
	* Reports the PageRank value for two lowest-numbered notes in each block after the entire computation converged
* BlockedMapper.java
	* Emits < src-blockID, PR node entry > to compute residual in reducer and reconstruct node structure
	* Emits < dest-blockID, BE srcNodeID-blockID destNodeID-blockID > for every destionation node in the same block as the source node
	* Emits < dest-blockID, BC srcNodeID-blockID emittedPageRank destNodeID-blockID > for every destionation node not in the same block as the source node
* BlockedReducer.java
	* Parse the input values and store in five HashMaps accordingly
	* Performs in-block iterations until the in-block residual for the last iteration reaches convergence threshold 0.001
	* Emits < nodeID-blockID, updated PageRank value and reconstructed adjancency list >
	* Add the entire block reducer residual to a residual counter
	* The relative residual error reflects the difference of a node's PageRank value before and after internal block iterations: |(PR<sup>start</sup>(v) - PR<sup>end</sup>(v))| / PR<sup>end</sup>(v)
	* Add the number of iterations in this block to an iteration counter

==The result of this version is as the following==
## 6. Gauss-Seidel Computation of PageRank

`src/proj2/main/gauss` package:

* GaussPageRank.java
	* This is the same as the BlockedPageRank.java 
* GaussMapper.java
	* This is the same as the BlocedMapper.java
* GaussReducer.java
	* 

## 7. ==Random Block Partition==

## Appendix
