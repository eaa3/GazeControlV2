package uk.ac.bham.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import uk.ac.bham.data.EntityPool;
import uk.ac.bham.entities.VisualEntity;
import uk.ac.bham.util.Config;
import uk.ac.bham.util.GazeControl;
import uk.ac.bham.util.Vector2;

public class Hud implements VisualEntity {
	
	public static int i = 0;
	public static double sumHeadFPS = 0;

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g.create();
		
		g2d.setColor(Color.BLACK);
		g2d.drawString("Experiment: " + Config.EXPERIMENT + " Gaze Model: " + Config.GAZE_MODEL.name(), 100, 50);
		g2d.drawString("OBS_NOISE: " + Config.OBS_NOISE + " ACTION_NOISE: " + Config.ACTION_NOISE + " REACH_THR: " + Config.REACH_THR + " FOV: " + Config.ROBOT_HALF_FOV*2 , 100, 65);
		g2d.drawString("ObjsIn: " + EntityPool.rep.nobjs_in + " Attempts: " + EntityPool.rep.nobjs_total + " Trial: " + EntityPool.rep.n_trials + " AvgObjIn: " + EntityPool.rep.mean_objs_in/Config.MAX_TRIALS + " AvgAttempts: " + EntityPool.rep.mean_objs_total/Config.MAX_TRIALS, 100, 80);
		g2d.drawString("Right Arm State: " + EntityPool.robot.getRightArm().getTaskState().toString() + " Previous Action: " + (EntityPool.robot.getRightArm().getTaskState().parentAction == null? null : EntityPool.robot.getRightArm().getTaskState().parentAction.name()) , 100, 95);
		g2d.drawString("Left Arm State: " + EntityPool.robot.getLeftArm().getTaskState().toString() + " Previous Action: " + (EntityPool.robot.getLeftArm().getTaskState().parentAction == null? null : EntityPool.robot.getLeftArm().getTaskState().parentAction.name()) , 100, 110);
		
		g2d.drawString("Simulation Time: "  + GCFrame.getTimeElapsed()/Config.SECOND, 20, 135);
		g2d.drawString("VisualMemSize: " + EntityPool.vm.visualMemory.size(), 20, 150);
		g2d.drawString("Trial time: " + Config.TRIAL_TIME, 20, 165);
		
		sumHeadFPS += EntityPool.robot.getPMS().last_fps;
		i++;
		g2d.drawString("HEAD FPS: " + (sumHeadFPS/i), 20, 435);
		g2d.drawString("RIGHT ARM FPS: " + EntityPool.robot.getRightArm().last_fps, 20, 450);
		g2d.drawString("LEFT ARM FPS: " + EntityPool.robot.getLeftArm().last_fps, 20, 465);
		
		
		if( EntityPool.robot.fixP!= null)g2d.drawString("Gaze allocated to: " + EntityPool.robot.fixP.getOwnerMotorSystem().name(), 20, 400);
		g2d.dispose();
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return Config.FRAME_W;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return Config.FRAME_H;
	}

	@Override
	public Vector2 getPos() {
		// TODO Auto-generated method stub
		return null;
	}

}
