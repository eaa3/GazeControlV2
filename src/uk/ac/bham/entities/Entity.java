package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import uk.ac.bham.control.Robot.EMotorSystem;

import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;

public abstract class Entity implements VisualEntity {
	
	private static long nextId = 0;
	
	private Vector2 pos, center;
	private long id;
	
	private double predValue;
	private double gain;
	private double value;
	
	private Color centerColor;
	
	
	private ParticleFilter pf;
	
	private static double maxBarValue = 0;;
	
	private boolean inVM;
	
	private long seenTime;
	
	private EMotorSystem ownerMotorSystem;
	
	public boolean drawBrighter;
	
	
	public boolean freezePf = false;

	
	public Entity()
	{
		this.pos = new Vector2();
		this.center = new Vector2();
		
		this.id = nextId++;
		
		centerColor = Config.CENTER_COLOR;
		
		this.inVM = false;
		
		
	}
	
	
	public void resetPf()
	{
		
		//this.pf = new ParticleFilter(Config.N_PARTICLES, this.getCenter());
		if( this.pf == null ) {
			this.pf = new ParticleFilter(Config.N_PARTICLES, this.getCenter());
		}
		else
		{	
			synchronized (this.pf) {
				this.pf.initialize(this.getCenter().getX(), this.getCenter().getY());
			}
			
		}
		
		this.inVM = false;
	}
	

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setColor(centerColor);
		g2d.fillRect((int)this.getCenter().getX(), (int)this.getCenter().getY(), Config.CENTER_DIM, Config.CENTER_DIM);
		
		if( this.isInVM() ) {
			this.drawBar(g2d, 40, this.getValue()*20, Color.GREEN );
			
			
			if( this.pf != null )
			{
				synchronized (this.pf) {
					this.pf.paint(g2d);
				}
			}
			
			
		}
		
		
		
		g2d.dispose();
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
	public void drawBar(Graphics2D g2d, double max, double value, Color color)
	{
		g2d = (Graphics2D)g2d.create();
		if( value >= Entity.maxBarValue )
			maxBarValue = value;
		
		int height = (int)((value*40)/max);

		int offset = -40;


		if( height < 0 ) {
			color = Color.RED;
			height = -height;
		}
		

		g2d.setColor(color);

		g2d.fillRect((int)(this.getPos().getX() + this.getWidth()), (int)(this.getPos().getY() + offset), 10, height);

		g2d.dispose();
	}
	
	
	public Vector2 getPos() {
		return pos;
	}



	public void setPos(Vector2 pos) {
		this.pos = pos;
		
		this.updateCenter();
	}
	
	public Vector2 getEstPos()
	{
		return this.pf.getPos();
	}
	
	public Vector2 getImaginaryPos()
	{
		return this.pf.getImaginaryPf().getPos();
	}
	
	public long getId()
	{
		return this.id;
	}
	
	
	public void setPredValue(double predValue)
	{
		this.predValue = predValue;
	}
	
	public double getPredValue()
	{
		return predValue;
	}
	
	public void setGain(double gain)
	{
		this.gain = gain;
	}
	
	public double getGain()
	{
		return gain;
	}
	
	public Vector2 getCenter()
	{
		return this.center;
	}
	
	public void setCenter(Vector2 center)
	{
		this.center = center;
	}
	
	public void updateCenter()
	{
		this.getCenter().setXY(this.getPos().getX()+this.getWidth()/2, this.getPos().getY()+this.getHeight()/2);
	}
	
	public void setCenterColor(Color color)
	{
		this.centerColor = color;
	}
	
	public Color getCenterColor()
	{
		return this.centerColor;
	}
	
	public boolean isOn(Vector2 pos)
	{
		return (this.getPos().getX() <= pos.getX() && this.getPos().getY() <= pos.getY() && pos.getX() <=  (this.getPos().getX()+this.getWidth()) && pos.getY() <=  (this.getPos().getY()+this.getHeight()) );
	}
	
	public boolean isInVM()
	{
		return inVM;
	}
	
	public void setInVM(boolean inVM)
	{
		this.inVM = inVM;
	}
	
	public EMotorSystem getOwnerMotorSystem()
	{
		return this.ownerMotorSystem;
	}
	
	public void setOwnerMotorSystem(EMotorSystem ownerMotorSystem)
	{
		this.ownerMotorSystem = ownerMotorSystem;
	}
	
	public void setSeenTime()
	{
		this.seenTime = System.currentTimeMillis();
	}

	public long notSeenTime()
	{
		return System.currentTimeMillis() - this.seenTime;
	}
	
	public void forget()
	{
		this.pf.forget();
	}
	
	public ParticleFilter getParticleFilter()
	{
		return this.pf;
	}

	public void setValue(double val)
	{
		this.value = val;
	}

	public double getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}
	
	
	public String toString()
	{
		if( this instanceof TableObject )
			return "(Object" + this.getId()+", " + this.getValue() + "," +ownerMotorSystem.name()+")"; 

		else
			return "(Container" + this.getId()+", " + this.getValue() + "," +ownerMotorSystem.name()+")"; 
	}
}
