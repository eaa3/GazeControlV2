package uk.ac.bham.util;

import java.util.Comparator;

import uk.ac.bham.entities.Entity;


public class EntityValueComparator implements Comparator<Entity> {
	
	@Override
	public int compare(Entity e1, Entity e2) {
		
		//Returns the negation, for decreasing ordering
		int comp = -Double.compare(e1.getValue(), e2.getValue());
		
		return comp;

	}

}