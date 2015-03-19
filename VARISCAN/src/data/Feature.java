package data;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Feature.
 */
public class Feature implements Comparable<Feature>{

	/** The name of the feature. */
	public String Name;
	
	/** The lines of feature code. */
	private int _lofc;
	
	/** The annotation locations*/
	private List<FeatureLocation> locs;
	
	/** nesting Depth informations */
	public int minNestingDepth;
	public int maxNestingDepth;
	
	/** Granularity information*/
	public EnumGranularity maxGranularity = EnumGranularity.NOTDEFINED;
	public EnumGranularity minGranularity = EnumGranularity.NOTDEFINED;
	
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
	 * Gets all locations of the feature
	 *
	 * @return the locs
	 */
	public List<FeatureLocation> getLocs()
	{
		return this.locs;
	}
	
	/**
	 * Instantiates a new feature.
	 *
	 * @param name the name
	 */
	public Feature(String name)
	{
		this.Name = name;
		this.locs = new ArrayList<FeatureLocation>();
		
		this.maxNestingDepth = -1;
		this.minNestingDepth = -1;
	}

	/**
	 * Adds the feature location and increases lines of feature code.
	 *
	 * @param loc the loc
	 */
	public void AddFeatureLocation(FeatureLocation loc)
	{
		// connect location with this feature (both directions)
		loc.corresponding = this;
		
		// set loc for the feature
		this.locs.add(loc);
		this._lofc += loc.end - loc.start;
		
		// assign nesting depth
		if (this.minNestingDepth == -1)
			this.minNestingDepth = loc.nestingDepth;
		if (this.maxNestingDepth == -1)
			this.maxNestingDepth = loc.nestingDepth;
		
		if (this.maxNestingDepth < loc.nestingDepth)
			this.maxNestingDepth = loc.nestingDepth;
		if (this.minNestingDepth > loc.nestingDepth)
			this.minNestingDepth = loc.nestingDepth;
		
		
		 //System.out.println(loc.filePath + "\t\t\t" + this.Name + "\t\t" + loc.notFlag);
	}

	@Override
	public int compareTo(Feature o) {
		if (o.getLofc() > this.getLofc())
			return -1;
		else 
			return 1;
	}

	
}
