package uk.ac.bham.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Vector;

import uk.ac.bham.control.Action;
import uk.ac.bham.control.EActions;
import uk.ac.bham.control.EPercepAction;
import uk.ac.bham.control.Robot.EMotorSystem;
import uk.ac.bham.data.EntityPool;
import uk.ac.bham.entities.Container;
import uk.ac.bham.entities.Entity;
import uk.ac.bham.entities.MotorSystem;
import uk.ac.bham.entities.Particle;
import uk.ac.bham.entities.ParticleFilter;
import uk.ac.bham.entities.ParticleFilterOld;
import uk.ac.bham.entities.TableObject;
import uk.ac.bham.entities.VisualMemory;




public abstract class GazeControl {



	public static enum GazeModels {
		RUG,
		RU,
		UNC,

		N_MODELS
	}

	public static void setModelValues(ArrayList<Action> actions, Vector<Entity> vm, int arm, GazeModels model)
	{
		switch(model)
		{
		case RUG:

			setGainValues(actions, vm, arm);

			break;
		case RU:

			setPredValues(actions,vm,arm);

			break;
		case UNC:

			setUncValues(actions,vm,arm);

			break;
		default:
			break;
		}

		//printSelection(vm);
	}

	//Gaze control model functions:

	public static double cost(Entity e, Particle p, Vector2 center, ArrayList<Action> actions, int index)
	{
		/*
		 * i) succeed, if the offset between the centre of the hand and the location
					given by particle gj is less than or equal to some threshold (e.g. 1.0 cm); 
					or ii) fail, if the offset is greater than the threshold. 

					If an action succeeds, value(gj ; a) takes the Q-value of Qms(s; a) (from
					the learnt policy PIms), 

					--> I don't get this part
					if it fails, it takes the Q-value of the second best action and is punished by
					adding -1 units of reward
					--

					OBS: I assume that the hand position is the mean of the particle cloud, or will be there for a successful action

		 *
		 *		The cost(g, o) takes
					the value of Qms(s, o) (i.e. the Q-value taken from the policy PIms, where s is
					the current discrete state) if the offset between the centre of the hand and the
					particle g is less than or equal to some threshold, 

					--> Alternative implementation of the other article
					otherwise it takes the value of
					min(o belonging to Oms) Qms(s, o), indicating that the option failed.
					--
		 *
		 *
		 *
		 */
		double val = 0.0;

		if( (p.getPos().dist(center) < Config.ACTION_THR)   )
		{
			val = actions.get(index).getQValue();
		}
		else
		{
			val = actions.get(actions.size()-1).getQValue() - 1;
		}

		return val;
	}

	/** 1/J*Sum(cost(g_i,a)*weight_i) for all particles g_i in G  (Eq 2)**/
	public static double avgValue(Entity e, ParticleFilter pf, ArrayList<Action> actions, int index)
	{
		Vector2 center = e.getCenter();
		Vector<Particle> particles = pf.getParticles();

		double avg = 0.0;
		double sum = 0.0;
		for(Particle p : particles)
		{
			//sum += p.getWeight();

			avg += cost(e, p, center, actions, index);
		}
		//avg += Config.ZERO_THR;
		//avg /= (sum+Config.ZERO_THR);
		avg /= particles.size();




		return avg;

	}


	public static Action argMaxAction(Entity e, ParticleFilter pf, ArrayList<Action> actions)
	{
		Action max_action = null;
		double max_avg = Double.NEGATIVE_INFINITY;
		double tmp_avg = 0.0;

		for(int i = 0; i < actions.size(); i++)
		{
			tmp_avg = avgValue(e, pf, actions, i);

			if( tmp_avg > max_avg )
			{
				max_action = actions.get(i);
				max_avg = tmp_avg;
			}

		}

		max_action = max_action.getCpy();
		max_action.setValue(max_avg);


		return max_action;


	}

