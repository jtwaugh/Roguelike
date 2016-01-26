package math;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Random;

public class Util
{
	public static int Int(int a, int b)
	{
		return (int)(a + (Math.random() * (b - a)));
	}
	
	public static float Float(float a, float b)
	{
		return (float)(a + (Math.random() * (b - a)));
	}
	
	public static int gaussian(int a, int b, Random ayn)
	{
		return (int)(a + (Math.abs(ayn.nextGaussian()) * (b - a)));
	}
	
	public static double leftAngle(Point2D.Float a, Point2D.Float mid, Point2D.Float b)
    {
		// Find the ccw angle from mid-b to mid-a
		
		// Get the right absolute angle
		double right = Math.atan2(b.y - mid.y, b.x - mid.x);
		
		// Get the left absolute angle
		double left = Math.atan2(a.y - mid.y, a.x - mid.x);
		
		return right - left;
    }
	
	public static double rightAngle(Point2D.Float a, Point2D.Float mid, Point2D.Float b)
    {
		// Find the cw angle from mid-a to mid-b

		// Get the right absolute angle
		double right = Math.PI - Math.atan2(b.y - mid.y, b.x - mid.x);
		
		// Get the left absolute angle
		double left = Math.PI - Math.atan2(a.y - mid.y, a.x - mid.x);
		
		return left - right;
    }
	
	public static Point2D.Float norm(Point2D.Float p)
	{
		return new Point2D.Float((float)(p.x / p.distance(0,0)), (float)(p.y / p.distance(0,0)));
	}
	
	public static double dot(Point2D.Double q0, Point2D.Double q1) 
	{
		return q0.x*q1.x + q0.y*q1.y;
	}
}
