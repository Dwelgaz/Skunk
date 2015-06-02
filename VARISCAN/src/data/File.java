package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

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
	private int processedLoac;
	
	/** The feature constants. */
	public LinkedHashMap<UUID, String> featureConstants;
	
	/** The methods. */
	public List<Method> methods;
	
	/** The number feature constants in the method (non-duplicated). */
	public int numberFeatureConstantsNonDup;
	
	/** The number feature occurences.*/
	public int numberOfFeatureLocations;
	
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
		this.numberFeatureConstantsNonDup = 0;
		this.numberOfFeatureLocations = 0;
		this.negationCount = 0;
		
		this.featureConstants = new LinkedHashMap<UUID, String>();
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
				
				// TODO Gucken ob hier ein caller auf ne methode ist --> hashmap speichern
				
				
				
				
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
				else
					loc++;
				
				index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Adds the feature constant if it is not already added.
	 *
	 * @param constant the feature constant
	 */
	public void AddFeatureConstant(FeatureConstant constant)
	{
		if (!this.featureConstants.containsKey(constant.id))
		{
			// connect feature to the method
			this.featureConstants.put(constant.id, constant.corresponding.Name);
			
			// assign nesting depth values
			if (constant.nestingDepth > this.nestingDepthMax)
				this.nestingDepthMax = constant.nestingDepth;
			
			// calculate lines of feature code (if the feature is longer than the method, use the method end)
			this.lofc += constant.end - constant.start + 1;
			for (int current : this.emptyLines)
				if (current > constant.start && current < constant.end)
					this.lofc--;
			
			// add lines of visibile annotated code (amount of loc that is inside annotations) until end of feature constant or end of method
			for (int current = constant.start; current <= constant.end; current++)
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
	 * Gets the number of feature constants of the method
	 */
	public void SetNumberOfFeatureConstants()
	{
		ArrayList<String> constants = new ArrayList<String>();
		
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			if (!constants.contains(constant.corresponding.Name))
				constants.add(constant.corresponding.Name);
		}
		
		this.processedLoac = this.loac.size();
		this.numberFeatureConstantsNonDup = constants.size();
	}
	
	/**
	 * Gets the number of feature occurences. A feature occurence is a complete set of feature constants on one line.
	 *
	 * @return the number of feature occurences in the method
	 */
	public void SetNumberOfFeatureLocations()
	{
		ArrayList<Integer> noLoc = new ArrayList<Integer>();
		
		// remember the starting position of each feature constant, but do not add it twice
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			if (!noLoc.contains(constant.start))
				noLoc.add(constant.start);
		}
		
		this.numberOfFeatureLocations = noLoc.size();
	}
	

	/**
	 * Set the amount of negated annotations
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
		
		// add each nesting to the nesting sum
		for (UUID id : featureConstants.keySet())
		{
			FeatureConstant constant = FeatureExpressionCollection.GetFeatureConstant(featureConstants.get(id), id);
			res += constant.nestingDepth;
		}
		
		this.nestingSum = res;
	}
}