	//One step lookahead prediction
	public static double predValue(Entity e, ArrayList<Action> actions)
	{
		double v_pi = 0.0;
		double val = 0.0;

		ParticleFilter updatedPf = null;
		Vector2 mean = e.getParticleFilter().mean().getCpy();
		for(int o = 0; o < Config.N_IMAGINARY_OBS; o++ )
		{
			updatedPf = e.getParticleFilter().getCpy();

			//Imaginary observation
			updatedPf.currentNoise = 0;
			updatedPf.updateEstimates(mean);
			Action a = argMaxAction(e, updatedPf, actions);

			val = a.getValue();
			v_pi += val;

			updatedPf = null;
		}

		v_pi /= Config.N_IMAGINARY_OBS;


		return v_pi;
	}


	public static double uncValue(Entity e)
	{
		double unc_vi = 0.0;
		double unc_v = 0.0;
		double unc = 0.0;

		//ParticleFilter updatedPf = null;
		//Vector2 mean = e.getParticleFilter().mean().getCpy();

		unc_v = e.getParticleFilter().overallUnc();

		for(int i = 0; i < Config.N_OBS_SETS; i++)
		{
			e.getParticleFilter().initImaginaryPf();

			for(int o = 0; o < Config.N_IMAGINARY_OBS; o++ )
			{
				e.getParticleFilter().imaginaryUpdate();

				//Imaginary observation

				unc_vi = e.getParticleFilter().getImaginaryPf().overallUnc();

				//Unc value
				unc += Math.abs(unc_v - unc_vi);


			}
		}


		unc /= (Config.N_IMAGINARY_OBS*Config.N_OBS_SETS);

		return unc;
	}

	public static void printSelection(Vector<Entity> visualMemory)
	{
		System.out.println("Pick action");
		for(Entity e : visualMemory)
		{
			if( e instanceof TableObject ) System.out.printf("%.5f ", e.getValue());
		}
		System.out.println();

	}

	public static void setPredValues(ArrayList<Action> actions,Vector<Entity> visualMemory, int arm)
	{



		double v_pi = 0.0;
		for(Entity e : visualMemory)
		{
			if( e.getOwnerMotorSystem().ordinal() == arm )
			{
				v_pi = predValue(e,actions);
				e.setValue(v_pi);
			}

		}

		Collections.sort(visualMemory,StaticUtils.entityValueComp);


	}

	public static void setUncValues(ArrayList<Action> actions, Vector<Entity> visualMemory, int arm)
	{
		synchronized (visualMemory) {


			double unc = 0.0;
			for(Entity e : visualMemory)
			{
				if( e.getOwnerMotorSystem().ordinal() == arm )
				{
					unc = uncValue(e);
					e.setValue(unc);
				}

			}

			Collections.sort(visualMemory,StaticUtils.entityValueComp);

		}
	}

	public static void setGainValues(ArrayList<Action> actions, Vector<Entity> visualMemory, int arm)
	{
		synchronized (visualMemory) {
			double v_pi = 0.0;
			double val = 0.0;
			double gain = 0.0;
			//For each entity, we compute the corresponding gain
			for(Entity e : visualMemory)
			{
				if( e.getOwnerMotorSystem().ordinal() == arm )
				{


					val = argMaxAction(e, e.getParticleFilter(), actions).getValue();
					v_pi = predValue(e, actions);

					gain = v_pi - val;



					e.setValue(gain);

				}
			}


			//We order the visual memory in decreasing order according the the gain of each particle to make it easier to select the best one later
			Collections.sort(visualMemory,StaticUtils.entityValueComp);


		}
	}


