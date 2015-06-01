package input;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.FeatureExpressionCollection;
import data.FeatureConstant;

/**
 * The Class CppStatsFeatureConstant.
 */
public class CppStatsFeatureConstant 
{
	public List<String> featureExpressions;
	public List<Boolean> notFlags;
	
	
	public String filePath;
	public String type;
	
	public int start;
	public int end;
	
	public CppStatsFeatureConstant parent;
	
	/**
	 * Instantiates a new feature location.
	 *
	 * @param entry the entry
	 * @param filePath the file path
	 * @param type the type
	 * @param start the start
	 * @param end the end
	 * @param parent the parent
	 */
	public CppStatsFeatureConstant(String entry, String filePath, String type, int start, int end, CppStatsFeatureConstant parent)
	{
		this.filePath = filePath;
		this.type = type;
		
		this.start = start;
		this.end = end;
		
		this.parent = parent;
		
		// get features from entry
		this.featureExpressions = new LinkedList<String>();
		this.notFlags = new LinkedList<Boolean>();
		
		this.getFeaturesFromEntry(entry);
		
		// remove features from parent;
		if (parent != null)
			this.removeFeaturesFromParents(this.parent);
	}

	/**
	 * Save this feature constant information to the feature expression collection
	 */
	public void SaveFeatureConstantInformation(int stackSize)
	{
		// stackSize 1 means nesting depth of 0;
		stackSize--;
		
		List<FeatureConstant> create = new LinkedList<FeatureConstant>();
		
		// search for the corresponding feature expression and save information
		for (String feature : this.featureExpressions)
		{
			// end-1 = #endif does not belong to lines of code????
			FeatureConstant constant = new FeatureConstant(this.filePath, this.start, this.end, stackSize, this.notFlags.get(this.featureExpressions.indexOf(feature)));
			FeatureExpressionCollection.GetFeature(feature).AddFeatureLocation(constant);
			
			// remember created locations for combinations
			create.add(constant);
		}
		
		// set combined feature constants
		for (FeatureConstant current : create)
			for (FeatureConstant other : create)
			{
				if (other != current)
					current.combinedWith.add(other.id);
			}		
	}
	
	/**
	 * Gets the features from the entry
	 *
	 * @param entry the entry
	 * @return the features from entry
	 */
	private void getFeaturesFromEntry(String entry)
	{
		// remove comments from entry
		if (entry.contains("/*"))
		{
			String comment = entry.substring(entry.indexOf("/*"), entry.indexOf("*/") + 2);
			entry = entry.replace(comment, "");
		}
		
		Pattern pattern = Pattern.compile("[\\w!]+");
		Matcher matcher = pattern.matcher(entry);
		
		// get each feature from entry
		boolean notFlag = false;
		while (matcher.find())
		{
			String match = matcher.group();
			
			// set notFlag and replace !
			if(match.contains("!"))
				notFlag = true;
				
			// defined is not a feature
			if (match.contains("defined"))
				continue;
			
			// numbers only, or it begins with a number --> version numbers or whatever
			else if (match.matches("(\\d)+$") || Character.isDigit(match.charAt(0)) || match.charAt(0) == '!')
				continue;
			
			// save feature expression and save boolean
			this.featureExpressions.add(match);

			if (notFlag)
			{
				this.notFlags.add(true);
				notFlag = false;
			}
			else
				this.notFlags.add(false);
			
		}
	}
	
	/**
	 * Removes features that are already included in the parent.
	 *
	 * @param parent the parent
	 */
	private void removeFeaturesFromParents(CppStatsFeatureConstant nextParent)
	{
		// get features that are in both collection
		List<String> toRemove = new LinkedList<String>();
		List<Boolean> toRemoveFlags = new LinkedList<Boolean>();
		
		// remove features that are already included in of the items parents
		while (nextParent != null)
		{
			for (String parentFeature : nextParent.featureExpressions)
			{
				for (String feature : this.featureExpressions)
				{
					if (feature.equals(parentFeature) && (!toRemove.contains(feature)))
					{
						toRemoveFlags.add(this.notFlags.get(this.featureExpressions.indexOf(feature)));
						toRemove.add(feature);
					}
				}
			}
			
			nextParent = nextParent.parent;
		}
		
		// remove doubled features from current set (and respective notFlag)
		for (String remove : toRemove)
			this.featureExpressions.remove(remove);

		for (Boolean remove : toRemoveFlags)
			this.notFlags.remove(remove);
	}
}
