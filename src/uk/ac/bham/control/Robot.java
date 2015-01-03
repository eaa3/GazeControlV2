package uk.ac.bham.control;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;

import uk.ac.bham.data.Assets;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.entities.Entity;
import uk.ac.bham.entities.MotorSystem;
import uk.ac.bham.entities.PerceptualMotorSystem;
import uk.ac.bham.entities.TableObject;
import uk.ac.bham.entities.VisualEntity;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.GazeControl;
import uk.ac.bham.util.GazeControl.GazeModels;
import uk.ac.bham.util.Vector2;



public class Robot implements VisualEntity {


	private PerceptualMotorSystem pms;
	private MotorSystem[] ms;
	
	private Thread[] arms;
	private Thread head;

	private BufferedImage body;

	private Vector2 pos;
	
	public Graph taskSpace;
	
	public Entity fixP;
	

	public static enum EMotorSystem{
		RIGHT,
		LEFT,

		N_MS
	}

	public Robot() throws IOException
	{

		this.arms = new Thread[2];
		
		this.ms = new MotorSystem[EMotorSystem.N_MS.ordinal()];

		this.ms[EMotorSystem.RIGHT.ordinal()] = new MotorSystem(Assets.rightArm, EMotorSystem.RIGHT);
		this.ms[EMotorSystem.LEFT.ordinal()] = new MotorSystem(Assets.leftArm, EMotorSystem.LEFT);

		this.body = Assets.body;

		this.pos = new Vector2();
		
		this.pms = new PerceptualMotorSystem(this);
		
		this.resetPos();

		
		
		taskSpace = Environment.createStateEnvironmentGraph2();

		
	}
	
	public void startThreads()
	{
		for( MotorSystem m : this.ms )
		{
			
			this.arms[m.side.ordinal()] =  new Thread(m);
			this.arms[m.side.ordinal()].start();
		}
		
		this.head = new Thread(this.pms);
		this.head.start();
	}

	public void resetPos()
	{
		//Setting initial robot position
		this.pos.setX((Config.FRAME_W-body.getWidth())/2);
		this.pos.setY(Config.FRAME_H-body.getHeight() - 60);

		this.getRightArm().setPos(this.pos.getX() + body.getWidth() - Assets.rightArm.getWidth(), this.pos.getY() - Assets.rightArm.getHeight()/2);
		this.getLeftArm().setPos(this.pos.getX(), this.getPos().getY()-Assets.leftArm.getHeight()/2);
	
		this.pms.resetPos();
	}


	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		Graphics2D g2d = (Graphics2D) g.create();

		g2d.drawImage(this.body, (int)this.pos.getX(),(int)this.pos.getY(),null);

		pms.paint(g2d);

		for(int i = 0; i < EMotorSystem.N_MS.ordinal(); i++)
		{
			ms[i].paint(g2d);
		}

		g2d.dispose();


	}

	@Override
	public void update() {
		// TODO Auto-generated method stub	
		
		try {
			EntityPool.rep.updateReport(0, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return this.body.getWidth();
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return this.body.getHeight();
	}


	public Vector2 getPos()
	{
		return this.pos;
	}

	public void setPos(Vector2 pos)
	{
		this.pos.setXY(pos.getX(), pos.getY());
	}

	public void moveToGoal(EMotorSystem motorSystem, Entity goal)
	{
		this.getArm(motorSystem).moveToGoal(goal);
	}
	
	public MotorSystem getArm(EMotorSystem motorSystem)
	{
		return this.ms[motorSystem.ordinal()];
	}
	
	public MotorSystem getLeftArm()
	{
		return this.ms[EMotorSystem.LEFT.ordinal()];
	}
	
	public MotorSystem getRightArm()
	{
		return this.ms[EMotorSystem.RIGHT.ordinal()];
	}
	
	
	public PerceptualMotorSystem getPMS()
	{
		return this.pms;
	}
	
	
	public void finishAll() throws InterruptedException
	{
		this.pms.finish();
		System.out.printf("Waiting head to finsh.\n");
		this.head.join();
		
		for(int i = 0; i < this.ms.length; i++)
		{
			this.ms[i].finish();
			
			
			
		}
		
		for(int i = 0; i < this.ms.length; i++)
		{
			System.out.printf("Waiting arm %s to finsh.\n",this.ms[i].side.name());
			this.arms[this.ms[i].side.ordinal()].join();
			
			
			
		}
		
		
		
		
	}




}