	public static double cost(Entity e, Particle p, double valSucc, double valFail, boolean imaginary)
	{
		/*
		 * i) succeed, if the offset between the centre of the hand and the location
					given by particle gj is less than or equal to some threshold (e.g. 1.0 cm); 
					or ii) fail, if the offset is greater than the threshold. 

					If an action succeeds, value(gj ; a) takes the Q-value of Qms(s; a) (from
					the learnt policy PIms), 

					--> I don't get this part
					if it fails, it takes the Q-value of the second best action and is punished by
					adding -1 units of reward
					--

					OBS: I assume that the hand position is the mean of the particle cloud, or will be there for a successful action

		 *
		 *		The cost(g, o) takes
					the value of Qms(s, o) (i.e. the Q-value taken from the policy PIms, where s is
					the current discrete state) if the offset between the centre of the hand and the
					particle g is less than or equal to some threshold, 

					--> Alternative implementation of the other article
					otherwise it takes the value of
					min(o belonging to Oms) Qms(s, o), indicating that the option failed.
					--
		 *
		 *
		 *
		 */
		double val = 0.0;
		
		double distX = 0.0;
		double distY = 0.0;
		
		if( imaginary )
		{
			distX = Math.abs(p.getPos().getX() - e.getImaginaryPos().getX());
			distY = Math.abs(p.getPos().getY() - e.getImaginaryPos().getY());
		}
		else
		{
			distX = Math.abs(p.getPos().getX() - e.getEstPos().getX());
			distY = Math.abs(p.getPos().getY() - e.getEstPos().getY());
		}
		
		if( (distX <= Config.REACH_THR) && (distY <= Config.REACH_THR) )
		{
			val = valSucc;
		}
		else
		{
			val = valFail;
		}

		return val;
	}

	public static double avgValue(Entity e, double valSucc, double valFail)
	{
		Vector<Particle> particles = e.getParticleFilter().getParticles();

		double avg = 0.0;

		double weight = 1.0/particles.size();

		for(Particle p : particles)
		{

			avg += cost(e, p, valSucc, valFail,false)*weight;
		}


		return avg;

	}




