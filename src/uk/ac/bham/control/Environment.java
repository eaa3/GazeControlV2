package uk.ac.bham.control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class Environment {

	public static int NObjs = 1;


	public static State2 mvt(State2 s)
	{
		State2 nextS = nop(s);

		if( s.armP == ArmPos.OutTable )
		{
			
			nextS.armP = ArmPos.OnTable;			
		}	
		
		nextS.parentState = s;
		nextS.parentAction = EActions.MV_TO_TABLE;
		return nextS;
	}

	public static State2 mvc(State2 s)
	{
		State2 nextS = nop(s);

		if( s.armP != ArmPos.OnCont )
		{
			nextS.armP = ArmPos.OnCont;
		}		
		
		nextS.parentState = s;
		nextS.parentAction = EActions.MV_TO_CONTAINER;
		return nextS;
	}

	public static State2 mvo(State2 s)
	{
		State2 nextS = nop(s);

		if( s.armP != ArmPos.OnObj  && s.tableS == TableStatus.ObjsOn )
		{
			nextS.armP = ArmPos.OnObj;
		}
		
		nextS.parentState = s;
		nextS.parentAction = EActions.MV_TO_OBJECT;
		return nextS;
	}


	public static State2 grsp(State2 s)
	{
		State2 nextS = nop(s);

		if( s.handS == HandStatus.Empty && s.armP == ArmPos.OnObj )
		{
			nextS.handS = HandStatus.Grasping;
		}	
		
		nextS.parentState = s;
		nextS.parentAction = EActions.GRASP;
		return nextS;
	}


	public static State2 rls(State2 s)
	{
		State2 nextS = nop(s);

		if( s.handS == HandStatus.Grasping )
		{
			nextS.handS = HandStatus.Empty;

			if( s.armP == ArmPos.OnCont )
			{
				NObjs--;
				
				if( NObjs == 0 )
				{
					nextS.value = 1000;
					
					nextS.tableS = TableStatus.Empty;
				}
			}
		}
		
		nextS.parentState = s;
		nextS.parentAction = EActions.RELEASE;
		return nextS;
	}

	public static State2 nop(State2 s)
	{

		State2 nextS = new State2();


		nextS.armP = s.armP;
		nextS.handS = s.handS;
		nextS.tableS = s.tableS;
		
		nextS.parentAction = EActions.NOP;
		return nextS;
	}



	public static ArrayList<State2> GenerateStateSpace(State2 s0)
	{
		ArrayList<State2> stateSpace = new ArrayList<State2>();
		LinkedList<State2> queue = new LinkedList<State2>();

		queue.add(s0);
		State2 s = null;
		State2 newS = null;
		while( !queue.isEmpty() )
		{
			s = queue.removeFirst();
			
			newS = nop(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}

			newS = mvt(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}

			newS = mvc(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}
			

			newS = mvo(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}
			

			newS = grsp(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}
			

			newS = rls(s);

			if( !stateSpace.contains(newS) )
			{
				stateSpace.add(newS);
				queue.add(newS);
			}
			

			

		}
		
		return stateSpace;
	}
	
	public static Graph createStateEnvironmentGraph2() throws IOException
	{
		return new Graph("assets/qtable.txt");
	}
	
	public static Graph createStateEnvironmentGraph() throws IOException
	{
		State2 s0 = new State2();
		s0.tableS = TableStatus.ObjsOn;
		
		System.out.println(ArmPos.OnCont.ordinal());
		ArrayList<State2> ss = GenerateStateSpace(s0);
		
		Graph g = new Graph();
		State s = null;
		Action a = null;
		for(int i = 0; i < ss.size(); i++)
		{
			s = new State();
			s.armP = ss.get(i).armP;
			s.handS = ss.get(i).handS;
			s.tableS = ss.get(i).tableS;
			s.parentAction = ss.get(i).parentAction;
			s.value = ss.get(i).value;
			System.out.println(s.value);
			
			s.hash = ss.get(i).hashCode();
			
			s.parentState = new State(ss.get(i).parentState.armP,ss.get(i).parentState.handS,ss.get(i).parentState.tableS);
			a = new Action(s.parentAction,ss.get(i).value,s);
			//a.getValue()
			
			g.addAction(s.parentState, a);
			
			System.out.println(ss.get(i).toString());
			
			
		}
		
		g.qLearning(2000);
		//g.writeReport();
		
		return g;
	}
	




}
