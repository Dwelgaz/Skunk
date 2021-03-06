package main;

import input.CppStatsFolderReader;
import input.SrcMlFolderReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import output.AnalyzedDataHandler;
import output.ProcessedDataHandler;
import data.FeatureExpressionCollection;
import data.FeatureConstant;
import data.FileCollection;
import data.MethodCollection;
import detection.DetectionConfig;
import detection.Detector;
import detection.EnumReason;

public class Program {

	/** The code smell configuration. */
	private static DetectionConfig conf = null;
	
	/** The path of the source folder. */
	private static String sourcePath = "";
	
	/** A flag that defines, if intermediate formats will be saved. */
	public static boolean saveIntermediate = false;
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) 
	{	
		// Initialize Components
		FeatureExpressionCollection.Initialize();
		MethodCollection.Initialize();
		FileCollection.Initialize();
		
		// gather input
		boolean run = analyzeInput(args);
		
		if (!run)
		{
			System.out.println("Program exits due to input error.");
			return;
		}
		
		if (!sourcePath.isEmpty())
		{
			// process necessary csv files in project folder
			CppStatsFolderReader cppReader = new  CppStatsFolderReader(sourcePath);
			cppReader.ProcessFiles();
	
			// process srcML files
			SrcMlFolderReader mlReader = new SrcMlFolderReader();
			mlReader.ProcessFiles();
			
			// do post actions
			MethodCollection.PostAction();
			FileCollection.PostAction();			
			
			// save processed data
			if (saveIntermediate)
				ProcessedDataHandler.SaveProcessedData();
		}
		
		
		// display loc, loac, #feat, NOFL and NOFC
		System.out.println(FeatureExpressionCollection.GetLoc() + " Loc");
		System.out.println(FeatureExpressionCollection.GetCount() + " Features");
		System.out.println(FeatureExpressionCollection.numberOfFeatureConstants + " Feature Constants");
		
		int loac = 0;
		int nofl = 0;
		for (data.File f : FileCollection.Files)
		{
			loac += f.GetLinesOfAnnotatedCode();
			nofl += f.numberOfFeatureLocations;
		}
		
		System.out.println(loac + " Loac: " + (loac * 100) / FeatureExpressionCollection.GetLoc());
		System.out.println(nofl + " nofl");
	
		
		// run detection with current configuration
		if (conf != null)
		{
			Detector detector = new Detector(conf);
			HashMap<FeatureConstant, ArrayList<EnumReason>> res = (HashMap<FeatureConstant, ArrayList<EnumReason>>) detector.Perform();
			
			AnalyzedDataHandler presenter = new AnalyzedDataHandler(conf);
			presenter.SaveTextResults(res);
			presenter.SaveCsvResults();
		}
	}
	
	/**
	 * Analyze input to decide what to do during runtime
	 *
	 * @param args the input arguments
	 * @return true, if input is correct
	 */
	private static boolean analyzeInput(String[] args)
	{
		// for easier handling, transform to list
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].contains("--"))
				args [i] = args[i].toLowerCase();
		}
		
		List<String> input = Arrays.asList(args);
		
		
		
		// get the path to the codesmell configuration
		if (input.contains("--config"))
		{
			try 
			{
				String configPath = input.get(input.indexOf("--config") + 1);
				File f = new File(configPath);

				if(f.exists() && !f.isDirectory()) 
					conf = new DetectionConfig(configPath);
				else 
				{
					System.out.println("The path to the configuration file does not exist.");
					return false;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("ERROR: Could not load code smell configuration file!");
				e.printStackTrace();
				return false;
			}
		}
		
		if (input.contains("--saveintermediate"))
		{
			saveIntermediate = true;
		}
		
		// get the path of the source folder
		if (input.contains("--source"))
		{
			try {
				String path = input.get(input.indexOf("--source") + 1);
				File f = new File(path);
				
				if (f.exists() && f.isDirectory())
					sourcePath = path;
				else
				{
					System.out.println("Source path does not exist.");
					return false;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("ERROR: Could not load source folder");
				e.printStackTrace();
				return false;
			}
		}
		// read previously processed data
		else if (input.contains("--processed"))
		{
			ProcessedDataHandler.LoadProcessedData(input.get(input.indexOf("--processed") + 1));
		}
		else
		{
			System.out.println("You either need to set a source folder (--source) or a processed data folder (--processed)!");
			return false;
		}
		
		return true;
	}

}
