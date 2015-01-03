package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;



public class Particle implements VisualEntity {
	
	private Color color;
	private double weight;
	private Vector2 pos;
	
	public Particle()
	{
		
		super();
		this.color = Color.RED;
		this.pos = new Vector2();
		
	}
	
	//Initial Particle position. We add noise to this position and set the noisy position as this particle estimated position
	public Particle(double x0, double y0)
	{
		this.pos = new Vector2();
		this.uniformInitialisation();
		//this.gaussianInitialisation(x0, y0);
		
		this.color = Color.RED;
		
	}
	
	
	public void uniformInitialisation()
	{
		
		double x0 = EntityPool.table.getPos().getX();
		double y0 = EntityPool.table.getPos().getY();
		
		double table_w = EntityPool.table.getWidth();
		double table_h = EntityPool.table.getHeight();
		
		this.pos.setX(x0 + table_w*Math.random());
		this.pos.setY(y0 + table_h*Math.random());
	}
	
	
	public void gaussianInitialisation(double x0, double y0)
	{
		Random r = Config.rand;
		double rnd_x = (double)r.nextGaussian();
		double rnd_y = (double)r.nextGaussian();
		
		this.getPos().setX(x0 + rnd_x*Math.sqrt(Config.INITIAL_VAR));
		this.getPos().setY(y0 + rnd_y*Math.sqrt(Config.INITIAL_VAR));
	}
	
	public void reinit(double x0, double y0)
	{
		//this.gaussianInitialisation(x0, y0);
		this.uniformInitialisation();
		this.weight = 0;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setColor(this.color);

		g2d.fillOval((int)this.getPos().getX(), (int)this.getPos().getY(), this.getWidth(), this.getHeight());
		
		
		g2d.dispose();
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	
	public Particle getCpy()
	{
		Particle p = new Particle();
		
		p.getPos().setVector2(this.getPos());
		p.setWeight(this.getWeight());
		
		return p;
	}
	
	public void setTo(Particle other)
	{
		this.color = other.getColor();
		this.weight = other.getWeight();
		this.getPos().setXY(other.getPos().getX(), other.getPos().getY());
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setWeight(double w)
	{
		this.weight = w;
	}
	
	public double getWeight()
	{
		return this.weight;
	}

	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.pos;
	}
	
	public void setPos(Vector2 pos) {
		this.pos = pos;
	}


}
