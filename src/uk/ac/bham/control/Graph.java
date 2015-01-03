package uk.ac.bham.control;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import uk.ac.bham.util.Config;



public class Graph {

	public HashMap<State, ArrayList<Action>> graph;

	private boolean hasLearnt;


	public Graph()
	{
		this.graph = new HashMap<State, ArrayList<Action>>();
		//this.graph.
		this.hasLearnt = false;
	}

	public Graph(String filename) throws IOException
	{
		this();


		Scanner in = new Scanner(new File(filename));

		double matrix[][] = new double[16][7];

		for(int i = 0; i < matrix.length; i++)
		{
			for(int j = 0; j < matrix[i].length; j++)
			{
				matrix[i][j] = in.nextDouble();
				System.out.print( matrix[i][j] + " ");
			}
			System.out.println();
		}

		HashMap<State, Integer> statesList = new HashMap<State, Integer>();

		int index = 0;
		for (int a = 0; a < ArmPos.values().length; a++)
			for (int b = 0; b < HandStatus.values().length; b++)
				for (int c = 0; c < TableStatus.values().length; c++)
				{
					// Encode state in a string
					State s = new State(ArmPos.values()[a],HandStatus.values()[b],TableStatus.values()[c]);
					statesList.put(s, index);
					index++;
				}
		
		
		for(Entry<State,Integer> entry : statesList.entrySet())
		{
			ArrayList<Action> actions = new ArrayList<Action>();
			Action a = null;
			for(int i = 0; i < matrix[entry.getValue()].length; i++ )
			{
				a =  new Action(EActions.values()[i], matrix[entry.getValue()][i], null);
				a.setQValue(matrix[entry.getValue()][i]);
				actions.add( a );
				
			}
			//Collections.sort(actions);
			this.graph.put(entry.getKey(), actions);
		}
		
		this.writeReport();
		
		this.hasLearnt = true;




		//		Scanner in = new Scanner(new File(filename));
		//
		//
		//		int state, next_state, n_actions;
		//		double value;
		//		String action_str, state_str, next_state_str;
		//		Action action;
		//		while( in.hasNext() )
		//		{
		//			state_str = in.next();
		//			
		//			state = Integer.parseInt(state_str, 2);
		//			System.out.println(state_str + " = " + state);
		//			this.graph.put(state, new ArrayList<Action>());
		//
		//			n_actions = in.nextInt();
		//			for(int i = 0; i < n_actions; i++)
		//			{
		//				action_str = in.next();
		//				next_state = in.nextInt(2);
		//				value = in.nextDouble()*100000000000.0;
		//				action = new Action(action_str,value, next_state);
		//
		//				this.graph.get(state).add(action);
		//
		//			}
		//		}
		//
		//		in.close();
	}

	public void addAction(State state, Action action)
	{
		if( !this.graph.containsKey(state) )
		{
			this.graph.put(state, new ArrayList<Action>());
		}

		this.graph.get(state).add(action);


	}
	public void qLearning(int nEpisodes) throws IOException
	{

		int rindex = (int)(this.graph.entrySet().size()*Math.random());
		State state0 = null;

		for( Entry<State, ArrayList<Action>> entry :  this.graph.entrySet())
		{
			state0 = entry.getKey();
			rindex--;

			if( rindex <= 0 ) break;
		}



		State goalState = new State(ArmPos.OnCont, HandStatus.Empty, TableStatus.Empty);
		State currentState = state0;
		State nextState = null;
		Action selectedAction = null;

		double max_value = 0;

		double gama = Config.GAMA;
		double alpha = Config.ALPHA;

		System.out.println("Started training!");
		for(int i = 0; i < nEpisodes; i++)
		{
			Object states[] = (Object[])this.graph.keySet().toArray();
			state0 = (State)states[(int)(states.length*Math.random())];

			currentState = state0;

			while( !currentState.equals(goalState) )
			{
				ArrayList<Action> actions = this.graph.get(currentState);



				//Selects a random action from the current state
				selectedAction = actions.get((int)(actions.size()*Math.random()));

				//Consider going to the next state using the selected action
				nextState = selectedAction.getNextState();


				//System.out.println("CurrentState = " + currentState + " Action = (" + selectedAction.getAction().toString() + ", " + selectedAction.getQValue() + ")" + " nextState = " + nextState );

				//Get the maximum value of this next state based on all its possible actions
				max_value = getMaxQValue(this.graph.get(nextState));


				//Update q-value of this action
				selectedAction.setQValue( selectedAction.getQValue() + alpha*(selectedAction.getValue() + gama*max_value - selectedAction.getQValue()));

				//Go to next state
				currentState = nextState;
			}

			//System.out.println("GOAL STATE!!!");
		}

		this.hasLearnt = true;

		this.writeReport();

		//To make it easier for later action selection, we sort the action list of each state
		for( Entry<State, ArrayList<Action>> entry :  this.graph.entrySet())
		{
			Collections.sort(entry.getValue());
		}






	}


