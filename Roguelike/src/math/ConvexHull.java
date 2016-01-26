package math;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConvexHull 
{
	public ArrayList<Point2D.Float> hull;
	public Point2D.Float top;
	public Point2D.Float bot;
	
	public ConvexHull(List<Float> left, List<Float> right)
	{
		// Get convex hull of all considered points (that is, in both subsets)
		List<Float> pts = new ArrayList<Float>();
		pts.addAll(left);
		pts.addAll(right);
		hull = new ArrayList<Float>();
			
		// Find the point with the lowest y- and then lowest x-value, make it bot
		bot = pts.get(0);
		
		for (int i = 1; i < pts.size(); i++)
		{
			Float c = pts.get(i);
			if (c.y > bot.y)
			{
				bot = c;
			}
			else if (c.y == bot.y)
			{
				if (c.x > bot.x)
				{
					bot = c;
				}
			}
		}
		
		pts.remove(bot);
		
		// Organize the points by their polar coordinates wrt bot
		PolarCompare p = new PolarCompare();
		Collections.sort(pts, p);
		
		// For each point:
		
			// Find the next valid point
			
			// Add to the return collection
		
	}
	
	private float ccw (Float p1, Float p2, Float p3)
	{
		return (p2.x - p1.x)*(p3.y - p1.y) - (p2.y - p1.y)*(p3.x - p1.x);
		
		// ccw > 0: counter-clockwise
		// ccw == 0: collinear
		// ccw < 0: clockwise
	}
	
	private class PolarCompare implements Comparator
	// Compare two points' polar coordinates wrt bot
	{
		public int compare (Object o1, Object o2)
		{
			Float c1 = (Float)o2;
			Float c2 = (Float)o1;
			
			double r1 = bot.distance(c1);
			double r2 = bot.distance(c2);
			double theta1 = Math.atan2(c1.y - bot.y, c1.x - bot.x);
			double theta2 = Math.atan2(c2.y - bot.y, c2.x - bot.x);
			
			if (theta1 == theta2)
			{
				if (r2 > r1)
				{
					return 1;
				}
				else if (r2 < r1)
				{
					return -1;
				}
				else return 0;
			}
			else
			{
				return (theta2 > theta1) ? 1 : -1;
			}
		}
	}
}
