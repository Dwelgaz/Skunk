package output;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import data.FeatureExpressionCollection;
import data.FeatureLocation;
import data.Method;
import detection.DetectionConfig;
import detection.EnumReason;

public class AnalyzedDataHandler {

	private DetectionConfig conf = null;
	
	/** A comparator that compares featurenames of feature locations. */
	public final static Comparator<FeatureLocation> FEATURELOCATION_FEATURENAME_COMPARATOR = new Comparator<FeatureLocation>()
	{
		@Override public int compare(FeatureLocation f1, FeatureLocation f2)
		{
			return f1.corresponding.Name.compareTo(f2.corresponding.Name);
		}
	};
	
	/** A comparator that compares the filepath of feature location*/
	public final static Comparator<FeatureLocation> FEATURELOCATION_FILEPATH_COMPARATOR = new Comparator<FeatureLocation>()
	{
		@Override public int compare(FeatureLocation f1, FeatureLocation f2)
		{
			return f1.filePath.compareTo(f2.filePath);
		}
	};
	
	/** A comparator that compares startposition of feature locations. */
	public final static Comparator<FeatureLocation> FEATURELOCATION_START_COMPARATOR = new Comparator<FeatureLocation>()
	{
		@Override public int compare(FeatureLocation f1, FeatureLocation f2)
		{
			if (f1.start > f2.start)
				return 1;
			else if (f1.start < f2.start)
				return -1;
			else
				return 0;
		}
	};
	
	/** A comparator that compares startposition of feature locations methods. */
	public final static Comparator<FeatureLocation>  FEATURELOCATION_METHOD_COMPARATOR = new Comparator<FeatureLocation>()
	{
		@Override public int compare(FeatureLocation f1, FeatureLocation f2)
		{
			if (f1.inMethod == null)
				return -1;
			if (f2.inMethod == null)
				return 1;
			
			if (f1.inMethod.start > f2.inMethod.start)
				return 1;
			else if (f1.inMethod.start < f2.inMethod.start)
				return -1;
			else
				return 0;
		}
	};
	
	
	
	
	/**
	 * 	 Instantiates a new presenter.
	 *
	 * @param conf the detection configuration
	 */
	public AnalyzedDataHandler(DetectionConfig conf)
	{
		this.conf = conf;
	}
	
	public void saveResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{			
		// get the results of the complete detection process and the whole project
		String overview = this.getOverviewResults(results);
		
		// get overview per attribute
		String attributes = this.getAttributeOverviewResults(results);
		
		// Sortiert nach Location und file
		String files = this.getFileSortedRestults(results);
		
		String methods = this.getMethodSortedResults(results);
		
		// get the results sorted per feature
		String features = this.getFeatureSortedResults(results);
		
		String fileName = new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "_detection_";
		
		try 
		{
			FileUtils.write(new File(fileName + "overview.txt"), overview);
			FileUtils.write(new File(fileName + "attributes.txt"), attributes);
			FileUtils.write(new File(fileName + "files.txt"), files);
			FileUtils.write(new File(fileName + "methods.txt"), methods);
			FileUtils.write(new File(fileName + "features.txt"), features);
			System.out.println("Result files saved to the working directory!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the overview metrics for each attribute, and saves it to the output result
	 *
	 * @param results the results
	 * @return the attribute overview results
	 */
	private String getAttributeOverviewResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() +"\r\n\r\n\r\n\r\n\r\n";

		ArrayList<AttributeOverview> attributes = new ArrayList<AttributeOverview>();
		
		for (FeatureLocation key : results.keySet())
		{
			for (EnumReason reason : results.get(key))
			{
				// get fitting attribute or create one
				boolean add = true;
				for (AttributeOverview overview : attributes)
				{
					if (overview.Reason.equals(reason))
						add = false;
				}
				if (add)
					attributes.add(new AttributeOverview(reason));
				
				// add location information
				for (AttributeOverview overview : attributes)
					if (overview.Reason.equals(reason))
						overview.AddFeatureLocationInfo(key);
			}
		}
		
		// add attribute overview to output
		for (AttributeOverview attr : attributes)
			res += attr.toString();
		
		return res;
	}
	
	/**
	 * Sorts the result per file and start and adds it to the resulting file
	 *
	 * @param results the results
	 * @return the location results
	 */
	private String getFileSortedRestults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n\r\n";
		
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureLocation> sortedKeys = new ArrayList<FeatureLocation>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureLocation>(FEATURELOCATION_FILEPATH_COMPARATOR, FEATURELOCATION_START_COMPARATOR));
		
		res += ">>> File-Sorted Results:\r\n";
		
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureLocation key : sortedKeys)
		{		
			if (!key.filePath.equals(currentPath))
			{
				currentPath = key.filePath;
				res += "\r\n\r\n\r\n[File: " + currentPath + "]\r\n";
				res += "Start\t\tEnd\t\tFeature\t\tReason\r\n";
			}
			
			res += key.start + "\t\t" + key.end + "\t\t" + key.corresponding.Name + "\t\t"+results.get(key).toString() + "\r\n";
		}
		
