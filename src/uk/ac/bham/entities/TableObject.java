package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import uk.ac.bham.control.Robot.EMotorSystem;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;

public class TableObject extends Entity {
	
	private Table parentTable;
	private Color color;
	
	public int nr,nl;
	
	public TableObject()
	{
		this.resetPf();
	}
	public TableObject(Table parentTable)
	{
		this.parentTable = parentTable;
		
		this.resetPos();
		
		
		this.color = new Color((float) Math.random(), (float)Math.random(), (float) Math.random(), .7f);
	}
	
	public void resetPos()
	{
		
		this.setInVM(false);
		
		this.setPos(new Vector2());
		
		
		
		
		
		this.getPos().setX(this.parentTable.getPos().getX()+(70+(this.parentTable.getWidth()-140-this.getWidth())*Math.random()));
		this.getPos().setY(this.parentTable.getPos().getY()+((this.parentTable.getHeight()-this.getHeight())*Math.random()));
		this.updateCenter();
		
		
		this.resetPf();
		this.freezePf = false;
		
		this.setValue(0);
		this.setPredValue(0);
		this.setGain(0);
		
		
		
	}
	
	public void paint(Graphics g)
	{
		
		Graphics2D g2d = (Graphics2D)g.create();
		
		g2d.setColor(this.drawBrighter?color.brighter():color);
		
		g2d.fillRect((int)this.getPos().getX(),(int)this.getPos().getY(), this.getWidth(), this.getHeight());	
		
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect((int)this.getPos().getX(),(int)this.getPos().getY(), this.getWidth(), this.getHeight());
		
		
		g2d.dispose();
		
		super.paint(g);
		
		
	}
	
	public void update()
	{
		super.update();
		
		
	}

	@Override
	public int getWidth() {

		return Config.OBJ_DIM;
	}

	@Override
	public int getHeight() {

		return Config.OBJ_DIM;
	}

}
