package org.highsource.storyteller.jung.algorithms.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

/**
 * Arranges the nodes with the Sugiyama Layout Algorithm.<br>
 * * <a href="http://plg.uwaterloo.ca/~itbowman/CS746G/Notes/Sugiyama1981_MVU/">
 * Link to the algorithm</a><br>
 * 
 * Originally, source was posted to the Jung2 forum, for Jung 1.x. Not sure
 * where the original code came from, but ti didn;t work for Jung2, but it was
 * not that complicated, so I pounded it into shape for Jung2, complete with
 * generics and such. Lays out either top-down to left-right.
 * 
 * Seems to work. Paramterize with spacing and orientation.
 * 
 * C. Schanck (chris at schanck dot net)
 */
public class SugiyamaLayout<V, E> extends AbstractLayout<V, E> {

	private static final Orientation DEFAULT_ORIENTATION = Orientation.TOP;

	private static final int DEFAULT_HORIZONTAL_SPACING = 200;

	private static final int DEFAULT_VERTICAL_SPACING = 100;

	public static enum Orientation {
		TOP, LEFT
	};

	private boolean executed = false;

	/**
	 * represents the size of the grid in horizontal grid elements
	 * 
	 */
	private int gridAreaSize = Integer.MIN_VALUE;

	private int horzSpacing;

	private int vertSpacing;

	private final Set<V> traversalSet = new HashSet<V>();

	private final Map<V, CellWrapper<V>> vertToWrapper = new HashMap<V, CellWrapper<V>>();

	private Orientation orientation;

	public SugiyamaLayout(Graph<V, E> g) {
		this(g, DEFAULT_ORIENTATION, DEFAULT_HORIZONTAL_SPACING,
				DEFAULT_VERTICAL_SPACING);
	}

	public SugiyamaLayout(Graph<V, E> g, Orientation orientation,
			int horzSpacing, int vertSpacing) {
		super(g);
		this.orientation = orientation;
		this.horzSpacing = horzSpacing;
		this.vertSpacing = vertSpacing;
	}

	@Override
	public void initialize() {
		if (!executed) {
			LinkedList<LinkedList<CellWrapper<V>>> graphLevels = runSugiyama();
			for (LinkedList<CellWrapper<V>> level : graphLevels) {
				for (CellWrapper<V> wrapper : level) {
					V vertex = wrapper.getVertexView();

					if (orientation.equals(Orientation.TOP)) {
						double xCoordinate = 10.0 + (wrapper.gridPosition * horzSpacing);
						double yCoordinate = 10.0 + (wrapper.level * vertSpacing);
						setLocation(vertex, xCoordinate, yCoordinate);
					} else {
						double yCoordinate = 10.0 + (wrapper.gridPosition * vertSpacing);
						double xCoordinate = 10.0 + (wrapper.level * horzSpacing);
						setLocation(vertex, xCoordinate, yCoordinate);
					}
				}
			}
		}

	}

	@Override
	public String toString() {
		return "Jung Sugiyama";
	}

	/**
	 * Implementation.
	 * 
	 * First of all, the Algorithm searches the roots from the Graph. Starting
	 * from this roots the Algorithm creates levels and stores them in the
	 * member <code>levels</code>. The Member levels contains LinkedList Objects
	 * and the LinkedList per level contains Cell Wrapper Objects. After that
	 * the Algorithm tries to solve the edge crosses from level to level and
	 * goes top down and bottom up. After minimization of the edge crosses the
	 * algorithm moves each node to its bary center.
	 * 
	 */
	private LinkedList<LinkedList<CellWrapper<V>>> runSugiyama() {
		executed = true;
		Set<V> vertexSet = new HashSet<V>(graph.getVertices());

		// search all roots
		LinkedList<V> roots = searchRoots(vertexSet);

		// LinkedList<V> leaves = searchLeaves(vertexSet);

		// create levels -> its a LinkedList of LinkedLists
		LinkedList<LinkedList<CellWrapper<V>>> levels =
		// findLevels(graph);
		fillLevels(roots, vertexSet);

		// solves the edge crosses
		solveEdgeCrosses(levels);

		// move all nodes into the barycenter
		moveToBarycenter(levels, vertexSet);

		// you could probably nuke the maps at this point, but i'm not certain,
		// and I don't care.
		return levels;
	}

