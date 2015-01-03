package uk.ac.bham.control;




public class Action implements Comparable<Action> {
	
	private EActions action;
	private double value;
	private double q_value;
	private State nextState;
	
	

	
	
	
	public Action(String action_str, double value, State nextState)
	{
		if( action_str.equals(EActions.MV_TO_OBJECT.toString()) )
		{
			this.action = EActions.MV_TO_OBJECT;
		}
		else if( action_str.equals(EActions.MV_TO_CONTAINER.toString()) )
		{
			this.action = EActions.MV_TO_CONTAINER;
		}
		else if( action_str.equals(EActions.MV_TO_TABLE.toString()) )
		{
			this.action = EActions.MV_TO_TABLE;
		}
		else if( action_str.equals(EActions.GRASP.toString()) )
		{
			this.action = EActions.GRASP;
		}
		else if( action_str.equals(EActions.RELEASE.toString()) )
		{
			this.action = EActions.RELEASE;
		}		
		else
		{
			this.action = EActions.NOP;
		}
		
		this.value = value;
		this.nextState = nextState;
	}
	
	public Action(EActions action, double value, State nextState)
	{
		this.action = action;
		this.value = value;
		this.nextState = nextState;
	}
	
	public State getNextState()
	{
		return this.nextState;			
		
	}

	public EActions getAction() {
		return action;
	}

	public void setAction(EActions action) {
		this.action = action;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public void setQValue(double q_value)
	{
		this.q_value = q_value;
	}
	
	public double getQValue()
	{
		return this.q_value;
	}

	@Override
	public int compareTo(Action o) {
		
		int comp = 0;
		
		if( this.getQValue() > o.getQValue() ) comp = -1;
		if( this.getQValue() < o.getQValue() ) comp = 1;
		
		return comp;
	}
	
	
	public Action getCpy()
	{
		Action cpy = new Action(this.action,this.value,this.nextState);
		cpy.setQValue(this.q_value);
		
		return cpy;
	}
	
	
	

}
