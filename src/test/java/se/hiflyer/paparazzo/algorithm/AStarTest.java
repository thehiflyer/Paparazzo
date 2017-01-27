package se.hiflyer.paparazzo.algorithm;

import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.googlecode.gentyref.TypeToken;
import org.junit.Test;
import se.hiflyer.paparazzo.impl.Paths;
import se.hiflyer.paparazzo.interfaces.DistanceCalculator;
import se.hiflyer.paparazzo.interfaces.HeuristicEstimator;
import se.hiflyer.paparazzo.interfaces.NeighbourLookup;
import se.hiflyer.paparazzo.interfaces.Path;
import se.mockachino.CallHandler;
import se.mockachino.MethodCall;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static se.mockachino.Mockachino.mock;
import static se.mockachino.Mockachino.when;
import static se.mockachino.matchers.Matchers.any;

public class AStarTest {
	private TypeToken<HeuristicEstimator<String>> HEURISTIC_TOKEN = new TypeToken<HeuristicEstimator<String>>() {};
	private TypeToken<NeighbourLookup<String>> NEIBOURLOOKUP_TOKEN = new TypeToken<NeighbourLookup<String>>() {};
	private TypeToken<DistanceCalculator<String>> DISTANCECALCULATOR_TOKEN = new TypeToken<DistanceCalculator<String>>() {};

	@Test
	public void findAPath() throws Exception {
		String start = "start";
		String goal = "goal";
		String a = "a";
		String b = "b";
		String c = "c";
		String d = "d";
		String e = "e";
		HeuristicEstimator<String> estimator = mock(HEURISTIC_TOKEN);
		when(estimator.estimate(a, goal)).thenReturn(4.0);
		when(estimator.estimate(b, goal)).thenReturn(2.0);
		when(estimator.estimate(c, goal)).thenReturn(4.0);
		when(estimator.estimate(d, goal)).thenReturn(4.5);
		when(estimator.estimate(e, goal)).thenReturn(2.0);

		NeighbourLookup<String> neighbourLookup = mock(NEIBOURLOOKUP_TOKEN);
		when(neighbourLookup.getNeighbours(start)).thenReturn(Lists.newArrayList(a, d));
		when(neighbourLookup.getNeighbours(a)).thenReturn(Lists.newArrayList(start, b));
		when(neighbourLookup.getNeighbours(b)).thenReturn(Lists.newArrayList(a, c));
		when(neighbourLookup.getNeighbours(c)).thenReturn(Lists.newArrayList(b, goal));
		when(neighbourLookup.getNeighbours(d)).thenReturn(Lists.newArrayList(start, e));
		when(neighbourLookup.getNeighbours(e)).thenReturn(Lists.newArrayList(d, goal));
		when(neighbourLookup.getNeighbours(goal)).thenReturn(Lists.newArrayList(e, c));

		DistanceCalculator<String> distanceCalculator = mock(DISTANCECALCULATOR_TOKEN);
		final Table<String, String, Double> dist = HashBasedTable.create();
		dist.put(start, a, 1.5);
		dist.put(a, b, 2.0);
		dist.put(c, b, 3.0);
		dist.put(c, goal, 4.0);
		dist.put(start, d, 2.0);
		dist.put(e, d, 3.0);
		dist.put(e, goal, 2.0);


		when(distanceCalculator.getDistanceBetween(any(String.class), any(String.class))).thenAnswer(new CallHandler() {
			@Override
			public Object invoke(Object o, MethodCall methodCall) throws Throwable {
				Object[] arguments = methodCall.getArguments();
				String n1 = (String) arguments[0];
				String n2 = (String) arguments[1];
				if (dist.contains(n1, n2)) {
					return dist.get(n1, n2);
				} else if (dist.contains(n2, n1)) {
					return dist.get(n2, n1);
				}
				throw new IllegalArgumentException(String.format("Can't find entry for %s and %s", n1, n2));
			}
		});

		AStar<String> aStar = new AStar<String>(estimator, neighbourLookup, distanceCalculator);

		Path<String> path = aStar.search(start, goal);
		assertFalse(path == Paths.FAIL);
		assertNotNull(path);

		Iterator<String> iterator = path.iterator();
		assertEquals(start, iterator.next());
		assertEquals(d, iterator.next());
		assertEquals(e, iterator.next());
		assertEquals(goal, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void biggerMap() throws Exception {
		final Table<Integer, Integer, Integer> table = HashBasedTable.create();

		table.put(3, 4, 1);
		table.put(3, 5, 1);
		table.put(3, 6, 1);
		table.put(4, 6, 1);
		table.put(5, 6, 1);
		table.put(6, 6, 1);
		table.put(7, 6, 1);
		table.put(8, 6, 1);
		table.put(8, 5, 1);
		table.put(8, 4, 1);
		table.put(8, 3, 1);
		table.put(8, 2, 1);
		table.put(8, 1, 1);

		HeuristicEstimator<Pos> estimator = new HeuristicEstimator<Pos>() {
			@Override
			public double estimate(Pos start, Pos goal) {
				return Math.abs(start.row - goal.row) + Math.abs(start.col - goal.col);
			}
		};
		NeighbourLookup<Pos> neighbourLookup = new NeighbourLookup<Pos>() {
			@Override
			public Iterable<Pos> getNeighbours(Pos p) {
				List<Pos> neighbours = Lists.newArrayList();
				neighbours.add(new Pos(p.row - 1, p.col));
				neighbours.add(new Pos(p.row + 1, p.col));
				neighbours.add(new Pos(p.row, p.col - 1));
				neighbours.add(new Pos(p.row, p.col + 1));

				return Iterables.filter(neighbours, new Predicate<Pos>() {
					@Override
					public boolean apply(Pos p) {
						//return p.row >= 0 && p.row < 10 && p.col >= 0 && p.col < 10;
						Integer integer = table.get(p.row, p.col);
						boolean walkable = integer == null || integer == 0;
						return p.row >= 0 && p.row < 10 && p.col >= 0 && p.col < 10 && walkable;
					}
				});
			}
		};
		DistanceCalculator<Pos> distanceCalculator = new DistanceCalculator<Pos>() {
			@Override
			public double getDistanceBetween(Pos start, Pos goal) {
				return Math.abs(start.row - goal.row) + Math.abs(start.col - goal.col);
			}
		};
		AStar<Pos> aStar = new AStar<Pos>(estimator, neighbourLookup, distanceCalculator);

		Pos goal = new Pos(9, 9);
		Path<Pos> path = aStar.search(new Pos(4, 4), goal);
		assertNotNull(path);
		assertFalse(path == Paths.FAIL);
		Pos last = Iterables.getLast(path);
		assertEquals(goal, last);
//		for (Pos pos : path) {
//			System.out.println(String.format("%d, %d", pos.col, pos.row));
//		}
	}

	static class Pos {
		int row;
		int col;

		Pos(int row, int col) {
			this.row = row;
			this.col = col;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Pos pos = (Pos) o;

			if (row != pos.row) return false;
			if (col != pos.col) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = row;
			result = 31 * result + col;
			return result;
		}

		@Override
		public String toString() {
			return "Pos{" +
					"row=" + row +
					", col=" + col +
					'}';
		}
	}
}
