package gui;

import game.Game;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Window extends FrameInterface
{
	protected boolean open;
	protected boolean close;
	protected boolean pressed;
	
	protected Point pos;
	
	public Window(int _width, int _height, String frameName, Point _pos) 
	{
		super(_width, _height, frameName);
		close = false;
		open = false;
		pressed = false;
		pos = _pos;
	}
	
	public void update()
	{
		
	}
	
	public void setPosition(int x, int y)
	{
		pos = new Point(x, y);
	}
	
	public void move(int x, int y)
	{
		pos = new Point(pos.x + x, pos.y + y);
	}
	
	public boolean keyPressHandle(KeyEvent e) 
	{
		return false;
	}

	public boolean keyReleaseHandle(KeyEvent e) 
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			setClosed(true);
		}
		
		return true;
	}
	
	public boolean mousePressHandle(MouseEvent m) 
	{
		// If the mouse collides with the window, it's now pressed
		if (m.getX() >= pos.x && m.getX() <= pos.x + width && m.getY() >= pos.y && m.getY() <= pos.y + height)
		{
			pressed = true;
			Game.mousePos = m.getPoint();
			Game.oldMousePos = Game.mousePos;
			return true;
		}
		return false;
	}
	
	public boolean mouseReleaseHandle(MouseEvent m) 
	{
		pressed = false;
		
		if (open && !close && (m.getX() >= pos.x && m.getX() <= pos.x + width && m.getY() >= pos.y && m.getY() <= pos.y + height))
			return true;
		
		return false;
	}

	public void setOpened(boolean b)
	{
		open = b;
	}
	//
	public boolean getOpened()
	{
		return open;
	}
	
	public void setClosed(boolean b)
	{
		close = b;
	}
	
	public boolean getClosed()
	{
		return close;
	}
	
	public void setPressed(boolean b)
	{
		pressed = b;
	}
	
	public boolean getPressed()
	{
		return pressed;
	}
	
	public void draw(Graphics2D g)
	{
		super.draw(g, pos);
	}
	
	public String getExitChoice()
	{
		return "";
	}
	
	public void reset()
	{
		
	}

	public int getExitIndex() 
	{
		return -1;
	}
}