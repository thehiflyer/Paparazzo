package se.hiflyer.paparazzo.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.hiflyer.paparazzo.impl.Paths;
import se.hiflyer.paparazzo.impl.SimplePath;
import se.hiflyer.paparazzo.interfaces.DistanceCalculator;
import se.hiflyer.paparazzo.interfaces.HeuristicEstimator;
import se.hiflyer.paparazzo.interfaces.NeighbourLookup;
import se.hiflyer.paparazzo.interfaces.Path;

import java.util.*;


public class AStar<T> {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HeuristicEstimator<T> estimator;
	private final NeighbourLookup<T> neighbourLookup;
	private final DistanceCalculator<T> distanceCalculator;

	public AStar(HeuristicEstimator<T> estimator, NeighbourLookup<T> neighbourLookup, DistanceCalculator<T> distanceCalculator) {
		this.estimator = estimator;
		this.neighbourLookup = neighbourLookup;
		this.distanceCalculator = distanceCalculator;
	}

	public Path<T> search(T start, T goal) {
		final Map<T, NodeData> nodeDataMap = new HashMap<>();


		Comparator<T> nodeComparator = new NodeComparator<T>(nodeDataMap);
		Queue<T> openSet = new PriorityQueue<T>(20, nodeComparator);


		double estimate = estimator.estimate(start, goal);
		nodeDataMap.put(start, new NodeData<T>(0.0, estimate, null));

		openSet.add(start);

		while (!openSet.isEmpty()) {
			T x = openSet.poll();
			log.debug("Current node is {}, retrieved from the open set", x);
			if (x.equals(goal)) {
				SimplePath<T> path = reconstructPath(nodeDataMap, goal);
				log.debug("At goal, reconstructed path is {}", path);

				return path;
			}
			nodeDataMap.get(x).closed = true;
			for (T y : neighbourLookup.getNeighbours(x)) {
				NodeData nodeData = nodeDataMap.get(y);
				if (nodeData != null && nodeData.closed) {
					continue;
				}
				double tentativeGScore = nodeDataMap.get(x).g + distanceCalculator.getDistanceBetween(x, y);

				log.debug("Tentative G score for {} is {}", y, tentativeGScore);
				if (!openSet.contains(y) || nodeData == null) {
					nodeDataMap.put(y, new NodeData<T>(tentativeGScore, estimator.estimate(y, goal), x));

					log.debug("Adding {} to the open set", y);
					openSet.add(y);
				} else {
					if (tentativeGScore < nodeData.g) {
						log.debug("Tentative score is better than old score {} < {}, updating", tentativeGScore, nodeData);
						nodeData.g = tentativeGScore;
						nodeData.h =  estimator.estimate(y, goal);
						nodeData.parent = x;
						openSet.remove(y);
						openSet.add(y);
					}
				}
			}
		}
		return Paths.FAIL;
	}


	private SimplePath<T> reconstructPath(Map<T, NodeData> nodeDataMap, T currentNode) {
		T parent = (T) nodeDataMap.get(currentNode).parent;
		if (parent != null) {

			SimplePath<T> p = reconstructPath(nodeDataMap, parent);
			p.add(currentNode);
			return p;
		} else {
			return new SimplePath<T>(currentNode);
		}
	}

	private static class NodeComparator<T> implements Comparator<T> {
		private final Map<T, NodeData> nodeDataMap;


		public NodeComparator(Map<T, NodeData> nodeDataMap) {
			this.nodeDataMap = nodeDataMap;
		}

		@Override
		public int compare(T o1, T o2) {
			return (int) (getFScore(o1, nodeDataMap) - ((getFScore(o2, nodeDataMap))));
		}

		double getFScore(T node, Map<T, NodeData> nodeDataMap) {
			NodeData nodeData = nodeDataMap.get(node);
			Double g = nodeData.g;
			Double h = nodeData.h;
			return g + h;
		}
	}

	private static class NodeData<T> {
		double g;
		double h;
		T parent;
		boolean closed;

		public NodeData(double g, double h, T parent) {
			this.g = g;
			this.h = h;
			this.parent = parent;
		}
	}
}
