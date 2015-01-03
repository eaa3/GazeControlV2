package uk.ac.bham.util;

import java.util.Comparator;

import uk.ac.bham.entities.Entity;


public class EntityGainComparator implements Comparator<Entity> {


	@Override
	public int compare(Entity e1, Entity e2) {
		
		//Returns the negation, for decreasing ordering
		int comp = 0;//-Double.compare(e1.getGain(), e2.getGain());
		
		return comp;

	}

}
