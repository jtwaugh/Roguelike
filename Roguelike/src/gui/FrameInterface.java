package gui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import game.Game;
import graphics.Sprite;
import io.FileReader;

public class FrameInterface extends InterfaceParent
{
	protected BufferedImage corner;
	protected BufferedImage side;
	protected BufferedImage end;
	
	protected ArrayList<Sprite> picArray;
	
	protected Sprite pic;
	
	public FrameInterface(int _width, int _height, String frameName)
	{
		super(_width, _height);
		
		BufferedImage frame = Game.RESOURCES.getSprite(frameName).getImage();
		
		corner = frame.getSubimage(0, 0, frame.getWidth()/2, frame.getHeight()/2);
		side = frame.getSubimage(frame.getWidth()/2, 0, frame.getWidth()/2, frame.getHeight()/2);
		end = frame.getSubimage(0, frame.getHeight()/2, frame.getWidth()/2, frame.getHeight()/2);
		
		bgColor = FileReader.RGBA(frame.getWidth()/2, frame.getHeight()/2, frame);
		
		picArray = new ArrayList<Sprite>();
		
		createGraphics2D();	
	}
	
	protected void createGraphics2D()
	{
		Point c = new Point(0, 0);
		
		int w = side.getWidth();
		int h = end.getHeight();
		
		Sprite[] corners =  new Sprite[4];
		
		corners[0] = new Sprite(corner, c.x, c.y);
		corners[1] = new Sprite(corner, c.x + width, c.y);
		corners[1].flipHorizontal();
		corners[2] = new Sprite(corner, c.x, c.y + height);
		corners[2].flipVertical();
		corners[3] = new Sprite(corner, c.x + width, c.y + height);
		corners[3].flipVertical();
		corners[3].flipHorizontal();
		
		BufferedImage myImg = new BufferedImage(width - w, height - h, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D z = (Graphics2D) myImg.getGraphics();
		z.setColor(bgColor);
		z.fillRect(c.x, c.y, width - w, height - h);
		z.setColor(null);
		z.dispose();
		
		picArray.add(new Sprite(myImg, c.x + w, c.y + h));
		
		
		picArray.add(new Sprite(side, (c.x + width) - w, c.y));
		Sprite extraSide = new Sprite(side, (c.x + width) - w, c.y + height);
		extraSide.flipVertical();
		picArray.add(extraSide);
		
		for (int x = 1; x < width / w; x++)
		{
			picArray.add(new Sprite(side, c.x + (w * x), c.y));
			Sprite mySprite = new Sprite(side, c.x + (w * x), c.y + height);
			mySprite.flipVertical();
			picArray.add(mySprite);
		}
		
		
		picArray.add(new Sprite(end, c.x, (c.y + height) - h));
		Sprite extraEnd = new Sprite(end, c.x + width, (c.y + height) - h);
		extraEnd.flipHorizontal();
		picArray.add(extraEnd);
		
		for (int y = 1; y < height / h; y++)
		{
			picArray.add(new Sprite(end, c.x, c.y + (h * y)));
			Sprite mySprite = new Sprite(end, c.x + width, c.y + (h * y));
			mySprite.flipHorizontal();
			picArray.add(mySprite);
		}
		
		for (int q = 0; q < 4; q++)
		{
			picArray.add(corners[q]);
		}
		
		
	}
	
	public void draw (Graphics2D g, Point c)
	{
		Graphics2D g2d = (Graphics2D)(g);
		
		g2d.translate(c.x, c.y);
		
		for (int p = 0; p < picArray.size(); p++)
		{
			picArray.get(p).draw(g2d);
		}
		
		g2d.translate(-c.x, -c.y);
	}
}
