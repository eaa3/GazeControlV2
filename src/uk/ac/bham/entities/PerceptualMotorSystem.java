package uk.ac.bham.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.Map.Entry;

import uk.ac.bham.control.EActions;
import uk.ac.bham.control.EPercepAction;
import uk.ac.bham.control.Robot;
import uk.ac.bham.data.Assets;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.GazeControl;
import uk.ac.bham.util.GazeControl.GazeModels;
import uk.ac.bham.util.Vector2;

public class PerceptualMotorSystem implements VisualEntity, Runnable {

	private BufferedImage head;
	private double phi, phiI, phiF, t;
	private double dir;
	private boolean busy;

	private Entity goal;

	private Vector2 headPos, eyePos, centralHeadPos;
	private Robot body;

	/*Execution control*/
	public boolean finish;
	/* Frame Rate Control */
	public long t0, ti, timeTrack;
	public long last_fps, fps;


	public PerceptualMotorSystem(Robot body)
	{
		this.body = body;

		this.head = Assets.head;
		this.headPos = new Vector2();
		this.eyePos = new Vector2();
		this.centralHeadPos = new Vector2();

		this.busy = false;

		this.resetPos();
		this.dir = -1;
	}


	public void resetPos()
	{
		this.headPos.setXY((Assets.body.getWidth() - head.getWidth())/2 , -head.getHeight()/4);
		this.headPos.add(this.body.getPos());

		this.eyePos.setXY(headPos.getX()+head.getWidth()/2.0f, headPos.getY());

		this.centralHeadPos.setXY(headPos.getX()+head.getWidth()/2.0f, headPos.getY()+head.getHeight()/2.0f); 

		this.busy = false;
		this.phi = this.phiI = this.phiF = 0;
	}


	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();

		AffineTransform afTemp = g2d.getTransform();


		g2d.setTransform(this.getCurrentTransform());

		g2d.drawImage(this.head, (int)this.headPos.getX(), (int)this.headPos.getY(),null);

		//Drawing field of view
		g2d.setColor(Color.ORANGE);

		g2d.drawLine((int)eyePos.getX(), (int)eyePos.getY(),(int) (eyePos.getX()+Math.sin(Math.toRadians(Config.ROBOT_HALF_FOV))*Config.ROBOT_RADIUS),(int) (eyePos.getY()-Math.cos(Math.toRadians(Config.ROBOT_HALF_FOV))*Config.ROBOT_RADIUS));
		g2d.drawLine((int)eyePos.getX(), (int)eyePos.getY(),(int) (eyePos.getX()+Math.sin(Math.toRadians(-Config.ROBOT_HALF_FOV))*Config.ROBOT_RADIUS),(int) (eyePos.getY()-Math.cos(Math.toRadians(-Config.ROBOT_HALF_FOV))*Config.ROBOT_RADIUS));

		g2d.setTransform(afTemp);

		Vector2 e = this.getCurrentEyePos();

		g2d.setColor(Color.RED);
		Vector2 y_axis = transform(new Vector2(0,-100).add(eyePos)).sub(e);


		g2d.drawLine((int) e.getX(), (int)e.getY(),(int)(y_axis.getX()+e.getX()),(int)(y_axis.getY()+e.getY()));