	/**
	 * Searches all Roots for the current Graph First the method marks any Node
	 * as not visited. Than calls searchRoots(MyGraphCell) for each not visited
	 * Cell. The Roots are stored in the LinkedList named roots
	 * 
	 * @return returns a LinkedList with the roots
	 * @see #searchRoots(JGraph, CellView[])
	 */
	private LinkedList<V> searchRoots(Set<V> vertexSet) {
		LinkedList<V> roots = new LinkedList<V>();
		// first: mark all as not visited
		// it is assumed that vertex are not visited
		for (V vert : vertexSet) {
			if (!traversalSet.contains(vert)) {
				traversalSet.add(vert);

				int in_degree = getGraph().inDegree(vert);
				if (in_degree == 0) {
					roots.add(vert);
				}
			}
		}
		return roots;
	}

	private LinkedList<V> searchLeaves(Set<V> vertexSet) {
		LinkedList<V> leaves = new LinkedList<V>();
		// first: mark all as not visited
		// it is assumed that vertex are not visited
		for (V vert : vertexSet) {
			if (!traversalSet.contains(vert)) {
				traversalSet.add(vert);
				if (getGraph().outDegree(vert) == 0) {
					leaves.add(vert);
				}
			}
		}
		return leaves;
	}

	/**
	 * Method fills the levels and stores them in the member levels.
	 * 
	 * Each level was represended by a LinkedList with Cell Wrapper objects.
	 * These LinkedLists are the elements in the <code>levels</code> LinkedList.
	 * 
	 * @return
	 * 
	 */
	private LinkedList<LinkedList<CellWrapper<V>>> fillLevels(
			LinkedList<V> roots, Set<V> vertexSet) {
		LinkedList<LinkedList<CellWrapper<V>>> levels = new LinkedList<LinkedList<CellWrapper<V>>>();

		// clear the visit
		traversalSet.clear();

		for (V r : roots) {
			fillLevels(levels, 0, r); // 0 indicates level 0
		} // i.e root level
		return levels;
	}

	private LinkedList<LinkedList<CellWrapper<V>>> fillLevels0(
			LinkedList<V> leaves, Set<V> vertexSet) {
		LinkedList<LinkedList<CellWrapper<V>>> levels = new LinkedList<LinkedList<CellWrapper<V>>>();

		// clear the visit
		traversalSet.clear();

		for (V leafNode : leaves) {
			fillLevels0(levels, 0, leafNode); // 0 indicates level 0
		} // i.e root level
		return levels;
	}

	private void fillLevels0(LinkedList<LinkedList<CellWrapper<V>>> levels,
			int level, V leafNode) { // this is a recursive function
										// precondition control
		if (leafNode == null)
			return;

		// be sure that a LinkedList container exists for the current level
		if (levels.size() == level)
			levels.add(level, new LinkedList<CellWrapper<V>>());

		// if the cell already visited return
		if (traversalSet.contains(leafNode)) {
			return;
		}

		// mark as visited for cycle tests
		traversalSet.add(leafNode);

		// put the current node into the current level
		// get the Level LinkedList
		LinkedList<CellWrapper<V>> vecForTheCurrentLevel = levels.get(level);

		// Create a wrapper for the node
		int numberForTheEntry = vecForTheCurrentLevel.size();

		CellWrapper<V> wrapper = new CellWrapper<V>(level, numberForTheEntry,
				leafNode);

		// put the Wrapper in the LevelLinkedList
		vecForTheCurrentLevel.add(wrapper);

		// concat the wrapper to the cell for an easy access
		// vertexView.getAttributes().put(SUGIYAMA_CELL_WRAPPER, wrapper);
		vertToWrapper.put(leafNode, wrapper);

		Collection<E> in_edge_set = graph.getInEdges(leafNode);
		for (E edge : in_edge_set) {
			V targetVertex = graph.getSource(edge);
			fillLevels0(levels, level + 1, targetVertex);
		}

		if (vecForTheCurrentLevel.size() > gridAreaSize) {
			gridAreaSize = vecForTheCurrentLevel.size();
		}

	}

