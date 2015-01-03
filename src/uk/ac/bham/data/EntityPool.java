package uk.ac.bham.data;

import java.io.IOException;
import java.util.Vector;

import uk.ac.bham.control.Robot;
import uk.ac.bham.entities.Entity;
import uk.ac.bham.entities.Table;
import uk.ac.bham.entities.VisualEntity;
import uk.ac.bham.entities.VisualMemory;
import uk.ac.bham.util.Config;
import uk.ac.bham.view.Hud;

public abstract class EntityPool {
	
	public static Vector<VisualEntity> entities;
	
	public static Robot robot;
	public static Table table;
	
	public static VisualMemory vm;
	
	public static ReportRecorder rep = new ReportRecorder(Config.EXPERIMENT);
	
	public static Hud hud;
	
	
	public static void loadEntities() throws IOException
	{
	
		
		hud = new Hud();
		
		
		entities = new Vector<VisualEntity>();
		
		vm = new VisualMemory();
		
		robot = new Robot();
		table = new Table();
		table.init();
		
		entities.add(table);
		entities.add(robot);
		entities.add(hud);
		
		
		robot.startThreads();
		
		
		
	}

}
