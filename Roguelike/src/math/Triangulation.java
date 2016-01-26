package math;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Triangulation 
{
	protected HashSet<Line2D> graph;

	public Triangulation()
	{
		graph = new HashSet<Line2D>();
	}
	
	public void triangulate(List<Point2D.Float> pts, BufferStrategy strategy)
	{
		// Divide and conquer algorithm to recursively connect all triangles and line segments
		// Called on a set of points and subdivides until reaching a terminal case
		
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		
		int size = pts.size();
		
		// Terminal cases
		if (size == 2)
		// Connect line segment
		{
			graph.add(new Line2D.Float(pts.get(0), pts.get(1)));
			return;
		}
		else if (size == 3)
		// Connect triangle
		{
			graph.add(new Line2D.Float(pts.get(0), pts.get(1)));
			graph.add(new Line2D.Float(pts.get(1), pts.get(2)));
			graph.add(new Line2D.Float(pts.get(2), pts.get(0)));
			return;
		}
		
		// And otherwise...

		// Subdivide the set
		int splitPt = (int)(size / 2);
		List<Point2D.Float> left = pts.subList(0, splitPt);
		List<Point2D.Float> right = pts.subList(splitPt, pts.size());
		
		// Construct the subsets
		triangulate(left, strategy);
		triangulate(right, strategy);
		
		// Connect the halves
		
		// Compute the vertices with maximum ycoordinate for both P and Q. If more than one exist, take the ones with greater x coordinates.
		
		Float[] min = sortCoords(left, right);
		Float pmin = min[0];
		Float qmin = min[1];
		
		// Connect the interior vertices
		
		connectVertices(left, right, pmin, qmin);
		
		// Debug render the graph
		render(strategy, g, pts);
	}
	
	private void connectVertices(List<Float> _left, List<Float> _right, Float pmin, Float qmin)
	{
		List<Float> left = new ArrayList<Float>();
		for (Float f : _left)
		{	
			left.add(f);
		}
		List<Float> right = new ArrayList<Float>();
		for (Float f : _right)
		{
			right.add(f);
		}
		
		while(true)
		{	
			left.remove(pmin);
			right.remove(qmin);
			
			Line2D.Float bot = new Line2D.Float(pmin, qmin);
			
			graph.add(bot);
			
			// Select the candidates
			
			Float pcand = null;
			Float qcand = null;
		
			// Start on the right
		
			ArrayList<Double> theta = new ArrayList<Double>();
			
			for (int q = 0; q < right.size(); q++)
			{
				// Find the angle between the base and each edge
				theta.add(Util.rightAngle(pmin, qmin, right.get(q)));
			}		
			// Organize the points by lowest theta first
			Collections.sort(theta);
			
			// Check our potential candidates
			for (int q = 0; q < right.size(); q++)
			{
				// If theta > pi, no candidate on this side
				if (theta.get(q) > Math.PI)
				{
					break;
				}
				if (right.size() - q > 1)
				// If the circumcircle of these three points contains the next point, delete the RR edge
				// Potentially breaks if array is too small
				{
					if (inCircumcircle(pmin, qmin, right.get(q), right.get(q+1)))
					{
						// Delete the edge connecting qmin and right[q]
						Line2D.Float kill = new Line2D.Float(qmin, right.get(q));
						graph.remove(kill);
						
					}
					else
					// Now we have a candidate
					{
						qcand = right.get(q);
						break;
					}
				}
				else
				// Now we have a candidate
				{
					qcand = right.get(q);
					break;
				}
			}
			
			// Do the same on the left
			
			theta = new ArrayList<Double>();
			
			for (int p = 0; p < left.size(); p++)
			{
				// Find the angle between the base and each edge
				theta.add(Util.leftAngle(left.get(p), pmin, qmin));
			}
			
			// Check our potential candidates
			for (int p = 0; p < left.size(); p++)
			{
				// If theta > pi, no candidate on this side
				if (theta.get(p)> Math.PI)
				{
					break;
				}
				if (left.size() - p > 1)
				// If the circumcircle of these three points contains the next point, delete the RR edge
				// Potentially breaks if array is too small
				{
					if (inCircumcircle(pmin, qmin, left.get(p), left.get(p+1)))
					{
						// Delete the edge connecting qmin and right[q]
						Line2D.Float kill = new Line2D.Float(pmin, left.get(p));
						graph.remove(kill);
					}
					else
					// Now we have a candidate
					{
						pcand = left.get(p);
						break;
					}
				}
				else
				// Now we have a candidate
				{
					pcand = left.get(p);
					break;
				}
			}
			
			// Handle the candidates
			// When neither a right nor a left candidate is submitted, the merge is complete. 
			//If only one candidate is submitted, it automatically defines the LR-edge to be added. 
			//In the case where both candidates are submitted, the approprate LR-edge is decided by a simple test: 
			//if the right candidate is not contained in interior of the circle defined by the two endpoints of the base LR-edge and the left candidate, then the left candidate defines the LR-edge and vice-versa.
			
			// Move to the next edge, then repeat
			
			if (pcand == null && qcand == null)
			// We're done!
			{
				break;
			}
			else if (pcand == null)
			{
				graph.add(new Line2D.Float(pmin, qcand));
				qmin = qcand;
			}
			else if (qcand == null)
			{
				graph.add(new Line2D.Float(pcand, qmin));
				pmin = pcand;
			}
			else
			{
				if (inCircumcircle(pmin, qmin, pcand, qcand))
				{
					graph.add(new Line2D.Float(pmin, qcand));
					qmin = qcand;
				}
				else
				{
					graph.add(new Line2D.Float(pcand, qmin));
					pmin = pcand;
				}
			}
		}
	}
	
	private Float[] sortCoords(List<Point2D.Float> left, List<Point2D.Float> right)
	{
		Float pmin = left.get(0);
		Float qmin = right.get(0);
		
		for (int p = 1; p < left.size(); p++)
		{
			if (left.get(p).y > pmin.y || (left.get(p).y == pmin.y && left.get(p).x > pmin.x))
			{
				pmin = left.get(p);
			}
		}
		
		for (int q = 1; q < right.size(); q++)
		{
			if (right.get(q).y > qmin.y || (right.get(q).y == qmin.y && right.get(q).x > qmin.x))
			{
				qmin = right.get(q);
			}
		}
		
		Float[] min = {pmin, qmin};
		return min;
	}
	
	private boolean isEquivalent(Line2D.Float a, Line2D.Float b)
	{
		return (((a.getY1() == b.getY1() && a.getX1() == b.getX1() && (a.getY2() == b.getY2() && a.getX2() == b.getX2()))) || ((a.getY1() == b.getY2() && a.getX1() == b.getX2()) && (a.getY2() == b.getY1() && a.getX2() == b.getX1())));
	}
	
	private boolean inCircumcircle(Point2D.Float p0, Point2D.Float p1, Point2D.Float p2, Point2D.Float candidate) 
	{
		Point2D.Double midDiff = new Point2D.Double((p2.x-p0.x)/2,(p2.y-p0.y)/2);
		Point2D.Double u = new Point2D.Double(p0.y-p1.y, p1.x-p0.x);
		Point2D.Double v = new Point2D.Double(p2.x-p1.x, p2.y-p1.x);
		double t = Util.dot(midDiff, v)/Util.dot(u, v);
		Point2D.Double circumcenter = new Point2D.Double(t*u.x + (p0.x+p1.x)/2, t*u.y + (p0.y+p1.y)/2);
		double r = p0.distance(circumcenter);
		
		return (circumcenter.distance(candidate) <= circumcenter.distance(p0));
	}
	
	public void render(BufferStrategy strategy, Graphics2D g, List<Float> pts)
	{
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 1600, 900);
		g.setColor(Color.WHITE);
		for (Point2D.Float p : pts)
		{
			g.fillOval((int)p.getX(), (int)p.getY(), 3, 3);
		}
		
		for (Line2D l : graph)
		{
			g.draw(l);
		}
		
		g.dispose();
		strategy.show();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
