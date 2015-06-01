package data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * The Class MethodCollection.
 */
public class MethodCollection 
{
	
	/** The methods per file. */
	public static HashMap<String, List<Method>> methodsPerFile;
	
	/**
	 * Instantiates a new method collection.
	 */
	public static void Initialize()
	{
		methodsPerFile = new HashMap<String, List<Method>>();
	}
	
	/**
	 * Adds the file.
	 *
	 * @param fileName the file name
	 */
	public static void AddFile(String fileName)
	{
		if (!methodsPerFile.containsKey(fileName))
			methodsPerFile.put(fileName, new LinkedList<Method>());
	}

	/**
	 * Adds the method to file.
	 *
	 * @param fileName the file name
	 * @param method the method
	 */
	public static void AddMethodToFile(String fileName, Method method)
	{
		if (methodsPerFile.containsKey(fileName))
		{
			if (!methodsPerFile.get(fileName).contains(method))
				methodsPerFile.get(fileName).add(method);
		}
		else
		{
			AddFile(fileName);
			AddMethodToFile(fileName, method);
		}
	}
	
	/**
	 * Gets the methods of file.
	 *
	 * @param fileName the file name
	 * @return the list
	 */
	public static List<Method> GetMethodsOfFile(String fileName)
	{
		if (methodsPerFile.containsKey(fileName))
			return methodsPerFile.get(fileName);
		else
			return null;
	}
	
	/**
	 * Gets the method of a file based on the function signature
	 *
	 * @param fileName the file name
	 * @param functionSignature the function signature
	 * @return the method
	 */
	public static Method GetMethod(String fileName, String functionSignature)
	{
		// get the method based on the methodsignature
		List<Method> methods = GetMethodsOfFile(fileName);
		
		if (methods != null)
		{
			for (Method method : methods)
			{
				if (method.functionSignatureXml.equals(functionSignature))
					return method;
			}
		}
		
		return null;
	}
	
	/**
	 * Calculate metrics for all metrics after finishing the collection
	 */
	public static void PostAction()
	{
		for (String file : methodsPerFile.keySet())
		{
			for (Method meth : methodsPerFile.get(file))
			{
				meth.SetLoc();
				meth.SetNegationCount();
				meth.SetNumberOfFeatureConstantsNonDup();
				meth.SetNumberOfFeatureLocations();
				meth.SetNestingSum();
			}
		}
	}
	
	/**
	 * Serialize the features into a xml representation
	 *
	 * @return A xml representation of this object.
	 */
	public static String SerializeMethods()
	{
		for (String key : methodsPerFile.keySet())
		{
			for (Method meth : GetMethodsOfFile(key))
			{
				meth.loac.clear();
			}
		}
		
		XStream stream = new XStream();
		String xmlFeatures = stream.toXML(methodsPerFile);
		
		return xmlFeatures;
	}
	
	/**
	 * Deserializes an xml string into the collection.
	 *
	 * @param xml the serialized xml representation
	 */
	public static void DeserialzeMethods(File xmlFile)
	{
		XStream stream = new XStream();
		methodsPerFile = (HashMap<String, List<Method>>) stream.fromXML(xmlFile);
	}
}
