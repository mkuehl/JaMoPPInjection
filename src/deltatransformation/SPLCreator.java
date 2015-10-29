package deltatransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import logger.Logger;

import com.max.jamoppinjection.PropertiesReader;

public class SPLCreator {

	private List<String> deltaList;
	private	List<String> featureList;
	private PropertiesReader configReader;
	private Logger log;

	private final static String FEATURE = "Feature";
	
	public SPLCreator() {
		configReader = new PropertiesReader("config.properties");
		try {
			log = new Logger(configReader.getPropValue("LogFilePath"), false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		deltaList = new LinkedList<String>();
		featureList = new LinkedList<String>();
		configReader = new PropertiesReader("config.properties");
	}
	
	public void setDeltaList(List<String> deltas) {
		deltaList = deltas;
	}
//	
//	public void setFeatureList(List<String> features) {
//		featureList = features;
//	}
	
	/**
	 * Adds a delta to the deltas list. Param delta must only be name beause 
	 * keyword "delta" gets added automatically.
	 * @param delta
	 */
	public void addDelta(String delta) {
		deltaList.add(delta);
	}
//
//	public void addFeature(String feature) {
//		featureList.add(feature);
//	}
	
	/**
	 * Computes all features automatically based on their deltanames. deltaList must have been set
	 * otherwise nothing happens. 
	 */
	public void computeFeaturesFromDeltas() {
		featureList = new LinkedList<String>();
		for (String delta : deltaList) {
			featureList.add(FEATURE + delta);
		}
	}
	
	public void createSPLFile(File f) {
		log.writeToLog(this.getClass().toString() + " : Creating SPL-file...");
		try {
			
			// without getParentFile() the path is created with the designated file as directory.
			f.getParentFile().mkdirs();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));

			out.append(createSPLString());
			
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
		createSPLString();
		
	}
	
	private String createSPLString() {
		if (!isDeltaListSet()) {
			return "";
		}
		String projectName = null;
		try {
			projectName = configReader.getPropValue("GitRepoURL");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// relies heavily on the structure of GitExePath in config file.
		projectName = projectName.substring(projectName.lastIndexOf("/")+1, projectName.lastIndexOf(".git"));
		String splString = "SPL " + projectName + "SPL {\n"
				+ "\tFeatures = {";
		String deltaString = "\tDeltas = {";
		String constraintString = "\tConstraints {";
		String partitionsString = "\tPartitions {\n";
		String productComponents = "";
		String productsString = "\tProducts {\n";
		computeFeaturesFromDeltas();
		for (String feature : featureList) {
			splString += feature + ", ";
		}
		splString = splString.trim().substring(0, splString.lastIndexOf(",")).trim();
		splString += "}\n\n";
		for (String delta : deltaList) {
			deltaString += delta + ", ";
		}
		deltaString = deltaString.substring(0, deltaString.lastIndexOf(","));
		deltaString += "}\n\n";
		splString += deltaString;
		for (String feature : featureList) {
			if (feature.contains("core")) {
				constraintString += feature + " & ";
			}
		}
		constraintString = constraintString.substring(0, constraintString.lastIndexOf(" &"));
		constraintString += ";}\n\n";
		splString += constraintString;
		for (int i = 0; i < deltaList.size(); i++) {
			if (deltaList.get(i).contains("core")) {
				partitionsString += "\t\t{" + deltaList.get(i) + "} when (" + featureList.get(i) + ");\n";
			} else {
				partitionsString += "\t\t{" + deltaList.get(i) + "} when (" + featureList.get(i-1) + " & " + featureList.get(i) + ");\n";
			}
		}
		partitionsString +="\t}\n\n";
		splString += partitionsString;
		for (String feature : featureList) {
			productComponents += feature;
			productsString += "\t\tProductTill" + feature + " = {" + productComponents + "};\n";
			productComponents += ", ";
		}
		productsString += "\t}\n";
		splString += productsString + "}";
		return splString;
	}
	
	private boolean isDeltaListSet() {
		if (deltaList == null) {
			return false;
		}
		if (deltaList.isEmpty()) {
			return false;
		}
		return true;
	}
}
