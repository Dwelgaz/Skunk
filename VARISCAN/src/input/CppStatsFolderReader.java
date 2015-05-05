package input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Stack;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import data.FeatureExpressionCollection;

/**
 * The Class CppStatsFolderReader for reading and processing csv files.
 */
public class CppStatsFolderReader {

	/** The CppStats results folder. */
	private String _cppStatsFolder;
	
	/**
	 * Instantiates a new CppStatsFolderReader
	 *
	 * @param folderPath the path of the folder
	 */
	public CppStatsFolderReader(String folderPath)
	{
		this._cppStatsFolder = folderPath;
	}
	
	/**
	 * Processes all CppStatsFiles
	 */
	public void ProcessFiles()
	{
		System.out.println("Processing CppStats CSV files in folder " + _cppStatsFolder + " ...");
		
		//this.getFeatureNames(new File(this._cppStatsFolder + "/merged_scattering_degrees.csv"));
		this.getFeatureLocations(new File(this._cppStatsFolder + "/cppstats_featurelocations.csv"));
		this.getLOCProject(new File(this._cppStatsFolder + "/cppstats.csv"));
		
		System.out.println("... CppStats processing done!");
	}
	
//	/**
//	 * Gather features from file "merged_scattering_degrees.csv"
//	 */
//	private void getFeatureNames(File csvFile)
//	{
//		System.out.print("... getting feature names ...");
//
//		// Parse CSV and get all feature expressions from the project
//		try 
//		{
//			CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.DEFAULT);
//			for (CSVRecord rec : parser)
//			{
//				String featureName = rec.get(0);
//				
//				// first two lines are no features
//				if ((!featureName.equals("sep=,")) && (!featureName.equals("define")))
//				{
//					FeatureExpressionCollection.AddFeature(new Feature(featureName));
//				}
//			}
//			
//		} catch (IOException e) {
//		
//			e.printStackTrace();
//		}
//		
//		System.out.println(" complete! ");		
//	}

	/**
	 * Get feature locations and lofc from file "cppstats_featurelocations.csv"
	 *
	 * @param csvFile the csv file
	 */
	private void getFeatureLocations(File csvFile)
	{
		System.out.print("... getting feature position metrics  ...");
		try 
		{
			Stack<CppStatsFeatureLocation> locs = new Stack<CppStatsFeatureLocation>();
			CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.DEFAULT);
			
			for (CSVRecord rec : parser)
			{
				// first lines are not necessary
				if ((rec.get(0).equals("sep=,")) || (rec.get(0).equals("FILENAME")))
						continue;
				else
				{	
					// assemble feature information
					String filePath = rec.get(0);
					
					// don't use header files
					if (filePath.contains(".h.xml"))
							continue;
					
					int start = Integer.parseInt(rec.get(1));
					int end = Integer.parseInt(rec.get(2));
					String type = rec.get(3);
					String entry = rec.get(4);
					
					// if file changes, empty stack and save all information
					if ((locs.size() > 0) && (!locs.peek().filePath.equals(filePath)))
					{
						while (locs.size() > 0)
							locs.pop().SaveFeatureLocationInformation(locs.size() + 1);
					}
					
					// if stack is empty, add featureLocation without parent
					if (locs.size() == 0)
					{
						CppStatsFeatureLocation fl = new CppStatsFeatureLocation(entry, filePath, type, start, end, null);
						if (fl.featureExpressions.size() != 0)
							locs.push(fl);
					}
					else
					{
						// if end of top element is bigger than start, the current element is nested in the top element --> push on stack			
						if (locs.peek().end > start)
						{
							CppStatsFeatureLocation fl = new CppStatsFeatureLocation(entry, filePath, type, start, end, locs.peek());
							if (fl.featureExpressions.size() != 0)
								locs.push(fl);
						}
						else
						{
							// save feature location if the endline of the top element is lower as the curent start location
							while ((locs.size() > 0) && (locs.peek().end <= start))
								locs.pop().SaveFeatureLocationInformation(locs.size() + 1);
							
							// item has to be put on stack, use top as reference for current featureloc, else push first element
							if (locs.size() > 0)
							{
								CppStatsFeatureLocation fl = new CppStatsFeatureLocation(entry, filePath, type, start, end, locs.peek());
								if (fl.featureExpressions.size() != 0)
									locs.push(fl);
							}
							else
							{
								CppStatsFeatureLocation fl = new CppStatsFeatureLocation(entry, filePath, type, start, end, null);
								if (fl.featureExpressions.size() != 0)
									locs.push(fl);
							}
						}
					}
				}
			}
			
			// if there is still an element
			while (locs.size() > 0)
				locs.pop().SaveFeatureLocationInformation(locs.size() + 1);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FeatureExpressionCollection.PostAction();
		
		System.out.println(" complete!");
	}
	
	/**
	 * Gets the lines of code for the project from file "cppstats.csv"
	 *
	 * @param csvFile the csv file
	 * @return the LOC project
	 */
	private void getLOCProject(File csvFile)
	{
		System.out.print("... getting lines of code ...");

		// Parse CSV and get lines of code from aggregation line
		try 
		{
			CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.RFC4180);
			for (CSVRecord rec : parser)
			{
				String featureName = rec.get(0);
				if ((!featureName.equals("sep=,")) && (!featureName.equals("FILENAME")) && (!featureName.equals("FUNCTIONS")) && (!featureName.equals("ALL - MERGED")) )
				{
					FeatureExpressionCollection.AddLoc(Integer.parseInt(rec.get(1)));;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(" complete!");	
	}
	
}
