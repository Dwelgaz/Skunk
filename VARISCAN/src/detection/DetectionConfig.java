package detection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class DetectionConfig {

	/** A ratio that defines the difference of a feature to the mean lofc of other features (1 = returns all features that are as big as the mean value and bigger)*/
	public double Feature_MeanLofcRatio = -1000;
	public boolean Feature_MeanLofcRatio_Mand = false;
	public float Feature_MeanLofcRatio_Weight = 1;
	
	/** A ratio that defines the ratio between a feature lofc and the project loc (1 = 100% of the code)*/
	public double Feature_ProjectLocRatio = -1000;
	public boolean Feature_ProjectLocRatio_Mand = false;
	public float Feature_ProjectLocRatio_Weight = 1;
	
	/** A ratio that defines the ratio between the amount of feature locations for a feature to the amount of feature locations in the project */
	public double Feature_NoFeatureConstantsRatio = -1000;
	public boolean Feature_NoFeatureConstantsRatio_Mand = false;
	public float Feature_NoFeatureConstantsRatio_Weight = 1;
	
	/** The amount of compilation units (files) the feature is in */
	public double Feature_NumberOfCompilUnits = -1;
	public boolean Feature_NumberOfCompilUnits_Mand = false;
	public float Feature_NumberOfCompilUnits_Weight = 1;
	
	/** A ratio that defines the percentage of lofc to loc (of a method) (1 = 100% of the code*/
	public double Method_LofcToLocRatio = -1000;
	public boolean Method_LofcToLocRatio_Mand = false;
	public float Method_LofcToLocRatio_Weight = 1;
	
	/** A ratio that defines the percentage of loac to loc (of a method) (1 = 100% of the code) */
	public double Method_LoacToLocRatio = -1000;
	public boolean Method_LoacToLocRatio_Mand = false;
	public float Method_LoacToLocRatio_Weight = -1;
	
	/** The amount of feature locations a method should have minimally*/
	public int Method_NumberOfFeatureConstants = -1;
	public boolean Method_NumberOfFeatureConstants_Mand = false;
	public float Method_NumberOfFeatureConstants_Weight = 1;
	
	/** The amount of feature constants (without duples) a method should have minimally */
	public int Method_NumberOfFeatureConstantsNonDup = -1;
	public boolean Method_NumberOfFeatureConstantsNonDup_Mand = false;
	public float Method_NumberOfFeatureConstantsNonDup_Weight = 1;
	
	/** The minimal nesting depth of a method. (summarized nestings) */
	public int Method_NestingSum = -1;
	public boolean Method_NestingSum_Mand = false;
	public float Method_NestingSum_Weight = 1;
	
	/** The minimal nesting depth of a method.*/
	public int Method_NestingDepthMin = -1;
	public boolean Method_NestingDepthMin_Mand = false;
	public float Method_NestingDepthMin_Weight = 1;
	
	/** The minimal negation count of a method */
	public int Method_NegationCount = -1;
	public boolean Method_NegationCount_Mand = false;
	public float Method_NegationCount_Weight = 1;
	
	/** The minimal amount of featureoccurences. */
	public int Method_NumberOfFeatureLocations = -1;
	public boolean Method_NumberOfFeatureLocations_Mand = false;
	public float Method_NumberOfFeatureLocations_Weight = 1;
	
	/** Defines how much values have been set. */
	public int SetValues = 0;
	
	
	/**
	 * Instantiates a new detection config.
	 *
	 * @param pathToFile the path to file
	 * @throws IOException IOException if the file can not be read properly
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NumberFormatException 
	 */
	public DetectionConfig(String pathToFile) throws IOException, NoSuchFieldException, SecurityException, NumberFormatException, IllegalArgumentException, IllegalAccessException
	{
		// read all lines of the configuration file
		ArrayList<String> lines = (ArrayList<String>) FileUtils.readLines(new File(pathToFile));
		
		for (String line : lines)
		{
			// "#" are commentaries
			if (!line.startsWith("#") && !line.isEmpty())
			{
				// remove unnecessary white spaces
				line = line.trim();
				
				// check for mandatory value
				boolean mandatory = false;
				float weight = 1;
				if (line.contains(";"))
				{
					String[] split = line.split(";");
					
					if (split[1].equals("mandatory"))
						mandatory = true;
					else if (split[1].matches("(\\d)+(\\.)(\\d)+))"))
						weight = Float.parseFloat(split[1].trim());
					
					if (split.length == 3 && split[2].equals("mandatory"))
						mandatory = true;
					else if(split.length == 3 && split[2].matches("(\\d)+(\\.)(\\d)+))"))
						weight = Float.parseFloat(split[2].trim());
						
					line = split[0];
				}
				
				// split input with =; [0] == fieldname; [1] == value
				String[] split = line.split("=");
				
				// get field by name
				Class<DetectionConfig> thisClass = (Class<DetectionConfig>) this.getClass();
				Field current = thisClass.getField(split[0]);
				
				// set value (ratio needs double value, threshold needs int)
				if (split[0].contains("Ratio"))
					current.setDouble(this, Double.parseDouble(split[1].trim()));
				else
					current.setInt(this, Integer.parseInt(split[1].trim()));
				
				// set mandatory value
				thisClass.getField(split[0] + "_Mand").setBoolean(this, mandatory);
				
				// set weight value
				thisClass.getField(split[0] + "_Weight").setFloat(this, weight);
			}
		}
	}
	
	@Override public String toString()
	{
		String res = ">>> Code Smell Configuration \r\n";
		
		res += "[Feature-based Values]";
		
		if (this.Feature_MeanLofcRatio != -1000)
			res += "\r\nRatio - LOFC to mean LOFC: " + this.Feature_MeanLofcRatio + "; mandatory=" + this.Feature_MeanLofcRatio_Mand;
		if (this.Feature_ProjectLocRatio != -1000)
			res += "\r\nRatio - LOFC to LOC: " + this.Feature_ProjectLocRatio + "; mandatory=" + this.Feature_ProjectLocRatio_Mand;
		if (this.Feature_NoFeatureConstantsRatio != -1000)
			res += "\r\nRatio - Feature Constants(FC) to all FC: " + this.Feature_MeanLofcRatio + "; mandatory=" + this.Feature_MeanLofcRatio_Mand;
		if (this.Feature_NumberOfCompilUnits != -1)
			res += "\r\nAmount - Number of compilation units: " + this.Feature_NumberOfCompilUnits + "; mandatory=" + this.Feature_NumberOfCompilUnits_Mand;
		
		res += "\r\n\r\n[Method-based Values]";
		
		if (this.Method_LofcToLocRatio != -1000)
			res += "\r\nRatio - LOFC to LOC: " + this.Method_LofcToLocRatio + "; mandatory=" + this.Method_LofcToLocRatio_Mand;
		if (this.Method_LoacToLocRatio != -1000)
			res += "\r\nRatio - LOAC to LOC: " + this.Method_LoacToLocRatio + "; mandatory=" + this.Method_LoacToLocRatio_Mand;
		if (this.Method_NumberOfFeatureConstants != -1)
			res += "\r\nAmount - Number of feature constants: " + this.Method_NumberOfFeatureConstants + "; mandatory=" + this.Method_NumberOfFeatureConstants_Mand;
		if (this.Method_NumberOfFeatureConstantsNonDup != -1)
			res += "\r\nAmount - Number of feature constants (without doubles): " + this.Method_NumberOfFeatureConstantsNonDup + "; mandatory=" + this.Method_NumberOfFeatureConstantsNonDup_Mand;
		if (this.Method_NumberOfFeatureLocations != -1)
			res += "\r\nAmount - Number of feature locations: " + this.Method_NumberOfFeatureLocations + "; mandatory=" + this.Method_NumberOfFeatureLocations_Mand;
		if (this.Method_NestingSum != -1)
			res += "\r\nAmount - Number of nestings: " + this.Method_NestingSum + "; mandatory=" + this.Method_NestingSum_Mand;
		if (this.Method_NestingDepthMin != -1)
			res += "\r\nAmount - Minimal nesting depth: " + this.Method_NestingDepthMin + "; mandatory=" + this.Method_NestingDepthMin_Mand;
		if (this.Method_NegationCount != -1)
			res += "\r\nAmount - Number of negations: " + this.Method_NegationCount + "; mandatory=" + this.Method_NegationCount_Mand;
		
		return res;
	}
	
	// TODO Annotation File --> selbe wie bundle, nur auf file ebene
	// TODO Speculative Generalty
	// TODO In Output für jede Reason die entsprechende Zahl noch anzeigen
	
	
	
	
	// TODO Median für lofc
	// TODO Logging und Zeitverbrauch pro größerer Operation
	// TODO DetectionResult vor Presentation abspeichern möglich machen (z.b. eclipse einbindung)
}
