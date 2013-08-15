package hero;

enum CraftSkills { Blacksmithing, Alchemy, Woodworking, Tailoring, Cooking };

public class CraftSkill extends Skill
{
	protected CraftSkills type;
	
	public CraftSkill()
	{
		super();
		type = CraftSkills.values()[(int)(Math.random() * CraftSkills.values().length)];
	}
	
	public String name()
	{
		return type.name();
	}
}