package detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import data.Feature;
import data.FeatureExpressionCollection;
import data.FeatureLocation;
import data.Method;
import data.MethodCollection;

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
		System.out.println("... Start detection based on the config file...");
		
		checkFeatureCollection();
		
		checkMethodCollection();
		
		filterResults();
		
		System.out.println("... detection done!");
		
		// return the result
		return this.featureResult;
	}

	
	
	/**
	 * Filter results based on the mandatory values of the configuration
	 */
	private void filterResults()
	{
		// check for mandatory attributes in the detection configuration
		ArrayList<EnumReason> mandatories = new ArrayList<EnumReason>();
		
		if (config.Feature_MeanLofcRatio_Mand)
			mandatories.add(EnumReason.LARGEFEATURE_LOFCTOMEANLOFC);
		if (config.Feature_ProjectLocRatio_Mand)
			mandatories.add(EnumReason.LARGEFEATURE_LOFCTOLOC);
		if (config.Feature_NoflToSumRatio_Mand)
			mandatories.add(EnumReason.SHOTGUNSURGERY_NOFLTOSUMNOFL);
		if (config.Feature_NumberOfCompilUnits_Mand)
			mandatories.add(EnumReason.SHOTGUNSURGERY_NUMBERCOMPILATIONUNITS);
		if (config.Method_LoacToLocRatio_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_LOACTOLOC);
		if (config.Method_LofcToLocRatio_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_LOFCTOLOC);
		if (config.Method_NegationCount_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_NUMBERNEGATIONS);
		if (config.Method_NestingDepthMin_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_NUMBERNESTINGDEPTHMIN);
		if (config.Method_NestingSum_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_NUMBERNESTINGSUM);
		if (config.Method_NumberOfFeatureConstants_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_NUMBERFEATURECONST);
		if (config.Method_NumberOfFeatureLocs_Mand)
			mandatories.add(EnumReason.ANNOTATIONBUNDLE_NUMBERFEATURELOCS);
		
		// delete featurelocations from the result if it does not contain a mandatory attribute
		ArrayList<FeatureLocation> toDelete = new ArrayList<FeatureLocation>();
		for (FeatureLocation key : featureResult.keySet())
		{
			for (EnumReason mandatory : mandatories)
			{
				if (!featureResult.get(key).contains(mandatory))
					toDelete.add(key);
			}
		}
		
		for (FeatureLocation key : toDelete)
			featureResult.remove(key);
	}
	
	
	/**
	 * Checks the methodlocation for suitable locations in a method
	 */
	private void checkMethodCollection() {
		for (String file: MethodCollection.methodsPerFile.keySet())
		{
			for (Method meth : MethodCollection.methodsPerFile.get(file))
			{
				// sort functions
				Collections.sort(meth.featureLocations);
				
				// ratio lofc to loc
				checkForMethodLofcToLoc(meth);
				
				// ratio loac to loc
				checkForMethodLoacToLoc(meth);
				
				checkMethodForNumberOfFeatureLocs(meth);
				
				checkMethodForNumberOfFeatureOccs(meth);
				
				checkMethodForNumberFeatureConstants(meth);
				
				checkMethodForNumberNegations(meth);
				
				checkForMethodNestingSum(meth);
				
				checkForMethodNestingDepthMax(meth);
			}
		}
	}
	
	/**
	 * Check the feature collection for suitable feature locations
	 */
	private void checkFeatureCollection() {
		// check each feature and location
		for (Feature feat : FeatureExpressionCollection.GetFeatures())
		{	
			checkForFeatureNoflToSum(feat);
			
			checkForFeatureCompilUnits(feat);
			
			for (FeatureLocation loc : feat.getLocs())
			{
				// check for features that take up a huge part of the project loc
				checkForFeatureToProjectRatio(feat, loc);	
				
				// check for features that are bigger than the mean lofc
				checkForFeatureToFeatureRatio(loc);
			}
		}
	}

	
	
	
	
	/**
	 * Check the ratio between lofc and loc in a method. If the ratio exceeds the configuration value,
	 * add all features with the annotationbundle lofctoloc reason to the result
	 *
	 * @param meth the method
	 */
	private void checkForMethodLofcToLoc(Method meth) {
		if (this.config.Method_LofcToLocRatio != -1000)
		{
			double minLofc = (this.config.Method_LofcToLocRatio * meth.loc);
			
			if(meth.lofc >= minLofc)
			{
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_LOFCTOLOC);
			}
		}
	}
	
	/**
	 * Check the ratio between lofa and loc in a method. If the ratio exceeds the configuration value,
	 * add all features with the annotationbundle loactoloc reason to the result
	 *
	 * @param meth the method
	 */
	private void checkForMethodLoacToLoc(Method meth) {
		if (this.config.Method_LoacToLocRatio != -1000)
		{
			double minLoac = (this.config.Method_LoacToLocRatio * meth.loc);
			
			if(meth.GetLinesOfAnnotatedCode() >= minLoac)
			{
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_LOACTOLOC);
			}
		}
	}
	
	/**
	 * Check if the number of featurelocations in the method exceeds the configuration value.
	 * Add all feature locs to the result with the Number of FeatureLocs reason
	 * @param meth the meth
	 */
	private void checkMethodForNumberOfFeatureLocs(Method meth) {
		if (this.config.Method_NumberOfFeatureLocs != -1)
		{
			if (meth.GetFeatureLocationCount() > this.config.Method_NumberOfFeatureLocs)
			{
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_NUMBERFEATURELOCS);
			}
		}
	}
	
	/**
	 * Check if the number of feature occurences in the method exceeds the configuration value.
	 * Add all feature locs to the result with the Number of FeatureLocs reason
	 * @param meth the meth
	 */
	private void checkMethodForNumberOfFeatureOccs(Method meth) {
		if (this.config.Method_NumberOfFeatureOccurences != -1)
		{
			if (meth.GetFeatureLocationCount() > this.config.Method_NumberOfFeatureOccurences)
			{
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_NUMBERFEATUREOCC);
			}
		}
	}

	/**
	 * Check if the number of feature constants in the method exceeds the configuration value.
	 * Add all feature locs to the result with the number of feature constants reason
	 *
	 * @param meth the meth
	 */
	private void checkMethodForNumberFeatureConstants(Method meth) {
		if (this.config.Method_NumberOfFeatureConstants != -1)
		{
			if (meth.numberFeatureConstants > this.config.Method_NumberOfFeatureConstants)
			{
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_NUMBERFEATURECONST);
			}
		}
	}
	
	/**
	 * Check method for number negations. If it exceeds the configuration value, add all featurelocations
	 * with the specific reason
	 *
	 * @param meth the method
	 */
	private void checkMethodForNumberNegations(Method meth) {
		if (this.config.Method_NegationCount != -1)
		{
			if (meth.negationCount > this.config.Method_NegationCount)
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_NUMBERNEGATIONS);
		}
	}

	/**
	 * Check if the sum of nestings exceeds the code smell configuration value.
	 * If yes, add all feature locations with the corresponding reason to the result.
	 *
	 * @param meth the method
	 */
	private void checkForMethodNestingSum(Method meth) {
		if (this.config.Method_NestingSum != -1)
		{
			if (meth.nestingSum >= this.config.Method_NestingSum)
				for(FeatureLocation loc : meth.featureLocations)
					this.addFeatureLocWithReason(loc, EnumReason.ANNOTATIONBUNDLE_NUMBERNESTINGSUM);
		}
	}
	
	/**
	 * Check if the max nesting depth exceeds the code smell configuration value.
	 * If yes, add all feature locations with the corresponding reason to the result.
	 *
	 * @param meth the method
	 */
	private void checkForMethodNestingDepthMax(Method meth) {
		if (this.config.Method_NestingDepthMin != -1)
		{
				// check nesting via stacks and nesting depth
				Stack<FeatureLocation> nestingStack = new Stack<FeatureLocation>();		
				int beginNesting = -1;
				
				for(FeatureLocation loc : meth.featureLocations)
				{	
					// add the item instantly if the stack is empty, set the beginning nesting depth to the nd of the loc (nesting depth is file-based not method based)
					if (nestingStack.isEmpty())
					{
						beginNesting = loc.nestingDepth;
						nestingStack.push(loc);
					}
					else
					{
						// current nesting in consideration with starting location
						int curNesting = loc.nestingDepth - beginNesting;
						
						// 0 is the beginning nesting degree, everything higher than zero means it is a nested location
						if(curNesting > 0)
							nestingStack.push(loc);
						else
						{
							// calculate nestingdepth of bundle
							int ndm = -1;
							for (FeatureLocation current : nestingStack)
								if ((current.nestingDepth - beginNesting) > ndm)
									ndm = current.nestingDepth - beginNesting;
							
							// if the ndm of the bundle is higher than the configuration add all to the result
							if (ndm >= config.Method_NestingDepthMin)
							{
								while (!nestingStack.isEmpty())
									this.addFeatureLocWithReason(nestingStack.pop(), EnumReason.ANNOTATIONBUNDLE_NUMBERNESTINGDEPTHMIN);
							}
							else
								nestingStack.empty();
						}
					}
				}
				
				// final emptiing if something is left
				if (!nestingStack.isEmpty())
				{
					// calculate nestingdepth of bundle
					int ndm = -1;
					for (FeatureLocation current : nestingStack)
						if ((current.nestingDepth - beginNesting) > ndm)
							ndm = current.nestingDepth - beginNesting;
					
					if (ndm >= config.Method_NestingDepthMin)
					{
						while (!nestingStack.isEmpty())
							this.addFeatureLocWithReason(nestingStack.pop(), EnumReason.ANNOTATIONBUNDLE_NUMBERNESTINGDEPTHMIN);
					}
					else
						nestingStack.empty();
				}
			//}
		}
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
		if (this.config.Feature_MeanLofcRatio != -1000)
		{
			// calculate the minimal lofc a feature location should have to be considered big
			int lofc = (loc.end - loc.start);
			double minLofc = (this.config.Feature_MeanLofcRatio * FeatureExpressionCollection.GetMeanLofc());
			
			// add the feature location if the feature lofc is bigger than the minimal
			if (lofc >= minLofc)
				this.addFeatureLocWithReason(loc, EnumReason.LARGEFEATURE_LOFCTOMEANLOFC);
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
		if (this.config.Feature_ProjectLocRatio != -1000)
		{
			// calculate the minimal lofc the feature must have to be a large feature
			double minLofc = (FeatureExpressionCollection.GetLoc() * this.config.Feature_ProjectLocRatio);
			
			// add the feature location
			if (feat.getLofc() >= minLofc)
				this.addFeatureLocWithReason(loc, EnumReason.LARGEFEATURE_LOFCTOLOC);
		}
	}
	
	/**
	 * Check if the feature has more locations than ratio amount.
	 * If yes, add all locs to the result with the corresponding reason.
	 *
	 * @param feat the feat
	 */
	private void checkForFeatureNoflToSum(Feature feat) 
	{
		if (this.config.Feature_NoflToSumRatio != -1000)
		{
			// amount of nofls the feature has to exceed for a smell
			double minNofl = FeatureExpressionCollection.amountOfFeatureLocs * this.config.Feature_NoflToSumRatio;
			
			if (feat.getLocs().size() > minNofl)
			{
				for(FeatureLocation loc : feat.getLocs())
					this.addFeatureLocWithReason(loc, EnumReason.SHOTGUNSURGERY_NOFLTOSUMNOFL);
			}
		}
	}

	/**
	 * Check if the feature exceeds the configuration value for compilation units.
	 * If yes, add all locations with the corresponding reason to the result.
	 *
	 * @param feat the feat
	 */
	private void checkForFeatureCompilUnits(Feature feat) 
	{
		if (this.config.Feature_NumberOfCompilUnits != -1)
		{
			if (feat.GetAmountCompilationFiles() > this.config.Feature_NumberOfCompilUnits)
			{
				for(FeatureLocation loc : feat.getLocs())
					this.addFeatureLocWithReason(loc, EnumReason.SHOTGUNSURGERY_NUMBERCOMPILATIONUNITS);
			}
		}
	}
	
	
	
	/**
	 * Adds the feature location to the result list with the specified reason, or appends another reason if the location is already inside the result list
	 *
	 * @param loc the feature location to add
	 * @param reason the reason
	 */
	private void addFeatureLocWithReason(FeatureLocation loc, EnumReason reason)
	{
		if (this.featureResult.containsKey(loc))
			this.featureResult.get(loc).add(reason);
		else
		{
			ArrayList<EnumReason> enumReason = new ArrayList<EnumReason>();
			enumReason.add(reason);
			this.featureResult.put(loc, enumReason);
		}
	}
	
}
