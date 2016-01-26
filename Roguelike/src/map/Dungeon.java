package map;

import game.Game;
import game.UnitTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import math.ConvexHull;
import math.Triangulation;
import math.Util;

enum Direction { UP, DOWN, LEFT, RIGHT };

public class Dungeon
{
	// Parameters
	
	public static final int NUM_CELLS = 250;
	public static final int ROOM_DIM_MIN = 3;
	public static final int ROOM_DIM_MAX = 6;
	public static final int ROOM_RADIUS = 5;
	
	public ArrayList<Rectangle> rooms;
	protected Point bounds;
	
	protected float centerX;
	protected float centerY;
	
	public Triangulation delaunay;
	
	public Dungeon()
	{
		rooms =  new ArrayList<Rectangle>();
		delaunay = new Triangulation();
	}
	
	private void generate()
	{
		generateRooms();
		//drift();
		adjustRooms();
		fill();
		//triangulate();
		mst();
		drawcorridors();
	}
	
	private void drawcorridors()
	{
		
	}
	
	private void mst()
	{
		
	}
	
	public void triangulate(BufferStrategy strategy)
	{
		ArrayList<Point2D.Float> pts = new ArrayList<Point2D.Float>();
		
		for (Rectangle r : rooms)
		{
			if (r.width > 4 && r.height > 4)
			{
				pts.add(new Point2D.Float(UnitTest.WIDTH/2 + (r.x + (float)r.width / 2) * 10, UnitTest.HEIGHT/2 + (r.y + (float)r.height / 2) * 10));
			}
		}
		
		Collections.sort(pts, new PointCompare());
		
		delaunay.triangulate(pts, strategy);
	}
	
	
	
	public void generateRooms()
	{
		long seed = 3;
		Random ayn = new Random();
		
		// Create overlapping rooms
		Point currentPoint = new Point(0,0);
		
		for (int q = 0; q < NUM_CELLS; q++)
		{
			boolean valid = true;
			Rectangle e;
			do
			{
				valid = true;
				double theta = (ayn.nextDouble() * 2 * Math.PI);
				int width = Util.gaussian(ROOM_DIM_MIN, ROOM_DIM_MAX, ayn);
				int height = Util.gaussian(ROOM_DIM_MIN, ROOM_DIM_MAX, ayn);
				int x = currentPoint.x + (int)(ROOM_RADIUS * Math.cos(theta));
				int y = currentPoint.y + (int)(ROOM_RADIUS * Math.sin(theta));
				e = new Rectangle(x, y, width, height);
				for (Rectangle r : rooms)
				{
					if (r.equals(e))
						valid = false;
				}
			}
			while (!valid);
			
			rooms.add(e);
		}
	}
	
	public void fill()
	{
		int xMax = 0;
		int yMax = 0;
		
		int xMin = 0;
		int yMin = 0;
		
		for (Rectangle r : rooms)
		// Find the corners of the dungeon
		{
			xMax = ((r.x + r.width) > xMax) ? (r.x + r.width) : xMax;
			xMax = ((r.y + r.height) > yMax) ? (r.y + r.height) : yMax;
			
			xMin = (r.x < xMin) ? r.x : xMin;
			yMin = (r.y < yMin) ? r.y : yMin;
		}
		
		for (Rectangle r : rooms)
		// Adjust so we can start at zero
		{
			r.x -= xMin;
			r.y -= yMin;
		}
		
		xMax -= xMin;
		yMax -= yMin;
		
		for (int y = 0; y < yMax; y++)
		{
			for (int x = 0; x < xMax; x++)
			{
				Rectangle s = new Rectangle(x, y, 1, 1);
				
				boolean valid = true;
				
				for(Rectangle r : rooms)
				{	
					if (r.intersects(s))
					{
						valid = false;
					}
				}
				
				if (valid)
					rooms.add(s);
			}
		}
		
		bounds = new Point(xMax, yMax);
	}
	
	/*public void drift()
	// Flock the rectangles apart until none of them touch
	{
		// Initialize the velocity table
		HashMap<Rectangle, Point> velocity = new HashMap<Rectangle, Point>();
		
		// Start it at zero
		// LITERALLY EVERY KEYSET ENTRY HAS A DEFINED VALUE
		for (Rectangle r : rooms)
		{
			velocity.put(r, new Point(0,0));
		}
		
		int sizeBefore = NUM_CELLS;
		
		// While there are collisions
		while (collisionsExist())
		{
			driftIterate(velocity);
		}
	}*/
	
