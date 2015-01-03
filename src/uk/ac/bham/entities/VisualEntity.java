package uk.ac.bham.entities;

import java.awt.Graphics;

import uk.ac.bham.util.Vector2;

public interface VisualEntity {
	
	
	public void paint(Graphics g);
	public void update();
	
	
	public int getWidth();
	public int getHeight();
	
	public Vector2 getPos();

}
