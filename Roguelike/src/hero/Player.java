package hero;

import java.awt.Graphics2D;

import game.Game;
import map.OverworldEntity;

public class Player
// Holds hero stats, not direct entity vars
{
	protected String 			name;
	
	protected CombatSkill 		breaking;
	protected GatheringSkill 	taking;
	protected CraftSkill	 	making;

	protected OverworldEntity 	overworldSprite;
	
	public Player(String _name)
	{
		name = _name;
		breaking = new CombatSkill();
		taking = new GatheringSkill();
		making = new CraftSkill();
		overworldSprite = new OverworldEntity(Game.RESOURCES.getSprite("heromap"));
	}
	
	public OverworldEntity mapSprite()
	{
		return overworldSprite;
	}
	
	public void mapRender(Graphics2D g)
	{
		overworldSprite.render(g);
	}
}
