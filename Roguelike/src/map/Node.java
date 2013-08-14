package map;

import game.Game;
import graphics.Sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class Node
{
	protected Point loc;
	protected ArrayList<Node> adjacencies;
	protected ArrayList<Connection> connections;
	
	protected NodeType type;
	
	public Node(int x, int y)
	{
		loc = new Point(x, y);
		adjacencies = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
	}
	
	public Sprite getSprite()
	{
		switch (type)
		{
		case Town:
		{
			return Game.RESOURCES.getSprite("town");
		}
		case Dungeon:
		{
			return Game.RESOURCES.getSprite("dungeon");
		}
		}
		
		return null;
	}
	
	public void render(Graphics2D g)
	{
		Sprite s = getSprite();
		
		s.setPosition(loc.x - s.getWidth() / 2, loc.y - s.getHeight() / 2);
		
		s.draw(g);
	}
	
	public Point getLoc()
	{
		return loc;
	}
	
	public void addNode(Node n)
	{
		adjacencies.add(n);
	}
	
	public ArrayList<Node> getNodes()
	{
		return adjacencies;
	}
	
	public void addConnection(Connection c)
	{
		connections.add(c);
	}
	
	public ArrayList<Connection> getConnections()
	{
		return connections;
	}
	
	public void setType(NodeType t)
	{
		type = t;
	}
}



class Connection
{
	public Node a;
	public Node b;
	
	public Connection(Node m, Node n)
	{
		a = m;
		b = n;
	}
	
	public void render(Graphics2D g, Color c)
	{
		g.setColor(c);
		g.drawLine(a.getLoc().x,a.getLoc().y, b.getLoc().x, b.getLoc().y);
	}
	
	public Line2D toLine()
	{
		return new Line2D.Float(a.getLoc().x,a.getLoc().y, b.getLoc().x, b.getLoc().y);
	}
}