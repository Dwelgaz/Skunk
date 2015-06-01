package data;

import java.util.LinkedList;
import java.util.UUID;

import org.w3c.dom.Node;

/**
 * The Class FeatureConstant.
 */
public class FeatureConstant implements Comparable<FeatureConstant>{
	
	/** The corresponding feature on this location. */
	public Feature corresponding;
	
	/** The file path. */
	public String filePath;
	
	/** The id. */
	public UUID id;
	
	/** The start position. */
	public int start;
	
	/** The end position. */
	public int end;
	
	
	
	/** The nesting depth. */
	public int nestingDepth;
	
	/** The not flag. */
	public Boolean notFlag;

	/** The list of features of combined feature constants in a location (i.e. Feature 1 && Feature 2. */
	public LinkedList<UUID> combinedWith;

	
	
	public EnumGranularity granularity;
	public EnumDiscipline discipline;
	
	
	
	/** The method that contains the featureconstant (if inside a method); else = null.  */
	public Method inMethod;

	/**
	 * Instantiates a new featureconstant.
	 *
	 * @param filePath the file path
	 * @param start the start
	 * @param end the end
	 * @param nestingDepth the nesting depth
	 * @param notFlag the not flag
	 */
	public FeatureConstant(String filePath, int start, int end, int nestingDepth, Boolean notFlag)
	{
		this.filePath = filePath;
		this.id = java.util.UUID.randomUUID();
		
		
		this.start = start;
		this.end = end;
		
		this.nestingDepth = nestingDepth;
		
		this.notFlag = notFlag;
		
		this.combinedWith = new LinkedList<UUID>();
		
		this.granularity = EnumGranularity.NOTDEFINED;
		this.discipline = EnumDiscipline.NOTDEFINED;
	}
	
	/**
	 * Indicates if the location if the feature is combined with other features
	 *
	 * @return the boolean
	 */
	public Boolean CombinedConstant()
	{
		if (combinedWith.size() == 0)
			return false;
		else 
			return true;
	}
	
	/**
	 * Sets the granularity based on the current nodeName
	 *
	 * @param loc the loc
	 * @param nodeName the node name
	 */
	public void SetGranularity(Node node)
	{
		// decide the granularity of the node based on the nodeName
		EnumGranularity glValue = EnumGranularity.NOTDEFINED;
		switch (node.getNodeName())
		{	
			case "name":
				String parent = node.getParentNode().getNodeName();
				if (parent.equals("function"))
					glValue = EnumGranularity.FUNCTIONSIGNATURE;
				else if (parent.equals("expr"))
					glValue = EnumGranularity.EXPRESSION;
				else if (parent.equals("type"))
					glValue = EnumGranularity.STATEMENT;
				break;
			case "parameter_list":
				glValue = EnumGranularity.FUNCTIONSIGNATURE;
				break;
			case "param":
				glValue = EnumGranularity.FUNCTIONSIGNATURE;
				break;
			case "argument":
				glValue = EnumGranularity.EXPRESSION;
				break;
			case "argument_list":
				glValue = EnumGranularity.EXPRESSION;
				break;
			case "call":
				glValue = EnumGranularity.EXPRESSION;
				break;
			case "expr":
				glValue = EnumGranularity.EXPRESSION;
				break;
			case "empty_stmt":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "do":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "case":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "block":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "switch":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "return":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "expr_stmt":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "decl_stmt": 
				glValue = EnumGranularity.FUNCTION;
				break;
			case "if":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "else":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "while":
				glValue = EnumGranularity.FUNCTION;
				break;
			case "function":
				glValue = EnumGranularity.GLOBAL;
				break;
			case "typedef":
				glValue = EnumGranularity.GLOBAL;
				break;
			case "struct":
				glValue = EnumGranularity.GLOBAL;
				break;
			case "union":
				glValue = EnumGranularity.GLOBAL;
				break;
			case "function_decl":
				glValue = EnumGranularity.GLOBAL;
				break;
			default:
				if (!node.getNodeName().contains("cpp:"))
					//TODO System.out.println(node.getNodeName());
				break;
		}
		
		// only assign new granularity if value is higher than the current 
		if (this.granularity.GetValue() < glValue.GetValue())
			this.granularity = glValue;
		
		// reassign feature max/min granularity if necessary
		if (!(this.granularity == EnumGranularity.NOTDEFINED))
		{
			// first time initialize
			if (this.corresponding.minGranularity == EnumGranularity.NOTDEFINED)
				this.corresponding.minGranularity = this.granularity;
			if (this.corresponding.maxGranularity == EnumGranularity.NOTDEFINED)
				this.corresponding.maxGranularity = this.granularity;
			
			if (this.corresponding.maxGranularity.GetValue() < this.granularity.GetValue())
				this.corresponding.maxGranularity = this.granularity;
			if (this.corresponding.minGranularity.GetValue() > this.granularity.GetValue())
				this.corresponding.minGranularity = this.granularity;
		}

	}

	/**
	 * Sets the discipline of the feature constant based on node inside the feature (e.g, a FeatureLocation containing one case is undisciplined)
	 *
	 * @param node a node inside the feature constant
	 */
	public void SetDiscipline(Node node)
	{
		// if not notdefined or disciplined, check for undisciplined node annotations
		if (this.discipline.GetValue() < 0)
			return;
		
		// decide on the basis of the siblings of each annotation
		EnumDiscipline discValue = EnumDiscipline.NOTDEFINED;
		switch (node.getNodeName())
		{
			case "else":
				discValue = EnumDiscipline.UNDISC_ELSE_IF;
				break;
			case "case":
				discValue = EnumDiscipline.UNDISC_CASE;
				break;
			case "expr":
				discValue = EnumDiscipline.UNDISC_EXPRESSION;
				break;
			case "parameter_list":
				discValue = EnumDiscipline.UNDISC_PARAM;
				break;
			case "param":
				discValue = EnumDiscipline.UNDISC_PARAM;
				break;
			case "argument":
				discValue = EnumDiscipline.UNDISC_PARAM;
				break;
			case "argument_list":
				discValue = EnumDiscipline.UNDISC_PARAM;
				break;
			case "if":
				// check if the if node has a child node. If the previous sibling of the <else> child node is not the <then> node, it is undisciplined
				for (int current = 0; current < node.getChildNodes().getLength(); current++)
				{
					Node child = node.getChildNodes().item(current);
					if (child.getNodeName().equals("else"))
					{
						if (!node.getChildNodes().item(current - 1).equals("then"))
						{
							discValue = EnumDiscipline.UNDISC_IF;
							break;
						}
						else
						{
							discValue = EnumDiscipline.DISCIPLINED;
							break;
						}
					}
				}
				break;
			default:
				discValue = EnumDiscipline.DISCIPLINED;
				break;
		}
		
		this.discipline = discValue;
	}

	@Override
	public int compareTo(FeatureConstant arg0) 
	{
		if (this.start > arg0.start)
			return 1;
		else if (this.start < arg0.start)
			return -1;
		else
			return 0;
	}
}
