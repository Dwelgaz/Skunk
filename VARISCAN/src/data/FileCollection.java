package data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;

public class FileCollection 
{
	/** The methods per file. */
	public static List<data.File> Files;
	
	/**
	 * Instantiates a new method collection.
	 */
	public static void Initialize()
	{
		Files = new ArrayList<data.File>();
	}
	
	/**
	 * Adds the file or gets it if already inside the list
	 *
	 * @param filePath the file path
	 * @param doc the doc
	 * @return the data. file
	 */
	public static data.File GetOrAddFile(String filePath)
	{
		for (data.File file : Files)
		{
			if (file.filePath.equals(filePath))
				return file;
		}
		
		data.File newFile = new data.File(filePath);
		Files.add(newFile);
		
		return newFile;
	}
	
	/**
	 * Gets the file.
	 *
	 * @param filePath the file path
	 * @return the data. file
	 */
	public static data.File GetFile(String filePath)
	{
		for (data.File file : Files)
		{
			if (file.filePath.equals(filePath))
				return file;
		}
		
		return null;
	}

	
	/**
	 * Calculate metrics for all metrics after finishing the collection
	 */
	public static void PostAction()
	{
		for (data.File file : Files)
		{
			file.SetNegationCount();
			file.SetNumberOfFeatureConstants();
			file.SetNumberOfFeatureLocations();
			file.SetNestingSum();
		}
	}
	
	/**
	 * Serialize the features into a xml representation
	 *
	 * @return A xml representation of this object.
	 */
	public static String SerializeFiles()
	{
		// nullify already processed data for memory reasons
		for (data.File file : Files)
		{
			file.emptyLines.clear();
			file.loac.clear();
		}
		
		XStream stream = new XStream();
		String xmlFeatures = stream.toXML(Files);
		
		return xmlFeatures;
	}
	
	/**
	 * Deserializes an xml string into the collection.
	 *
	 * @param xml the serialized xml representation
	 */
	public static void DeserialzeFiles(File xmlFile)
	{
		XStream stream = new XStream();
		Files = (List<data.File>) stream.fromXML(xmlFile);
	}
}
