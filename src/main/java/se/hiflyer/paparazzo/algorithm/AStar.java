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
import java.util.concurrent.atomic.AtomicInteger;


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
		Set<T> closedSet = new HashSet<T>();
		final Map<T, NodeData> costMap = new HashMap<>();


		Comparator<T> nodeComparator = new NodeComparator<T>(costMap);
		Queue<T> openSet = new PriorityQueue<T>(20, nodeComparator);


		double estimate = estimator.estimate(start, goal);
		costMap.put(start, new NodeData<T>(0.0, estimate, null));

		openSet.add(start);

		while (!openSet.isEmpty()) {
			T x = openSet.poll();
			log.debug("Current node is {}, retrieved from the open set", x);
			if (x.equals(goal)) {
				SimplePath<T> path = reconstructPath(costMap, goal);
				log.debug("At goal, reconstructed path is {}", path);

				return path;
			}
			closedSet.add(x);
			for (T y : neighbourLookup.getNeighbours(x)) {
				if (closedSet.contains(y)) {
					continue;
				}
				double tentativeGScore = costMap.get(x).g + distanceCalculator.getDistanceBetween(x, y);

				log.debug("Tentative G score for {} is {}", y, tentativeGScore);
				if (!openSet.contains(y)) {
					costMap.put(y, new NodeData<T>(tentativeGScore, estimator.estimate(y, goal), x));

					log.debug("Adding {} to the open set", y);
					openSet.add(y);
				} else {
					NodeData nodeData = costMap.get(y);
					if (tentativeGScore < nodeData.g) {
						log.debug("Tentative score is better than old score {} < {}, updating", tentativeGScore, costMap.get(y));
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


	private SimplePath<T> reconstructPath(Map<T, NodeData> costMap, T currentNode) {
		T parent = (T) costMap.get(currentNode).parent;
		if (parent != null) {

			SimplePath<T> p = reconstructPath(costMap, parent);
			p.add(currentNode);
			return p;
		} else {
			return new SimplePath<T>(currentNode);
		}
	}

//	private SimplePath<T> reconstructPath(Map<T, NodeData> costMap, T node) {
//		SimplePath<T> path = new SimplePath<T>();
//		do {
//			path.add(node);
//			node = (T) costMap.get(node).parent;
//		} while (node != null);
//		return path;
//	}

	private static class NodeComparator<T> implements Comparator<T> {
		private final Map<T, NodeData> gScore;


		public NodeComparator(Map<T, NodeData> gScore) {
			this.gScore = gScore;
		}

		@Override
		public int compare(T o1, T o2) {
			return (int) (getFScore(o1, gScore) - ((getFScore(o2, gScore))));
		}

		double getFScore(T o1, Map<T, NodeData> gScore) {
			Double g = gScore.get(o1).g;
			Double h = gScore.get(o1).h;
			return g + h;
		}
	}

	private static class NodeData<T> {
		double g;
		double h;
		T parent;

		public NodeData(double g, double h, T parent) {
			this.g = g;
			this.h = h;
			this.parent = parent;
		}
	}
}
