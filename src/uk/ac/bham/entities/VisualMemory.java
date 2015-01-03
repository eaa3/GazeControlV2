package uk.ac.bham.entities;

import java.awt.Point;
import java.util.Collections;
import java.util.Vector;

import uk.ac.bham.control.Robot;
import uk.ac.bham.control.Robot.EMotorSystem;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.StaticUtils;
import uk.ac.bham.util.Vector2;


public class VisualMemory {
	public Vector<Entity> visualMemory;

	public VisualMemory()
	{
		visualMemory = new Vector<Entity>();
	}


	public void removeObj(Entity entity)
	{
		synchronized (visualMemory) {

			visualMemory.remove(entity);

		}
	}

	public boolean hasObjects()
	{
		boolean ret = false;

		synchronized (visualMemory) {

			for(Entity obj : visualMemory )
			{
				if( obj instanceof TableObject )
				{
					ret = true;
				}
			}

		}


		return ret;
	}

	public boolean hasContainers()
	{
		int c = 0;

		synchronized (visualMemory) {
			for(Entity obj : visualMemory )
			{
				if( obj instanceof Container )
				{
					c++;
				}
			}

		}


		return c > 1;
	}


	public Entity getRandomTObject(EMotorSystem ems)
	{
		Entity e = null;

		synchronized (visualMemory) {

			for(int i = 0; i < this.visualMemory.size(); i++)
			{
				e = this.visualMemory.get(i);

				if( e instanceof TableObject && e.getOwnerMotorSystem() == ems )
				{
					return e;
				}
			}

		}



		return e;
	}

	public Entity getRandomObj()
	{
		Entity e = null;

		synchronized (visualMemory) {
			e = this.visualMemory.get((int)(Math.random()*this.visualMemory.size()));
		}

		return e;
	}

	public Entity getContainer(EMotorSystem ems)
	{
		Entity e = null;
		Entity ret = null;

		synchronized (visualMemory) {
			for(int i = 0; i < this.visualMemory.size() && ret == null; i++)
			{
				e = this.visualMemory.get(i);

				if( e instanceof Container && e.getOwnerMotorSystem() == ems )
				{
					ret =  e;
				}

			}

		}


		return ret;
	}

	public Entity selectObj()
	{
		Entity ret = null;

		synchronized (visualMemory) {

			for(Entity e : visualMemory)
			{
				if( e instanceof TableObject )
					ret =  e;
			}

		}


		return ret;
	}

	public Entity selectObj(EMotorSystem ems)
	{
		Entity e = null;
		Entity ret = null;

		synchronized (visualMemory) {

			for(int i = 0; i < this.visualMemory.size(); i++)
			{
				e = this.visualMemory.get(i);

				if( (e instanceof TableObject) && e.getOwnerMotorSystem().equals(ems))
				{
					ret =  e;
				}

			}

		}


		return ret;
	}

	public Entity selectAnyObj(EMotorSystem ems)
	{
		Entity e = null;
		Entity ret = null;

		double maxVal = -100;
		double val = 0;
		synchronized (visualMemory) {

			for(int i = 0; i < this.visualMemory.size(); i++)
			{
				e = this.visualMemory.get(i);

				if( e.getValue() > maxVal && e.getOwnerMotorSystem().equals(ems) )
				{
					maxVal = e.getValue();
					ret =  e;
				}

			}

		}


		return ret;
	}

	public Entity selectNearestObj(EMotorSystem ems, Vector2 pos)
	{
		Entity e = null;
		double minDist = Double.MAX_VALUE;
		Entity nearestEntity = null;

		synchronized (visualMemory) {

			for(int i = 0; i < this.visualMemory.size(); i++)
			{
				e = this.visualMemory.get(i);

				if( (e instanceof TableObject) && e.getOwnerMotorSystem().equals(ems) && pos.dist(e.getCenter()) < minDist)
				{
					nearestEntity = e;
				}

			}

		}



		return nearestEntity;
	}


	public void updateObjectsToForget(Robot robot)
	{
		synchronized (visualMemory) {
			Entity etmp = null;

			for(int i = 0; i < EntityPool.table.getNObjs(); i++)
			{
				etmp = EntityPool.table.getObj(i);

				//Else, if it's outside fov, but the robot had seen it before, then we make the robot forget its position by apply gaussian noise std=1 and mu = 0
				if( etmp.isInVM() )
				{

					if( etmp.notSeenTime() >= Config.TIME_TO_FORGET)
					{
						etmp.forget();
						//	System.out.println("TIME TO FORGET!");
					}


				}

			}
		}
	}
	
	public boolean isInsideFOV(double ang)
	{
		return (Math.abs(ang+10) <= Config.ROBOT_HALF_FOV) || (Math.abs(ang-10) <= Config.ROBOT_HALF_FOV);
	}

	public void updateVM(Robot robot)
	{

		synchronized (visualMemory) {


			PerceptualMotorSystem pms = robot.getPMS();

			Vector2 currentEyePos = pms.getCurrentEyePos();


			Vector2 vtmp = new Vector2();


			Vector2 y_axis = pms.transform(new Vector2(0,-100).add(pms.getEyePos())).sub(currentEyePos);




			Entity etmp = null;
			double ang = 0;
			for(int i = 0; i < EntityPool.table.getNObjs(); i++)
			{
				etmp = EntityPool.table.getObj(i);
				etmp.updateCenter();
				vtmp = etmp.getCenter().getCpy();
				vtmp.sub(currentEyePos);


				ang = Math.abs(vtmp.degreesAngle(y_axis));

				if( this.isInsideFOV(ang) )
				{

					etmp.drawBrighter = true;
				}
				else
				{
					//System.out.println(ang);
					etmp.drawBrighter = false;
				}

				//If the object is not already in the VM and if it's inside the FOV of the robot, then add it to the VM.
				if( !etmp.isInVM() &&  this.isInsideFOV(ang) )
				{




					this.visualMemory.add(etmp);


					if( etmp.getCenter().getX() <= EntityPool.table.getCenter().getX() )
					{
						etmp.setOwnerMotorSystem(EMotorSystem.LEFT);
					}
					else
					{
						etmp.setOwnerMotorSystem(EMotorSystem.RIGHT);
					}


					etmp.setInVM(true);


				}


				//If its inside the field of view, but was already seen, then we can update its particle filter.
				if( !etmp.freezePf && this.isInsideFOV(ang) && etmp.isInVM() /*&& !etmp.isPfUpdated()*/ )
				{

					etmp.getParticleFilter().currentNoise = Config.noiseOffset + ((ang*ang)/Config.ROBOT_HALF_FOV)*Config.ACUITY_NOISE;
					etmp.getParticleFilter().updateEstimates(etmp.getCenter().getCpy());
					etmp.setSeenTime();
					//etmp.setPfUpdated(true);
				}
				else if( etmp.isInVM() )
				{

					if( etmp.notSeenTime() >= Config.TIME_TO_FORGET && !etmp.freezePf)
					{
						etmp.forget();
						//	System.out.println("TIME TO FORGET!");
					}


				}

			}
		}
	}

	public void printIt()
	{
		synchronized (visualMemory) {

			for( Entity e : visualMemory )
			{
				System.out.print(e.toString() + ", ");
			}
			System.out.println();


		}
	}

	public void printMaximum()
	{
		synchronized (visualMemory) {

			Collections.sort(visualMemory,StaticUtils.entityValueComp);
			
			System.out.println("Maximum: " + this.visualMemory.get(0).toString());


		}
	}
}
