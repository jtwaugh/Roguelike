package map;

import game.Game;
import gui.DialogBox;
import gui.Window;
import hero.Player;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import language.NameGen;
import math.Util;

public class NodeMap
{
	// Parameters
	
	public static final int MIN_NODE_DIST = 50;
	public static final int MAX_NODE_DIST = 200;
	
	public static final int MIN_TERMINAL_DIST = 20;
	public static final int MAX_TERMINAL_DIST = 25;
	
	public static final float ANGLE_OFFSET = 2;
	
	public static final int MAX_NODES = 20;
	
	
	// Members
	
	protected ArrayList<Node> nodes;
	protected ArrayList<Connection> connections;
	protected Node startNode;
	protected Node currentNode;
	protected Node endNode;
	
	protected WindowHandler windows;
	
	protected Player steve;
	
	public NodeMap(Player _steve)
	{
		// Initialize
		steve = _steve;

		PathCreator p = new PathCreator();
		p.create();
		
		steve.mapSprite().setNode(currentNode);
		
		windows = new WindowHandler();
}	
	
	public void logic()
	{
		windows.updateInterface();
	}
	
	public void render(Graphics2D g)
	{
		g.setColor(new Color(90, 130, 90, 255));
		g.fillRect(0,  0, Game.WIDTH, Game.HEIGHT);
		
		for (Connection c : connections)
		{
			c.render(g, Color.DARK_GRAY);
		}
		
		if (currentNode != null)
		{
			g.setColor(new Color(255, 220, 200, 64));
			g.fillOval(currentNode.getLoc().x - 10, currentNode.getLoc().y - 10, 20, 20);
			for (Connection c : currentNode.getConnections())
			{
				c.render(g, Color.WHITE);
			}
		}
		
		
		for (Node n : nodes)
		{
			n.render(g);
		}
		
		steve.mapRender(g);
		
		windows.drawWindows(g);
	}

	public void mousePressHandle(MouseEvent e)
	{
		windows.mousePressHandle(e);
	}
	
	public void mouseReleaseHandle(MouseEvent e)
	{
		if (!windows.mouseReleaseHandle(e))
		{
			clickOnNodes(e);
		}
	}
	
	public void mouseDragHandle(MouseEvent e)
	{
		windows.mouseDragHandle(e);
	}
	
	public void keyPressHandle(KeyEvent e)
	{
		windows.keyPressHandle(e);
	}
	
	public void keyReleaseHandle(KeyEvent e)
	{
		windows.keyReleaseHandle(e);
	}
	
	public Node clicksOn(Point p)
	{
		for (Node n : nodes)
		{
			int r = (int)Math.sqrt(n.getSprite().getWidth() * n.getSprite().getWidth() + n.getSprite().getHeight() * n.getSprite().getHeight());
			if (Point.distance(p.x, p.y, n.getLoc().x, n.getLoc().y) < r) return n;
		}
		
		return null;
	}
	
	private void clickOnNodes(MouseEvent e)
	{
		Point p = e.getPoint();
		
		Node n = clicksOn(p);
		
		if (n != null)
		{
			if (steve.mapSprite().getNode().getNodes().contains(n))
			{
				currentNode = n;
				int x = (p.x < 0) ? 0 : p.x;
				x = (x > (Game.WIDTH - windows.nodeWindows.get(currentNode).getWidth())) ? (Game.WIDTH - windows.nodeWindows.get(currentNode).getWidth()) : x;
				
				int y = (p.y < 0) ? 0 : p.y;
				y = (y > (Game.HEIGHT - windows.nodeWindows.get(currentNode).getHeight())) ? (Game.HEIGHT - windows.nodeWindows.get(currentNode).getHeight()) : y;
				
				windows.nodeWindows.get(currentNode).setPosition(x, y);
				windows.nodeWindows.get(currentNode).setOpened(true);	
			}
		}
	}

