package se.hiflyer.paparazzo.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.hiflyer.paparazzo.impl.Paths;
import se.hiflyer.paparazzo.impl.SearchListenerAdapter;
import se.hiflyer.paparazzo.impl.SimplePath;
import se.hiflyer.paparazzo.interfaces.*;

import java.util.*;


public class AStar<T> {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HeuristicEstimator<T> estimator;
	private final NeighbourLookup<T> neighbourLookup;
	private final DistanceCalculator<T> distanceCalculator;
	private final SearchListener<T> searchListener;

	public AStar(HeuristicEstimator<T> estimator, NeighbourLookup<T> neighbourLookup, DistanceCalculator<T> distanceCalculator, SearchListener<T> searchListener) {
		this.estimator = estimator;
		this.neighbourLookup = neighbourLookup;
		this.distanceCalculator = distanceCalculator;
		this.searchListener = searchListener;
	}

	public AStar(HeuristicEstimator<T> estimator, NeighbourLookup<T> neighbourLookup, DistanceCalculator<T> distanceCalculator) {
		this(estimator, neighbourLookup, distanceCalculator, new SearchListenerAdapter<T>());
	}

	public Path<T> search(T start, T goal) {
		final Map<T, NodeData<T>> nodeDataMap = new HashMap<>();


		Comparator<NodeData<T>> nodeComparator = this::compareNodes;
		Queue<NodeData<T>> openSet = new PriorityQueue<>(20, nodeComparator);


		double estimate = estimator.estimate(start, goal);
		NodeData<T> nodeDataForStart = new NodeData<>(start, 0.0, estimate, null);
		nodeDataMap.put(start, nodeDataForStart);

		openSet.add(nodeDataForStart);
		searchListener.addedToOpenSet(start);

		while (!openSet.isEmpty()) {
			NodeData<T> nodeDataForX = openSet.poll();
			T x = nodeDataForX.node;
			log.debug("Current node is {}, retrieved from the open set", x);
			if (x.equals(goal)) {
				SimplePath<T> path = reconstructPath(nodeDataMap, goal);
				log.debug("At goal, reconstructed path is {}", path);

				return path;
			}

			nodeDataForX.closed = true;
			searchListener.addedToClosedSet(x);
			for (T y : neighbourLookup.getNeighbours(x)) {

				NodeData<T> nodeData = nodeDataMap.get(y);
				if (nodeData != null && nodeData.closed) {
					continue;
				}
				double tentativeGScore = nodeDataForX.g + distanceCalculator.getDistanceBetween(x, y);
				NodeData<T> nodeDataForY = new NodeData<T>(y, 0, 0, null);
				log.debug("Tentative G score for {} is {}", y, tentativeGScore);
				if (!openSet.contains(nodeDataForY) || nodeData == null) {
					nodeDataForY.g = tentativeGScore;
					nodeDataForY.h = estimator.estimate(y, goal);
					nodeDataForY.parent = x;
					nodeDataMap.put(y, nodeDataForY);
					searchListener.updatedGCost(y, tentativeGScore);
					log.debug("Adding {} to the open set", y);
					openSet.add(nodeDataForY);
					searchListener.addedToOpenSet(y);
				} else {
					if (tentativeGScore < nodeData.g) {
						log.debug("Tentative score is better than old score {} < {}, updating", tentativeGScore, nodeData);
						nodeData.g = tentativeGScore;
						nodeData.h =  estimator.estimate(y, goal);
						nodeData.parent = x;
						openSet.remove(nodeData);
						openSet.add(nodeData);
						searchListener.updatedGCost(y, tentativeGScore);
					}
				}
			}
		}
		return Paths.FAIL;
	}

	private int compareNodes(NodeData<T> n1, NodeData<T> n2) {
		return (int) (n1.g + n1.h - ((n2.g + n2.h)));
	}


	private SimplePath<T> reconstructPath(Map<T, NodeData<T>> nodeDataMap, T currentNode) {
		T parent = nodeDataMap.get(currentNode).parent;
		if (parent != null) {

			SimplePath<T> p = reconstructPath(nodeDataMap, parent);
			p.add(currentNode);
			return p;
		} else {
			return new SimplePath<T>(currentNode);
		}
	}


	private static class NodeData<T> {
		T node;
		double g;
		double h;
		T parent;
		boolean closed;

		public NodeData(T node, double g, double h, T parent) {
			this.node = node;
			this.g = g;
			this.h = h;
			this.parent = parent;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			NodeData<?> nodeData = (NodeData<?>) o;
			return node.equals(nodeData.node);
		}

		@Override
		public int hashCode() {
			return Objects.hash(node);
		}
	}
}
