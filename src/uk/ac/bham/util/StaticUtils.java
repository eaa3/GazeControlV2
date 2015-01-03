package uk.ac.bham.util;

import java.util.Comparator;

import uk.ac.bham.entities.Entity;



public abstract class StaticUtils {
	
	public static final Comparator<Entity> entityValueComp = new EntityValueComparator();
	public static final Comparator<Entity> entityRUGComp = new EntityGainComparator();
	public static final Comparator<Entity> entityRUComp = new EntityPredValueComparator();
	public static final Comparator<Entity> entityUNCComp = new EntityUncComparator();
	public static final ParticleComparator particleComp = new ParticleComparator();

}
