package proj2.main.util;

public class Constants {
	/**
	 * Common
	 */
	public static final double DAMPING_FACTOR = 0.85;
	public static final int NODE_NUM = 685230;
	public static final double PRECISION_FACTOR = 1000000000.0;
	
	/**
	 * Simple PageRank
	 */
	public static final int SIMPLE_MP_PASS_NUM = 5;
	public static enum SimpleCounterEnum{
		SIMPLE_RESIDUAL;
	}
	
	/**
	 * Blocked PageRank 
	 */
	public static final int INBLOCK_MAX_ITERATION = 100;
	public static final double BLOCK_NUM = 68;
	public static final double CONVERGENCE = 0.001;
	public static enum BlockedCounterEnum {
		BLOCKED_RESIDUAL, N_ITERATION, TOPO_FLAG;
	}
	public static final double TOPO_THRESHOLD = 10;
}
