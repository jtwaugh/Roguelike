package graphics;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;


public class Animation
{
	private BufferedImage[] images;
	private int index;
	private int loops;
	public boolean flipped;		// Animations face left by default
	
	public Animation(BufferedImage[] imgs)
	{
		images = imgs;
		index = 0;
		loops = -1;
		fixImages();
		flipped = false;
	}
	
	public Animation(Animation that)
	{
		images = that.getFrames();
		index = 0;
		loops = -1;
		fixImages();
		flipped = false;
	}
	
	private void fixImages()
	{
		for (int i = 0; i < images.length; i++)
		{
			if (images[i] == null)
			{
				return;
			}
	        // obtain the current system graphical settings
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    GraphicsDevice device = env.getDefaultScreenDevice();
			GraphicsConfiguration gfx_config = device.getDefaultConfiguration();

	        /*
	         * if image is already compatible and optimized for current system
	         * settings, simply return it
	         */
	        if (images[i].getColorModel().equals(gfx_config.getColorModel()))
	        {
	        	images[i].setAccelerationPriority(1.0f);
	            return;
	        }

	        // image is not optimized, so create a new image that is
	        BufferedImage new_image = gfx_config.createCompatibleImage(images[i].getWidth(), images[i].getHeight(), images[i].getTransparency());

	        // get the graphics context of the new image to draw the old image on
	        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

	        // actually draw the image and dispose of context no longer needed
	        g2d.drawImage(images[i], 0, 0, null);
	        g2d.dispose();

	        new_image.setAccelerationPriority(1.0f);

	        // return the new optimized image
	        images[i] = new_image;
	    }
	}
	
	public BufferedImage[] getFrames()
	{
		return images.clone();
	}
	
	private boolean isNull()
	{
		return (index < 0);
	}
	
	public BufferedImage get(int idx)
	{
		if (!isNull())
		{
			return images[idx];
		}
		else return null;
	}
	
	public BufferedImage getCurrent()
	{
		if (!isNull())
		{
			return images[index];
		}
		else return null;
	}
	
	public BufferedImage getNext()
	{
		if (!isNull())
		{
			if (index + 1 < images.length)
			{
				return images[index+1];
			}
			else
			{
				return images[0];
			}
		}
		else return null;
	}
		
	public void setNext(BufferedImage img)
	{
		if (!isNull())
		{
			if (index + 1 < images.length)
			{
				images[index+1] = img;
			}
			else
			{
				images[0] = img;
			}
		}
	}
	
	public void bump()
	{
		index++;
		
		if (index > images.length-1)
		{
			if (loops != -1)
			{
				loops--;
			}
			
			index = 0;
		}
		
		if (loops == 0)
		{
			index = -1;
		}
		
		
	}

	public void flip()
	{
		for (int a = 0; a < images.length; a++)
		{
			int w = images[a].getWidth();  
	        int h = images[a].getHeight();  
	        BufferedImage dimg = new BufferedImage(w, h, images[a].getType());  
	        Graphics2D g = dimg.createGraphics();  
	        g.drawImage(images[a], 0, 0, w, h, w, 0, 0, h, null);  
	        g.dispose();  
	        images[a] = dimg; 
		}
		
		if (flipped)
		{
			flipped = false;
		}
		else
		{
			flipped = true;
		}
	}
}

