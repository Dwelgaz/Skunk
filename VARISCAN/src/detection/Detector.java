package detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data.Feature;
import data.FeatureExpressionCollection;
import data.FeatureLocation;

public class Detector {

	/** The config contains the definition of the codesmell */
	private DetectionConfig config;
	
	/** Fitting feature locations with an explanation */
	private Map<FeatureLocation, ArrayList<EnumReason>> featureResult;
	
	/**
	 * Instantiates a new detector.
	 *
	 * @param config the codesmell configuration
	 */
	public Detector (DetectionConfig config)
	{
		this.config = config;
		this.featureResult = new HashMap<FeatureLocation, ArrayList<EnumReason>>();
	}
	
	/**
	 * Perform the detection based on the configuration and return fitting features
	 *
	 * @return a list with fitting features
	 */
	public Map<FeatureLocation, ArrayList<EnumReason>> Perform()
	{
		System.out.println("... Start detection based on the config file:...");
		
		// check each feature and location
		for (Feature feat : FeatureExpressionCollection.GetFeatures())
		{	
			for (FeatureLocation loc : feat.getLocs())
			{
				// check for features that take up a huge part of the project loc
				checkForFeatureToProjectRatio(feat, loc);	
				
				// check for features that are bigger than the mean lofc
				checkForFeatureToFeatureRatio(loc);
			}
		}
		
		System.out.println("... detection done!");
		
		// return the result
		return this.featureResult;
	}

	/**
	 * Check if the feature location is bigger than the mean value of feature lofc
	 * Indicates a large feature.
	 *
	 * @param loc the feature location to examine
	 */
	private void checkForFeatureToFeatureRatio(FeatureLocation loc)
	{
		// if the value is not set, it is -1000
		if (this.config.RatioFeatureToFeature != -1000)
		{
			// calculate the minimal lofc a feature location should have to be considered big
			int lofc = (loc.end - loc.start);
			double minLofc = (this.config.RatioFeatureToFeature * FeatureExpressionCollection.GetMeanLofc());
			
			// add the feature location if the feature lofc is bigger than the minimal
			if (lofc >= minLofc)
			{
				if (this.featureResult.containsKey(loc))
					this.featureResult.get(loc).add(EnumReason.FEATURELOC_BIGGERMEANLOFC);
				else
				{
					ArrayList<EnumReason> enumReason = new ArrayList<EnumReason>();
					enumReason.add(EnumReason.FEATURELOC_BIGGERMEANLOFC);
					this.featureResult.put(loc, enumReason);
				}
			}
		}
	}

	/**
	 * Check if the feature takes up a huge percentage of the whole project.
	 * Indicates a large feature.
	 *
	 * @param feat the feature
	 * @param loc the current location
	 */
	private void checkForFeatureToProjectRatio(Feature feat, FeatureLocation loc) 
	{
		// value is set if it is not -1000
		if (this.config.RatioFeatureToProject != -1000)
		{
			// calculate the minimal lofc the feature must have to be a large feature
			double minLofc = (FeatureExpressionCollection.GetLoc() * this.config.RatioFeatureToProject);
			
			// add the feature location
			if (feat.getLofc() >= minLofc)
			{
				if (this.featureResult.containsKey(loc))
					this.featureResult.get(loc).add(EnumReason.FEATURELOC_BIGPROJECTPART);
				else
				{
					ArrayList<EnumReason> enumReason = new ArrayList<EnumReason>();
					enumReason.add(EnumReason.FEATURELOC_BIGPROJECTPART);
					this.featureResult.put(loc, enumReason);
				}
			}
		}
	}
}
