package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;








import uk.ac.bham.control.ArmPos;
import uk.ac.bham.data.Assets;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;

public class Table implements VisualEntity{

	Vector<Entity> objs;

	BufferedImage sprite;

	private Vector2 center;
	private Vector2 pos;


	public Table() throws IOException
	{
		this.sprite = Assets.table;

		objs = new Vector<Entity>();
		
		this.pos = new Vector2();

		this.getPos().setX((Config.FRAME_W-this.getWidth())/2);
		this.getPos().setY((Config.FRAME_H-this.getHeight())/2 -15);

		center = new Vector2(this.getPos().getX()+this.getWidth()/2,this.getPos().getY() + this.getHeight()/2);
		


	}
	
	public void init() throws IOException
	{
		this.objs.clear();
		
		//Adding left side container
		Container container = new Container();
		Vector2 pos1 = new Vector2();
		pos1.setX(10);
		pos1.setY((this.getHeight() - container.getHeight())/2);
		pos1.add(this.getPos());
		container.setPos(pos1);
		container.updateCenter();
		container.resetPf();
		objs.add(container);


		//Adding right side container
		container = new Container();
		Vector2 pos2 = new Vector2();
		pos2.setX(this.getWidth() - container.getWidth() - 10);
		pos2.setY((this.getHeight() - container.getHeight())/2);
		pos2.add(this.getPos());
		container.setPos(pos2);
		container.updateCenter();
		container.resetPf();
		objs.add(container);

		while ( (this.objs.size()-2) < Config.MAX_TABLE_OBJS)
		{
			this.objs.add(new TableObject(this));
		}
	}


	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g.create();


		g2d.drawImage(this.sprite,(int)this.getPos().getX(),(int)this.getPos().getY(),null);


		for(Entity obj : objs)
		{
			obj.paint(g2d);			
		}

		g2d.dispose();


	}

	@Override
	public void update()
	{

		for(Entity obj : objs)
		{
			obj.update();		
		}


	}

	@Override
	public int getWidth() {

		return this.sprite.getWidth();
	}

	@Override
	public int getHeight() {

		return this.sprite.getHeight();
	}

	public boolean isOnObject(MotorSystem ms)
	{	
		Vector2 pos = ms.getPos();
		for(Entity obj : objs )
		{
			if( obj instanceof TableObject && ms.getPos().dist(obj.getCenter())<= obj.getWidth())
			{
				return true;
			}

		}

		return false;
	}

	public boolean isOnContainer(MotorSystem ms)
	{	
		Vector2 pos = ms.getPos();
		for(Entity obj : objs )
		{
			if( obj instanceof Container && ms.getPos().dist(obj.getCenter()) <= obj.getWidth())
			{
				return true;
			}

		}

		return false;
	}

	public boolean hasObjects()
	{
		for(Entity obj : objs )
		{
			if( obj instanceof TableObject )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public Entity getObj(int i)
	{
		return this.objs.get(i);
	}
	
	public int getNObjs()
	{
		return this.objs.size();
	}


	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.pos;
	}
	
	public Vector2 getCenter()
	{
		return this.center;
	}
	
	public boolean isOn(Vector2 pos)
	{
		return (this.getPos().getX() <= pos.getX() && this.getPos().getY() <= pos.getY() && pos.getX() <=  (this.getPos().getX()+this.getWidth()) && pos.getY() <=  (this.getPos().getY()+this.getHeight()) );
	}

}
