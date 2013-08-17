package map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import math.Util;

public class Dungeon
{
	// Parameters
	
	public static final int NUM_CELLS = 150;
	public static final int ROOM_DIM_MIN = 4;
	public static final int ROOM_DIM_MAX = 9;
	public static final int ROOM_RADIUS = (int)(NUM_CELLS / 20);
	
	protected ArrayList<Rectangle> rooms;
	protected Point bounds;
	
	public Dungeon()
	{
		rooms =  new ArrayList<Rectangle>();
	}
	
	private void generate()
	{
		generateRooms();
		drift();
		fill();
	}
	
	private void generateRooms()
	{
		// Create overlapping rooms
		Point currentPoint = new Point(0,0);
		
		for (int q = 0; q < NUM_CELLS; q++)
		{
			double theta = (Math.random() * 2 * Math.PI);
			int width = Util.gaussian(ROOM_DIM_MIN, ROOM_DIM_MAX);
			int height = Util.gaussian(ROOM_DIM_MIN, ROOM_DIM_MAX);
			int x = currentPoint.x + (int)(Math.cos(theta));
			int y = currentPoint.y + (int)(Math.sin(theta));
			
			rooms.add(new Rectangle(x, y, width, height));
		}
	}
	
	private void fill()
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
	
	private void drift()
	{
		// Flock the rectangles apart until none of them touch
		
		HashMap<Rectangle, Point> velocity = new HashMap<Rectangle, Point>();
		
		for (Rectangle r : rooms)
		{
			velocity.put(r, new Point(0,0));
		}
		
		while (collisionsExist())
		{
			for (Rectangle r : rooms)
			{
				for (Rectangle s : rooms)
				{
					if (s.intersects(r))
					{
						Point rp = velocity.get(r);
						Point sp = velocity.get(s);
						Point rv = driftVector(r, s);
						Point sv = driftVector(s, r);
						velocity.put(r, new Point(rv.x + rp.x, rv.y + rp.y));
						velocity.put(s, new Point(sv.x + sp.x, sv.y + sp.y));
					}
				}
			}
			
			for (Rectangle r : rooms)
			{
				r.x += velocity.get(r).x;
				r.y += velocity.get(r).y;
			}
		}
	}
	
	private Point driftVector(Rectangle r, Rectangle s)
	{
		// This might be hideously wrong
		
		int x = (r.x < s.x) ? -1 : 1;
		int y = (r.y < s.y) ? -1 : 1;
		return new Point (x, y);
	}
	
	private boolean collisionsExist()
	{
		// Checks if room overlap
		for (Rectangle r : rooms)
		{
			for (Rectangle s : rooms)
			{
				if (r.intersects(s))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
