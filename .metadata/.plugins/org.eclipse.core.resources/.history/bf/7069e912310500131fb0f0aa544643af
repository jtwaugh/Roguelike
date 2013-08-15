package language;

public class NameGen
{
	protected static String[] townTitle = 
		{ 	
			"Village of",
			"Village of",
			"Village of",
			"Village of",
			"Town of",
			"Town of",
			"Town of",
			"Fort",
			"Fort",
			"Hamlet",
			"Bad",
			"Great",
			"City of"
		};
	
	protected static String[] townPrefix = 
		{
			"kings",
			"queens",
			"new",
			"liver",
			"ips",
			"scar",
			"war",
			"nor",
			"suf",
			"sus",
			"shef",
			"middle",
			"south",
			"man",
			"bed",
			"wat",
			"wor",
			"shrews",
			"wald"	
		};
	
	protected static String[] townSuffix = 
		{
			"sex",
			"wich",
			"ing",
			"wick", 
			"ton", 
			"castle", 
			"shire", 
			"borough", 
			"ham", 
			"keep", 
			"bury", 
			"forth", 
			"ford", 
			"field",
			"pool",
			"folk",
			"cester",
			"try",
			"by",
			"port",
			"bridge",
			"mouth"	
		};
	
	public static String getTownName()
	{
		String s = townPrefix[(int)(Math.random() * townPrefix.length)] + townSuffix[(int)(Math.random() * townSuffix.length)];
		return townTitle[(int)(Math.random() * townTitle.length)] + " " + s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}
