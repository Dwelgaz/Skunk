package output;

import java.util.ArrayList;
import java.util.HashMap;

import data.FeatureExpressionCollection;
import data.FeatureConstant;
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
	 * Adds the feature constant information to the attribute overview
	 *
	 * @param constant the loc
	 */
	public void AddFeatureLocationInfo(FeatureConstant constant)
	{
		// add metrics
		this.noFeatureLocs++;
		this.lofc = constant.end - constant.start;
		
		// add feature constant if not already part of it
		if (!this.featureConstants.contains(constant.corresponding.Name))
			this.featureConstants.add(constant.corresponding.Name);
		
		// add all lines per file to the data structure, that are part of the feature constant... no doubling for loac calculation
		if (!loacs.keySet().contains(constant.filePath))
			loacs.put(constant.filePath, new ArrayList<Integer>());
		
		for (int i = constant.start; i <= constant.end; i++)
		{
			if (!loacs.get(constant.filePath).contains(i))
				loacs.get(constant.filePath).add(i);
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
		float percentOfLocations = this.noFeatureLocs * 100 / FeatureExpressionCollection.numberOfFeatureConstants;
		float percentOfConstants = this.featureConstants.size() * 100 / FeatureExpressionCollection.GetFeatures().size();
		
		// Complete overview
		String res = ">>> Overview "+ Reason +"\r\n";
		res += "Number of features: \t" + this.featureConstants.size() + " (" + percentOfConstants + "% of " + FeatureExpressionCollection.GetFeatures().size() + " constants)\r\n";
		res += "Number of feature constants: \t" + this.noFeatureLocs  + " (" + percentOfLocations + "% of " + FeatureExpressionCollection.numberOfFeatureConstants + " locations)\r\n";
		res += "Lines of annotated Code: \t" + completeLoac + " (" + percentOfLoc + "% of " + FeatureExpressionCollection.GetLoc() + " LOC)\r\n";
		res += "Lines of feature code: \t\t" + this.lofc + "\r\n\r\n";
		
		return res;
	}
}
