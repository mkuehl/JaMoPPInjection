package deltatransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import logger.Logger;

import org.deltaj.deltaJ.AddsUnit;
import org.deltaj.deltaJ.Delta;
import org.deltaj.deltaJ.DeltaAction;
import org.deltaj.deltaJ.DeltaJFactory;
import org.deltaj.deltaJ.DeltaJUnit;
import org.deltaj.deltaJ.JavaCompilationUnit;
import org.deltaj.deltaJ.ModifiesAction;
import org.deltaj.deltaJ.ModifiesImport;
import org.deltaj.deltaJ.ModifiesInheritance;
import org.deltaj.deltaJ.ModifiesInterface;
import org.deltaj.deltaJ.ModifiesPackage;
import org.deltaj.deltaJ.ModifiesUnit;
import org.deltaj.deltaJ.RemovesUnit;
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffpreprocessor.LineChecker;
import preprocessing.diffpreprocessor.ModificationType;
import preprocessing.diffs.ClassChanges;

import com.max.jamoppinjection.ChangesValidator;
import com.max.jamoppinjection.PropertiesReader;

public class DeltaJCreator {

	/**
	 * Reference to the singleton instance of the DeltaJFactory. It is used to
	 * create AST nodes.
	 */
	private DeltaJFactory factory = DeltaJFactory.eINSTANCE;
	private SPLCreator splc;
	
	private String deltaString;

	private PropertiesReader configReader;
	private Logger log;

