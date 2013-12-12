package map;

import game.Game;
import game.UnitTest;

import java.awt.Color;
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
import java.util.List;
import java.util.Random;

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
	
	protected ArrayList<Line2D> graph;
	
	public Dungeon()
	{
		rooms =  new ArrayList<Rectangle>();
	}
	
	private void generate()
	{
		generateRooms();
		//drift();
		adjustRooms();
		fill();
		triangulate();
		mst();
		drawcorridors();
	}
	
	private void drawcorridors()
	{
		
	}
	
	private void mst()
	{
		
	}
	
	public void triangulate()
	{
		ArrayList<Point2D.Float> pts = new ArrayList<Point2D.Float>();
		graph = new ArrayList<Line2D>();
		for (Rectangle r : rooms)
		{
			pts.add(new Point2D.Float(r.x + (float)r.width / 2, r.y + (float)r.height / 2));
		}
		
		Collections.sort(pts, new PointCompare());
		

		delaunay(pts);
	}
	
	public void delaunay(List<Point2D.Float> pts)
	{
		int size = pts.size();
		
		if (size == 2)
		{
			graph.add(new Line2D.Float(pts.get(0), pts.get(1)));
		}
		else if (size == 3)
		{
			graph.add(new Line2D.Float(pts.get(0), pts.get(1)));
			graph.add(new Line2D.Float(pts.get(1), pts.get(2)));
			graph.add(new Line2D.Float(pts.get(2), pts.get(0)));
		}
		else
		{
			int splitPt = (int)(size / 2);
			List<Point2D.Float> left = pts.subList(0, splitPt);
			List<Point2D.Float> right = pts.subList(splitPt, pts.size());
			delaunay(left);
			delaunay(right);
			
			// Get convex hull
			ArrayList<Float> hull = new ArrayList<Float>();
			Float[] top = null;
			Float[] bot = null;
			
			// Find the bottom point
			Float bottom = left.get(0);
			
			for (Float f : pts)
			{
				bottom = (f.y < bottom.y) ? f : bottom;
			}
			
			hull.add(bottom);
			
			Float min = right.get(0);
			
			// Add points to the hull
			do
			{
				int q = 0;
				
				for (Float f : pts)
				{
					if (!hull.contains(f))
					{
						double theta = Math.atan2(f.y - hull.get(q).y, f.x - hull.get(q).x);
						double mintheta = Math.atan2(min.y - hull.get(q).y, min.x - hull.get(q).x);
						min = (theta < mintheta) ? f : min;
						q++;
					}
				}
				
				if (min == bottom)
					break;
				
				if (hull.size() > 1)
				{
					double thisAngle = Math.atan2(min.y - hull.get(q).y, min.x - hull.get(q).x);
					double lastAngle = Math.atan2(hull.get(q).y - hull.get(q-1).y, hull.get(q).x - hull.get(q-1).x);
					
					if (lastAngle < Math.PI && thisAngle >= Math.PI)
						// Top tangent
						top = new Float[] {hull.get(q), min};
					
					if (lastAngle < 0 && thisAngle >= 0)
						// Bottom tangent
						bot = new Float[] {hull.get(q), min};
						// 0 is left, 1 is right
				}
					
				hull.add(min);
			}
			while (true);
			
			Float l1 = null;
			Float l2 = null;
			Float r1 = null;
			Float r2 = null;
			
			do
			{	
				// Get candidates #1 and #2, by smallest angle below PI formed by f and the bottom tangent
				// Right side
				
				double base = Math.atan2(bot[1].y - bot[0].y, bot[1].x - bot[1].x);
				
				for (Float f : right)
				{
					double thetaF = Math.atan2(f.y - bot[0].y, f.x - bot[0].x) - base;
					
					if (thetaF < 2*Math.PI)
					{
						double theta1 = (r1 == null) ? 3 : Math.atan2(r1.y - bot[0].y, r1.x - bot[0].x) - base;
						double theta2 = (r2 == null) ? 3 : Math.atan2(r2.y - bot[0].y, r2.x - bot[0].x) - base;
						
						if (thetaF < theta1)
						{
							r2 = r1;
							r1 = f;
						}
						else if (thetaF < theta2)
						{
							r2 = f;
						}
					}
				}
				
				// Left side
				
				for (Float f : right)
				{
					double thetaF = Math.atan2(f.y - bot[0].y, f.x - bot[0].x) - base;
					
					if (thetaF < 2*Math.PI)
					{
						double theta1 = (l1 == null) ? 3 : Math.atan2(l1.y - bot[0].y, l1.x - bot[0].x) - base;
						double theta2 = (l2 == null) ? 3 : Math.atan2(l2.y - bot[0].y, l2.x - bot[0].x) - base;
						
						if (thetaF < theta1)
						{
							l2 = l1;
							l1 = f;
						}
						else if (thetaF < theta2)
						{
							l2 = f;
						}
					}
				}
				
				// Check if #1's circumcircle contains #2
				if (inCircumcircle(bot[0], bot[1], r1, r2))
				{
					// If r2 is contained by the circumcircle, delete the segment between r1 and bottom
					for (int q = 0; q < graph.size(); q++)
					{
						if (isEquivalent((Line2D.Float)graph.get(q), new Line2D.Float(bot[0].x, bot[0].y, r1.x, r1.y)))
						{
							graph.remove(q);
						}
					}
				}
				
				if (inCircumcircle(bot[0], bot[1], l1, l2))
				{
					// If l2 is contained by the circumcircle, delete the segment between l1 and bottom
					for (int q = 0; q < graph.size(); q++)
					{
						if (isEquivalent((Line2D.Float)graph.get(q), new Line2D.Float(bot[1].x, bot[1].y, l1.x, l1.y)))
						{
							graph.remove(q);
						}
					}
				}
				
				// Choose the candidate whose circumcircle does not contain the other, connect it to the opposite bottom point
				
				if (!inCircumcircle(bot[0], bot[1], l1, r1))
				{
					// Use left
					bot = new Float[] {l1, bot[1]};
				}
				else if (!inCircumcircle(bot[0], bot[1], r1, l1))
				{
					// Use right
					bot = new Float[] {bot[0], r1};
				}
				else
				{
					// This shouldn't happen
				}
				
				// Set the created edge as the base, add it to the graph
				graph.add(new Line2D.Float(bot[0].x, bot[0].y, bot[1].x, bot[1].y));
				
				
				
				// Do this with the right and left sides until neither produces a candidate
			}
			while((l1 != null) || (r1 != null));
		}
	}
	
	private boolean isEquivalent(Line2D.Float a, Line2D.Float b)
	{
		return (((a.getY1() == b.getY1() && a.getX1() == b.getX1() && (a.getY2() == b.getY2() && a.getX2() == b.getX2()))) || ((a.getY1() == b.getY2() && a.getX1() == b.getX2()) && (a.getY2() == b.getY1() && a.getX2() == b.getX1())));
	}
	
	private boolean inCircumcircle(Float a, Float b, Float c, Float candidate)
	{
		Float center = circumcenter(a, b, c);
		return (Float.distance(center.x, center.y, candidate.x, candidate.y) < Float.distance(center.x, center.y, c.x, c.y));
	}
	
	private Float circumcenter(Float a, Float b, Float c)
	{
		// Feeble 1:15 AM attempt at line intersection
		Float midAB = new Float((a.x + b.x) / 2, (a.y + b.y) / 2);
		Float midAC = new Float((a.x + c.x) / 2, (a.y + c.y) / 2);
		
		float slopeAB = -((b.y - a.y) / (b.x - a.x));
		float slopeAC = -((c.y - a.y) / (c.x - a.x));
		
		float yAB = a.y - slopeAB * a.x;
		float yAC = a.y - slopeAC * a.x;
		
		float x = (yAC - yAB) / (slopeAB - slopeAC);
		float y = x * slopeAB + yAB;
		
		return new Float(x, y);
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