	private class WindowHandler
	{
		public ArrayList<Window> windowQueue;
		public HashMap<Node, Window> nodeWindows;
		
		public WindowHandler()
		{
			windowQueue = new ArrayList<Window>();
			nodeWindows = new HashMap<Node, Window>();
			
			for (Node n : nodes)
			{
				Window w = new DialogBox(150, 160, "nice", "nice", NameGen.getTownName(), n.getLoc(), new String[] {"lolno", "Visit"}, Color.WHITE);
				nodeWindows.put(n, w);
			}
		}
		
		public void drawWindows(Graphics2D g)
		{
			for (Node n : nodeWindows.keySet())
			{
				if (nodeWindows.get(n).getOpened())
				{
					nodeWindows.get(n).draw(g);
				}
			}
			
			for (Window w : windowQueue)
			{
				if (w.getOpened())
				{
					w.draw(g);
				}
			}
		}
		
		public boolean keyPressHandle(KeyEvent e)
		{
			if (windowQueue.size() > 0)
			{
				return windowQueue.get(windowQueue.size()-1).keyPressHandle(e);
			}
			return false;
		}
		
		public boolean keyReleaseHandle(KeyEvent e)
		{
			if (windowQueue.size() > 0)
			{
				return windowQueue.get(windowQueue.size()-1).keyReleaseHandle(e);
			}
			return false;
		}
		
		public boolean mousePressHandle(MouseEvent e)
		{
			for (Node n : nodeWindows.keySet())
			{
				if (nodeWindows.get(n).mousePressHandle(e))
					return true;
			}
			
			return false;
		}
		
		public boolean mouseReleaseHandle(MouseEvent e)
		{
			for (Node n : nodeWindows.keySet())
			{
				if (nodeWindows.get(n).mouseReleaseHandle(e))
				{
					return true;
				}
			}
			
			return false;
		}
		
		public void mouseDragHandle(MouseEvent e)
		{
			for (Node n : nodeWindows.keySet())
			{
				if (nodeWindows.get(n).getPressed())
				{
					int x = Game.mousePos.x - Game.oldMousePos.x;
					int y = Game.mousePos.y - Game.oldMousePos.y;
					nodeWindows.get(n).move(x, y);
				}
			}
		}
		
		public void updateInterface()
		{
			windowQueue.clear();
			
			Window w;
			
			for (Node n : nodeWindows.keySet())
			{
				w = nodeWindows.get(n);
				if (w.getClosed())
				{
					if (w.getExitChoice().equals("Visit"))
					{
						steve.mapSprite().setNode(n);
					}
					else
					{
						currentNode = steve.mapSprite().getNode();
					}
					
					w.reset();
				}
				else if (w.getOpened())
				{
					if (n != currentNode)
					{
						w.reset();
					}
					else
					{
						windowQueue.add(w);
					}
				}
			}
			
			for (int q = 0; q < windowQueue.size(); q++)
			{
				w = windowQueue.get(q);
				w.update();
				
				if (w.getClosed())
				{
					windowQueue.remove(q);
					q--;
				}
			}
		}
	}

	private class PathCreator
	{
		private void create()
		{
			nodes = new ArrayList<Node>();
			connections = new ArrayList<Connection>();
			ArrayList<Node> path = new ArrayList<Node>();
			
			// Generate a path from start to end, add extraneous nodes
			
			// Create the diametrically opposing terminal nodes
			startNode = createTerminalNode();
			endNode = new Node(Game.WIDTH - startNode.getLoc().x, Game.HEIGHT - startNode.getLoc().y);
			path.add(startNode);
			
			// Create a path between them
			currentNode = startNode;
			Point p = startNode.getLoc();
			Point q = endNode.getLoc();
			
			
			// Create the initial path
			
			while (Point.distance(p.x, p.y, q.x, q.y) > MAX_NODE_DIST)
			// While the current point is out of range of the end node
			{
				// Get a new node and add it
				Node n = createPath(p, q, path);
				path.add(n);
				
				// Move on
				currentNode = n;
				p = currentNode.getLoc();
			}
			
			path.add(endNode);
		
			System.out.println("Path created.");
			
			
			// Add path nodes to the set of all nodes
			addNodes(path);
			
			System.out.println("Nodes added.");
		
			
			// Draw new paths to the original until we have enough nodes
			int killswitch = 0;
			while (nodes.size() < MAX_NODES)
			{
				integrateNode(path, p);
				killswitch++;
				if (killswitch > 1000)
				{
					System.out.println("Integration loop broken.");
					break;
				}
			}
			
			System.out.println("Integration loop completed.");
			
			currentNode = nodes.get(0);
			
			// Connect all nodes
			connectNodes();
			
			System.out.println("Nodes connected.");
			
			
			// Assign node types
			assignNodeTypes();
			
			System.out.println("Node types assigned.");
		}
	
