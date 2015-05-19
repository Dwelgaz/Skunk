package data;

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
	public int lofc;
	
	/** The amount of nestings in the method (1 per nesting) */
	public int nestingSum;
	
	/** The maximal nesting depth in the method */
	public int nestingDepthMax;
	
	/** The lines of visible annotated code. (amount of loc that is inside annotations)*/
	public ArrayList<Integer> loac;
	private int processedLoac;
	
	/** The feature locations. */
	public LinkedHashMap<UUID, String> featureLocations;
	
	/** The number feature constants in the method. */
	public int numberFeatureConstants;
	
	/** The number feature occurences.*/
	public int numberFeatureOccurences;
	
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
		
		this.featureLocations = new LinkedHashMap<UUID, String>();
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
		if (!this.featureLocations.containsKey(loc.id))
		{
			// connect feature to the method
			this.featureLocations.put(loc.id, loc.corresponding.Name);
			loc.inMethod = this;
			
			// assign nesting depth values
			if (loc.nestingDepth > this.nestingDepthMax)
				this.nestingDepthMax = loc.nestingDepth;
			
				
			// calculate lines of feature code (if the feature is longer than the method, use the method end)
			if (loc.end > this.end)
				this.lofc += this.end - loc.start + 1;
			else
				this.lofc += loc.end - loc.start + 1;
			
			data.File file = FileCollection.GetFile((loc.filePath));
			for (int current : file.emptyLines)
			{
				if (loc.end > this.end)
				{
					if (current > loc.start && current < this.end)
						this.lofc--;
				}
				else
					if (current > loc.start && current < loc.end)
						this.lofc--;		
			}
			
			// add lines of visibile annotated code (amount of loc that is inside annotations) until end of feature location or end of method
			for (int current = loc.start; current <= loc.end; current++)
			{
				if (!(this.loac.contains(current)) && !FileCollection.GetFile(this.filePath).emptyLines.contains(current))
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
		return this.processedLoac;
	}

	/**
	 * Gets the number of feature constants of the method
	 *
	 * @return the int
	 */
	public void SetNumberOfFeatureConstants()
	{
		ArrayList<String> constants = new ArrayList<String>();
		
		for (UUID id : featureLocations.keySet())
		{
			FeatureLocation loc = FeatureExpressionCollection.GetFeatureLocation(featureLocations.get(id), id);
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
		for (UUID id : featureLocations.keySet())
		{
			FeatureLocation loc = FeatureExpressionCollection.GetFeatureLocation(featureLocations.get(id), id);
			if (!noOcc.contains(loc.start))
				noOcc.add(loc.start);
		}
		
		this.processedLoac = this.loac.size();
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
		
		for (UUID id : featureLocations.keySet())
		{
			FeatureLocation loc = FeatureExpressionCollection.GetFeatureLocation(featureLocations.get(id), id);
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
		for (UUID id : featureLocations.keySet())
		{
			FeatureLocation loc = FeatureExpressionCollection.GetFeatureLocation(featureLocations.get(id), id);
			res += loc.nestingDepth;
			if (loc.nestingDepth < minNesting)
				minNesting = loc.nestingDepth;
		}
		
		// substract the complete minNesting depth (for each added location)
		res -= this.featureLocations.size() * minNesting;
		
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