	/**
	 * Fills the LinkedList for the specified level with a wrapper for the
	 * MyGraphCell. After that the method called for each neighbor graph cell.
	 * 
	 * @param level
	 *            The level for the graphCell
	 * @param graphCell
	 *            The Graph Cell
	 */
	private void fillLevels(LinkedList<LinkedList<CellWrapper<V>>> levels,
			int level, V rootNode) { // this is a recursive function
										// precondition control
		if (rootNode == null)
			return;

		// be sure that a LinkedList container exists for the current level
		if (levels.size() == level)
			levels.add(level, new LinkedList<CellWrapper<V>>());

		// if the cell already visited return
		if (traversalSet.contains(rootNode)) {
			return;
		}

		// mark as visited for cycle tests
		traversalSet.add(rootNode);

		// put the current node into the current level
		// get the Level LinkedList
		LinkedList<CellWrapper<V>> vecForTheCurrentLevel = levels.get(level);

		// Create a wrapper for the node
		int numberForTheEntry = vecForTheCurrentLevel.size();

		CellWrapper<V> wrapper = new CellWrapper<V>(level, numberForTheEntry,
				rootNode);

		// put the Wrapper in the LevelLinkedList
		vecForTheCurrentLevel.add(wrapper);

		// concat the wrapper to the cell for an easy access
		// vertexView.getAttributes().put(SUGIYAMA_CELL_WRAPPER, wrapper);
		vertToWrapper.put(rootNode, wrapper);

		Collection<E> out_edge_set = graph.getOutEdges(rootNode);
		for (E edge : out_edge_set) {
			V targetVertex = graph.getDest(edge);
			fillLevels(levels, level + 1, targetVertex);
		}

		if (vecForTheCurrentLevel.size() > gridAreaSize) {
			gridAreaSize = vecForTheCurrentLevel.size();
		}

	}

	private void solveEdgeCrosses(LinkedList<LinkedList<CellWrapper<V>>> levels) {
		int movementsCurrentLoop = -1;

		while (movementsCurrentLoop != 0) {
			// reset the movements per loop count
			movementsCurrentLoop = 0;

			// top down
			for (int i = 0; i < levels.size() - 1; i++) {
				movementsCurrentLoop += solveEdgeCrosses(true, levels, i);
			}

			// bottom up
			for (int i = levels.size() - 1; i >= 1; i--) {
				movementsCurrentLoop += solveEdgeCrosses(false, levels, i);
			}
		}
	}

	/**
	 * @return movements
	 */
	private int solveEdgeCrosses(boolean down,
			LinkedList<LinkedList<CellWrapper<V>>> levels, int levelIndex) {
		// Get the current level
		LinkedList<CellWrapper<V>> currentLevel = levels.get(levelIndex);
		int movements = 0;

		// restore the old sort
		Object[] levelSortBefore = currentLevel.toArray();

		// new sort
		Collections.sort(currentLevel);

		// test for movements
		for (int j = 0; j < levelSortBefore.length; j++) {
			if (((CellWrapper) levelSortBefore[j]).getEdgeCrossesIndicator() != ((CellWrapper) currentLevel
					.get(j)).getEdgeCrossesIndicator()) {
				movements++;
			}
		}
		// Collections Sort sorts the highest value to the first value
		for (int j = currentLevel.size() - 1; j >= 0; j--) {
			CellWrapper<V> sourceWrapper = currentLevel.get(j);

			V sourceView = sourceWrapper.getVertexView();

			Collection<E> edgeList = getNeighborEdges(sourceView);

			for (E edge : edgeList) {
				// if it is a forward edge follow it
				V targetView = null;
				if (down && sourceView == graph.getSource(edge)) {
					targetView = graph.getDest(edge);
				}
				if (!down && sourceView == graph.getDest(edge)) {
					targetView = graph.getSource(edge);
				}
				if (targetView != null) {
					CellWrapper<V> targetWrapper = vertToWrapper
							.get(targetView);

					// do it only if the edge is a forward edge to a deeper
					// level
					if (down && targetWrapper != null
							&& targetWrapper.getLevel() > levelIndex) {
						targetWrapper.addToEdgeCrossesIndicator(sourceWrapper
								.getEdgeCrossesIndicator());
					}
					if (!down && targetWrapper != null
							&& targetWrapper.getLevel() < levelIndex) {
						targetWrapper.addToEdgeCrossesIndicator(sourceWrapper
								.getEdgeCrossesIndicator());
					}
				}
			}
		}
		return movements;
	}

