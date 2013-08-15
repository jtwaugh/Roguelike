package hero;

enum CombatSkills { Sword, Daggers, Staff, Bow, Crossbow };

public class CombatSkill extends Skill
{
	protected CombatSkills type;
	
	public CombatSkill()
	{
		super();
		type = CombatSkills.values()[(int)(Math.random() * CombatSkills.values().length)];
	}
	
	public String name()
	{
		return type.name();
	}
}