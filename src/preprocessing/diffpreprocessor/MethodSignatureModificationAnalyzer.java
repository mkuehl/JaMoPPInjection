package preprocessing.diffpreprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodSignatureModificationAnalyzer {
	
	private String ignoredMethodsPathString;
	private File f;
	
	public MethodSignatureModificationAnalyzer(String pathWithFile) {
		if (pathWithFile != null && !pathWithFile.equals("")) {
			ignoredMethodsPathString = pathWithFile;
		} else {
			ignoredMethodsPathString = "E:\\methodsToIgnore.txt";
		}
		f = new File(ignoredMethodsPathString);
	}
	
	/**
	 * Checks if the given line contains a method signature. 
	 * @param line
	 * @return
	 */
	public boolean isMethod(String line) {

		String methodRegex = "(\\s)*((public|protected|private)?[\\s]+)?"
				+ "((final[\\s]+|static[\\s]+|((final[\\s]+static[\\s]+))|((static[\\s]+final[\\s]+)))?)"
				+ "[a-zA-Z0-9_]+[\\s]+[a-zA-Z0-9_]+[\\s]*(\\()";
		Pattern methodPattern = Pattern.compile(methodRegex);
		Matcher m;
		m = methodPattern.matcher(line);
		if (m.find()) {
			// method signatures may not contain the equals-sign or new keyword.
//			String s = m.group();
			if (!m.group().contains("=") && !m.group().contains("new")) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Checks if the given lines are together a signature modification. Assumption: lines are subsequent.
	 * @param line
	 * @param lineBefore
	 * @return
	 */
	public boolean isMethodNameModification(String line, String lineBefore) {
		if (isMethod(line)) {
			if (isMethod(lineBefore)) {
				return true;
			}
		}
		return false;
	}
	
	public void addIgnoredMethod(String signatureLine) {
		// methods might have two space characters. If so, extract the name from the second, if not, extract it from the first.
		String name = signatureLine.substring(signatureLine.indexOf(" ")+1, signatureLine.indexOf("("));
		if (name.contains(" ")) {
			name = name.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
			name = name.substring(name.indexOf(" ")+1);
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.append(name +"\n" + signatureLine + "\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	private List<String> getIgnoredMethods() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();

			return null;
		}
		List<String> list = new LinkedList<String>();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public boolean isMethodIgnored(String method) {
		List<String> ignoredMethods = getIgnoredMethods();
		if (ignoredMethods == null || ignoredMethods.size() == 0) {
			return false;
		}
		for (String m : ignoredMethods) {
			if (m.equals(method)) {
				return true;
			}
		}
		return false;
	}
}
