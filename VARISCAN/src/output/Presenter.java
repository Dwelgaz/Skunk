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
import detection.DetectionConfig;
import detection.EnumReason;

public class Presenter {

	private String res = ">>> Detection results\r\n";
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
	
	/**
	 * 	 Instantiates a new presenter.
	 *
	 * @param conf the detection configuration
	 */
	public Presenter(DetectionConfig conf)
	{
		this.conf = conf;
	}
	
	public void saveResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{	
		// save config in result
		this.res += "\r\n\r\n" + conf.toString();
		
		// get the results of the complete detection process and the whole project
		this.getOverviewResults(results);
		
		// das ganez aufgedröselt nochmal mit Attributsklassen und wieviel prozent das vom gesamten annimmt
		// pro Attributsklasse
		// Anzahl der Feature, Anzahl der Feature locations, wieviel Lofc, Loac + Anteil der Configs, die größten Werte der klasse
		
		// get the results sorted per feature
		this.getResultsPerFeature(results);
		
		// Sortiert nach Location und file
		this.getLocationResults(results);
		
		String fileName = new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "_detection.txt";
		
		try {
			FileUtils.write(new File(fileName), this.res);
			System.out.println("Results saved to " + fileName + " in the working directory");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sorts the result per file and start and adds it to the resulting file
	 *
	 * @param results the results
	 * @return the location results
	 */
	private void getLocationResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureLocation> sortedKeys = new ArrayList<FeatureLocation>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureLocation>(FEATURELOCATION_FILEPATH_COMPARATOR, FEATURELOCATION_START_COMPARATOR));
		
		res += "\r\n\r\n>>> Location Results:\r\n";
		
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureLocation key : sortedKeys)
		{		
			if (!key.filePath.equals(currentPath))
			{
				currentPath = key.filePath;
				res += "[File: " + currentPath + "]\r\n";
				res += "Start\t\tEnd\t\tFeature\t\tReason\r\n";
			}
			
			res += key.start + "\t\t" + key.end + "\t\t" + key.corresponding.Name + "\t\t"+results.get(key).toString() + "\r\n";
		}
	}
	
	/**
	 * Sorts the results per feature, and presents the locations and reason for each corresponding feature
	 *
	 * @param results the detection results
	 * @return the results per feature
	 */
	private void getResultsPerFeature(HashMap<FeatureLocation, ArrayList<EnumReason>> results)
	{
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureLocation> sortedKeys = new ArrayList<FeatureLocation>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureLocation>(FEATURELOCATION_FEATURENAME_COMPARATOR, FEATURELOCATION_FILEPATH_COMPARATOR, FEATURELOCATION_START_COMPARATOR));
		
		res += ">>> Feature Results";
		
		String currentName = "";
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureLocation key : sortedKeys)
		{
			if (!key.corresponding.Name.equals(currentName))
			{
				currentName = key.corresponding.Name;
				res += "\r\n[Feature: " + currentName + "]\r\n"; 
				
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
	}
	
	/**
	 * Get the results of the complete set.
	 *
	 * @param results the result hasmap from the detection process
	 */
	private void getOverviewResults(HashMap<FeatureLocation, ArrayList<EnumReason>> results) 
	{
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
	}
}

