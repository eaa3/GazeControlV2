package uk.ac.bham.view;


import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import uk.ac.bham.control.EActions;
import uk.ac.bham.control.Robot;
import uk.ac.bham.control.Robot.EMotorSystem;
import uk.ac.bham.data.Assets;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.entities.Entity;
import uk.ac.bham.entities.MotorSystem;
import uk.ac.bham.entities.Table;
import uk.ac.bham.entities.TableObject;
import uk.ac.bham.entities.VisualEntity;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.Vector2;
import uk.ac.bham.util.GazeControl.GazeModels;


public class GCFrame extends JFrame implements KeyListener, MouseListener {	

	BufferedImage buffer;



	/* Frame Rate Control */
	public static long t0, ti, timeTrack;
	private long last_fps, fps;


	private EActions nextAction;

	public GCFrame () throws IOException {
		super("Gaze Control Simulation");

		setSize(Config.FRAME_W, Config.FRAME_H);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {


			}
		} );

		//Adding listeners
		this.addKeyListener(this);
		this.addMouseListener(this);



		buffer = new BufferedImage(Config.FRAME_W, Config.FRAME_H, BufferedImage.TYPE_INT_ARGB);


		Assets.loadAssets();
		EntityPool.loadEntities();

		ti = System.currentTimeMillis();
		timeTrack = System.currentTimeMillis();


	}


	//Methodo Main, o metodo de entrada do programa: primeiro metodo chamado pela JVM
	public static void main(String[] args) throws IOException {
		
		if( args.length >= 1)
		{
			Config.REACH_THR = Double.parseDouble(args[0]);
		}
		
		if( args.length >= 2 )
		{
			Config.ROBOT_HALF_FOV = Double.parseDouble(args[1])/2;
		}
		
		if( args.length >= 3)
		{
			Config.TRIAL_TIME = Integer.parseInt(args[2]);
		}
		
		if( args.length >= 4 )
		{
			if( args[3].equals("RUG"))
				Config.GAZE_MODEL = GazeModels.RUG;
			
			if( args[3].equals("RU"))
				Config.GAZE_MODEL = GazeModels.RU;
			
			if( args[3].equals("UNC"))
			{
				Config.GAZE_MODEL = GazeModels.UNC;
				Config.MAX_FPS = 20;
				Config.MAX_FPS_HEAD = 25;
				
			}
				
		}
		
		if( args.length > 4 )
		{
			Config.EXPERIMENT = args[4];
		}
		

		final GCFrame  cgframe = new GCFrame ();

		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				cgframe .setVisible(true);
			}
		});
	}




	public void sleep(long time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public void paint(Graphics g) {


		t0 = System.currentTimeMillis();

		/* Update Simulation Logic */

		for(VisualEntity e : EntityPool.entities)
			e.update();



		//Gets graphical context
		Graphics2D g2d = (Graphics2D) buffer.createGraphics();	

		//Clear screen
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, Config.FRAME_W, Config.FRAME_H);

		/*********Draw stuff here************/
		//Draw background
		g2d.drawImage(Assets.background,0,0,null);


		for(VisualEntity e : EntityPool.entities)
			e.paint(g2d);


		g2d.setColor(Color.BLACK);
		g2d.drawString("FPS: "+last_fps, 20, 50);

		//Draw final buffer image on the screen
		g.drawImage(buffer,0,0,this);		

		//End of drawing
		g2d.dispose();

		this.frameControl();
		this.repaint();



	}

	public void frameControl()
	{
		fps++;

		long tf = System.currentTimeMillis() - t0;

		if( (System.currentTimeMillis() - timeTrack) >= Config.SECOND)
		{
			last_fps = fps;
			fps = 0;
			timeTrack = System.currentTimeMillis();
		}

		if( tf < Config.TIME_PER_FRAME )
		{
			//To create a loop
			this.sleep( Config.TIME_PER_FRAME - tf);
		}
	}

	public static long getTimeElapsed()
	{
		return (System.currentTimeMillis() - ti);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if( arg0.getKeyChar() == '1' )
		{
			nextAction = EActions.MV_TO_OBJECT;
		}
		if( arg0.getKeyChar() == '2' )
		{
			nextAction = EActions.GRASP;
		}
		if( arg0.getKeyChar() == '3' )
		{
			nextAction = EActions.MV_TO_CONTAINER;
		}
		if( arg0.getKeyChar() == '4' )
		{
			nextAction = EActions.RELEASE;
		}
		if( arg0.getKeyChar() == '5' )
		{
			nextAction = EActions.MV_TO_TABLE;
		}
		if( arg0.getKeyChar() == '6' )
		{
			nextAction = EActions.NOP;
		}

		//System.out.println("NextAction: " + nextAction.name());
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		/*Vector2 pos = new Vector2(arg0.getX(),arg0.getY());

		Entity g = null;

		g = EntityPool.vm.selectNearestObj(EMotorSystem.RIGHT, pos);

		if(g!=null) System.out.println("Found it!");
		if( g == null )
		{
			g = new TableObject(EntityPool.table);

			g.setPos(pos);

			for(int i = 0; i < 100; i++)
				g.getParticleFilter().updateEstimates(g.getCenter());


			g.updateCenter();
		}

		for(int i = 0; i < 10000; i++)
			g.getParticleFilter().updateEstimates(g.getCenter());
		System.out.printf("Mouse(%f,%f)\n",g.getEstPos().getX(),g.getEstPos().getY());

		EntityPool.robot.getPMS().saccade(g);


		//EntityPool.robot.moveToGoal(EMotorSystem.RIGHT, g);*/
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	public static void initAll() throws IOException
	{
		Assets.loadAssets();
		EntityPool.loadEntities();

		ti = System.currentTimeMillis();
		timeTrack = System.currentTimeMillis();
	}



}
