package output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import data.Feature;
import data.FeatureExpressionCollection;
import data.FeatureConstant;
import data.FileCollection;
import data.Method;
import data.MethodCollection;
import detection.DetectionConfig;
import detection.EnumReason;

public class AnalyzedDataHandler {

	private DetectionConfig conf = null;
	private String currentDate = "";
	
	/** A comparator that compares featurenames of feature constants. */
	public final static Comparator<FeatureConstant> FEATURECONSTANT_FEATURENAME_COMPARATOR = new Comparator<FeatureConstant>()
	{
		@Override public int compare(FeatureConstant f1, FeatureConstant f2)
		{
			return f1.corresponding.Name.compareTo(f2.corresponding.Name);
		}
	};
	
	/** A comparator that compares the filepath of feature constant*/
	public final static Comparator<FeatureConstant> FEATURECONSTANT_FILEPATH_COMPARATOR = new Comparator<FeatureConstant>()
	{
		@Override public int compare(FeatureConstant f1, FeatureConstant f2)
		{
			return f1.filePath.compareTo(f2.filePath);
		}
	};
	
	/** A comparator that compares startposition of feature constants. */
	public final static Comparator<FeatureConstant> FEATURECONSTANT_START_COMPARATOR = new Comparator<FeatureConstant>()
	{
		@Override public int compare(FeatureConstant f1, FeatureConstant f2)
		{	
			return Integer.compare(f1.start,f2.start);
		}
	};
	
	/** A comparator that compares startposition of feature constants methods. */
	public final static Comparator<FeatureConstant>  FEATURECONSTANT_METHOD_COMPARATOR = new Comparator<FeatureConstant>()
	{
		@Override public int compare(FeatureConstant f1, FeatureConstant f2)
		{
			if (f1.inMethod == null)
			{
				if (f2.inMethod == null)
					return 0;
				return -1;
			}
			if (f2.inMethod == null)
				return 1;
			
			int s1 = f1.inMethod.start;
			int s2 = f2.inMethod.start;
			
			return Integer.compare(s1, s2);
		}
	};
	
	/** The comparator that compares the smell value of the csv records. */
	public final static Comparator<Object[]> ABSmellComparator = new Comparator<Object[]>()
	{
		@Override public int compare(Object[] f1, Object[] f2)
		{
			float s1 = (float) f1[3];
			float s2 = (float) f2[3];
			
			return Float.compare(s2, s1);
		}
	};
	
	/** The comparator that compares the smell value of the csv records. */
	public final static Comparator<Object[]> AFSmellComparator = new Comparator<Object[]>()
	{
		@Override public int compare(Object[] f1, Object[] f2)
		{
			float s1 = (float) f1[1];
			float s2 = (float) f2[1];
			
			return Float.compare(s2, s1);
		}
	};
	
