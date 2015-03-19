package data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
}