	public void drift(BufferStrategy strategy)
	{
		// Initialize the velocity table
		HashMap<Rectangle, Point> velocity = new HashMap<Rectangle, Point>();
		
		// Start it at zero
		for (Rectangle r : rooms)
		{
			velocity.put(r, new Point(0,0));
		}
		
		int sizeBefore = NUM_CELLS;
		int tries = 0;
				
		// While there are collisions
		while (collisionsExist())
		{
			
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			
			center();
			
			// Try to resolve collisions
			driftIterate(velocity);
			
			// Break for debug if we think it's hung
			if (UnitTest.mousePressed)
			{
				UnitTest.mousePressed = false;
			}
			
			// Render results
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, UnitTest.WIDTH, UnitTest.HEIGHT);
			
			drawGrid(g);
			
			for (Rectangle r : rooms)
			{
				g.setColor(UnitTest.ROOMCOLORS[rooms.indexOf(r) % 8]);
				int x = UnitTest.WIDTH/2 + r.x * 10;
				int y = UnitTest.HEIGHT/2 + r.y * 10;
				g.drawRect(x, y, r.width * 10, r.height * 10);
			}
			
			g.dispose();
			strategy.show();
			
		}
		
		System.out.println("");
	}
	
	public void driftIterate(HashMap<Rectangle, Point> velocity)
	// Flock the rectangles apart until none of them touch
	{
		ArrayList<Rectangle> doomed = new ArrayList<Rectangle>();
		HashMap<Rectangle, ArrayList<Float>> vectors = new HashMap<Rectangle, ArrayList<Float>>();
		
		for (Rectangle r : rooms)
		{
			vectors.put(r, new ArrayList<Float>());
		}
		
		// For each rectangle
		for (int ir = 0; ir < rooms.size(); ir++)
		{
			Rectangle r = rooms.get(ir);
			
			// For each other rectangle, if the two collide...
			for (int is = 0; is < ir; is++)
			{
				Rectangle s = rooms.get(is);
				
				if (r.intersects(s) && r != s)
				{
					if (r.x == s.x && r.y == s.y && r.width == s.width && r.height == s.height && !doomed.contains(r) && !doomed.contains(s))
					{
						doomed.add(s);
					}
					else
					{
						// Get the distance it must travel toward the closest edge of the collider
						Point2D.Float d = driftVector(r, s);
						vectors.get(r).add(d);
						vectors.get(s).add(new Float(d.x * -1, d.y * -1));
					}
				}	
			}
		}
			
		for (Rectangle r : doomed)
		{
			rooms.remove(r);
		}
		
		for (Rectangle r : rooms)
		{
			// Average all the velocities
			if (vectors.get(r).size() > 0)
			{
				int x = 0;
				int y = 0;
				
				for (Float f : vectors.get(r))
				{
					x += f.x;
					y += f.y;
				}
							
				x = (int) Math.signum(x);
				y = (int) Math.signum(y);
				
				Point p = new Point((int)x, (int)y);
				velocity.put(r, p);
			}
		}
	
		// For each rectangle
		for (Rectangle r : rooms)
		{
			// Move according to velocity
			Point v = velocity.get(r);
			if (v == null)
				System.out.println("");
			
			r.x += v.x;
			r.y += v.y;
			
			// Wipe velocity
			velocity.remove(r);
			velocity.put(r, new Point(0,0));
		}
	}
	
	public void drawGrid(Graphics2D g)
	{
		g.setColor(Color.DARK_GRAY);
		
		for (int y = 0; y < UnitTest.HEIGHT; y += 10)
		{
			g.drawLine(0, y, UnitTest.WIDTH, y);
		}
		
		for (int x = 0; x < UnitTest.WIDTH; x += 10)
		{
			g.drawLine(x, 0, x, UnitTest.HEIGHT);
		}
		
		g.setColor(Color.WHITE);
		
		g.drawLine(UnitTest.WIDTH/2 + (int)(centerX * 10), 0, UnitTest.WIDTH/2 + (int)(centerX * 10), UnitTest.HEIGHT);
		g.drawLine(0, UnitTest.HEIGHT/2 + (int)(centerY * 10), UnitTest.WIDTH, UnitTest.HEIGHT/2 + (int)(centerY * 10));
	}
	
	private Point2D.Float driftVector(Rectangle escapee, Rectangle collider)
	{
		// Find the nearest edge
	
		// Distances
		int left = escapee.x - collider.x;
		int right = (collider.x + collider.width) - (escapee.x + escapee.width) ;
		int up = escapee.y - collider.y;
		int down = (collider.y + collider.height) - (escapee.y + escapee.height);
		
		// Find the centroids
		Float ce = new Float(escapee.x + (float)escapee.width / 2, escapee.y + (float)escapee.height / 2);
		Float cc = new Float(collider.x + (float)collider.width / 2, collider.y + (float)collider.height / 2);
		
		Rectangle2D in = escapee.createIntersection(collider);
		
		// Round, if necessary
		float hor = (float) (in.getWidth());
		float ver = (float) (in.getHeight());
		
		// Decide the direction to move
		Direction dir = null;
		Direction oppDir = null;
		if (cc.x > ce.x)
		{
			if (cc.y > ce.y)
			// I
			{
				dir = (left < up) ? Direction.LEFT : Direction.UP;
				oppDir = (right > down) ? Direction.RIGHT : Direction.DOWN;
			}
			else // (cc.y <= ce.y)
			// IV
			{
				dir = (left < down) ? Direction.LEFT : Direction.DOWN;
				oppDir = (right > down) ? Direction.RIGHT : Direction.UP;
			}
		}
		else 
		// (cc.x <= ce.x)
		{
			if (cc.y > ce.y)
			// II
			{
				dir = (right < up) ? Direction.RIGHT : Direction.UP;
				oppDir = (right > down) ? Direction.LEFT : Direction.DOWN;
			}
			else // (cc.y <= ce.y)
			// III
			{
				dir = (right < down) ? Direction.RIGHT : Direction.DOWN;
				oppDir = (right > down) ? Direction.LEFT : Direction.UP;
			}
		}
		
		if ((dir == Direction.UP && oppDir == Direction.DOWN) || (dir == Direction.DOWN && oppDir == Direction.UP))
			ver /=2;
		
		if ((dir == Direction.LEFT && oppDir == Direction.RIGHT) || (dir == Direction.RIGHT && oppDir == Direction.LEFT))
			hor /=2;
		
		int h = 0;
		int v = 0;
		//Math.abs(ce.x - centerX) > Math.abs(cc.x - centerX)
		if (Math.random() > .5)
		{
			h = (int) Math.ceil(hor);
		}
		else
		{
			h = (int) Math.floor(hor);
		}
		
		
		if (Math.random() > .5)
		{
			v = (int) Math.ceil(ver);
		}
		else
		{
			v = (int) Math.floor(ver);
		}
		
		switch(dir)
		{
		case LEFT:
			return new Float(-h, 0);
		case RIGHT:
			return new Float(h, 0);
		case UP:
			return new Float(0, -v);
		case DOWN:
			return new Float(0, v);
		}
		
		return null;
	}
	
	private void center()
	{
		centerX = 0;
		centerY = 0;
		
		for (Rectangle r : rooms)
		{
			centerX += r.x + (float)r.width / 2;
			centerY += r.y + (float)r.height / 2;
		}
		
		centerY /= rooms.size();
		centerX /= rooms.size();
	}
	
	private void adjustRooms()
	{
		int xMax = 0;
		int yMax = 0;
		
		int xMin = 0;
		int yMin = 0;
		
		for (Rectangle r : rooms)
		// Find the corners of the dungeon
		{
			xMax = ((r.x + r.width) > xMax) ? (r.x + r.width) : xMax;
			xMax = ((r.y + r.height) > yMax) ? (r.y + r.height) : yMax;
			
			xMin = (r.x < xMin) ? r.x : xMin;
			yMin = (r.y < yMin) ? r.y : yMin;
		}
		
		for (Rectangle r : rooms)
		// Adjust so we can start at zero
		{
			r.x -= xMin;
			r.y -= yMin;
		}
	}
	
	private boolean collisionsExist()
	{
		// Checks if room overlap
		for (Rectangle r : rooms)
		{
			for (Rectangle s : rooms)
			{
				if (r.intersects(s) && r != s)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	 public class PointCompare implements Comparator<Point2D.Float>
	 {
		@Override
		public int compare(Float o1, Float o2) 
		{
			if (o1.x > o2.x)
			{
				return 1;
			}
			else if (o1.x < o2.x)
			{
				return -1;
			}
			else if (o1.y > o2.y)
			{
				return 1;
			}
			else if (o1.y < o2.y)
			{
				return -1;
			}
			else return 0;
		}
	 }
}