	public static Entry<Entity,EActions> argMaxAction(VisualMemory vm, MotorSystem ms)
	{
		Entity bestObj = null;
		EActions bestAction = EActions.NOP;
		double maxVal = -100;
		double val = 0;
		double realVal = 0;

		synchronized (vm.visualMemory) {




			double valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.MV_TO_OBJECT);

			double valFail = EntityPool.robot.taskSpace.getMinQValue(EntityPool.robot.taskSpace.getActions(ms.getTaskState()));



			//Find obj with maximum MV_TO_OBJECT
			for(Entity obj : vm.visualMemory)
			{
				if( (obj instanceof TableObject) && obj.getOwnerMotorSystem().equals(ms.side) )
				{
					val = avgValue(obj, valSucc, valFail);

					if( val > maxVal )
					{
						maxVal = val;	
						realVal = valSucc;
						bestObj = obj;

					}
				}
			}
			bestAction = EActions.MV_TO_OBJECT;

			if( ms.selectedObj != null )
			{
				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.GRASP);
				val = avgValue(ms.selectedObj, valSucc, valFail);

				bestObj = ms.selectedObj;

				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestAction = EActions.GRASP;
				}
			}

			if( vm.getContainer(ms.side) != null )
			{
				Entity container = vm.getContainer(ms.side);
				
				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.MV_TO_CONTAINER);
				val = avgValue(vm.getContainer(ms.side), valSucc, valFail);

				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestAction = EActions.MV_TO_CONTAINER;
				}

				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.RELEASE);
				val = avgValue(container, valSucc, valFail);


				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestAction = EActions.RELEASE;
				}


			}

			/*for(EActions a : EActions.values())
			{
				if( !a.equals(EActions.MV_TO_OBJECT) && !a.equals(EActions.GRASP) && !a.equals(EActions.MV_TO_CONTAINER) && !a.equals(EActions.RELEASE)   )
				{
					val = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), a);
					valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), a);

					if( val > maxVal )
					{
						maxVal = val;	
						realVal = valSucc;

						bestAction = a;
					}
				}
			}*/


			ms.selectedObj = bestObj;
			ms.maxVal = maxVal;
			ms.realVal = realVal;

		}




		return new AbstractMap.SimpleEntry<Entity,EActions>(bestObj,bestAction);
	}

	public static Entry<Entity,EPercepAction> futureArgMaxAction(VisualMemory vm, MotorSystem ms)
	{
		Entity bestObj = null;
		EPercepAction bestAction = EPercepAction.SEARCH;
		double gain = 0;
		double maxVal = -100;
		double val = 0;
		double realVal = 0;

		synchronized (vm.visualMemory) {




			double valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.MV_TO_OBJECT);
			double valFail = EntityPool.robot.taskSpace.getMinQValue(EntityPool.robot.taskSpace.getActions(ms.getTaskState()));



			//Find obj with maximum MV_TO_OBJECT
			for(Entity obj : vm.visualMemory)
			{
				obj.setValue(0);

				if( obj instanceof TableObject && obj.getOwnerMotorSystem().equals(ms.side)  )
				{
					val = predValue(obj, valSucc, valFail);
				//	obj.setValue(val);
					if( val > maxVal )
					{
						maxVal = val;	
						realVal = valSucc;
						bestObj = obj;

					}
				}
			}
			bestAction = ms.side.equals(EMotorSystem.RIGHT)? EPercepAction.LOOK_AT_OBJECT_RIGHT : EPercepAction.LOOK_AT_OBJECT_LEFT;

			if( ms.selectedObj != null )
			{
				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.GRASP);
				val = predValue(ms.selectedObj, valSucc, valFail);

				//ms.selectedObj.setValue(val);

				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestObj = ms.selectedObj;
					bestAction = ms.side.equals(EMotorSystem.RIGHT)? EPercepAction.LOOK_AT_OBJECT_RIGHT : EPercepAction.LOOK_AT_OBJECT_LEFT;
				}
			}

			if( vm.getContainer(ms.side) != null )
			{
				Entity container = vm.getContainer(ms.side);



				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.MV_TO_CONTAINER);
				val = predValue(container, valSucc, valFail);
			//	container.setValue(val);
				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestObj = container;
					bestAction = ms.side.equals(EMotorSystem.RIGHT)? EPercepAction.LOOK_AT_CONTAINER_RIGHT : EPercepAction.LOOK_AT_CONTAINER_LEFT;
				}

				valSucc = EntityPool.robot.taskSpace.getQValue(ms.getTaskState(), EActions.RELEASE);
				val = predValue(container, valSucc, valFail);

			//	if( val > container.getValue() )
				//	container.setValue(val);

				if( val > maxVal )
				{
					maxVal = val;	
					realVal = valSucc;

					bestObj = container;
					bestAction = ms.side.equals(EMotorSystem.RIGHT)? EPercepAction.LOOK_AT_CONTAINER_RIGHT : EPercepAction.LOOK_AT_CONTAINER_LEFT;
				}


			}


			gain = maxVal - ms.maxVal;
			if( bestObj != null )
			{
				bestObj.setGain(gain);

				bestObj.setPredValue(maxVal);

			}


		}





		return new AbstractMap.SimpleEntry<Entity,EPercepAction>(bestObj,bestAction);
	}


	public static double futureAvgValue(Entity e, double valSucc, double valFail)
	{
		Vector<Particle> particles = e.getParticleFilter().getImaginaryPf().getParticles();

		double avg = 0.0;

		double weight = 1.0/particles.size();

		for(Particle p : particles)
		{

			avg += cost(e, p, valSucc, valFail,true)*weight;
		}


		return avg;
	}


	//One step lookahead prediction
	public static double predValue(Entity e, double valSucc, double valFail)
	{
		double v_pi = 0.0;
		double val = 0.0;

		for(int i = 0; i < Config.N_OBS_SETS; i++)
		{
			e.getParticleFilter().initImaginaryPf();
			for(int o = 0; o < Config.N_IMAGINARY_OBS; o++ )
			{
				e.getParticleFilter().imaginaryUpdate();


				val = futureAvgValue(e, valSucc, valFail);

				v_pi += val;
			}
		}


		v_pi /= (Config.N_IMAGINARY_OBS*Config.N_OBS_SETS);


		return v_pi;
	}










}
