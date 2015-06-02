package data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * The Class Method.
 */
public class Method 
{
	/** The function signature. */
	public String functionSignatureXml;
	
	/** The start position. */
	public int start;
	
	/** The end position. */
	public int end;
	
	/** The lines of code of the method. */
	public int loc;
	
	/** The lines of feature code inside the method. */
	public long lofc;
	
	/** The amount of nestings in the method (1 per nesting) */
	public int nestingSum;
	
	/** The maximal nesting depth in the method */
	public int nestingDepthMax;
	
	/** The lines of visible annotated code. (amount of loc that is inside annotations)*/
	public ArrayList<Integer> loac;
	private int processedLoac;
	
	/** The feature constants. */
	public LinkedHashMap<UUID, String> featureConstants;
	
	/** The number feature constants in the method (non-duplicated). */
	public int numberFeatureConstantsNonDup;
	
	/** The number feature locations.*/
	public int numberFeatureLocations;
	
	/** The number of negations in the method */
	public int negationCount;
	
	/** The file path. */
	public String filePath;
	
	/**
	 * Method.
	 *
	 * @param signature the signature
	 * @param start the start position of the method
	 * @param loc the lines of code
	 */
	public Method(String signature, int start, int loc)
	{
		this.functionSignatureXml = signature;
		this.start = start;
		this.loc = loc;
		this.nestingSum = 0;
		this.nestingDepthMax = 0;
		
		// do not count start line while calculating the end
		this.end = start + loc - 1;
		
		// initialize loc
		this.lofc = 0;
		
		this.featureConstants = new LinkedHashMap<UUID, String>();
		this.loac = new ArrayList<Integer>();
		
		this.numberFeatureConstantsNonDup = 0;
		this.numberFeatureLocations = 0;
		this.negationCount = 0;
	}	
	
	/**
	 * Adds the feature location if it is not already added.
	 *
	 * @param constant the loc
	 */
	public void AddFeatureConstant(FeatureConstant constant)
	{
		if (!this.featureConstants.containsKey(constant.id))
		{
			// connect feature to the method
			this.featureConstants.put(constant.id, constant.corresponding.Name);
			constant.inMethod = this;
			
			// assign nesting depth values
			if (constant.nestingDepth > this.nestingDepthMax)
				this.nestingDepthMax = constant.nestingDepth;
			
				
			// calculate lines of feature code (if the feature is longer than the method, use the method end)
			if (constant.end > this.end)
				this.lofc += this.end - constant.start + 1;
			else
				this.lofc += constant.end - constant.start + 1;
			
			data.File file = FileCollection.GetFile((constant.filePath));
			for (int current : file.emptyLines)
			{
				if (constant.end > this.end)
				{
					if (current > constant.start && current < this.end)
						this.lofc--;
				}
				else
					if (current > constant.start && current < constant.end)
						this.lofc--;		
			}
			
			// add lines of visibile annotated code (amount of loc that is inside annotations) until end of feature constant or end of method
			for (int current = constant.start; current <= constant.end; current++)
			{
				if (!(this.loac.contains(current)) && !FileCollection.GetFile(this.filePath).emptyLines.contains(current))
					this.loac.add(current);
				
				if (current == this.end)
					break;
			}
		}
	}

	/**
	 * Gets amount of feature constants (duplicated) 
	 *
	 * @return the int
	 */
	public int GetFeatureConstantCount()
	{
		return this.featureConstants.size();
	}
	
	/**
	 * Gets the lines of annotated code.
	 *
	 * @return lines of visible annotated code (not counting doubles per feature,..)
	 */
	public int GetLinesOfAnnotatedCode()
	{
		return this.processedLoac;
	}

	/**
	 * Gets the number of feature constants of the method (non-duplicated)
	 *
	 * @return the int
	 */
	public void SetNumberOfFeatureConstantsNonDup()
	{
		ArrayList<String> constants = new ArrayList<String>();
		
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			if (!constants.contains(constant.corresponding.Name))
				constants.add(constant.corresponding.Name);
		}
		
		this.numberFeatureConstantsNonDup = constants.size();
	}
	
	
	/**
	 * Gets the number of feature locations. A feature location is a complete set of feature constants on one line.
	 *
	 * @return the number of feature occurences in the method
	 */
	public void SetNumberOfFeatureLocations()
	{
		ArrayList<Integer> noLocs = new ArrayList<Integer>();
		
		// remember the starting position of each feature location, but do not add it twice
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			if (!noLocs.contains(constant.start))
				noLocs.add(constant.start);
		}
		
		this.processedLoac = this.loac.size();
		this.numberFeatureLocations = noLocs.size();
	}
	

	/**
	 * Cet the amount of negated annotations
	 *
	 * @return the amount of negated annotations
	 */
	public void SetNegationCount()
	{
		int result = 0;
		
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			if (constant.notFlag)
				result++;
		}
		
		this.negationCount = result;
	}
	
	/**
	 * Sets the nesting sum.
	 */
	public void SetNestingSum()
	{	
		// minNesting defines the lowest nesting depth of the method (nesting depths are file based)
		int res = 0;
		int minNesting = 5000;
		
		// add each nesting to the nesting sum
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			res += constant.nestingDepth;
			if (constant.nestingDepth < minNesting)
				minNesting = constant.nestingDepth;
		}
		
		// substract the complete minNesting depth (for each added location)
		res -= this.featureConstants.size() * minNesting;
		
		this.nestingSum = res;
	}
	
	public void SetLoc()
	{
		data.File file = FileCollection.GetFile(this.filePath);
		
		for (int empty : file.emptyLines)
		{
			if (empty >= this.start && empty <= this.end)
				this.loc--;
		}
	}
}
