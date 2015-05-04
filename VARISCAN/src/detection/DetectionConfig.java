package detection;

public class DetectionConfig {

	/** A ratio that defines the difference of a feature to the mean lofc of other features (1 = returns all features that are as big as the mean value and bigger)*/
	public double Feature_MeanLofcRatio = -1000;
	
	/** A ratio that defines the ratio between a feature lofc and the project loc (1 = 100% of the code)*/
	public double Feature_ProjectLocRatio = -1000;
	
	/** A ratio that defines the ratio between the amount of feature locations for a feature to the amount of feature locations in the project */
	public double Feature_NoflToSumRatio = -1000;
	
	/** The amount of compilation units (files) the feature is in */
	public double Feature_NumberOfCompilUnits = -1;
	
	/** A ratio that defines the percentage of lofc to loc (of a method) (1 = 100% of the code*/
	public double Method_LofcToLocRatio = -1000;
	
	/** A ratio that defines the percentage of loac to loc (of a method) (1 = 100% of the code) */
	public double Method_LoacToLocRatio = -1000;
	
	/** The amount of feature locations a method should have minimally*/
	public int Method_NumberOfFeatureLocs = -1;
	
	/** The amount of feature constants a method should have minimally */
	public int Method_NumberOfFeatureConstants = -1;
	
	/** The minimal nesting depth of a method. (summarized nestings) */
	public int Method_NestingSum = -1;
	
	/** The minimal nesting depth of a method.*/
	public int Method_NestingDepthMin = -1;
	
	/** The minimal negation count of a method */
	public int Method_NegationCount = -1;
	
	@Override public String toString()
	{
		String res = ">>> Code Smell Configuration \r\n";
		
		res += "[Feature-based Values]";
		
		if (this.Feature_MeanLofcRatio != -1000)
			res += "\r\nRatio - LOFC to mean LOFC: " + this.Feature_MeanLofcRatio;
		if (this.Feature_ProjectLocRatio != -1000)
			res += "\r\nRatio - LOFC to LOC: " + this.Feature_ProjectLocRatio;
		if (this.Feature_NoflToSumRatio != -1000)
			res += "\r\nRatio - Featurelocations(FL) to all FL: " + this.Feature_MeanLofcRatio;
		if (this.Feature_NumberOfCompilUnits != -1)
			res += "\r\nAmount - Number of compilation units: " + this.Feature_NumberOfCompilUnits;
		
		res += "\r\n\r\n[Method-based Values]";
		
		if (this.Method_LofcToLocRatio != -1000)
			res += "\r\nRatio - LOFC to LOC: " + this.Method_LofcToLocRatio;
		if (this.Method_LoacToLocRatio != -1000)
			res += "\r\nRatio - LOAC to LOC: " + this.Method_LoacToLocRatio;
		if (this.Method_NumberOfFeatureLocs != -1)
			res += "\r\nAmount - Number of featurelocations: " + this.Method_NumberOfFeatureLocs;
		if (this.Method_NumberOfFeatureConstants != -1)
			res += "\r\nAmount - Number of featureconstants: " + this.Method_NumberOfFeatureConstants;
		if (this.Method_NestingSum != -1)
			res += "\r\nAmount - Number of nestings: " + this.Method_NestingSum;
		if (this.Method_NestingDepthMin != -1)
			res += "\r\nAmount - Minimal nesting depth: " + this.Method_NestingDepthMin;
		if (this.Method_NegationCount != -1)
			res += "\r\nAmount - Number of negations: " + this.Method_NegationCount;
		
		return res;
	}
	
	
	
	// TODO featureoccurences Anzahl der Featureexpression (siehe Loac)
	// TODO Median für lofc
	// TODO Scoping
}