		private void addNodes(ArrayList<Node> path)
		{
			for (int w = 0; w < path.size(); w++)
			{
				if (w > 0)
				{
					Connection c = new Connection(path.get(w), path.get(w-1));
					if (!handleIntersection(c))
					{
						connections.add(c);
					}
				}
				if (!nodes.contains(path.get(w)))
					nodes.add(path.get(w));
			}
		}
		
		private void assignNodeTypes()
		{
			for (Node n : nodes)
			{
				int l = (int)(Math.random() * 10);
				if (l == 0)
				{
					n.setType(NodeType.Dungeon);
				}
				else
				{
					n.setType(NodeType.Town);
				}
			}
		}
		
		private void connectNodes()
		{
			for (Connection c : connections)
			{
				c.a.addNode(c.b);
				c.a.addConnection(c);
				c.b.addNode(c.a);
				c.b.addConnection(c);
			}
		}
		
		private Node createPath(Point p, Point q, ArrayList<Node> path)
		{
			float theta = (float) Math.atan2(q.y - p.y, q.x - p.x);
			
			int x = -9001;
			int y = -9001;
			
			ArrayList<Node> nodez = nodes;
			nodez.remove(endNode);
			
			do
			{
				Point a = wander(x, y, theta, p);
				x = a.x;
				y = a.y;
			}
			while (Point.distance(x, y, q.x, q.y) < MIN_NODE_DIST || intersects(path, x, y) != null || intersects(nodez, x, y) != null || (x <= 0) || (x >= Game.WIDTH) || (y <= 0 ) || (y >= Game.HEIGHT));
			
			// Create the node, add it to the path
			return new Node(x, y);
		}
		
		private Point wander(int x, int y, float theta, Point p)
		{
			// Find a random distance and angular offset
			float r = Util.Int(MIN_NODE_DIST, MAX_NODE_DIST);
			float t = Util.Float(theta - ANGLE_OFFSET, theta + ANGLE_OFFSET);
			
			// Create the point
			x = p.x + (int)(r * Math.cos(t));
			y = p.y + (int)(r * Math.sin(t));
			
			return new Point(x, y);
		}
			
		private void integrateNode(ArrayList<Node> path, Point p)
		{
			path = new ArrayList<Node>();
			
			do
			{
				startNode = createTerminalNode();
			}
			while (intersects(nodes, startNode.getLoc().x, startNode.getLoc().y) != null);
			
			currentNode = startNode;
			p = startNode.getLoc();
			
			while (isolated(nodes, p.x, p.y) == null)
			// While the current point is out of range of any other node
			{
				Node n = generateNode(path, p);
				path.add(n);
				currentNode = n;
				p = currentNode.getLoc();
			}
			System.out.println("Generation loop completed.");
			
			path.add(isolated(nodes, p.x, p.y));
			
			// Add path nodes to the set of all nodes
			addNodes(path);
		}
		
