package data;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class FeatureExpressionCollection.
 */
public class FeatureExpressionCollection 
{

	private static ArrayList<Feature> _features;
	private static int _count;
	private static int _loc;
	private static int _meanLofc;
	public static int amountOfFeatureLocs;
	
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
	 * Gets the amount of lines of code.
	 *
	 * @return the amount of features in the collection
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
		amountOfFeatureLocs = 0;
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
}
