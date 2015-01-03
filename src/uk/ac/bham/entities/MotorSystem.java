package uk.ac.bham.entities;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Vector;
import java.util.Map.Entry;

import uk.ac.bham.control.Action;
import uk.ac.bham.control.ActionOld;
import uk.ac.bham.control.ArmPos;
import uk.ac.bham.control.EActions;
import uk.ac.bham.control.HandStatus;
import uk.ac.bham.control.State;
import uk.ac.bham.control.TableStatus;
import uk.ac.bham.control.Robot.EMotorSystem;
import uk.ac.bham.data.Assets;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.GazeControl;
import uk.ac.bham.util.Vector2;


public class MotorSystem implements VisualEntity, Runnable {

	private BufferedImage sprite;

	public EMotorSystem side;

	//Execution control variables
	private Vector2 pos;
	private Vector2 pos0;
	private Vector2 posI;
	private Entity goal;
	private Vector2 goalPos;
	public Entity selectedObj;
	public Entity graspedObj;
	private double t;
	private boolean busy;

	public boolean onTask;

	private PerceptualMotorSystem pms;


	private State taskState;
	private boolean tryGrasp, tryRls;


	public double maxVal = 0;
	public double realVal = 0;


	/*Execution control*/
	public boolean finish;
	/* Frame Rate Control */
	public long t0, ti, timeTrack;
	public long last_fps, fps;

	public MotorSystem(EMotorSystem side)
	{
		this.pos = new Vector2();
		this.pos0 = this.getPos().getCpy();
		this.posI = this.getPos().getCpy();


		this.busy = false;
		this.t = 0;

		this.taskState = new State();
		this.tryGrasp = false;
		this.tryRls = false;

		this.goalPos = null;

		this.pms = null;

		this.side = side;

		this.selectedObj = null;
		this.graspedObj = null;

		this.finish = false;

	}

	public MotorSystem(BufferedImage sprite, EMotorSystem side)
	{
		this(side);


		this.sprite = sprite;

	}

	public MotorSystem(BufferedImage sprite, Vector2 pos0, EMotorSystem side)
	{
		this(sprite,side);

		this.pos0 = pos0;



	}

	public void resetPos()
	{
		this.posI = this.pos0.getCpy();
		this.pos = this.posI;
		this.onTask = false;
	}

	@Override
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();


		g2d.drawImage(this.sprite, (int)this.getPos().getX(), (int)this.getPos().getY(), null);

		g2d.setColor(Config.CENTER_COLOR);
		g2d.fillRect((int)this.getPos().getX(), (int)this.getPos().getY(), Config.CENTER_DIM, Config.CENTER_DIM);

		g2d.dispose();

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub


