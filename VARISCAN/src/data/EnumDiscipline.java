package data;

public enum EnumDiscipline {
	DISCIPLINED (1),
	NOTDEFINED (0),
	UNDISC_IF (-1),
	UNDISC_CASE (-2),
	UNDISC_ELSE_IF (-3),
	UNDISC_PARAM (-4),
	UNDISC_EXPRESSION (-5);
	
	private int value;
	
	private EnumDiscipline(int value)
	{
		this.value = value;
	}
	
	public int GetValue()
	{
		return this.value;
	}
	
}


/**Disciplined
FT: Annotations on one or multiple functions or type definitions (abgeschlossene functions usw.)
SF: Annotations on one or multiple statements inside a function or on fields inside a type definition (Statements komplett usw.)

Undisciplined
IF: Partial annotations of an if statement, e.g., an annotation of the if condition or the if-then branch without the corresponding else branch ().

CA: Annotations on a case statement in which only a case block is annotated (<case>).

EI: Annotations on an else-if branch inside an if-then-else cascade (<else>).

PA: Annotations on a parameter of a function declaration or a function call (<param>/<argument>).

EX: Annotations on well-formed parts of expressions (<expression>). */
