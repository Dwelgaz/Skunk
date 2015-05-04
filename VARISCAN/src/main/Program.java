package main;

import java.util.ArrayList;
import java.util.HashMap;

import output.Presenter;
import data.FeatureExpressionCollection;
import data.FeatureLocation;
import data.MethodCollection;
import detection.DetectionConfig;
import detection.Detector;
import detection.EnumReason;
import input.CppStatsFolderReader;
import input.SrcMlFolderReader;

public class Program {

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
		
		// gather input
		String path = args[0];
		
		// process necessary csv files in project folder
		CppStatsFolderReader cppReader = new  CppStatsFolderReader(path);
		cppReader.ProcessFiles();

		// process srcML files
		SrcMlFolderReader mlReader = new SrcMlFolderReader();
		mlReader.ProcessFiles();
	
		DetectionConfig config = new DetectionConfig();
		//config.Method_NestingDepthMin = 2;  
		// config.Method_NumberOfFeatureConstants = 5;
		// config.Method_LoacToLocRatio = 0.6;
		 config.Feature_ProjectLocRatio = 0.15;
		
		Detector detector = new Detector(config);
		HashMap<FeatureLocation, ArrayList<EnumReason>> res = (HashMap<FeatureLocation, ArrayList<EnumReason>>) detector.Perform();
		
		Presenter presenter = new Presenter(config);
		presenter.saveResults(res);

	}

}
