package data;

import java.util.LinkedList;

import org.w3c.dom.Node;

/**
 * The Class FeatureLocation. Saves location information for each feature occurence
 */
public class FeatureLocation {
	
	/** The corresponding feature on this location. */
	public Feature corresponding;
	
	/** The file path. */
	public String filePath;
	
	/** The start position. */
	public int start;
	
	/** The end position. */
	public int end;
	
	/** The nesting depth. */
	public int nestingDepth;
	
	/** The not flag. */
	public Boolean notFlag;

	/** The list of features of combined feature locaions (i.e. Feature 1 && Feature 2. */
	public LinkedList<FeatureLocation> combinedWith;

	public EnumGranularity granularity;
	
	/**
	 * Instantiates a new feature location.
	 *
	 * @param filePath the file path
	 * @param start the start
	 * @param end the end
	 * @param nestingDepth the nesting depth
	 * @param notFlag the not flag
	 */
	public FeatureLocation(String filePath, int start, int end, int nestingDepth, Boolean notFlag)
	{
		this.filePath = filePath;
		
		this.start = start;
		this.end = end;
		
		this.nestingDepth = nestingDepth;
		
		this.notFlag = notFlag;
		
		this.combinedWith = new LinkedList<FeatureLocation>();
		
		this.granularity = EnumGranularity.NOTDEFINED;
	}
	
	/**
	 * Indicates if the location if the feature is combined with other features
	 *
	 * @return the boolean
	 */
	public Boolean CombinedLocation()
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
	public void setGranularity(Node node)
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
					System.out.println(node.getNodeName());
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
}
