package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * The Class Feature.
 */
public class Feature implements Comparable<Feature>{

	/** The name of the feature. */
	public String Name;
	
	/** The lines of feature code. */
	private int _lofc;
	
	/** The feature constants that are part of the feature*/
	public HashMap<UUID, FeatureConstant> constants;
	
	/** nesting Depth informations */
	public int minNestingDepth;
	public int maxNestingDepth;
	
	/** Granularity information*/
	public EnumGranularity maxGranularity = EnumGranularity.NOTDEFINED;
	public EnumGranularity minGranularity = EnumGranularity.NOTDEFINED;
	
	/* scattering information */
	public ArrayList<String> compilationFiles;
	
	/**
	 * Gets the lines of code.
	 *
	 * @return the lofc
	 */
	public int getLofc()
	{
		return this._lofc;
	}
	
	/**
	 * Gets all constants of the feature
	 *
	 * @return the locs
	 */
	public List<FeatureConstant> getConstants()
	{
		List<FeatureConstant> locs = new LinkedList(this.constants.values());
		return locs;
	}
	
	/**
	 * Instantiates a new feature.
	 *
	 * @param name the name
	 */
	public Feature(String name)
	{
		this.Name = name;
		this.constants = new HashMap<UUID, FeatureConstant>();
		this.compilationFiles = new ArrayList<String>();
		
		this.maxNestingDepth = -1;
		this.minNestingDepth = -1;
	}

	/**
	 * Adds the feature constant and increases lines of feature code.
	 *
	 * @param loc the loc
	 */
	public void AddFeatureLocation(FeatureConstant loc)
	{
		// connect constant with this feature (both directions)
		loc.corresponding = this;
		
		// set loc for the feature
		this.constants.put(loc.id, loc);
		this._lofc += loc.end - loc.start + 1;
		
		data.File file = FileCollection.GetFile((loc.filePath));
		for (int current : file.emptyLines)
			if (current > loc.start && current < loc.end)
				this._lofc--;
		
		// assign nesting depth
		if (this.minNestingDepth == -1)
			this.minNestingDepth = loc.nestingDepth;
		if (this.maxNestingDepth == -1)
			this.maxNestingDepth = loc.nestingDepth;
		
		if (this.maxNestingDepth < loc.nestingDepth)
			this.maxNestingDepth = loc.nestingDepth;
		if (this.minNestingDepth > loc.nestingDepth)
			this.minNestingDepth = loc.nestingDepth;
		
		// add cu if not already in the list
		if (!this.compilationFiles.contains(loc.filePath))
			this.compilationFiles.add(loc.filePath);
		
		// count loc in feature collection
		FeatureExpressionCollection.numberOfFeatureConstants++;
	}

	/**
	 * Gets the amount compilation files.
	 *
	 * @return the int
	 */
	public int GetAmountCompilationFiles()
	{
		return this.compilationFiles.size();
	}
	
	@Override
	public int compareTo(Feature o) {
		if (o.getLofc() > this.getLofc())
			return -1;
		else 
			return 1;
	}

	
}
