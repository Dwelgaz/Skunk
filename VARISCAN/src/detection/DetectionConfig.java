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
	
	/** The minimal nesting depth of a method. (maximal nestingDepth*/
	public int Method_NestingDepthMax = -1;
	
	/** The minimal negation count of a method */
	public int Method_NegationCount = -1;
	
	
	
}