		private Node generateNode(ArrayList<Node> path, Point p)
		{
			int x, y;
			int i = 0;
			do
			{
				i++;
				
				if (i > 1000)
				{
					System.out.println("Broken loop");
				}
					
				// Find a random distance and angular offset
				float r = Util.Int(MIN_NODE_DIST, MAX_NODE_DIST);
				float t = (float)(Math.random() * 2 * Math.PI);
				
				// Create the point
				x = p.x + (int)(r * Math.cos(t));
				y = p.y + (int)(r * Math.sin(t));
			}
			while (intersects(path, x, y) != null || intersects(nodes, x, y) != null || (x <= MIN_NODE_DIST) || (x >= Game.WIDTH - MIN_NODE_DIST) || (y <= MIN_NODE_DIST) || (y >= Game.HEIGHT - MIN_NODE_DIST));
			
			System.out.println("Node generated.");
			
			// Create the node, add it to the path
			return new Node(x, y);
		}
		
		public Node createTerminalNode()
		// Within range of the exterior
		{	
			int w = (int)(Math.random() * 4);
			
			int x = -9001;
			int y = -9001;
			
			switch (w)
			{
				case (0):
				{
					// North
					x = (int)(Math.random() * Game.WIDTH);
					y = Game.HEIGHT - Util.Int(MIN_TERMINAL_DIST, MAX_TERMINAL_DIST);
					break;
				}
				
				case (1):
				{
					// East
					x = Game.WIDTH - Util.Int(MIN_TERMINAL_DIST, MAX_TERMINAL_DIST);
					y = (int)(Math.random() * Game.HEIGHT);
					break;
				}
				
				case (2):
				{
					// South
					x = (int)(Math.random() * Game.WIDTH);
					y = Util.Int(MIN_TERMINAL_DIST, MAX_TERMINAL_DIST);
					break;
				}
				
				case (3):
				{
					// West
					x = Util.Int(MIN_TERMINAL_DIST, MAX_TERMINAL_DIST);
					y = (int)(Math.random() * Game.HEIGHT);
					break;
				}
			}
			
			return new Node(x, y);
		}

		public Node intersects(ArrayList<Node> nodes, int x, int y)
		{
			for (Node n : nodes)
			{
				if (Point.distance(x, y, n.getLoc().x, n.getLoc().y) < MIN_NODE_DIST) return n;
			}
			
			return null;
		}

		public Node isolated(ArrayList<Node> nodes, int x, int y)
		{
			for (Node n : nodes)
			{
				if (Point.distance(x, y, n.getLoc().x, n.getLoc().y) < MAX_NODE_DIST) return n;
			}
			
			return null;
		}
		
		public boolean handleIntersection(Connection c)
		{
			boolean ret = false;
			
			ArrayList<Connection> delete = new ArrayList<Connection>();
			
			for (int w = 0; w < connections.size(); w++)
			{
				Connection d = connections.get(w);
				
				if (!shareNodes(c, d) && c.toLine().intersectsLine(d.toLine()))
				{
					delete.add(d);
					if (distanceBetween(c.a, d.a) < MAX_NODE_DIST)
					{
						connections.add(new Connection(c.a, d.a));
					}
					if (distanceBetween(c.a, d.b) < MAX_NODE_DIST)
					{
						connections.add(new Connection(c.a, d.b));
					}
					if (distanceBetween(c.b, d.a) < MAX_NODE_DIST)
					{
						connections.add(new Connection(c.b, d.a));
					}
					if (distanceBetween(c.b, d.b) < MAX_NODE_DIST)
					{
						connections.add(new Connection(c.b, d.b));
					}
					ret = true;
				}
			}
			
			for (Connection w : delete)
			{
				connections.remove(w);
			}
			
			return ret;
		}
		
		public float distanceBetween(Node a, Node b)
		{
			return (float) Point.distance(a.getLoc().x, a.getLoc().y, b.getLoc().x, b.getLoc().y);
		}
		
		public boolean shareNodes(Connection a, Connection b)
		{
			return (a.a == b.a || a.a == b.b || a.b == b.a || a.b == b.b);
		}
	}
}