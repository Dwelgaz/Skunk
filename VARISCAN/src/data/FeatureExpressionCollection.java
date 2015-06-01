package data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;

/**
 * The Class FeatureExpressionCollection.
 */
public class FeatureExpressionCollection 
{

	private static ArrayList<Feature> _features;
	private static int _count;
	private static int _loc;
	private static int _meanLofc;
	public static int numberOfFeatureConstants;
	
	/**
	 * Gets the count of features.
	 *
	 * @return the amount of features in the collection
	 */
	public static int GetCount()
	{
		return _count;
	}
	
	/**
	 * Sets the amount of features
	 *
	 * @param value the value
	 */
	public static void SetCount(int value)
	{
		_count = value;
	}
	
	/**
	 * Gets the amount of lines of code.
	 *
	 * @return the amount of loc
	 */
	public static int GetLoc()
	{
		return _loc;
	}
	
	/**
	 * Gets the mean value of lines of feature code.
	 *
	 * @return the amount of features in the collection
	 */
	public static int GetMeanLofc()
	{
		return _meanLofc;
	}
	
	/**
	 * Sets the mean value of lines of feature code
	 *
	 * @param value the value
	 */
	public static void SetMeanLofc(int value)
	{
		_meanLofc = value;
	}
	
	/**
	 * Sets the loc.
	 *
	 * @param loc the loc
	 */
	public static void AddLoc(int loc)
	{
		_loc += loc;
	}
	
	/**
	 * Gets the feature with the input name
	 *
	 * @param name the name
	 * @return the feature
	 */
	public static Feature GetFeature(String name)
	{
		for (Feature feature : _features)
		{
			if (feature.Name.equals(name))
				return feature;
		}
		
		// feature missing --> add new
		Feature newFeature = new Feature(name);
		AddFeature(newFeature);
		return newFeature;
	}
	
	/**
	 * Gets the feature constant of the specified feature
	 *
	 * @param name the name
	 * @param id the id of the constant
	 * @return the feature constant
	 */
	public static FeatureConstant GetFeatureConstant(String name, UUID id)
	{
		for (Feature feature : _features)
		{
			if (feature.constants.containsKey(id))
				return feature.constants.get(id);
		}
		return null;
	}
	
	/**
	 * Get all features.
	 *
	 * @return the list
	 */
	public static List<Feature> GetFeatures()
	{
		return _features;
	}
	
	/**
	 * Initialize necessary components of the collection
	 */
	public static void Initialize()
	{
		_features = new ArrayList<Feature>();
		_count = 0;
		_loc = 0;
		numberOfFeatureConstants = 0;
	}
	
	/**
	 * Adds a feature to the collection
	 *
	 * @param feature the feature
	 */
	public static void AddFeature(Feature feature)
	{
		_features.add(feature);
		_count++;
	}
	
	/**
	 * Misc operations (calculate mean lofc)
	 */
	public static void PostAction()
	{
		for (Feature feat : _features)
			_meanLofc = _meanLofc + feat.getLofc();
		
		if (_features.size() != 0)
			_meanLofc = _meanLofc / _features.size();
	}
	
	/**
	 * Serialize the features into a xml representation
	 *
	 * @return A xml representation of this object.
	 */
	public static String SerializeFeatures()
	{
		XStream stream = new XStream();
		String xmlFeatures = stream.toXML(_features);
		
		return xmlFeatures;
	}
	
	/**
	 * Deserializes an xml string into the collection.
	 *
	 * @param xml the serialized xml representation
	 */
	public static void DeserialzeFeatures(File xmlFile)
	{
		XStream stream = new XStream();
		_features = (ArrayList<Feature>) stream.fromXML(xmlFile);
	}
}
