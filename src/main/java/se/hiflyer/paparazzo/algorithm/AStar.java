package se.hiflyer.paparazzo.algorithm;

import se.hiflyer.paparazzo.impl.Paths;
import se.hiflyer.paparazzo.impl.SimplePath;
import se.hiflyer.paparazzo.interfaces.DistanceCalculator;
import se.hiflyer.paparazzo.interfaces.HeuristicEstimator;
import se.hiflyer.paparazzo.interfaces.NeighbourLookup;
import se.hiflyer.paparazzo.interfaces.Path;

import java.util.*;

public class AStar<T> {
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
		final Map<T, Double> gScore = new HashMap<T, Double>();
		final Map<T, Double> hScore = new HashMap<T, Double>();

		Comparator<T> nodeComparator = new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return (int) (getFScore(o1, gScore, hScore) - ((getFScore(o2, gScore, hScore))));
			}
		};
		Queue<T> openSet = new PriorityQueue<T>(10, nodeComparator);
		Map<T, T> cameFrom = new HashMap<T, T>();


		gScore.put(start, 0.0);
		hScore.put(start, estimator.estimate(start, goal));
		openSet.add(start);

		while (!openSet.isEmpty()) {
			T x = openSet.poll();
			if (x.equals(goal)) {
				Path<T> path = reconstructPath(cameFrom, cameFrom.get(goal));
				path.add(x);
				return path;
			}
			closedSet.add(x);
			for (T y : neighbourLookup.getNeighbours(x)) {
				if (closedSet.contains(y)) {
					continue;
				}
				double tentativeGScore = gScore.get(x) + distanceCalculator.getDistanceBetween(x, y);

				if (!openSet.contains(y)) {
					gScore.put(y, tentativeGScore);
					hScore.put(y, estimator.estimate(y, goal));
					openSet.add(y);
					cameFrom.put(y, x);
				} else if (tentativeGScore < gScore.get(y)) {
					gScore.put(y, tentativeGScore);
					hScore.put(y, estimator.estimate(y, goal));
					openSet.remove(y);
					openSet.add(y);
					cameFrom.put(y, x);
				}
			}
		}
		return Paths.FAIL;
	}

	private double getFScore(T o1, Map<T, Double> gScore, Map<T, Double> hScore) {
		Double gDouble = gScore.get(o1);
		Double hDouble = hScore.get(o1);
		double g = gDouble != null ? gDouble : 0.0;
		double h = hDouble != null ? hDouble : 0.0;
		return g + h;
	}


	private Path<T> reconstructPath(Map<T, T> cameFrom, T currentNode) {
		if (cameFrom.containsKey(currentNode)) {
			Path<T> p = reconstructPath(cameFrom, cameFrom.get(currentNode));
			p.add(currentNode);
			return p;
		} else {
			return new SimplePath<T>(currentNode);
		}
	}
}
