package graphics;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import io.FileReader;

public class Sprite 
{
	
	private File file;
	private BufferedImage image;
	private Point pos;
	private Animation anim;
	
	private HashMap<String, Animation> animations;
	
	public Sprite(String filename) throws IOException
	{
		file = new File(filename);
		image = ImageIO.read(file);
		anim = null;
		pos = new Point(0,0);
		fixImage();
	}
	
	public Sprite(String imgfile, String fileguide) throws IOException
	{
		file = new File(imgfile);
		image = ImageIO.read(file);
		pos = new Point(0,0);
		fixImage();
		
		FileReader r = new FileReader();
		animations = r.parseSpriteAnims(image, fileguide);
		anim = animations.get("default");
	}
	
	public Sprite(File myFile) throws IOException
	{
		file = myFile;
		image = ImageIO.read(file);
		anim = null;
		pos = new Point(0,0);
		fixImage();
	}
	
	public Sprite(BufferedImage _image)
	{
		image = _image;
		anim = null;
		pos = new Point(0,0);
		fixImage();
	}
	
	public Sprite(BufferedImage _image, int x, int y)
	{
		image = _image;
		pos = new Point(x, y);
		fixImage();
	}
	
	public Sprite(Sprite that)
	{
		image = that.image;
		fixImage();
		
		pos = that.pos;
		
		animations = new HashMap<String, Animation>();
		
		if (that.animations != null)
		{
			for (String a : that.animations.keySet())
			{
				animations.put(a, new Animation(that.animations.get(a)));
			}
			anim = animations.get("default");
		}	
	}
	
	private void fixImage()
    // Code found here: http://www.java-gaming.org/index.php/topic,24473
	// And here: http://stackoverflow.com/questions/658059/Graphics2D-drawimage-in-java-is-extremely-slow-on-some-computers-yet-much-faster
	{
		if (image == null)
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
        if (image.getColorModel().equals(gfx_config.getColorModel()))
        {
            image.setAccelerationPriority(1.0f);
            return;
        }

        // image is not optimized, so create a new image that is
        BufferedImage new_image = gfx_config.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());

        // get the Graphics2D context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        new_image.setAccelerationPriority(1.0f);

        // return the new optimized image
        image = new_image;
    }

	public int getWidth()
	{
		if (image ==  null)
		{
			return 0;
		}
		return image.getWidth(null);
	}
	
	public int getHeight()
	{
		if (image ==  null)
		{
			return 0;
		}
		return image.getHeight(null);
	}
	
	public void flipHorizontal()
	{  
		if (animations == null || animations.size() == 0)
		{
			int w = image.getWidth();  
	        int h = image.getHeight();  
	        BufferedImage dimg = new BufferedImage(w, h, image.getType());  
	        Graphics2D g = dimg.createGraphics();  
	        g.drawImage(image, 0, 0, w, h, w, 0, 0, h, null);  
	        g.dispose();  
	        image = dimg; 
		}
		else
		{
			anim.flip();
		}
        
    } 
	
	public void flipVertical()
	{  
        int w = image.getWidth();  
        int h = image.getHeight();  
        BufferedImage dimg = new BufferedImage(w, h, image.getType());  
        Graphics2D g = dimg.createGraphics();  
        g.drawImage(image, 0, 0, w, h, 0, w, h, 0, null);  
        g.dispose();  
        image = dimg; 
    }  
	
	public void draw(Graphics2D g)
	{
		animate();
		g.drawImage(image, pos.x, pos.y, null);
	}
	
	public Point getPosition()
	{
		return pos;
	}
	
	public void setPosition(int _x, int _y)
	{
		pos = new Point(_x, _y);
	}
	
	public void move(int _x, int _y)
	{
		pos.x += _x;
		pos.y += _y;
	}
	
	public Point centerOf()
	{
		Point myPt = new Point(pos.x, pos.y);
		
		myPt.x += getWidth()/2;
		myPt.y += getHeight()/2;
		
		return myPt;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public void setImage(BufferedImage _img)
	{
		image = _img;
	}
	
	public boolean playAnimation(String key)
	{
		if (anim != null)
		{
			Animation myAnim = animations.get(key);
			
			if (myAnim != null)
			{
				if (anim.flipped != myAnim.flipped)
				{
					myAnim.flip();
				}
				anim = myAnim;
				
				return true;
			}
		}
		return false;
	}
	
	public void animate()
	{
		if (anim != null)
		{
			anim.bump();
			image = anim.getCurrent();
		}
	}
}