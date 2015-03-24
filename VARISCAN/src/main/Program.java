package main;

import java.util.Collections;
import java.util.List;

import data.Feature;
import data.FeatureExpressionCollection;
import data.Method;
import data.MethodCollection;
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
		
		
		List<Feature> tmp = FeatureExpressionCollection.GetFeatures();
		Collections.sort(tmp);
		
		System.out.println("\r\n\r\nLOC: " + FeatureExpressionCollection.GetLoc());
		System.out.println("Mean LOFC: " + FeatureExpressionCollection.GetMeanLofc() + "\r\n\r\n");
		
		System.out.println("Lines of Code\tAnnotation Count\tMaxNesting\tMinNesting\tMaxGran\t\t\tminGran\t\t\tName");
		for (Feature feat : FeatureExpressionCollection.GetFeatures())
		{		
			System.out.println(feat.getLofc() + "\t\t" + feat.getLocs().size() + "\t\t\t" + feat.maxNestingDepth + "\t\t" + feat.minNestingDepth +" \t\t" + feat.maxGranularity + "\t\t" + feat.minGranularity +  "\t\t" + feat.Name);
		}
		
		System.out.println("\r\n\r\n");
		System.out.println("AnnonCount\tLofc\t\tAnnonLoc\tLoc\t\tSignature\t\t\tMethod");
		for (String key : MethodCollection.methodsPerFile.keySet())
		{
			for (Method method : MethodCollection.methodsPerFile.get(key))
			{
				System.out.println(method.GetAnnotationCount() + "\t\t" + method.lofc + "\t\t" + method.GetLinesOfAnnotatedCode() + "\t\t" + method.loc +  "\t\t"  + method.functionSignatureXml + "\t\t" + key);
			}
		}
	}

}