		this.processMoveToGoal();
		this.updateTaskState();
		
	}


	public void setPos(double x, double y) {
		this.getPos().setXY(x, y);
		this.pos0.setXY(x, y);
		this.posI = this.pos0.getCpy();

	}

	public void moveToGoal(Entity goal)
	{
		if( !this.busy )
		{
			this.goal = goal;

			this.t = 0;

			this.goalPos = goal.getEstPos().getCpy();

			this.busy = true;

			this.onTask = true;
		}


	}

	public void moveToGoal(Vector2 goalPos)
	{
		if( !this.busy )
		{


			this.t = 0;

			this.goalPos = goalPos.getCpy();

			this.busy = true;

			this.onTask = true;
		}


	}

	public void grasp()
	{
		if(this.selectedObj!=null)
		{
			if(this.getPos().dist(this.selectedObj.getEstPos()) <= Config.HAND_THR)
			{
				//Success
				this.taskState.armP = ArmPos.OnObj;
				this.taskState.handS = HandStatus.Grasping;

				this.graspedObj = selectedObj;


				graspedObj.setPos(this.getPos());
				//graspedObj.freezePf = true;


		//		System.out.println(this.side.name()+ ": Success on grasping!");
				this.taskState.parentAction = EActions.GRASP;

			}
			else
			{
				this.resetPos();

				this.getTaskState().handS = HandStatus.Empty;
				this.getTaskState().armP = ArmPos.OutTable;
		//		System.out.println(this.side.name()+ ": Failed to grasp!");
				//Fail
			}
		}
	}

	public void rls() throws IOException, InterruptedException
	{
		if( this.taskState.handS.equals(HandStatus.Grasping) && this.graspedObj!=null )
		{

			EntityPool.vm.removeObj(this.graspedObj);
			((TableObject)this.graspedObj).resetPos();


			graspedObj = null;
			selectedObj = null;
			this.resetPos();

			this.getTaskState().handS = HandStatus.Empty;
			this.getTaskState().armP = ArmPos.OutTable;

			EntityPool.rep.updateReport(1, 0);
			this.taskState.parentAction = EActions.RELEASE;
		//	System.out.println(this.side.name()+ ": Success of releasing!");


		}
		else 
		{
			//EntityPool.vm.removeObj(this.graspedObj);
			//((TableObject)this.graspedObj).resetPos();
/*
			graspedObj = null;
			selectedObj = null;
			this.resetPos();

			this.getTaskState().handS = HandStatus.Empty;
			this.getTaskState().armP = ArmPos.OutTable;
*/
		//	System.out.println(this.side.name()+ ": Fail to release!");
			//Fail
		}
	}

	public void processMoveToGoal()
	{
		if( !this.isBusy())
			return;

		this.getPos().setXY(posI.getX()*(1.0 - t) + goalPos.getX()*t, 
				posI.getY()*(1.0 - t) + goalPos.getY()*t
				);

		t += Config.interpolation_step_arm;



		//Achieved Goal
		if( t >= 1.0 )
		{
			t = 0;
			busy = false;
			this.getPos().setXY(goalPos.getX(), goalPos.getY());

			this.posI = this.getPos().getCpy();


		}
	}

	public void abort()
	{
		EActions a = this.taskState.parentAction;


		this.taskState = new State();
		this.resetPos();

		if( goal instanceof TableObject )
		{
			((TableObject)goal).resetPos();
		}

		EntityPool.robot.getPMS().resetPos();

	}

	public boolean isBusy()
	{
		return this.busy && !this.finish;
	}

	@Override
	public int getWidth() {
		return this.sprite.getWidth();
	}

	@Override
	public int getHeight() {

		return this.sprite.getHeight();
	}

	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return this.pos;
	}



	public void updateTaskState()
	{
		//We just update task space when the arm finishes performing some sub-task
		if(this.busy)
			return;


		int dtotal = 0;
		int success = 0;
		//Hand Status

/*

		//Arm Position		
		if( EntityPool.table.isOn(this.getPos()) && this.taskState.armP == ArmPos.OutTable )
		{
			this.taskState.armP = ArmPos.OnTable;

		}
		else if( !EntityPool.table.isOn(this.getPos()) )
		{
			this.taskState.armP = ArmPos.OutTable;
		}

		//Table Status
		if( EntityPool.table.hasObjects() )
		{
			this.taskState.tableS = TableStatus.ObjsOn;
		}
		else
		{
			this.taskState.tableS = TableStatus.Empty;
		}

*/



	}

	public void setPMS(PerceptualMotorSystem pms)
	{
		this.pms = pms;
	}

	public PerceptualMotorSystem getPMS()
	{
		return this.pms;
	}
	public State getTaskState()
	{
		return this.taskState;
	}

	public void executeAction(EActions action) throws InterruptedException, IOException
	{
		//System.out.println("Arm: " + this.side.name() + " Action: " + action.name());
		switch(action)
		{
		case MV_TO_OBJECT:

			//	System.out.println(this.side.name() + " Move to object!");
			this.moveToObject();

			break;
		case GRASP:
			//	System.out.println(this.side.name() + " Grasp!");
			this.grasp();

			break;
		case MV_TO_CONTAINER:

			//	System.out.println(this.side.name() + " Move to container!");

			this.moveToContainer();

			break;
		case MV_TO_TABLE:
			//System.out.println("Move to table");
			this.moveToTable();
			break;
		case RELEASE:
			//System.out.println(this.side.name() + " Release");

			this.rls();
			break;
		case NOP:

			System.out.println(this.side.name() + " Nop");

			break;
		default:
			System.out.println(this.side.name() + " Something else");
			break;
		}
	}

	public void moveToTable() throws InterruptedException
	{
		if( this.getTaskState().handS.equals(HandStatus.Grasping))
			return;

		double translationX = this.side.equals(EMotorSystem.RIGHT)? EntityPool.table.getWidth()/2 : -EntityPool.table.getWidth()/2; 
		Vector2 tableCenter = EntityPool.table.getCenter().getCpy();
		tableCenter.add(translationX, 0);


		this.moveToGoal(tableCenter);

		//checking repeated motion (do nothing in this case)
		if( this.getPos().dist(tableCenter) <= 0.1 )
		{
			this.sleep(100);
			//System.out.println("NO MOVEMENT!");
			return;
		}


		Thread exec = new Thread(new Executor());

		exec.start();

		exec.join(); //Wait action to complete

		//Check if the action succeeded (getCenter() is the true obj pos)
		if( this.getPos().dist(tableCenter) <= 0.1 )
		{
			//System.out.println("MV_TO_TABLE: Success");

			this.getTaskState().armP = ArmPos.OnTable;

		}
		else
		{
			//System.out.println("MV_TO_TABLE: Failed miserably");
			//Fail
		}

	}

	public void moveToContainer() throws InterruptedException
	{
		Entity container = EntityPool.vm.getContainer(this.side);

		if( container != null )
		{
			this.moveToGoal(container);

			//checking repeated motion (do nothing in this case)
			if( this.getPos().dist(container.getEstPos()) <= 1 )
			{
				this.sleep(100);
				//System.out.println("NO MOVEMENT!");
				return;
			}


			Thread exec = new Thread(new Executor());

			exec.start();

			exec.join(); //Wait action to complete

			//Check if the action succeeded (getCenter() is the true obj pos)
			if( this.getPos().dist(container.getCenter()) <= Config.REACH_THR )
			{

				if(this.getPos().dist(container.getEstPos()) <= Config.HAND_THR)
				{

					//Success
					this.taskState.armP = ArmPos.OnCont;
					this.taskState.parentAction = EActions.MV_TO_CONTAINER;
			//		System.out.println(this.side.name()+ ": Sucess on moving to container!!");
				}
				else
				{
					
					//this.graspedObj = null;
					//this.selectedObj = null;

		//			System.out.println(this.side.name()+ ": Failed moving to container!");
					//Fail
				}

			}
			else
			{
				//this.graspedObj = null;
				//this.selectedObj = null;
			//	System.out.println(this.side.name()+ ": Failed moving to container!");
				//Fail
			}
		}
	}

	public void moveToObject() throws InterruptedException, IOException
	{
		if( this.selectedObj != null )
		{

			//checking repeated motion (do nothing in this case)
			if( this.getPos().dist(this.selectedObj.getEstPos()) <= 1 )
			{
				this.sleep(100);
				//System.out.println("NO MOVEMENT!");
				return;
			}


			moveToGoal(this.selectedObj);


			Thread exec = new Thread(new Executor());

			exec.start();

			exec.join(); //Wait action finish


			//Check if the action succeeded (getCenter() is the true obj pos)
			if( this.getPos().dist(this.selectedObj.getCenter()) <= Config.REACH_THR )
			{

				if(this.getPos().dist(this.selectedObj.getEstPos()) <= Config.HAND_THR)
				{

					//Success
					this.taskState.armP = ArmPos.OnObj;

				//	System.out.println(this.side.name()+ ": Sucess on moving to object!!");
					this.taskState.parentAction = EActions.MV_TO_OBJECT;
					EntityPool.rep.updateReport(0, 1);
				}
				else
				{
					//this.resetPos();
					//this.selectedObj = null;
					//this.getTaskState().handS = HandStatus.Empty;
					//this.getTaskState().armP = ArmPos.OutTable;
				//	System.out.println(this.side.name()+ ": Failed moving to object!");
					//Fail
				}

			}
			else
			{
				//this.resetPos();

				//this.getTaskState().handS = HandStatus.Empty;
				//this.getTaskState().armP = ArmPos.OutTable;
				//System.out.println(this.side.name()+ ": Failed moving to object!");
				//Fail
			}




		}

	}

	@Override
	public void run() {



		while(!this.finish)
		{



			//Select best action
			Entry<Entity,EActions> entry = GazeControl.argMaxAction(EntityPool.vm, this);


			//System.out.println(this.side.name() + " " + (entry.getKey()!=null?entry.getKey().getId():entry.getKey())+ " - "+ entry.getValue().name() +": " + this.realVal + ": " + this.maxVal);



			//Schedule execution of best action (MotorSystem will become busy)
			try {

				
					this.executeAction(entry.getValue());
				
				//this.updateTaskState();



			} catch (Exception e) {

				e.printStackTrace();
			}

			//System.out.println("That's me: " + this.side.name() + " FPS: " + this.last_fps);


		}

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

		if( tf < Config.TIME_PER_FRAME_ARM )
		{
			//To create a loop
			this.sleep( Config.TIME_PER_FRAME_ARM - tf);
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

				processMoveToGoal();

				frameControl();
			}

		}

	}




}