		return res;
	}
	
	/**
	 * Sorts the results per feature, and presents the locations and reason for each corresponding feature
	 *
	 * @param results the detection results
	 * @return the results per feature
	 */
	private String getFeatureSortedResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n";
		
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureLocation> sortedKeys = new ArrayList<FeatureLocation>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureLocation>(FEATURELOCATION_FEATURENAME_COMPARATOR, FEATURELOCATION_FILEPATH_COMPARATOR, FEATURELOCATION_START_COMPARATOR));
		
		res += ">>> Feature-Sorted Results";
		
		String currentName = "";
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureLocation key : sortedKeys)
		{
			if (!key.corresponding.Name.equals(currentName))
			{
				currentName = key.corresponding.Name;
				res += "\r\n\r\n\r\n[Feature: " + currentName + "]\r\n"; 
				
				// reset filepath
				currentPath = "";
			}
			
			if (!key.filePath.equals(currentPath))
			{
				currentPath = key.filePath;
				res += "File: " + currentPath + "\r\n";
				res += "Start\t\tEnd\t\tReason\r\n";
			}
			
			
			res += key.start + "\t\t" + key.end + "\t\t" + results.get(key).toString() + "\r\n";
		}
		
		return res;
	}
	
	/**
	 * Sorts the results per Method and returns the smell metric of each method
	 *
	 * @param results the detection results
	 * @return the results per feature
	 */
	private String getMethodSortedResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n";
		ArrayList<FeatureLocation> sortedKeys = new ArrayList<FeatureLocation>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureLocation>(FEATURELOCATION_FILEPATH_COMPARATOR, FEATURELOCATION_METHOD_COMPARATOR, FEATURELOCATION_START_COMPARATOR));
		
		res += ">>> Method-Sorted Results";
		
		Method currentMethod = null;
		String currentPath = "";
		
		// print feature locations with reason per File and Method
		for (FeatureLocation key : sortedKeys)
		{
			// don't display feature that are not in a method
			if (key.inMethod == null)
				continue;
			
			// flag to indicate that the smell value was already displayed
			boolean presented = false;
			
			if (!key.filePath.equals(currentPath))
			{
				res += this.GetMethodSmellValue(currentMethod);
				presented = true;
				
				currentPath = key.filePath;
				res += "\r\n\r\nFile: " + currentPath;
			}
			
			if (!key.inMethod.equals(currentMethod))
			{
				if (!presented)
				{
					res += this.GetMethodSmellValue(currentMethod);
					presented = false;
				}
				
				currentMethod = key.inMethod;
				res += "\r\nMethod: " + currentMethod.functionSignatureXml + "\r\n";
				res += "Start\t\tEnd\t\tReason\r\n";
			}
				
				res += key.start + "\t\t" + key.end + "\t\t" + results.get(key).toString() + "\r\n";
				// wFeatOcc* ((Loac/Loc) *NoFeatOcc) + wFeatLoc * (NoFeatLoc/NoFeatOcc) + wNestingSum * (NestingSum/NoFeatOcc)
				
			if (sortedKeys.indexOf(key) == sortedKeys.size() - 1)
				res += this.GetMethodSmellValue(currentMethod);
		}
		
		return res;
	}
	
	
	/**
	 * Get the results of the complete set.
	 *
	 * @param results the result hasmap from the detection process
	 */
 	private String getOverviewResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results) 
	{
 		String res = conf.toString();
		// amount of feature constants
		ArrayList<String> constants = new ArrayList<String>();
		float percentOfConstants = 0;
		
		// amount of feature locations
		int countLocations = results.entrySet().size();
		float percentOfLocations = 0;
		
		// lofcs in project
		int completeLofc = 0;
		
		// loac in project
		HashMap<String, ArrayList<Integer>> loacs = new HashMap<String, ArrayList<Integer>>();
		int completeLoac = 0;
		float percentOfLoc = 0;
		
		for (FeatureLocation loc : results.keySet())
		{
			// get the amount of feature constants by saving each feature constant name
			if (!constants.contains(loc.corresponding.Name))
				constants.add(loc.corresponding.Name);
			
			// add lines of code to result
			completeLofc += loc.end-loc.start;
			
			// add all lines per file to the data structure, that are part of the feature location... no doubling for loac calculation
			if (!loacs.keySet().contains(loc.filePath))
				loacs.put(loc.filePath, new ArrayList<Integer>());
			
			for (int i = loc.start; i <= loc.end; i++)
			{
				if (!loacs.get(loc.filePath).contains(i))
					loacs.get(loc.filePath).add(i);
			}
		}
		
		// calculate max loac
		for (String file : loacs.keySet())
			completeLoac += loacs.get(file).size();
		
		// calculate percentages
		percentOfLoc = completeLoac * 100 / FeatureExpressionCollection.GetLoc();
		percentOfLocations = countLocations * 100 / FeatureExpressionCollection.amountOfFeatureLocs;
		percentOfConstants = constants.size() * 100 / FeatureExpressionCollection.GetFeatures().size();
		
		// Complete overview
		res += "\r\n\r\n\r\n>>> Complete Overview\r\n";
		res += "Number of feature constants: \t" + constants.size() + " (" + percentOfConstants + "% of " + FeatureExpressionCollection.GetFeatures().size() + " constants)\r\n";
		res += "Number of feature locations: \t" + countLocations  + " (" + percentOfLocations + "% of " + FeatureExpressionCollection.amountOfFeatureLocs + " locations)\r\n";
		res += "Lines of annotated Code: \t" + completeLoac + " (" + percentOfLoc + "% of " + FeatureExpressionCollection.GetLoc() + " LOC)\r\n";
		res += "Lines of feature code: \t\t" + completeLofc + "\r\n";
		res += "Mean LOFC per feature: \t\t" + FeatureExpressionCollection.GetMeanLofc() + "\r\n\r\n\r\n";
		
		return res;
	}
 	
 	
 	
 	
 	
 	/**
	 * Gets the method smell value and adds it to the result
	 *
	 * @param currentMethod the current method
	 */
	private String GetMethodSmellValue(Method currentMethod) 
	{
		String res = "";
		
		// show method smell value
		if (currentMethod != null)
		{
			float featOccSmell = 0;
			float featLocSmell = 0;
			float nestSumSmell = 0;
			
			if (conf.Method_NumberOfFeatureOccurences != -1)
			{
				featOccSmell = conf.Method_NumberOfFeatureOccurences_Weight * (((float) currentMethod.GetLinesOfAnnotatedCode() / (float) currentMethod.loc) * (float) currentMethod.numberFeatureOccurences);
				res += "Loac/Loc * #FeatOccurcens = " + featOccSmell + "\r\n";
			}
			if (conf.Method_NumberOfFeatureLocs != -1)
			{
				featLocSmell = conf.Method_NumberOfFeatureLocs_Weight * ((float) currentMethod.GetFeatureLocationCount() / (float) currentMethod.numberFeatureOccurences);
				res += "#FeatLocations/#FeatOccurences = " + featLocSmell + "\r\n";
			}
			if (conf.Method_NestingSum != -1)
			{
				nestSumSmell = conf.Method_NestingSum_Weight * ((float) currentMethod.nestingSum / (float) currentMethod.numberFeatureOccurences);
				res += "Loac/Loc * #FeatOccurcens = " + nestSumSmell + "\r\n";
			}
			
			float sum = (featOccSmell + featLocSmell + nestSumSmell);
			
			res += "Sum = " + sum + "\r\n\r\n"; 
		}
		
		return res;
	}
}

