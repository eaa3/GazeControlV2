package uk.ac.bham.control;


public class ActionOld implements Comparable<ActionOld> {
	
	private RobotActions action;
	private double value;
	private double q_value;
	private int nextState;
	
	
	public static enum RobotActions {
		NOP,
		MV_TO_OBJECT,
		MV_TO_CONTAINER,
		GRASP,
		RELEASE,
		
		NUM_STATES
		
	}
	
	
	
	
	public ActionOld(String action_str, double value, int nextState)
	{
		if( action_str.equals(RobotActions.MV_TO_OBJECT.toString()) )
		{
			this.action = RobotActions.MV_TO_OBJECT;
		}
		else if( action_str.equals(RobotActions.MV_TO_CONTAINER.toString()) )
		{
			this.action = RobotActions.MV_TO_CONTAINER;
		}
		else if( action_str.equals(RobotActions.GRASP.toString()) )
		{
			this.action = RobotActions.GRASP;
		}
		else if( action_str.equals(RobotActions.RELEASE.toString()) )
		{
			this.action = RobotActions.RELEASE;
		}
		else
		{
			this.action = RobotActions.NOP;
		}
		
		this.value = value;
		this.nextState = nextState;
	}
	
	public ActionOld(RobotActions action, double value, int nextState)
	{
		this.action = action;
		this.value = value;
		this.nextState = nextState;
	}
	
	public int getNextState()
	{
		return this.nextState;			
		
	}

	public RobotActions getAction() {
		return action;
	}

	public void setAction(RobotActions action) {
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
	public int compareTo(ActionOld o) {
		
		int comp = 0;
		
		if( this.getQValue() > o.getQValue() ) comp = -1;
		if( this.getQValue() < o.getQValue() ) comp = 1;
		
		return comp;
	}
	
	
	public ActionOld getCpy()
	{
		ActionOld cpy = new ActionOld(this.action,this.value,this.nextState);
		cpy.setQValue(this.q_value);
		
		return cpy;
	}
	
	
	

}