	public void writeReport2() throws IOException
	{
		FileWriter out = new FileWriter(new File("learning_report.txt"));


		int i = 1, j = 1;
		for( Entry<State, ArrayList<Action>> entry : this.graph.entrySet())
		{
			j = 1;
			out.write("g{"+i+"} = struct( \'state\', struct(\'armP\',\'" + entry.getKey().armP.name() + "\',\'handS\',\'"+entry.getKey().handS.name() + "\',\'tableS\',\'"+entry.getKey().tableS.name()+ "\'));"); // entry.getValue().size() + " ");
			out.write("\r\nedges{"+i+"} = {"); 
			for( Action action : entry.getValue())
			{
				if( j > 1 )
					out.write(", ");

				//String str = "a = struct( \'action\',\'" +action.getAction().toString()+"\',\'next_state\',"+" struct(\'armP\',\'" + action.getNextState().armP.name() + "\',\'handS\',\'"+action.getNextState().handS.name() + "\',\'tableS\',\'"+action.getNextState().tableS.name()+ "\'),\'value\',"+action.getQValue()+")");

				out.write("struct( \'action\',\'" +action.getAction().toString()+"\',\'next_state\',"+" struct(\'armP\',\'" + action.getNextState().armP.name() + "\',\'handS\',\'"+action.getNextState().handS.name() + "\',\'tableS\',\'"+action.getNextState().tableS.name()+ "\'),\'value\',"+action.getQValue()+")");
				//out.write( "("+action.getAction().toString() + ", " + action.getNextState().toString() + ", "+ action.getQValue() + ") ");
				j++;
			}
			out.write("};\r\n");

			i++;
		}




		out.close();
	}

	public void writeReport() throws IOException
	{
		FileWriter out = new FileWriter(new File("learning_report.txt"));


		int i = 1, j = 1;

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);

		for( Entry<State, ArrayList<Action>> entry : this.graph.entrySet())
		{
			j = 1;


			if( i == 1 )
			{
				out.write("STATE\t\t\t\t" );
				for(Action action : entry.getValue())
				{
					out.write("\t"+action.getAction().name());
				}
				out.write("\n");
			}

			out.write(entry.getKey().toString());
			//out.write(entry.getKey().armP.ordinal() +", "+entry.getKey().handS.ordinal()+", "+entry.getKey().tableS.ordinal());

			for( Action action : entry.getValue())
			{
				out.write(action.getAction().toString().replaceAll(".", " ") + nf.format(action.getQValue()));


				j++;
			}
			out.write("\n");

			i++;
		}




		out.close();
	}

	public double getMaxQValue(ArrayList<Action> actions)
	{
		//if(this.hasLearnt) return actions.get(0).getQValue();
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
	public double getMinQValue(ArrayList<Action> actions)
	{
		//if(this.hasLearnt) return actions.get(0).getQValue();
		double min = Double.POSITIVE_INFINITY;
		
		if( actions == null ) return -50000;

		for(int i = 0; i < actions.size(); i++)
		{
			if( min > actions.get(i).getQValue() )
			{
				min = actions.get(i).getQValue();
			}
		}


		return min;
	}

	public Action getNextAction(State state)
	{
		//System.out.println("State: " + state.toString());
		ArrayList<Action> actions = this.getActions(state);

		//System.out.println(actions);
		//System.out.println("Action: " + actions.get(0).getAction().name());

		if( actions == null ) return null;
		//if(this.hasLearnt) return actions.get(0);

		Action max_action = actions.get(0);
		for(int i = 0; i < actions.size(); i++)
		{
			if( max_action.getQValue() < actions.get(i).getQValue() )
			{
				max_action = actions.get(i);
			}
		}

		return max_action;
	}

	public double getQValue(State state, EActions action)
	{
		ArrayList<Action> actions = this.getActions(state);
		double qVal = -100;

		double maxQVal = -100;

		if( actions != null )
		{
			for(Action a : actions)
			{
				if( a.getAction().equals(action) )
				{
					qVal = a.getQValue();

					//It's a bug that I have which creates one more Release transition (this is a quickfix)
					if( qVal > maxQVal )
					{
						maxQVal = qVal;
					}
				}
			}
		}
		else
		{
			System.out.println("State: " + state.toString() + " has no actions!!!");
		}


		return maxQVal;
	}


	public ArrayList<Action> getActions(State state)
	{

		return this.graph.get(state);
	}

	public Action getAction(State state, int i)
	{
		return this.graph.get(state).get(i);
	}




}
