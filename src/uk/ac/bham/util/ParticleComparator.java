package uk.ac.bham.util;

import java.util.Comparator;

import uk.ac.bham.entities.Particle;



public class ParticleComparator implements Comparator<Particle>
{

	@Override
	public int compare(Particle p1, Particle p2) {

		return Double.compare(p1.getWeight(), p2.getWeight());
	}

}