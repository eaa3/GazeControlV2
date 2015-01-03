package uk.ac.bham.control;

public class State {
	
	public ArmPos armP;
	public HandStatus handS;
	public TableStatus tableS;
	
	public State parentState;
	public EActions parentAction;
	public double value;
	
	public int hash;

	
	public int hashCode()
	{
		int hash = 7;
		hash = 29*hash + this.armP.hashCode();
		hash = 29*hash + this.handS.hashCode();
		hash = 29*hash + this.tableS.hashCode();
		return hash;
	}
	
	public State()
	{
		armP = ArmPos.OutTable;
		handS = HandStatus.Empty;
		tableS = TableStatus.ObjsOn;
		parentAction = EActions.NOP;
		
		parentState = this;
	}
	
	public State(ArmPos armP, HandStatus handS, TableStatus tableS)
	{
		this.armP = armP;
		this.handS = handS;
		this.tableS = tableS;
		//parentAction = EActions.NOP;
		
		//parentState = this;
	}
	
	
	public String toString()
	{
		String ret = "{"+armP.name()+", " + handS.name() +", " + tableS.name() + "}";//parentAction.name()+ " -> ({"+armP.name()+", " + handS.name() +", " + tableS.name() + "}, " + this.value+")";
		//if( parentState != null ) ret = "({"+parentState.armP.name()+", " + parentState.handS.name() +", " + parentState.tableS.name() + "}" + ", " + ret;
		//else ret = "S0, " + ret;
		
		return ret;
	}
	
	
	
	public boolean equals(Object obj)
	{
		if( obj instanceof State )
		{
			State other = (State) obj; 
			return armP == other.armP && handS == other.handS && tableS == other.tableS;
		}
		else
		{
			return false;
		}
		
		
	}
	


}