	private void moveToBarycenter(
			LinkedList<LinkedList<CellWrapper<V>>> levels, Set<V> vertexSet) {
		for (V v : vertexSet) {

			CellWrapper<V> currentwrapper = vertToWrapper.get(v);

			Collection<E> edgeList = getNeighborEdges(v);

			for (E edge : edgeList) {
				// i have to find neigbhor vertex
				V neighborVertex = null;

				if (v == graph.getSource(edge)) {
					neighborVertex = graph.getDest(edge);
				} else {
					if (v == graph.getDest(edge)) {
						neighborVertex = graph.getSource(edge);
					}
				}

				if ((neighborVertex != null) && (neighborVertex != v)) {

					CellWrapper<V> neighborWrapper = vertToWrapper
							.get(neighborVertex);

					if (!(currentwrapper == null || neighborWrapper == null || currentwrapper.level == neighborWrapper.level)) {
						currentwrapper.priority++;
					}
				}
			}
		}
		for (LinkedList<CellWrapper<V>> level : levels) {
			int pos = 0;
			for (CellWrapper<V> wrapper : level) {
				// calculate the initial Grid Positions 1, 2, 3, .... per Level
				wrapper.setGridPosition(pos++);
			}
		}

		int movementsCurrentLoop = -1;

		while (movementsCurrentLoop != 0) {
			// reset movements
			movementsCurrentLoop = 0;

			// top down
			for (int i = 1; i < levels.size(); i++) {
				movementsCurrentLoop += moveToBarycenter(levels, i);
			}
			// bottom up
			for (int i = levels.size() - 1; i >= 0; i--) {
				movementsCurrentLoop += moveToBarycenter(levels, i);
			}
		}
	}

	private Collection<E> getNeighborEdges(V v) {
		Collection<E> outEdges = graph.getOutEdges(v);
		Collection<E> inEdges = graph.getInEdges(v);
		LinkedList<E> edgeList = new LinkedList<E>();
		edgeList.addAll(outEdges);
		edgeList.addAll(inEdges);
		return edgeList;
	}

	private int moveToBarycenter(LinkedList<LinkedList<CellWrapper<V>>> levels,
			int levelIndex) {
		// Counter for the movements
		int movements = 0;

		// Get the current level
		LinkedList<CellWrapper<V>> currentLevel = levels.get(levelIndex);

		for (int currentIndexInTheLevel = 0; currentIndexInTheLevel < currentLevel
				.size(); currentIndexInTheLevel++) {
			CellWrapper<V> sourceWrapper = currentLevel
					.get(currentIndexInTheLevel);

			float gridPositionsSum = 0;
			float countNodes = 0;

			V vertexView = sourceWrapper.getVertexView();

			Collection<E> edgeList = getNeighborEdges(vertexView);

			for (E edge : edgeList) {
				// if it is a forward edge follow it
				// Object neighborPort = null;
				V neighborVertex = null;
				if (vertexView == graph.getSource(edge)) {
					neighborVertex = graph.getDest(edge);
				} else {
					if (vertexView == graph.getSource(edge)) {
						neighborVertex = graph.getDest(edge);
					}
				}

				if (neighborVertex != null) {

					CellWrapper<V> targetWrapper = vertToWrapper
							.get(neighborVertex);

					if (!(targetWrapper == sourceWrapper)
							|| (targetWrapper == null || targetWrapper
									.getLevel() == levelIndex)) {
						gridPositionsSum += targetWrapper.getGridPosition();
						countNodes++;
					}
				}
			}

			if (countNodes > 0) {
				float tmp = (gridPositionsSum / countNodes);
				int newGridPosition = Math.round(tmp);
				boolean toRight = (newGridPosition > sourceWrapper
						.getGridPosition());

				boolean moved = true;

				while (newGridPosition != sourceWrapper.getGridPosition()
						&& moved) {
					moved = move(toRight, currentLevel, currentIndexInTheLevel,
							sourceWrapper.getPriority());
					if (moved) {
						movements++;
					}
				}
			}
		}
		return movements;
	}

