package uk.ac.bham.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import uk.ac.bham.util.Config;


public class GraphOld {

	private HashMap<Integer, ArrayList<ActionOld>> graph;
	
	private boolean hasLearnt;
	

	public GraphOld()
	{
		this.graph = new HashMap<Integer, ArrayList<ActionOld>>();
		this.hasLearnt = false;
	}

	public GraphOld(String filename) throws FileNotFoundException
	{
		this();

		Scanner in = new Scanner(new File(filename));


		int state, next_state, n_actions;
		double value;
		String action_str, state_str, next_state_str;
		ActionOld action;
		while( in.hasNext() )
		{
			state_str = in.next();
			
			state = Integer.parseInt(state_str, 2);
			System.out.println(state_str + " = " + state);
			this.graph.put(state, new ArrayList<ActionOld>());

			n_actions = in.nextInt();
			for(int i = 0; i < n_actions; i++)
			{
				action_str = in.next();
				next_state = in.nextInt(2);
				value = in.nextDouble();
				action = new ActionOld(action_str,value, next_state);

				this.graph.get(state).add(action);

			}
		}

		in.close();
	}


	public void qLearning(int nEpisodes) throws IOException
	{
		
		int rindex = (int)(this.graph.entrySet().size()*Math.random());
		int state0 = 0;
		
		for( Entry<Integer, ArrayList<ActionOld>> entry :  this.graph.entrySet())
		{
			state0 = entry.getKey();
			rindex--;
			
			if( rindex <= 0 ) break;
		}
		
		

		int goalState = 15;
		int currentState = state0;
		int nextState = 0;
		ActionOld selectedAction = null;

		double max_value = 0;

		double gama = Config.ALPHA;
		double alpha = Config.GAMA;

		System.out.println("Starting training!");
		for(int i = 0; i < nEpisodes; i++)
		{
			Object states[] = (Object[])this.graph.keySet().toArray();
			state0 = (Integer)states[(int)(states.length*Math.random())];
			
			currentState = state0;
			
			while( currentState != goalState )
			{
				ArrayList<ActionOld> actions = this.graph.get(currentState);

				//Selects a random action from the current state
				selectedAction = actions.get((int)(actions.size()*Math.random()));

				//Consider going to the next state using the selected action
				nextState = selectedAction.getNextState();
				
				
				System.out.println("CurrentState = " + currentState + " Action = (" + selectedAction.getAction().toString() + ", " + selectedAction.getQValue() + ")" + " nextState = " + nextState );

				//Get the maximum value of this next state based on all its possible actions
				max_value = getMaxQValue(this.graph.get(nextState));
				
				
				//Update q-value of this action
				selectedAction.setQValue( selectedAction.getQValue() + alpha*(selectedAction.getValue() + gama*max_value - selectedAction.getQValue()));
				
				//Go to next state
				currentState = nextState;
			}			
		}
		
		this.hasLearnt = true;
		
		//To make it easier for later action selection, we sort the action list of each state
		for( Entry<Integer, ArrayList<ActionOld>> entry :  this.graph.entrySet())
		{
			Collections.sort(entry.getValue());
		}
		
		this.writeReport();
		
		


	}
	
	
	public void writeReport() throws IOException
	{
		FileWriter out = new FileWriter(new File("learning_report.txt"));

		
		
		for( Entry<Integer, ArrayList<ActionOld>> entry : this.graph.entrySet())
		{
			out.write(Integer.toString(entry.getKey(), 2) + " " + entry.getValue().size() + " ");
			for( ActionOld action : entry.getValue())
			{
				out.write( action.getAction().toString() + " " + action.getQValue() + " ");
			}
			out.write("\r\n");
		}
		
		out.close();
	}

	public double getMaxQValue(ArrayList<ActionOld> actions)
	{
		if(this.hasLearnt) return actions.get(0).getQValue();
		double max = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < actions.size(); i++)
		{
			if( max < actions.get(i).getQValue() )
			{
				max = actions.get(i).getQValue();
			}
		}
		

		return max;
	}
	
	public ActionOld getNextAction(int state)
	{
		ArrayList<ActionOld> actions = this.graph.get(state);
		
		if(this.hasLearnt) return actions.get(0);
		
		ActionOld max_action = actions.get(0);
		for(int i = 0; i < actions.size(); i++)
		{
			if( max_action.getQValue() < actions.get(i).getQValue() )
			{
				max_action = actions.get(i);
			}
		}

		return max_action;
	}
	
	
	public ArrayList<ActionOld> getActions(int state)
	{
		return this.graph.get(state);
	}
	
	public ActionOld getAction(int state, int i)
	{
		return this.graph.get(state).get(i);
	}
	



}
