package state;

import gui.Window;

import map.Node;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import map.NodeMap;

public class OverworldState extends GameState
{
	protected NodeMap map;
	
	public OverworldState()
	{
		map = new NodeMap();
	}
	
	public void input() 
	{
		
	}

	public void logic() 
	{
		map.logic();
	}

	public void render(Graphics2D g) 
	{
		map.render(g);
	}
	
	public void keyPressHandle(KeyEvent e)
	{
		map.keyPressHandle(e);
	}
	
	public void keyReleaseHandle(KeyEvent e)
	{
		map.keyReleaseHandle(e);
	}
	
	public void mousePressHandle(MouseEvent e)
	{
		map.mousePressHandle(e);
	}
	
	public void mouseReleaseHandle(MouseEvent e)
	{
		map.mouseReleaseHandle(e);
	}
	
	public void mouseDragHandle(MouseEvent e)
	{
		map.mouseDragHandle(e);
	}
}
