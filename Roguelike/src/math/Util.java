package math;

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
}