	/**
	 * @param toRight
	 *            <tt>true</tt> = try to move the currentWrapper to right;
	 *            <tt>false</tt> = try to move the currentWrapper to left;
	 * @param currentLevel
	 *            LinkedList which contains the CellWrappers for the current
	 *            level
	 * @param currentIndexInTheLevel
	 * @param currentPriority
	 * @param currentWrapper
	 *            The Wrapper
	 * 
	 * @return The free GridPosition or -1 is position is not free.
	 */
	private boolean move(boolean toRight, LinkedList currentLevel,
			int currentIndexInTheLevel, int currentPriority) {
		CellWrapper currentWrapper = (CellWrapper) currentLevel
				.get(currentIndexInTheLevel);

		boolean moved = false;
		int neighborIndexInTheLevel = currentIndexInTheLevel
				+ (toRight ? 1 : -1);
		int newGridPosition = currentWrapper.getGridPosition()
				+ (toRight ? 1 : -1);

		if (0 > newGridPosition || newGridPosition >= gridAreaSize) {
			return false;
		}

		// if the node is the first or the last we can move
		if (toRight && currentIndexInTheLevel == currentLevel.size() - 1
				|| !toRight && currentIndexInTheLevel == 0) {
			moved = true;
		} else {
			// else get the neighbor and ask his gridposition
			// if he has the requested new grid position
			// check the priority

			CellWrapper neighborWrapper = (CellWrapper) currentLevel
					.get(neighborIndexInTheLevel);

			int neighborPriority = neighborWrapper.getPriority();

			if (neighborWrapper.getGridPosition() == newGridPosition) {
				if (neighborPriority >= currentPriority) {
					return false;
				} else {
					moved = move(toRight, currentLevel,
							neighborIndexInTheLevel, currentPriority);
				}
			} else {
				moved = true;
			}
		}

		if (moved) {
			currentWrapper.setGridPosition(newGridPosition);
		}
		return moved;
	}

	// ---------------cell wrapper-----------------
	/**
	 * cell wrapper contains all values for one node
	 */
	class CellWrapper<VV> implements Comparable<CellWrapper<VV>> {
		/**
		 * sum value for edge Crosses
		 */
		private double edgeCrossesIndicator = 0;

		/**
		 * counter for additions to the edgeCrossesIndicator
		 */
		private int additions = 0;

		/**
		 * the vertical level where the cell wrapper is inserted
		 */
		private int level = 0;

		/**
		 * current position in the grid
		 */
		private int gridPosition = 0;

		/**
		 * priority for movements to the barycenter
		 */
		private int priority = 0;

		/**
		 * reference to the wrapped cell
		 */
		private VV wrappedVertex = null;

		private String vertex_name = "";

		// CellWrapper constructor
		CellWrapper(int level, double edgeCrossesIndicator, VV vertex) {
			this.level = level;
			this.edgeCrossesIndicator = edgeCrossesIndicator;
			this.wrappedVertex = vertex;
			vertex_name = vertex.toString();
			additions++;
		}

		@Override
		public String toString() {
			return vertex_name + "," + level + "," + gridPosition + ","
					+ priority + "," + edgeCrossesIndicator + "," + additions;
		}

		/**
		 * returns the wrapped Vertex
		 */
		VV getVertexView() {
			return wrappedVertex;
		}

		/**
		 * retruns the average value for the edge crosses indicator
		 * 
		 * for the wrapped cell
		 * 
		 */

		double getEdgeCrossesIndicator() {
			if (additions == 0)
				return 0;
			return edgeCrossesIndicator / additions;
		}

