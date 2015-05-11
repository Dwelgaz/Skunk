package output;

import java.util.ArrayList;
import java.util.HashMap;

import data.FeatureExpressionCollection;
import data.FeatureLocation;
import detection.EnumReason;

public class AttributeOverview {

	public EnumReason Reason = null;
	private ArrayList<String> featureConstants = null;
	private int noFeatureLocs = 0;
	private int lofc = 0;
	private HashMap<String, ArrayList<Integer>> loacs = null;
	
	/**
	 * Instantiates a new attribute overview.
	 *
	 * @param reason the reason
	 */
	public AttributeOverview(EnumReason reason)
	{
		this.Reason = reason;
		this.loacs = new HashMap<String, ArrayList<Integer>>();
		this.featureConstants = new ArrayList<String>();
	}
	
	/**
	 * Adds the feature location information to the attribute overview
	 *
	 * @param loc the loc
	 */
	public void AddFeatureLocationInfo(FeatureLocation loc)
	{
		// add metrics
		this.noFeatureLocs++;
		this.lofc = loc.end - loc.start;
		
		// add feature constant if not already part of it
		if (!this.featureConstants.contains(loc.corresponding.Name))
			this.featureConstants.add(loc.corresponding.Name);
		
		// add all lines per file to the data structure, that are part of the feature location... no doubling for loac calculation
		if (!loacs.keySet().contains(loc.filePath))
			loacs.put(loc.filePath, new ArrayList<Integer>());
		
		for (int i = loc.start; i <= loc.end; i++)
		{
			if (!loacs.get(loc.filePath).contains(i))
				loacs.get(loc.filePath).add(i);
		}
	}
	
	@Override public String toString()
	{
		// calculate max loac
		int completeLoac = 0;
		for (String file : loacs.keySet())
			completeLoac += loacs.get(file).size();
		
		// calculate percentages
		float percentOfLoc = completeLoac * 100 / FeatureExpressionCollection.GetLoc();
		float percentOfLocations = this.noFeatureLocs * 100 / FeatureExpressionCollection.amountOfFeatureLocs;
		float percentOfConstants = this.featureConstants.size() * 100 / FeatureExpressionCollection.GetFeatures().size();
		
		// Complete overview
		String res = ">>> Overview "+ Reason +"\r\n";
		res += "Number of feature constants: \t" + this.featureConstants.size() + " (" + percentOfConstants + "% of " + FeatureExpressionCollection.GetFeatures().size() + " constants)\r\n";
		res += "Number of feature locations: \t" + this.noFeatureLocs  + " (" + percentOfLocations + "% of " + FeatureExpressionCollection.amountOfFeatureLocs + " locations)\r\n";
		res += "Lines of annotated Code: \t" + completeLoac + " (" + percentOfLoc + "% of " + FeatureExpressionCollection.GetLoc() + " LOC)\r\n";
		res += "Lines of feature code: \t\t" + this.lofc + "\r\n\r\n";
		
		return res;
	}
}
