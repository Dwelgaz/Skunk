package data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	public int lofc;
	
	/** The amount of nestings in the method (1 per nesting) */
	public int nestingSum;
	
	/** The maximal nesting depth in the method */
	public int nestingDepthMax;
	
	/** The lines of visible annotated code. (amount of loc that is inside annotations)*/
	public ArrayList<Integer> loac;
	
	/** The feature locations. */
	public List<FeatureLocation> featureLocations;
	
	/** The number feature constants in the method. */
	public int numberFeatureConstants;
	
	/** The number feature occurences.*/
	public int numberFeatureOccurences;
	
	/** The number of negations in the method */
	public int negationCount;
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
		
		this.featureLocations = new LinkedList<FeatureLocation>();
		this.loac = new ArrayList<Integer>();
		
		this.numberFeatureConstants = 0;
		this.numberFeatureOccurences = 0;
		this.negationCount = 0;
	}	
	
	/**
	 * Adds the feature location if it is not already added.
	 *
	 * @param loc the loc
	 */
	public void AddFeatureLocation(FeatureLocation loc)
	{
		if (!this.featureLocations.contains(loc))
		{
			// connect feature to the method
			this.featureLocations.add(loc);
			loc.inMethod = this;
			
			// assign nesting depth values
			if (loc.nestingDepth > this.nestingDepthMax)
				this.nestingDepthMax = loc.nestingDepth;
			
				
			// calculate lines of feature code (if the feature is longer than the method, use the method end)
			if (loc.end > this.end)
				this.lofc += this.end - loc.start;
			else
				this.lofc += loc.end - loc.start;
			
			// add lines of visibile annotated code (amount of loc that is inside annotations) until end of feature location or end of method
			for (int current = loc.start+1; current <= loc.end; current++)
			{
				if (!(this.loac.contains(current)))
					this.loac.add(current);
				
				if (current == this.end)
					break;
			}
		}
	}

	/**
	 * Gets the annotation count.
	 *
	 * @return the int
	 */
	public int GetFeatureLocationCount()
	{
		return this.featureLocations.size();
	}
	
	/**
	 * Gets the lines of annotated code.
	 *
	 * @return lines of visible annotated code (not counting doubles per feature,..)
	 */
	public int GetLinesOfAnnotatedCode()
	{
		return this.loac.size();
	}

	/**
	 * Gets the number of feature constants of the method
	 *
	 * @return the int
	 */
	public void SetNumberOfFeatureConstants()
	{
		ArrayList<String> constants = new ArrayList<String>();
		
		for (FeatureLocation loc : this.featureLocations)
		{
			if (!constants.contains(loc.corresponding.Name))
				constants.add(loc.corresponding.Name);
		}
		
		this.numberFeatureConstants = constants.size();
	}
	
	
	/**
	 * Gets the number of feature occurences. A feature occurence is a complete set of feature locations on one line.
	 *
	 * @return the number of feature occurences in the method
	 */
	public void SetNumberOfFeatureOccurences()
	{
		ArrayList<Integer> noOcc = new ArrayList<Integer>();
		
		// remember the starting position of each feature location, but do not add it twice
		for (FeatureLocation loc : this.featureLocations)
		{
			if (!noOcc.contains(loc.start))
				noOcc.add(loc.start);
		}
		
		this.numberFeatureOccurences = noOcc.size();
	}
	

	/**
	 * Cet the amount of negated annotations
	 *
	 * @return the amount of negated annotations
	 */
	public void SetNegationCount()
	{
		int result = 0;
		
		for (FeatureLocation loc : this.featureLocations)
		{
			if (loc.notFlag)
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
		for (FeatureLocation loc : this.featureLocations)
		{
			res += loc.nestingDepth;
			if (loc.nestingDepth < minNesting)
				minNesting = loc.nestingDepth;
		}
		
		// substract the complete minNesting depth (for each added location)
		res -= this.featureLocations.size() * minNesting;
		
		this.nestingSum = res;
	}
}
