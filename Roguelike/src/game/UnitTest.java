package game;

import io.ResourceManager;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;

import map.Dungeon;

public class UnitTest extends Canvas
{
	private JFrame container;
	private JPanel panel;
	
	private BufferStrategy strategy;
	
	private boolean waitingForKeyPress;
	private boolean spacePressed = false;
	
	public static Font FONT_TEXT;
	
	public static final int WIDTH = 1500;
	public static final int HEIGHT = 1100;
	
	public static Point mousePos;
	public static Point oldMousePos;
	
	public static boolean mousePressed = false;
	
	public static Color[] ROOMCOLORS = {Color.WHITE, Color.CYAN, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.PINK, Color.RED, Color.ORANGE }; 
	
	private boolean gameRunning = true;
	
	public UnitTest()
	{
		setUpWindow();
	}
	
	private void setUpWindow()
	{
		container = new JFrame("Unit Test Renderer");
		
		FONT_TEXT = new Font("SansSerif", Font.PLAIN, 12);
		
		panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setLayout(null);
		

		setBounds(5, 5, WIDTH, HEIGHT);
		
		panel.add(this);
		

		setIgnoreRepaint(true);

		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		

		container.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});
		

		//addKeyListener(new KeyInputHandler());
		//addMouseListener(new MouseInputHandler());
		

		requestFocus();


		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		addKeyListener(new KeyInputHandler());
		MouseInputHandler mih = new MouseInputHandler();
		addMouseListener(mih);
		addMouseMotionListener(mih);

	}
	
	private class KeyInputHandler extends KeyAdapter 
	{		
		public void keyPressed(KeyEvent e) 
		{
			if (waitingForKeyPress) 
			{
				return;
			}
			else
			{
			}
		} 
		
		public void keyReleased(KeyEvent e) 
		{
			/* if (waitingForKeyPress)
			{
				waitingForKeyPress = false;
			}
			else 
			{
				
			} */
			
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				spacePressed = true;
			}
		}

		public void keyTyped(KeyEvent e) 
		{
		}
	}
	
	private class MouseInputHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
		}
		
		public void mouseReleased(MouseEvent e)
		{
			mousePressed = true;
		}
		
		public void mouseDragged(MouseEvent e)
		{
			oldMousePos = mousePos;
			mousePos = e.getPoint();
		}
	}

	public void run()
	{
		Dungeon d = new Dungeon();
		
		Graphics2D gameRenderer = (Graphics2D) strategy.getDrawGraphics();
		
		d.generateRooms();
		d.drift(strategy);
		//d.triangulate();
		
		for (Rectangle r : d.rooms)
		{
			System.out.println("X: " + r.x + ", Y: " + r.y + ", Width: " + r.width + ", Height: " + r.height);
		}
		
		while (gameRunning)
		{	
			gameRenderer = (Graphics2D) strategy.getDrawGraphics();
			
			gameRenderer.setColor(Color.DARK_GRAY);
			gameRenderer.fillRect(0, 0, WIDTH, HEIGHT);
			
			if (spacePressed)
			{
				spacePressed = false;
			}
			
			d.drawGrid(gameRenderer);
			
			for (Rectangle r : d.rooms)
			{
				gameRenderer.setColor(UnitTest.ROOMCOLORS[d.rooms.indexOf(r) % 8]);
				int x = UnitTest.WIDTH/2 + r.x * 10;
				int y = UnitTest.HEIGHT/2 + r.y * 10;
				gameRenderer.fillRect(x, y, r.width * 10, r.height * 10);
				
				gameRenderer.setColor(Color.BLACK);
				gameRenderer.drawRect(x, y, r.width * 10, r.height * 10);
			}
			
			gameRenderer.dispose();
			strategy.show();
		}
	}
}
