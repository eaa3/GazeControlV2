package uk.ac.bham.control;

public class State2 {
	
	public ArmPos armP;
	public HandStatus handS;
	public TableStatus tableS;
	
	public State2 parentState;
	public EActions parentAction;
	public double value;
	
	public State2()
	{
		armP = ArmPos.OutTable;
		handS = HandStatus.Empty;
		tableS = TableStatus.Empty;
		parentAction = EActions.NOP;
		
		parentState = this;
	}
	
	
	public String toString()
	{
		String ret = parentAction.name()+ " -> ({"+armP.name()+", " + handS.name() +", " + tableS.name() + "}, " + this.value+")";
		if( parentState != null ) ret = "({"+parentState.armP.name()+", " + parentState.handS.name() +", " + parentState.tableS.name() + "}" + ", " + ret;
		else ret = "S0, " + ret;
		
		return ret;
	}
	
	
	
	public boolean equals(Object obj)
	{
		if( obj instanceof State2 )
		{
			State2 other = (State2) obj; 
			return armP == other.armP && handS == other.handS && tableS == other.tableS && parentState.armP == other.parentState.armP && parentState.handS == other.parentState.handS && parentState.tableS == other.parentState.tableS && parentAction == other.parentAction ;
		}
		else
		{
			return false;
		}
		
		
	}
	


}
