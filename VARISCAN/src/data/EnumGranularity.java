package data;

public enum EnumGranularity {
	GLOBAL (7), 
	FUNCTION (6), 
	BLOCK (5), 
	STATEMENT (4), 
	EXPRESSION (3), 
	FUNCTIONSIGNATURE (2), 
	NOTDEFINED (1);

	private int value;
	
	private EnumGranularity(int value)
	{
		this.value = value;
	}
	
	public int GetValue()
	{
		return this.value;
	}
}


// GLOBAL == adding structure or function
// FUNCTION = adding function or type (adding an if-block or statement inside a function or a field to a structure)
// BLOCK == adding a block
// STATEMENT == varying type of a local variable
// EXPRESSION = changing expression 
// FUNCTIONSIGNATURE == adding a parameter to a function