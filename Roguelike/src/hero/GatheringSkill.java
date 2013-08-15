package hero;

enum GatheringSkills { Fishing, Mining, Woodcutting, Foraging, Hunting };

public class GatheringSkill extends Skill
{
	protected GatheringSkills type;
	
	public GatheringSkill()
	{
		super();
		type = GatheringSkills.values()[(int)(Math.random() * GatheringSkills.values().length)];
	}
	
	public String name()
	{
		return type.name();
	}
}