	public DeltaJCreator() {
		configReader = new PropertiesReader("config.properties");
		try {
			log = new Logger(configReader.getPropValue("LogFilePath"), false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		splc = new SPLCreator();
		deltaString = "";
	}
	
	public DeltaJUnit createDeltaJUnit() {
		return factory.createDeltaJUnit();
	}
	
	/**
	 * Creating a new Delta. This delta is a node of the AST which represents a
	 * delta module in source code. The deltas name is set and the delta is linked 
	 * to its corresponding parent node, a DeltaJUnit.
	 * 
	 * @param parent
	 * @param deltaName
	 * @return
	 */
	public Delta createNewDelta(DeltaJUnit parent, String deltaName) {
		log.writeToLog(this.getClass().toString() + " : Creating new delta...");
		// Creating Delta object which is a node in the AST.
		Delta d = factory.createDelta();

		// Setting the deltas name attribute
		d.setName(deltaName);

		// Adding the delta node as child node to its parent, a DeltaJUnit.
		// a) referencing dju's list of delta nodes in a local variable.
		EList<Delta> djuDeltas = parent.getDelta();

		// b) Adding the delta node to the dju's list of delta child nodes.
		djuDeltas.add(d);

		// Returning the freshly created Delta object. It is much easier to work
		// with is after it is correctly added to the AST.

		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
		return d;
	}
	
	/**
	 * Creating an AddsUnit with jamoppParsedClass as content. It is then added
	 * to the parent node of type Delta.
	 * 
	 * @param parent
	 *            An instance of type Delta. It is the only valid parent node in
	 *            the DeltaJ AST.
	 * @param jamoppParsedClass
	 *            This is only a placeholder for a JaMoPP parsed Java snippet.
	 * @param addRem
	 * 			  a byte that indicates addition, modification or removal. Positive
	 * 			  numbers mean addition, negative removal and zero modification. 
	 */
	public void addJavaUnit(Delta parent, EObject jamoppParsedClass, String packageName, String className, 
			byte addRem, ModificationType typeOfChange) {
		log.writeToLog(this.getClass().toString() + " : Adding new DeltaAction...");
		if (typeOfChange.equals(ModificationType.CLASSADDITION)) {
			// Trigger transformation of JaMoPP to DeltaJ AST
			JavaCompilationUnit jcu = fancyJamoppToDeltaJTransformation(jamoppParsedClass);
			addJavaAddsUnit(parent, jcu);
		} else if (typeOfChange.equals(ModificationType.CLASSREMOVAL)) {
			addJavaRemovesUnit(parent, packageName + "." + className);
		} else if (!typeOfChange.equals(ModificationType.CLASSADDITION) && 
				!typeOfChange.equals(ModificationType.CLASSREMOVAL)) {
		
			addJavaModifiesUnit(parent, typeOfChange, jamoppParsedClass);
		}

		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
	}
	
	private void addJavaAddsUnit(Delta parent, JavaCompilationUnit deltaCode) {
		AddsUnit au = factory.createAddsUnit();
		
		// Adding the child node to the adds unit
		au.setUnit(deltaCode);
		
		// Adding the adds unit node as child to the delta node
		parent.getDeltaActions().add(au);

		// Nothing will be returned because we don't need to add anything here.
	}
	
	private void addJavaRemovesUnit(Delta parent, String qualifiedClassName) {
		RemovesUnit ru = factory.createRemovesUnit();
		
		ru.setQName(qualifiedClassName);
		
		parent.getDeltaActions().add(ru);
	}
	
	private void addJavaModifiesUnit(Delta parent, ModificationType typeOfChange, EObject ast) {
		if (typeOfChange == null || ast == null || parent == null) {
			return;
		}
		ModifiesTypeExaminer mte = new ModifiesTypeExaminer();
		ModifiesUnit mu = factory.createModifiesUnit();
		// represents the ModifiesType. Can be ModifiesAction but ModifiesImport, ModifiesInterface, ... as well.
		EObject eom = factory.createModifiesAction();
		
		eom = mte.examineModifiesType(typeOfChange, JavaResourceUtil.getText(ast));
		
		if (eom instanceof ModifiesAction) {
			mu.getModifiesClassMembers().add((ModifiesAction) eom);
		} else if (eom instanceof ModifiesImport) {
			mu.getModifiesImports().add((ModifiesImport) eom);
		} else if (eom instanceof ModifiesInterface) {
			mu.getModifiesInterfaces().add((ModifiesInterface)eom);
		} else if (eom instanceof ModifiesInheritance) {
			mu.setModifiesSuperclass((ModifiesInheritance) eom); 
		} else if (eom instanceof ModifiesPackage) {
			mu.setModifiesPackage((ModifiesPackage) eom);
		}
		if (mu != null) {
			parent.getDeltaActions().add(mu);
		}
	}
	
	private JavaCompilationUnit fancyJamoppToDeltaJTransformation(EObject jamoppAST) {

		log.writeToLog(this.getClass().toString() + " : Creating new JavaCompilationUnit...");
		if (jamoppAST == null) {
			return null;
		}
		String modifiedCode = JavaResourceUtil.getText(jamoppAST).replace(ChangesValidator.getTempClassName(), "");
		modifiedCode = modifiedCode.substring(0, modifiedCode.length()-1);
		JavaCompilationUnit jcu = factory.createJavaCompilationUnit();
		Sources ss = factory.createSources();
		Source s = factory.createSource();
		s.setDelta(JavaResourceUtil.getText(jamoppAST).trim());
		ss.getSources().add(s);
		jcu.setSource(ss);

		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
		return jcu;
	}
	
	/**
	 * The code for the delta is saved internally in this class until a write operation is executed.
	 * This method converts a delta into string representation.
	 * @param name
	 * 			name of the created file.
	 * @param path
	 * 			path to the created file. No trailing backslashes, get inserted automatically.
	 * @param d
	 * 			delta to write in file.
	 */
	public void addToDeltaString(Delta d, ClassChanges c, String commitComment) {

		log.writeToLog(this.getClass().toString() + " : Appending deltaString...");
		DeltaActionCreator dac = new DeltaActionCreator();
		StringBuilder delta = new StringBuilder("");

		if (!d.getName().contains("coredelta") && 
				(c.getChanges() == null || c.getChanges().equals(""))) {
			return;
		}
		if (deltaString == "") {
			//add line with commitMessage, if not empty.
			delta.append("/*\n");
			for (String line : commitComment.split("\\n")) {
				delta.append("* " + line + (commitComment != "" ? "\n" : ""));
			}
			delta.append("*/\n");
			delta.append("delta " + d.getName() + " {\n\t");
			// must have same length. Same index must correspond to delta with corresponding feature.
			splc.addDelta(d.getName());
		} 

		MemberSeparator ms = new MemberSeparator();
		LinkedList<String> memberList = null;
		if (c.getChanges() != null) {
			memberList = ms.separateMembers(c.getChanges(), c.getTypeOfChange());
		} else if (d.getDeltaActions().get(0) instanceof AddsUnit) {
		} else {
			return;
		}
		EList<DeltaAction> deltaActions = d.getDeltaActions();
		for (int i = d.getDeltaActions().size()-1; i < d.getDeltaActions().size(); i++) {
			/*
			 *  Has to be done to use the right delta actions for their designated members. 
			 *  If memberList is null (e.g. AddsUnit) 1 has to be subtracted from the size, because 
			 *  the DeltaActions then all have to be evaluated.
			 */
			if (i < d.getDeltaActions().size() - (memberList == null ? 1 : memberList.size())) {
				continue;
			}
			DeltaAction da = deltaActions.get(i);

			if (da instanceof AddsUnit) {
				AddsUnit au = (AddsUnit) da;
				for (Source s : au.getUnit().getSource().getSources()) {
					// add all classes subsequently with own "adds { " clauses.
					delta.append("adds { " + s.getDelta() + "\n");
				}
			} else if (da instanceof RemovesUnit) {
				RemovesUnit ru = (RemovesUnit) da;
				delta.append("removes " + ru.getQName() + ";\n");
			} else if (da instanceof ModifiesUnit) {
				delta.append("modifies " + c.getQualifiedClassName() + " {\n\t");
				ModifiesUnit mu = (ModifiesUnit) da;
				for (ModifiesImport mi : mu.getModifiesImports()) {
					delta.append(dac.createDeltaActionsForManyMembers(memberList, mi, c));
				}
				for (ModifiesInterface min : mu.getModifiesInterfaces()) {
					delta.append(dac.createDeltaActionsForManyMembers(memberList, min, c));
				}
				if (mu.getModifiesSuperclass() != null) {
					delta.append(dac.createDeltaActionsForManyMembers(memberList, 
							mu.getModifiesSuperclass(), c) + "\n");
				}
				for (ModifiesAction ma : mu.getModifiesClassMembers()) {
					// List of separated members is given to the delta action creator method

					delta.append(dac.createDeltaActionsForManyMembers(memberList, ma, c));
					break;
				}
				delta.append("}\n");
			} 
		}

		if (deltaString.contains(delta.toString())) {
			return;
		}
		
		String testForEmpty = delta.toString().replaceAll("(adds\\}|removes\\}|modifies\\})", "");
		delta = new StringBuilder(testForEmpty);
		testForEmpty = delta.toString().replace("modifies " + c.getQualifiedClassName() + " {\n\t", "");
		testForEmpty = testForEmpty.replaceAll("(adds|removes|modifies|\\s|\\{|\\})", "").trim();
		if (testForEmpty.length() > 1) {
			if (!deltaString.contains(delta.toString())) {
				deltaString += delta.toString();
			}
		}		
		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
	}

	/**
	 * Adds a curly closing bracket to the internal string representation of the delta.
	 * May be neccessary in some cases.
	 * @param deltaAction - if only deltaAction shall be closed.
	 */
	public void closeDeltaString(boolean deltaAction) {
		LineChecker lc = new LineChecker();
		int openingBrackets = 0,
			closingBrackets = 0;
		if (deltaString.length() > 0) {

			openingBrackets = lc.countNumberOfOccurencesInString(deltaString, "{");
			closingBrackets = lc.countNumberOfOccurencesInString(deltaString, "}");
			// if deltaAction shall be closed, subtract 1 from opening count, because delta must remain open.
			while (closingBrackets < openingBrackets - (deltaAction ? 1 : 0)) {
				deltaString += "}\n";
				closingBrackets++;
			}
			
		}
	}
	
	public String getDeltaString() {
		return deltaString;
	}
	
	/**
	 * Method for writing previously stored deltastring.
	 * @param name - name of file
	 * @param path - path to file, may not contain file.
	 */
	public void writeDeltas(String name, String path, boolean createNewFile) {

		if (createNewFile) {
			new File(path + "\\" + name).delete();	
		}
		
		log.writeToLog(this.getClass().toString() + " : Writing delta to delta-file...");
		try {
			File f = new File(path + "\\" + name);
			// without getParentFile() the path is created with the designated file as directory.
			f.getParentFile().mkdirs();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));

			out.append(deltaString);
			
			out.append("}\n");
			out.close();
			deltaString = "";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
	}

	/**
	 * Method for writing previously computed SPL-file. Deletes possibly existing file.
	 * @param name - name of file
	 * @param path - path to file, may not contain file.
	 */
	public void writeSPL(String name, String path) {
		new File(path + "\\" + name).delete();
		File f = new File(path + "\\" + name);
		splc.createSPLFile(f);
	}
}
