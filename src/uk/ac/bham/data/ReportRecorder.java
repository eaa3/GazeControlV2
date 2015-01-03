package uk.ac.bham.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import uk.ac.bham.control.Robot;
import uk.ac.bham.util.Config;
import uk.ac.bham.view.GCFrame;



public class ReportRecorder {


	public double nobjs_in, mean_objs_in;
	public double nobjs_total, mean_objs_total;
	public int n_trials;

	public String experiment;


	public ReportRecorder(String experiment)
	{
		this.nobjs_in = 0;
		this.nobjs_total = 0;
		
		this.n_trials = 1;

		this.experiment = experiment;

	}

	public void updateReport(int dobj_in, int dobj_total) throws IOException, InterruptedException
	{
		this.nobjs_in += dobj_in;
		this.nobjs_total += dobj_total;

		if( GCFrame.getTimeElapsed() >= Config.TRIAL_TIME )
		{
			this.mean_objs_in += this.nobjs_in;
			this.mean_objs_total += this.nobjs_total;
			save();
			//JOptionPane.showMessageDialog(null, "ObjsIn: " + this.nobjs_in);
			
			this.nobjs_in = 0;
			this.nobjs_total = 0;
			this.n_trials++;
			GCFrame.ti = System.currentTimeMillis();
			
			exit();
			
			
		}

		if( this.n_trials > Config.MAX_TRIALS )
		{

			

			FileWriter out = new FileWriter(Config.GAZE_MODEL.name()+"_"+Config.EXPERIMENT + "_" + (Config.ROBOT_HALF_FOV*2) + "_" + Config.GRSP_THR+".txt",true);

			out.write("\nMeanObjIn: "+(this.mean_objs_in/Config.MAX_TRIALS));
			out.write(" MeanTotal: "+ (this.mean_objs_total/Config.MAX_TRIALS) + "\n");


			out.close();
			
			this.mean_objs_in = 0;
			this.mean_objs_total = 0;
			this.n_trials = 1;
			
			if( Config.EXPERIMENT.equals("GRSP") ){
				Config.GRSP_THR += 1; 
				

				if( Config.REACH_THR > 5 )
				{
					exit();
				}
			}
			else 
			{
				Config.ROBOT_HALF_FOV += 15;
		
				if( Config.ROBOT_HALF_FOV > 60 )
				{
					exit();
				}
			}


		}
	}

	public void exit()
	{
		//Scanner in = new Scanner(System.in);


//		System.out.println("Simulation is over.");
	//	in.next();

		System.exit(0);
	}

	public void save() throws IOException
	{
		FileWriter out = new FileWriter(Config.GAZE_MODEL.name()+"_"+Config.EXPERIMENT + "_" + (Config.ROBOT_HALF_FOV*2) + "_" + Config.REACH_THR+".txt",true);

		out.write("ObjIn: "+this.nobjs_in);
		out.write(" Total: "+ this.nobjs_total + "\n");

		out.close();


	}
}
