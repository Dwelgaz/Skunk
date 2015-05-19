package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

public class File 
{
	/** the path to the file */
	public String filePath;
	
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
	
	/** The methods. */
	public List<Method> methods;
	
	/** The number feature constants in the method. */
	public int numberFeatureConstants;
	
	/** The number feature occurences.*/
	public int numberFeatureOccurences;
	
	/** The number of negations in the method */
	public int negationCount;
	
	/** The empty lines (whitespace or comments. */
	public List<Integer> emptyLines;
	
	
	/**
	 * Instantiates a new file.
	 *
	 * @param filePath the file path
	 * @param doc the doc
	 */
	public File(String filePath)
	{
		this.filePath = filePath;
		
		this.methods = new ArrayList<Method>();
		
		this.loc = 0;
		this.lofc = 0;
		this.nestingSum = 0;
		this.nestingDepthMax = 0;
		this.numberFeatureConstants = 0;
		this.numberFeatureOccurences = 0;
		this.negationCount = 0;
		
		this.featureLocations = new LinkedList<FeatureLocation>();
		this.loac = new ArrayList<Integer>();
		this.emptyLines = new ArrayList<Integer>();
		
		this.getEmptyLines(filePath);
	}
	
	/**
	 * Gets the empty lines and assign loc
	 *
	 * @return the empty lines
	 */
	private void getEmptyLines(String filePath)
	{
		try {
			int index = 0;
			boolean multiline = false;
			for (String line : FileUtils.readLines(FileUtils.getFile(filePath)))
			{
				if (multiline)
				{
					this.emptyLines.add(index);
					if (line.contains("*/"))
						multiline = false;
				}
				else if (line.isEmpty())
					this.emptyLines.add(index);
				
				//single line comment
				else if (line.trim().startsWith("//"))
					this.emptyLines.add(index);
				
				//multiline comment
				else if (line.trim().startsWith("/*"))
				{
					this.emptyLines.add(index);
					
					if (!line.contains("*/"))
						multiline = true;
				}
				
				index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
			
			// assign nesting depth values
			if (loc.nestingDepth > this.nestingDepthMax)
				this.nestingDepthMax = loc.nestingDepth;
			
			// calculate lines of feature code (if the feature is longer than the method, use the method end)
			this.lofc += loc.end - loc.start + 1;
			for (int current : this.emptyLines)
				if (current > loc.start && current < loc.end)
					this.lofc--;
			
			// add lines of visibile annotated code (amount of loc that is inside annotations) until end of feature location or end of method
			for (int current = loc.start; current <= loc.end; current++)
			{
				if (!(this.loac.contains(current)) && !(this.emptyLines.contains(current)))
					this.loac.add(current);
			}
		}
	}
	
	/**
	 * Connects a method to the file
	 *
	 * @param meth the meth
	 */
	public void AddMethod(Method meth)
	{
		if (!this.methods.contains(meth))
		{
			this.methods.add(meth);
			meth.filePath = this.filePath;
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
		
		// add each nesting to the nesting sum
		for (FeatureLocation loc : this.featureLocations)
			res += loc.nestingDepth;
		
		this.nestingSum = res;
	}
}
