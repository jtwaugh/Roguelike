package state;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class GameState
{
	public abstract void input();
	public abstract void logic();
	public abstract void render(Graphics2D g);
	
	public abstract void keyPressHandle(KeyEvent e);
	public abstract void keyReleaseHandle(KeyEvent e);
	
	public abstract void mousePressHandle(MouseEvent e);
	public abstract void mouseReleaseHandle(MouseEvent e);
	public abstract void mouseDragHandle(MouseEvent e);
}
