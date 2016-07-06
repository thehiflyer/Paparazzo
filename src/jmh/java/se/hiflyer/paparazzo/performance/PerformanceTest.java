package se.hiflyer.paparazzo.performance;

import org.openjdk.jmh.annotations.*;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import se.hiflyer.paparazzo.algorithm.AStar;
import se.hiflyer.paparazzo.interfaces.DistanceCalculator;
import se.hiflyer.paparazzo.interfaces.HeuristicEstimator;
import se.hiflyer.paparazzo.interfaces.NeighbourLookup;
import se.hiflyer.paparazzo.interfaces.Path;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@State(Scope.Benchmark)
public class PerformanceTest {

	private BufferedImage image;
	private Node[][] nodes;
	private AStar<Node> aStar;


	@Setup(Level.Trial)
	public void setUp() throws Exception {

		image = ImageIO.read(new File("src/jmh/resources/map.png"));
		nodes = new Node[image.getHeight()][image.getWidth()];
		int wallNodes = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int entry = image.getRGB(x, y);
				boolean isWall = entry != Color.WHITE.getRGB();
				nodes[y][x] = new Node(y, x, isWall);
				if (isWall) {
					wallNodes++;
				}
			}
		}
		System.out.println(wallNodes);
		System.out.println((double) wallNodes / (image.getWidth() * image.getHeight()));
		aStar = new AStar<>(new NodeHeuristicEstimator(), new NodeNeighbourLookup(), new NodeDistanceCalculator());
	}


	static class Node {
		final int row;
		final int col;
		final boolean isWall;

		public Node(int row, int col, boolean isWall) {
			this.row = row;
			this.col = col;
			this.isWall = isWall;
		}

	}



	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Path<Node> searchAlmostStraightPath() throws IOException {
		return aStar.search(getNode(178, 96), getNode(355, 151));
	}

	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Path<Node> searchAroundObstacles() throws IOException {
		return aStar.search(getNode(102, 90), getNode(20, 20));
	}

	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Path<Node> searchManyFalse() throws IOException {
		return aStar.search(getNode(123, 294), getNode(105, 293));
	}

	private Node getNode(int x, int y) {
		if (x > 0 && x < image.getWidth() && y > 0 && y < image.getHeight()) {
			return nodes[y][x];
		} else {
			return null;
		}
	}

	private class NodeHeuristicEstimator implements HeuristicEstimator<Node> {
		@Override
		public double estimate(Node start, Node goal) {
			return Math.abs(start.col - goal.col) + Math.abs(start.row - goal.row);
		}
	}

	private class NodeNeighbourLookup implements NeighbourLookup<Node> {
		private LoadingCache<Node, Iterable<Node>> cache = CacheBuilder.newBuilder().build(new CacheLoader<Node, Iterable<Node>>() {
			@Override
			public Iterable<Node> load(Node key) throws Exception {
				List<Node> neighbours = new ArrayList<>();
				for (int x = -1; x < 2; x++) {
					for (int y = -1; y < 2; y++) {
						if (x != 0 && y != 0) {
							Node neighbour = getNode(key.col + x, key.row + y);
							if (neighbour != null) {
								neighbours.add(neighbour);
							}
						}
					}
				}
				return neighbours;
			}
		});

		@Override
		public Iterable<Node> getNeighbours(Node node) {
			return cache.getUnchecked(node);
		}
	}

	private class NodeDistanceCalculator implements DistanceCalculator<Node> {
		@Override
		public double getDistanceBetween(Node x, Node y) {
			return Math.abs(x.col - y.col) + Math.abs(x.row - y.row);
		}
	}
}

