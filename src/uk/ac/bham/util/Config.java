package uk.ac.bham.util;

import java.awt.Color;
import java.util.Random;

import uk.ac.bham.util.GazeControl.GazeModels;

public abstract class Config {
	
	/* Graphical Configs */
	public static final int FRAME_W = 640;
	public static final int FRAME_H = 480;
	public static final int CENTER_DIM = 5;
	
	public static final int OBJ_DIM = 40;
	
	public static final Color CENTER_COLOR = Color.GREEN;
	
	
	
	
	/* Animation Configs */
	
	public static final double interpolation_step_arm = 0.05;
	public static final double interpolation_step_head = 0.1;
	public static  int MAX_FPS = 60;
	public static int MAX_FPS_ARM = 20;
	public static int MAX_FPS_HEAD = 60;
	public static final long SECOND = 1000;
	public static long TIME_PER_FRAME = SECOND/MAX_FPS;
	public static long TIME_PER_FRAME_HEAD = SECOND/MAX_FPS_HEAD;
	public static long TIME_PER_FRAME_ARM = SECOND/MAX_FPS_ARM;
	
	/* Learning Configs */
	
	public static final double ALPHA = 0.2f;
	public static final double GAMA = 0.8f;
	public static final int N_EPISODES = 1000;
	
	/* Simulation Configs */
	
	public static final int MAX_TABLE_OBJS = 8;
	public static final Random rand = new Random();
	public static final double INITIAL_VAR = 5;
	
	public static final double ACTION_NOISE = 3;
	public static final double OBS_NOISE = 5;
	public static final double FORGET_NOISE = 4;
	public static final double ACUITY_NOISE = 10;
	
	public static final long TIME_TO_FORGET = 1000;
	
	public static final int N_PARTICLES = 133;
	public static final int N_IMAGINARY_OBS = 3;
	public static final int N_OBS_SETS = 5;
	
	public static final double ZERO_THR = 0.0000001;
	
	/* Experiment Configs */
	public static int TRIAL_TIME = 60000;//300000; // 5 minutes = 5*60*1000 (ms)
	public static final int MAX_TRIALS = 5;
	public static final int TRIAL_PER_FOV = 5;
	public static final int TRIAL_PER_GRASP_THR = 5;
	
	public static String EXPERIMENT = "GRSP"; //GRSP OR FOV
	
	
	public static GazeModels GAZE_MODEL = GazeModels.RUG;
	
	/*Robot Configs*/
	public static final double ROBOT_RADIUS = 200;
	public static double GRSP_THR = 2;
	public static double REACH_THR = 5;
	public static double ACTION_THR = 2;
	public static double HAND_THR = 20;
	public static double ROBOT_HALF_FOV = 30;//22.5;
	
	public static double noiseOffset = 10;
	

}
