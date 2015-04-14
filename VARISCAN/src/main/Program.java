package main;

import java.util.ArrayList;
import java.util.HashMap;

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
		
		
//		List<Feature> tmp = FeatureExpressionCollection.GetFeatures();
//		Collections.sort(tmp);
//		
//		System.out.println("\r\n\r\nLOC: " + FeatureExpressionCollection.GetLoc());
//		System.out.println("Mean LOFC: " + FeatureExpressionCollection.GetMeanLofc() + "\r\n\r\n");
//		
//		System.out.println("Lines of Code\tAnnotation Count\tMaxNesting\tMinNesting\tMaxGran\t\t\tminGran\t\t\tName");
//		for (Feature feat : FeatureExpressionCollection.GetFeatures())
//		{		
//			System.out.println(feat.getLofc() + "\t\t" + feat.getLocs().size() + "\t\t\t" + feat.maxNestingDepth + "\t\t" + feat.minNestingDepth +" \t\t" + feat.maxGranularity + "\t\t" + feat.minGranularity +  "\t\t" + feat.Name);
//		}
//		
//		System.out.println("\r\n\r\n");
//		System.out.println("AnnonCount\tLofc\t\tAnnonLoc\tLoc\t\tSignature\t\t\tMethod");
//		for (String key : MethodCollection.methodsPerFile.keySet())
//		{
//			for (Method method : MethodCollection.methodsPerFile.get(key))
//			{
//				System.out.println(method.GetAnnotationCount() + "\t\t" + method.lofc + "\t\t" + method.GetLinesOfAnnotatedCode() + "\t\t" + method.loc +  "\t\t"  + method.functionSignatureXml + "\t\t" + key);
//			}
//		}
	
		DetectionConfig config = new DetectionConfig();
		config.Feature_MeanLofcRatio = 2;
		//config.RatioFeatureToProject = 0.1;
		
		Detector detector = new Detector(config);
		HashMap<FeatureLocation, ArrayList<EnumReason>> res = (HashMap<FeatureLocation, ArrayList<EnumReason>>) detector.Perform();
		
		System.out.println("lofc\t\tfeature\t\treason");
		System.out.println(FeatureExpressionCollection.GetMeanLofc());
		for (FeatureLocation loc : res.keySet())
		{	
			System.out.println((loc.end - loc.start) + "\t\t" + loc.corresponding.Name + "\t\t" + res.get(loc));
		}
	}

}
