package io;

import graphics.Sprite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ResourceManager
{
	private HashMap<String, Sprite> sprites;
	
	private int numLoaded;
	
	private FileReader reader;
	
	public ResourceManager() throws IOException
	{
		reader = new FileReader();
		
		numLoaded = 0;
		
		sprites = new HashMap<String, Sprite>();
	}
	
	public int loadNext() throws IOException
	{
		switch (numLoaded)
		{
		case 0:
		{
			loadSprites();
			break;
		}
		}
		
		numLoaded++;
		
		return numLoaded;
	}
	
	public void loadSprites() throws IOException
	{
		reader.getSprites(new File(FileReader.drive + "sprites"), sprites);
	}
	
	public Sprite getSprite(String key)
	{
		return new Sprite(sprites.get(key));
		
	}
	
	public void put(String key, Sprite pic)
	{
		sprites.put(key, pic);
	}
	
	
	public void loadSpriteFromFile(String filename) throws IOException
	{
		String key = filename.split(".")[0];
		
		sprites.put(key, new Sprite(filename));
	}
}
