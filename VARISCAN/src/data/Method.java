package data;

import java.util.LinkedList;
import java.util.List;

public class Method 
{
	/** The function signature. */
	public String functionSignatureXml;
	
	/** The start position. */
	public int start;
	
	/** The feature locations. */
	public List<FeatureLocation> featureLocations;
	
	/**
	 * Method.
	 *
	 * @param signature the signature
	 * @param start the start
	 * @param end the end
	 */
	public Method(String signature, int start)
	{
		this.functionSignatureXml = signature;
		this.start = start;
		
		this.featureLocations = new LinkedList<FeatureLocation>();
	}	
	
	/**
	 * Adds the feature location if it is not already added
	 *
	 * @param loc the loc
	 */
	public void AddFeatureLocation(FeatureLocation loc)
	{
		if (!this.featureLocations.contains(loc))
			this.featureLocations.add(loc);
	}

	public int GetAnnotationCount()
	{
		return this.featureLocations.size();
	}
}