		/**
		 * Addes a value to the edge crosses indicator for the wrapped cell
		 * 
		 */
		void addToEdgeCrossesIndicator(double addValue) {
			edgeCrossesIndicator += addValue;
			additions++;
		}

		/**
		 * gets the level of the wrapped cell
		 */
		int getLevel() {
			return level;
		}

		/**
		 * gets the grid position for the wrapped cell
		 */
		int getGridPosition() {
			return gridPosition;
		}

		/**
		 * Sets the grid position for the wrapped cell
		 */
		void setGridPosition(int pos) {
			this.gridPosition = pos;
		}

		/**
		 * increments the the priority of this cell wrapper.
		 * 
		 * The priority was used by moving the cell to its barycenter.
		 * 
		 */

		void incrementPriority() {
			priority++;
		}

		/**
		 * returns the priority of this cell wrapper.
		 * 
		 * The priority was used by moving the cell to its barycenter.
		 */
		int getPriority() {
			return priority;
		}

		/**
		 * @see java.lang.Comparable#compareTo(Object)
		 */
		@Override
		public int compareTo(CellWrapper<VV> compare) {
			if (compare.getEdgeCrossesIndicator() == this
					.getEdgeCrossesIndicator())
				return 0;

			double compareValue = compare.getEdgeCrossesIndicator()
					- this.getEdgeCrossesIndicator();

			return (int) (compareValue * 1000);

		}
	}

	// --------------------------------------------
	@Override
	public void reset() {
		traversalSet.clear();
		vertToWrapper.clear();
		executed = false;
	}

	int counter = 0;

	private LinkedList<LinkedList<CellWrapper<V>>> findLevels(Graph<V, E> graph) {
		final Map<V, Integer> levels = new HashMap<V, Integer>();

		int depth = 0;
		final List<V> verticesToVisit = new LinkedList<V>();
		for (V vertex : graph.getVertices()) {
			if (graph.inDegree(vertex) == 0) {
				levels.put(vertex, 0);
				verticesToVisit.add(vertex);
			}
		}

		while (!verticesToVisit.isEmpty()) {
			V vertex = verticesToVisit.remove(0);
			int level = levels.get(vertex);

			for (V successor : graph.getSuccessors(vertex)) {
				Integer successorLevel = levels.get(successor);
				if (successorLevel == null) {
					levels.put(successor, level + 1);
					verticesToVisit.add(successor);
					if (depth < level + 1) {
						depth = level + 1;
					}
				} else if (successorLevel.intValue() <= level) {
					levels.put(successor, level + 1);
					verticesToVisit.add(successor);
					if (depth < level + 1) {
						depth = level + 1;
					}
				}
			}
		}

		final Collection<E> edges = new ArrayList<E>(graph.getEdges());

		for (E edge : edges) {
			final V source = graph.getSource(edge);
			final V target = graph.getDest(edge);
			final int sourceLevel = levels.get(source);
			final int targetLevel = levels.get(target);
			if (targetLevel - sourceLevel > 1) {
				graph.removeEdge(edge);
				V current = source;
				for (int intermediateLevel = sourceLevel + 1; intermediateLevel < targetLevel; intermediateLevel++) {
					V dummy = (V) (source.toString() + "." + target.toString()
							+ "." + intermediateLevel);
					levels.put(dummy, intermediateLevel);
					graph.addEdge((E) new Object(), current, dummy);
					current = dummy;
				}
				graph.addEdge((E) new Object(), current, target);
			}
		}

		LinkedList<LinkedList<CellWrapper<V>>> lvls = new LinkedList<LinkedList<CellWrapper<V>>>();

		for (int index = 0; index <= depth; index++) {
			lvls.add(new LinkedList<CellWrapper<V>>());
		}

		for (Entry<V, Integer> entry : levels.entrySet()) {
			Integer level = entry.getValue();
			LinkedList<CellWrapper<V>> lvl = lvls.get(level);
			V vertex = entry.getKey();
			CellWrapper<V> cw = new CellWrapper<V>(level, lvl.size(), vertex);
			lvl.add(cw);
			vertToWrapper.put(vertex, cw);
		}
		return lvls;
	}
}