		g2d.dispose();


	}

	@Override
	public void update() {
		// TODO Auto-generated method stub


		this.processSaccade();


	}

	public void saccade(Entity goal)
	{
		if( goal == this.goal ) return;

		this.goal = goal;

		this.phiF = this.computePhiF(goal);

		this.saccade(phiF);

		//this.phi = this.phi_dest;
	}

	public void lookAt(Entity goal) throws InterruptedException
	{
		if( this.isBusy() )
			return;


		this.saccade(goal);


		Thread exec = new Thread(new Executor());
		exec.start();
		exec.join(); //wait subtask complete

		EntityPool.vm.updateVM(this.body);



	}

	public double computePhiF(Entity goal)
	{

		Point ptmp_src = this.eyePos.toPoint();
		Point ptmp_dst = new Point();

		this.getCurrentTransform().transform(ptmp_src, ptmp_dst);
		Vector2 v_eyes = new Vector2(ptmp_dst);

		Vector2 y_axis = new Vector2(0,-1);

		boolean left_side = false;
		Vector2 vtmp = goal.getCenter().getCpy();
		if( vtmp.getX() < ptmp_dst.getX() ) left_side = true;
		vtmp.sub(v_eyes);


		double phiF = left_side? -vtmp.degreesAngle(y_axis):vtmp.degreesAngle(y_axis);


		return phiF;
	}

	public void saccade(double phiF)
	{
		this.phiI = this.phi;
		this.phiF = phiF;
		this.t = 0;

		this.busy = true;
	}

	public void processSaccade()
	{
		if( this.isBusy())
		{
			t += Config.interpolation_step_head;
			if( goal != null ) this.phiF = this.computePhiF(goal);
			this.phi = this.phiI*(1.0-t) + this.phiF*t;

			//Achieved Goal
			if( t >= 1.0 )
			{
				t = 0;
				busy = false;
				this.phi = this.phiF;

				//Perform fixation here!!!
				//Update VM here!

			}
		}
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return this.head.getWidth();
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return this.head.getHeight();
	}

	public boolean isBusy()
	{
		return this.busy && !this.finish;
	}


	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.headPos;
	}

	public AffineTransform getCurrentTransform()
	{
		return AffineTransform.getRotateInstance(Math.toRadians(this.phi), this.centralHeadPos.getX(), this.centralHeadPos.getY());
	}

	public Vector2 getEyePos()
	{
		return this.eyePos;
	}

	public Vector2 getCurrentEyePos()
	{
		Point ptmp_src = this.eyePos.toPoint();
		Point ptmp_dst = new Point();

		this.getCurrentTransform().transform(ptmp_src, ptmp_dst);

		return new Vector2(ptmp_dst);
	}

	public double getPhi()
	{
		return this.phi;
	}

	public Vector2 transform(Vector2 v)
	{
		Point ptmp_src = v.toPoint();
		Point ptmp_dst = new Point();

		this.getCurrentTransform().transform(ptmp_src, ptmp_dst);

		return new Vector2(ptmp_dst);
	}


	public void search() throws InterruptedException
	{
		if( this.isBusy() )
			return;


		while( !EntityPool.vm.hasObjects() || !EntityPool.vm.hasContainers() )
		{
			if( this.getPhi()>=85 || this.getPhi() <= -85) dir *= -1;

			this.goal = null;
			this.saccade(this.getPhi()+10*dir);


			Thread exec = new Thread(new Executor());
			exec.start();
			exec.join(); //wait subtask complete

			EntityPool.vm.updateVM(this.body);

		}




	}

	public void executeAction(Entry<Entity,EPercepAction> fixationAction) throws InterruptedException
	{
		switch(fixationAction.getValue())
		{
		case LOOK_AT_OBJECT:
			this.lookAt(fixationAction.getKey());
			break;
		case SEARCH:
			this.search();
			break;
		default:
			break;
		}


	}
	
	public static double meanTime = 0;
	public static long mti=0;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!this.finish)
		{
			//Select best perception action w.r.t Gaze Model (LOOK_AT_TABLE, LOOK_AT_POINT, VISUAL_SEARCH)
			
			
			long time0 = System.currentTimeMillis();
			Entry<Entity,EPercepAction> fixationAction = selectPercepAction(Config.GAZE_MODEL);
			long timef = System.currentTimeMillis();
			long time = timef - time0;
			long dt = 125 - time;
			
			/*if( (Config.GAZE_MODEL.equals(GazeModels.UNC) ||Config.GAZE_MODEL.equals(GazeModels.RU)) && dt > 0 )
			{
				System.out.println("Sleep time: " + dt);
				sleep(dt);
			}*/
			
			mti++;
			meanTime += time;
			System.out.println("Time Elapsed: " + (timef-time0) + " MeanTime: " + (meanTime/mti));

			//Execute perception action
			try {
				
				this.executeAction(fixationAction);

				//EntityPool.vm.updateObjectsToForget(this.body);


			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			//this.sleep(1);
		
		}
		
		
	}

	public Entry<Entity,EPercepAction> selectPercepAction(GazeModels model)
	{
		EPercepAction bestPAction = EPercepAction.SEARCH;
		Entry<Entity,EPercepAction> fixationEntry = null;

		if( !EntityPool.vm.hasObjects() )
		{
			fixationEntry = new AbstractMap.SimpleEntry<Entity,EPercepAction>(null,bestPAction);
			return fixationEntry;
		}

		Entry<Entity,EPercepAction> entryRight = null;
		Entry<Entity,EPercepAction> entryLeft = null;



		if( model.equals(GazeModels.RUG) || model.equals(GazeModels.RU)  )
		{
			//Get candidate of right arm
			entryRight = GazeControl.futureArgMaxAction(EntityPool.vm, this.body.getRightArm());

			//Get candidate of left arm
			entryLeft = GazeControl.futureArgMaxAction(EntityPool.vm, this.body.getLeftArm());

		}
		else if( model.equals(GazeModels.UNC) ) 
		{
			//Unc model
			GazeControl.setUncValues(this.body.taskSpace.getActions(this.body.getRightArm().getTaskState()), EntityPool.vm.visualMemory, this.body.getRightArm().side.ordinal());

			GazeControl.setUncValues(this.body.taskSpace.getActions(this.body.getLeftArm().getTaskState()), EntityPool.vm.visualMemory, this.body.getLeftArm().side.ordinal());

			Entity objRight = EntityPool.vm.selectAnyObj(this.body.getRightArm().side);
			Entity objLeft = EntityPool.vm.selectAnyObj(this.body.getLeftArm().side);

			if( objRight == null )
				entryRight = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objRight,EPercepAction.SEARCH);
			else if( objRight instanceof TableObject)
				entryRight = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objRight,EPercepAction.LOOK_AT_OBJECT_RIGHT);
			else
				entryRight = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objRight,EPercepAction.LOOK_AT_CONTAINER_RIGHT);
			
			
			if( objLeft == null )
				entryLeft = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objLeft,EPercepAction.SEARCH);
			else if( objLeft instanceof TableObject)
				entryLeft = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objLeft,EPercepAction.LOOK_AT_OBJECT_LEFT);
			else
				entryLeft = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objLeft,EPercepAction.LOOK_AT_CONTAINER_LEFT);
			
			
			
			entryLeft = new AbstractMap.SimpleEntry<Entity,EPercepAction>(objLeft,EPercepAction.LOOK_AT_OBJECT);


		}


		if( entryRight.getKey() != null && entryLeft.getKey() != null )
		{
			switch (model) {
			case RUG:
				entryRight.getKey().setValue(entryRight.getKey().getGain());
				entryLeft.getKey().setValue(entryLeft.getKey().getGain());
				break;
			case RU:
				entryRight.getKey().setValue(entryRight.getKey().getPredValue());
				entryLeft.getKey().setValue(entryLeft.getKey().getPredValue());
				break;
			default:
				break;
			}


			//Select the best fixation entry between the two candidates
			if( entryRight.getKey().getValue() > entryLeft.getKey().getValue() )
			{
				fixationEntry = entryRight;
			}
			else if( entryRight.getKey().getValue() < entryLeft.getKey().getValue() )
			{
				fixationEntry = entryLeft;
			}
			else
			{
				fixationEntry = Math.random() >= 0.5? entryRight : entryLeft;
			}
		}
		else
		{
			fixationEntry = entryRight.getKey() != null? entryRight : entryLeft;
		}









		if( fixationEntry.getValue().equals(EPercepAction.LOOK_AT_OBJECT_RIGHT) )
		{
			//System.out.println("Look at right object");
			fixationEntry.setValue(EPercepAction.LOOK_AT_OBJECT);
		}
		if( fixationEntry.getValue().equals(EPercepAction.LOOK_AT_OBJECT_LEFT) )
		{
			//System.out.println("Look at left object");
			fixationEntry.setValue(EPercepAction.LOOK_AT_OBJECT);
		}
		if( fixationEntry.getValue().equals(EPercepAction.LOOK_AT_CONTAINER_RIGHT) )
		{
			//System.out.println("Look at right container");
			fixationEntry.setValue(EPercepAction.LOOK_AT_OBJECT);
		}	
		if( fixationEntry.getValue().equals(EPercepAction.LOOK_AT_CONTAINER_LEFT) )
		{
			//System.out.println("Look at left container");
			fixationEntry.setValue(EPercepAction.LOOK_AT_OBJECT);
		}

		//EntityPool.vm.printMaximum();
		//System.out.println("Fixation: " + fixationEntry.getKey().toString());
		
		

		return fixationEntry;
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

	public void markStartTime()
	{
		t0 = System.currentTimeMillis();
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

		if( tf < Config.TIME_PER_FRAME_HEAD )
		{
			//To create a loop
			this.sleep( Config.TIME_PER_FRAME_HEAD - tf);
		}
	}
	
	public void finish()
	{
		this.finish = true;
	}

	class Executor implements Runnable
	{

		@Override
		public void run() {
			while( isBusy() )
			{
				markStartTime();
				//Execute perception action
				processSaccade();


				frameControl();
			}



		}

	}

}
