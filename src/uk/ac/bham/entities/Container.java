package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.ac.bham.data.Assets;

public class Container extends Entity {
	
	
	private BufferedImage sprite;
	
	public Container() throws IOException
	{
		super();
		
		sprite = Assets.container;
		
	}
	
	@Override
	public void paint(Graphics g) {
		
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setColor(Color.CYAN.brighter());
		g2d.drawImage(this.sprite, (int)this.getPos().getX(),(int)this.getPos().getY(),null);
	
		
		g2d.dispose();
		super.paint(g);
	}
	

	
	@Override
	public void update() {

	}
	
	
	public int getWidth()
	{
		return sprite.getWidth();
	}
	
	public int getHeight()
	{
		return sprite.getHeight();
	}
	
	
	
		

}