	/** The comparator that compares the smell value of the csv records. */
	public final static Comparator<Object[]> LGSmellComparator = new Comparator<Object[]>()
	{
		@Override public int compare(Object[] f1, Object[] f2)
		{
			float s1 = (float) f1[1];
			float s2 = (float) f2[1];
			
			return Float.compare(s2, s1);
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
	
	
	
	
	
	
	
	/**** TXT Start End Saving *****/
	
	public void SaveTextResults(HashMap<FeatureConstant, ArrayList<EnumReason>> results)
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
		
		currentDate = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
		String fileName = currentDate + "_detection_";
		
		try 
		{
			FileUtils.write(new File(fileName + "overview.txt"), overview);
			FileUtils.write(new File(fileName + "attributes.txt"), attributes);
			FileUtils.write(new File(fileName + "files.txt"), files);
			FileUtils.write(new File(fileName + "methods.txt"), methods);
			FileUtils.write(new File(fileName + "features.txt"), features);
			System.out.println("Detection result files saved to the working directory...");
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
	private String getAttributeOverviewResults(HashMap<FeatureConstant, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() +"\r\n\r\n\r\n\r\n\r\n";

		ArrayList<AttributeOverview> attributes = new ArrayList<AttributeOverview>();
		
		for (FeatureConstant key : results.keySet())
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
	private String getFileSortedRestults(HashMap<FeatureConstant, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n\r\n";
		
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureConstant> sortedKeys = new ArrayList<FeatureConstant>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureConstant>(FEATURECONSTANT_FILEPATH_COMPARATOR, FEATURECONSTANT_START_COMPARATOR));
		
		res += ">>> File-Sorted Results:\r\n";
		
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureConstant key : sortedKeys)
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
	private String getFeatureSortedResults(HashMap<FeatureConstant, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n";
		
		// sort the keys after featurename, filepath and start
		ArrayList<FeatureConstant> sortedKeys = new ArrayList<FeatureConstant>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureConstant>(FEATURECONSTANT_FEATURENAME_COMPARATOR, FEATURECONSTANT_FILEPATH_COMPARATOR, FEATURECONSTANT_START_COMPARATOR));
		
		res += ">>> Feature-Sorted Results";
		
		String currentName = "";
		String currentPath = "";
		
		// print the the locations and reasons sorted after feature
		for (FeatureConstant key : sortedKeys)
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
	 * Sorts the results per Method and returns it in a string per file/method/cnstant
	 *
	 * @param results the detection results
	 * @return the results per feature
	 */
	private String getMethodSortedResults(HashMap<FeatureConstant, ArrayList<EnumReason>> results)
	{
		String res = conf.toString() + "\r\n\r\n\r\n\r\n\r\n";
		ArrayList<FeatureConstant> sortedKeys = new ArrayList<FeatureConstant>(results.keySet());
		Collections.sort(sortedKeys, new ComparatorChain<FeatureConstant>(FEATURECONSTANT_FILEPATH_COMPARATOR, FEATURECONSTANT_METHOD_COMPARATOR, FEATURECONSTANT_START_COMPARATOR));
		
		res += ">>> Method-Sorted Results";
		
		Method currentMethod = null;
		String currentPath = "";
		
		// print feature constants with reason per File and Method
		for (FeatureConstant key : sortedKeys)
		{
			// don't display feature that are not in a method
			if (key.inMethod == null)
				continue;
						
			if (!key.filePath.equals(currentPath))
			{
				currentPath = key.filePath;
				res += "\r\n\r\nFile: " + currentPath;
			}
			
			if (!key.inMethod.equals(currentMethod))
			{
				currentMethod = key.inMethod;
				res += "\r\nMethod: " + currentMethod.functionSignatureXml + "\r\n";
				res += "Start\t\tEnd\t\tReason\r\n";
			}
				
				res += key.start + "\t\t" + key.end + "\t\t" + results.get(key).toString() + "\r\n";
		}
		
		return res;
	}
	
	
	/**
	 * Get the results of the complete set.
	 *
	 * @param results the result hasmap from the detection process
	 */
 	private String getOverviewResults(HashMap<FeatureConstant, ArrayList<EnumReason>> results) 
	{
 		String res = conf.toString();
		// amount of feature constants
		ArrayList<String> constants = new ArrayList<String>();
		float percentOfConstants = 0;
		
		// amount of feature constants
		int countLocations = results.entrySet().size();
		float percentOfLocations = 0;
		
		// lofcs in project
		int completeLofc = 0;
		
		// loac in project
		HashMap<String, ArrayList<Integer>> loacs = new HashMap<String, ArrayList<Integer>>();
		int completeLoac = 0;
		float percentOfLoc = 0;
		
		for (FeatureConstant constant : results.keySet())
		{
			// get the amount of feature constants by saving each feature constant name
			if (!constants.contains(constant.corresponding.Name))
				constants.add(constant.corresponding.Name);
			
			// add lines of code to result
			completeLofc += constant.end-constant.start;
			
			// add all lines per file to the data structure, that are part of the feature constant... no doubling for loac calculation
			if (!loacs.keySet().contains(constant.filePath))
				loacs.put(constant.filePath, new ArrayList<Integer>());
			
			for (int i = constant.start; i <= constant.end; i++)
			{
				if (!loacs.get(constant.filePath).contains(i))
					loacs.get(constant.filePath).add(i);
			}
		}
		
		// calculate max loac
		for (String file : loacs.keySet())
			completeLoac += loacs.get(file).size();
		
		// calculate percentages
		percentOfLoc = completeLoac * 100 / FeatureExpressionCollection.GetLoc();
		percentOfLocations = countLocations * 100 / FeatureExpressionCollection.numberOfFeatureConstants;
		percentOfConstants = constants.size() * 100 / FeatureExpressionCollection.GetFeatures().size();
		
		// Complete overview
		res += "\r\n\r\n\r\n>>> Complete Overview\r\n";
		res += "Number of features: \t" + constants.size() + " (" + percentOfConstants + "% of " + FeatureExpressionCollection.GetFeatures().size() + " constants)\r\n";
		res += "Number of feature constants: \t" + countLocations  + " (" + percentOfLocations + "% of " + FeatureExpressionCollection.numberOfFeatureConstants + " locations)\r\n";
		res += "Lines of annotated Code: \t" + completeLoac + " (" + percentOfLoc + "% of " + FeatureExpressionCollection.GetLoc() + " LOC)\r\n";
		res += "Lines of feature code: \t\t" + completeLofc + "\r\n";
		
		res += "Mean LOFC per feature: \t\t" + FeatureExpressionCollection.GetMeanLofc() + "\r\n\r\n\r\n";
		
		return res;
	}
 	
 	/**** TXT Start End Saving *****/
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**** CSV Smell Value Saving ****/
 	
 	public void SaveCsvResults()
 	{
 		FileWriter writer = null;
 		CSVPrinter csv = null;
 		
 		// ensure consistent filenaming
 		if (this.currentDate.equals(""))
 			currentDate = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
 		String fileName = this.currentDate + "_metrics_";
 		
 		this.createMethodCSV(fileName + "methods.csv", writer, csv);
 		this.createFeatureCSV(fileName +"features.csv", writer, csv);
 		this.createFileCSV(fileName + "files.csv" , writer, csv);
 		
 		System.out.println("Metric files saved to the working directory");
 	}

 	/**
	  * Creates the file metric csv.
	  *
	  * @param fileName the file name
	  * @param writer the writer
	  * @param csv the csv printer
	  */
	private void createFileCSV(String fileName, FileWriter writer, CSVPrinter csv)
 	{
 		try
 		{
 			writer = new FileWriter(fileName);
 			csv = new CSVPrinter(writer, CSVFormat.EXCEL);
 			// add the header for the csv file
 	 		Object [] FileHeader = {"File", "AFSmell","LocationSmell","ConstantsSmell", "NestingSmell", "LOC", "LOAC", "LOFC", "NOFC_Dup", "NOFC_NonDup", "NOFL", "NONEST"};
 			csv.printRecord(FileHeader);
 			
 			// calculate values and add records
 			List<Object[]> fileData = new ArrayList<Object[]>();
 			for (data.File file : FileCollection.Files)
 			{
 				if (skipFile(file))
 						continue;
 					
 				fileData.add(createFileRecord(file));
 			}
 			
 			// sort after smellvalue
 			Collections.sort(fileData, new ComparatorChain<Object[]>(AFSmellComparator));
 			
 			for (Object[] record : fileData)
 				csv.printRecord(record);
 		}
 		catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } 
 		finally 
 		{
            try 
            {
                writer.flush();
                writer.close();
                csv.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
 	}
 	
 	/**
	  * Creates the file csv record
	  *
	  * @param file the file
	  * @return the object[] a csv record as array
	  */
	private Object[] createFileRecord(data.File file)
 	{
		// calculate smell values
		// Loac/Loc * #FeatLocs
		float featLocSmell = conf.File_LoacToLocRatio_Weight * (((float) file.GetLinesOfAnnotatedCode() / (float) file.loc) * (float) file.numberOfFeatureLocations);
		
		// #Constants/#FeatLocs
		float featConstSmell = conf.File_NumberOfFeatureConstants_Weight * ((float) file.GetFeatureConstantCount() / (float) file.numberOfFeatureLocations);
		
		// Loac/Loc * #FeatLocs
		float nestSumSmell = conf.Method_NestingSum_Weight * ((float) file.nestingSum / (float) file.numberOfFeatureLocations);
		float sum = (featLocSmell + featConstSmell + nestSumSmell);
		
 		Object[] result = new Object[12];
 		result[0] = file.filePath;
 		result[1] = sum;
 		result[2] = featLocSmell;
 		result[3] = featConstSmell;
 		result[4] = nestSumSmell;
 		result[5] = file.loc;
 		result[6] = file.GetLinesOfAnnotatedCode();
 		result[7] = file.lofc;
 		result[8] = file.GetFeatureConstantCount();
 		result[9] = file.numberFeatureConstantsNonDup;
 		result[10] = file.numberOfFeatureLocations;
 		result[11] = file.nestingSum;
 		
 		return result;
 	}
 	
 	/**
	 * Creates the method csv.
	 *
	 * @param writer the writer
	 * @param csv the csv
	 */
	private void createFeatureCSV(String fileName, FileWriter writer, CSVPrinter csv) {
		try
 		{
 			writer = new FileWriter(fileName);
 			csv = new CSVPrinter(writer, CSVFormat.EXCEL);
 		
 			// TODO Wieviele NOFC in Kombination
 			// add the header for the csv file
 	 		Object [] FeatureHeader = {"Name", "LGSmell", "SSSmell ","ConstantsSmell", "LOFCSmell", "CUSmell", "NOFC", "MAXNOFC", "LOFC", "ProjectLOC", "NOCU"};
 			csv.printRecord(FeatureHeader);
 			
 			// calculate values and add records
 			List<Object[]> featureData = new ArrayList<Object[]>();
 			for (Feature feat : FeatureExpressionCollection.GetFeatures())
 			{
 				if (skipFeature(feat))
 					continue;
 				
 				featureData.add(this.createFeatureRecord(feat));
 			}
 			
 			// sort after smellvalue
 			Collections.sort(featureData, new ComparatorChain<Object[]>(LGSmellComparator));
 			
 			for (Object[] record : featureData)
 				csv.printRecord(record);
 		}
 		catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } 
 		finally 
 		{
            try 
            {
                writer.flush();
                writer.close();
                csv.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
	}
 	
 	/**
	  * Creates the feature csv record.
	  *
	  * @param feat the feat
	  * @return the object[]
	  */
	 private Object[] createFeatureRecord(Feature feat)
 	{
 		// # featureConstants/#TotalLocations
 		float constSmell = this.conf.Feature_NumberNofc_Weight * (((float) feat.getConstants().size()) / ((float) FeatureExpressionCollection.numberOfFeatureConstants));
 		
 		// LOFC/TotalLoc   																				
 		float lofcSmell = this.conf.Feature_NumberLofc_Weight * (((float)feat.getLofc()) / ((float) FeatureExpressionCollection.GetLoc()));
 		
 		// CompilUnit/MaxCompilUnits
 		float compilUnitsSmell = ((float) feat.compilationFiles.size()) / ((float) FileCollection.Files.size());
 		
 		float sumLG = constSmell + lofcSmell;
 		float sumSS = constSmell + compilUnitsSmell;
 		
 		Object[] result = new Object[11];
 		result[0] = feat.Name;
 		result[1] = sumLG;
 		result[2] = sumSS;
 		result[3] = constSmell;
 		result[4] = lofcSmell;
 		result[5] = compilUnitsSmell;
 		
 		result[6] = feat.getConstants().size();
 		result[7] = FeatureExpressionCollection.numberOfFeatureConstants;
 		result[8] = feat.getLofc();
 		result[9] = FeatureExpressionCollection.GetLoc();
 		result[10] = feat.compilationFiles.size();
 		
 		return result;
 	}
	
	/**
	 * Creates the method csv.
	 *
	 * @param writer the writer
	 * @param csv the csv
	 */
	private void createMethodCSV(String fileName, FileWriter writer, CSVPrinter csv) {
		try
 		{
 			writer = new FileWriter(fileName);
 			csv = new CSVPrinter(writer, CSVFormat.EXCEL);
 		
 			// add the header for the csv file
 	 		Object [] MethodHeader = {"File","Start", "Method","ABSmell","LocationSmell","ConstantsSmell", "NestingSmell", "LOC", "LOAC", "LOFC", "NOFL", "NOFC_Dup", "NOFC_NonDup", "NONEST"};
 			csv.printRecord(MethodHeader);
 			
 			// calculate values and add records
 			List<Object[]> methodData = new ArrayList<Object[]>();
 			for (List<Method> methods : MethodCollection.methodsPerFile.values())
 			{
 				for (Method meth : methods)
 				{
 					if (skipMethod(meth))
 						continue;
 					
 					methodData.add(createMethodRecord(meth));
 				}
 			}
 			
 			// sort after smellvalue
 			Collections.sort(methodData, new ComparatorChain<Object[]>(ABSmellComparator));
 			
 			for (Object[] record : methodData)
 				csv.printRecord(record);
 		}
 		catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } 
 		finally 
 		{
            try 
            {
                writer.flush();
                writer.close();
                csv.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
	}
 	
 	
 	/**
	  * GCalculate the smell value of the current method and return data record as list
	  *
	  * @param currentMethod the current method
	  * @return the list the data record for the csv file
	  */
	private Object[] createMethodRecord(Method currentMethod) 
	{
		// calculate smell values
		// Loac/Loc * #FeatLocs
		float featLocSmell = conf.Method_LoacToLocRatio_Weight * (((float) currentMethod.GetLinesOfAnnotatedCode() / (float) currentMethod.loc) * (float) currentMethod.numberFeatureLocations);
		
		// #Constants/#FeatLocs
		float featConstSmell = conf.Method_NumberOfFeatureConstants_Weight * ((float) currentMethod.GetFeatureConstantCount() / (float) currentMethod.numberFeatureLocations);
		
		// Loac/Loc * #FeatLocs
		float nestSumSmell = conf.Method_NestingSum_Weight * ((float) currentMethod.nestingSum / (float) currentMethod.numberFeatureLocations);
		float sum = (featLocSmell + featConstSmell + nestSumSmell);
	
		Object[] result = new Object[14];
		
		// File Start Method
		result[0] = currentMethod.filePath;
		result[1] = currentMethod.start;
		result[2] = currentMethod.functionSignatureXml;
		
		// SmellValue, LocationSmell, ConstantSmell, NestingSmell
		result[3] = sum;
		result[4] = featLocSmell;
		result[5] = featConstSmell;
		result[6] = nestSumSmell;
		
		// Loc Loac Lofc NoLocs NoConst NoNestings
		result[7] = currentMethod.loc;
		result[8] = currentMethod.GetLinesOfAnnotatedCode();
		result[9] = currentMethod.lofc;
		result[10] = currentMethod.numberFeatureLocations;
		result[11] = currentMethod.GetFeatureConstantCount();
		result[12] = currentMethod.numberFeatureConstantsNonDup;
		result[13] = currentMethod.nestingSum;
		
		return result;
	}
	
	/**
	 * Skip the method for csv file creation depending on the mandatory settings of the configuration
	 *
	 * @param method the method
	 * @return true, if method does not fulfill mandatory settings
	 */
	private boolean skipMethod(Method method)
	{
		boolean result = false;
	
		if (conf.Method_LoacToLocRatio_Mand && ((float) method.GetLinesOfAnnotatedCode() / (float) method.loc) < conf.Method_LoacToLocRatio)
			result = true;
		if (conf.Method_NumberOfFeatureConstants_Mand && method.GetFeatureConstantCount() < conf.Method_NumberOfFeatureConstants)
			result = true;
		if (conf.Method_NestingSum_Mand && method.nestingSum < conf.Method_NestingSum)
			result = true;
		
		return result;
	}
	
	/**
	 * Skip the method for csv file creation depending on the mandatory settings of the configuration
	 *
	 * @param method the method
	 * @return true, if method does not fulfill mandatory settings
	 */
	private boolean skipFile(data.File file)
	{
		boolean result = false;
	
		if (conf.File_LoacToLocRatio_Mand && ((float) file.GetLinesOfAnnotatedCode() / (float) file.loc) < conf.File_LoacToLocRatio)
			result = true;
		if (conf.File_NumberOfFeatureConstants_Mand && file.GetFeatureConstantCount() < conf.File_NumberOfFeatureConstants)
			result = true;
		if (conf.File_NestingSum_Mand && file.nestingSum < conf.File_NestingSum)
			result = true;
		
		return result;
	}
	
	/**
	 * Skip the feature for csv file creation depending on the mandatory settings of the configuration
	 *
	 * @param feat the feature
	 * @return true, if feature does not fulfill mandatory settings
	 */
	private boolean skipFeature(Feature feat)
	{
		boolean result = false;
		
		if (conf.Feature_NumberNofc_Mand && (feat.getConstants().size() < conf.Feature_NumberNofc))
			result = true;
		if (conf.Feature_NumberLofc_Mand && (feat.getLofc() < conf.Feature_NumberLofc))
			result = true;
		
		return result;
	}
	
	/**** CSV Start End Saving *****/
}

