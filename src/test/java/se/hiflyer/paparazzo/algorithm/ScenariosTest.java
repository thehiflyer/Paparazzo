package se.hiflyer.paparazzo.algorithm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import se.hiflyer.paparazzo.impl.Paths;
import se.hiflyer.paparazzo.interfaces.DistanceCalculator;
import se.hiflyer.paparazzo.interfaces.HeuristicEstimator;
import se.hiflyer.paparazzo.interfaces.NeighbourLookup;
import se.hiflyer.paparazzo.interfaces.Path;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class ScenariosTest {

	private BufferedImage image;
	private Node[][] nodes;
	private AStar<Node> aStar;


	@Before
	public void setUp() throws Exception {

		image = ImageIO.read(new File("src/test/resources/map.png"));
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
		int size = image.getWidth() * image.getHeight();
		System.out.println(wallNodes);
		System.out.println(size);
		System.out.println((double) wallNodes / size);
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



	@Test
	public void searchAroundWall() throws IOException, InterruptedException {
		Node start = getNode(20, 33);
		Node goal = getNode(10, 17);
		Path<Node> search = aStar.search(start, goal);

		assertNotEquals(search, Paths.FAIL);
		assertTrue(Iterables.contains(search, getNode(5, 18)));
	}

	private void displaySearch(final Node start, final Node goal, final Path<Node> search) throws InterruptedException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(image.getWidth()* 4, image.getHeight()* 4);
		frame.setVisible(true);
		JPanel canvas = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g.drawImage(image, 0, 0, image.getWidth() * 4, image.getHeight() * 4, null);

				g.setColor(Color.red);
				for (Node node : search) {
					drawNode(g, node);
				}
				g.setColor(Color.green);
				drawNode(g, start);
				drawNode(g, goal);
			}
		};
		frame.getContentPane().add(canvas);
		Thread.sleep(10000);
	}

	private void drawNode(Graphics g, Node start) {
		g.drawRect(start.col * 4, start.row * 4, 4, 4);
	}

	@Test
	public void searchAroundObstacles() throws Exception {
		Node start = getNode(14, 63);
		Node goal = getNode(24, 36);
		Path<Node> search = aStar.search(start, goal);

		assertNotEquals(search, Paths.FAIL);
		assertTrue(Iterables.contains(search, getNode(31, 78)));
	}

	@Test
	public void searchIntoTheHeart() throws Exception {
		Node start = getNode(52, 43);
		Node goal = getNode(70, 18);
		Path<Node> search = aStar.search(start, goal);

		assertNotEquals(search, Paths.FAIL);
		assertTrue(Iterables.contains(search, getNode(65, 18)));
	}

	@Test
	public void impossibleSearch() throws IOException {
		Path<Node> search = aStar.search(getNode(42, 82), getNode(72, 72));
		assertEquals(search, Paths.FAIL);
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
						if (!(x == 0 && y == 0)) {
							Node neighbour = getNode(key.col + x, key.row + y);
							if (neighbour != null && !neighbour.isWall) {
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

