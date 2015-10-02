package deltatransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

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
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffpreprocessor.ModificationType;
import preprocessing.diffs.Change;

import com.max.jamoppinjection.ChangesValidator;

public class DeltaJCreator {

	/**
	 * Reference to the singleton instance of the DeltaJFactory. It is used to
	 * create AST nodes.
	 */
	private DeltaJFactory factory = DeltaJFactory.eINSTANCE;
	
	private String deltaString = "";

	public DeltaJCreator() {
		
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
		return d;
	}
	
	/**
	 * Creates a DeltaAction of the specified type. The parameter string has the following semantic:
	 * a = AddsUnit, m = ModifiesUnit, r = RemovesUnit;
	 * ModifiesUnits have to be further distinguished followingg this semantics:
	 * acbmd = AddsClassBodyMemberDeclaration, ai = AddsImport, ail = AddsInterfacesList,
	 * am = AddsMember, amd = AddsMemberDeclaration, aec = AddsEnumConstant, asc = AddsSuperclass,
	 * rf = RemovesField, ri = RemovesImport, ril = RemovesInterfacesList, rm = RemovesMethod,
	 * rsc = RemovesSuperclass, 
	 * Example for type: mrm = modifies->removes method
	 * @param type
	 * @return
	 */
	public DeltaAction createDeltaAction(String type) {
		switch (type) {
		case "a":
			return factory.createAddsUnit();
		case "r":
			return factory.createRemovesUnit();
		default:

			ModifiesUnit mu = factory.createModifiesUnit();
			switch (type.substring(1)) {
			case "acbmd":
				mu.getModifiesClassMembers().add(
						factory.createAddsClassBodyMemberDeclaration());
				break;
			case "rm":
				mu.getModifiesClassMembers().add(factory.createRemovesMethod());
				break;
			}
			return mu;
		}
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
		if (typeOfChange == ModificationType.CLASSADDITION) {
			// Trigger transformation of JaMoPP to DeltaJ AST
			JavaCompilationUnit jcu = fancyJamoppToDeltaJTransformation(jamoppParsedClass);
			addJavaAddsUnit(parent, jcu);
		} else if (typeOfChange != ModificationType.CLASSADDITION && typeOfChange != ModificationType.CLASSREMOVAL) {
			addJavaModifiesUnit(parent, typeOfChange, jamoppParsedClass);
		}
	}
	
	private void addJavaAddsUnit(Delta parent, JavaCompilationUnit deltaCode) {
		AddsUnit au = factory.createAddsUnit();
		
		// Adding the child node to the adds unit
		au.setUnit(deltaCode);
		
		// Adding the adds unit node as child to the delta node
		parent.getDeltaActions().add(au);

		// Nothing will be returned because we don't need to add anything here.
	}
	
	private void addJavaModifiesUnit(Delta parent, ModificationType typeOfChange, EObject ast) {
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
	
	/*
	 *  TODO create methods for modification and removal of elements. Works entirely different than AddsUnit, because
	 *  no unit is set but smaller actions for modifications have to be set. Do not know yet, how to apply removals!!
	 */
	
	private JavaCompilationUnit fancyJamoppToDeltaJTransformation(EObject jamoppAST) {
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
		
		return jcu;
	}
	
	/**
	 * Creates a .deltaj-file. 
	 * @param name
	 * 			name of the created file.
	 * @param path
	 * 			path to the created file. No trailing backslashes, get inserted automatically.
	 * @param d
	 * 			delta to write in file.
	 */
	public void addToDeltaString(Delta d, Change c) {
		DeltaActionCreator dac = new DeltaActionCreator();
		StringBuilder delta = new StringBuilder("");


		if (!d.getName().contains("coredelta") && 
				(c.getChanges() == null || c.getChanges().equals(""))) {
			return;
		}
		if (deltaString == "") {
			delta.append("delta " + d.getName() + " {\n\t");
		} 
		MemberSeparator ms = new MemberSeparator();
		LinkedList<String> memberList = null;
		if (c.getChanges() != null) {
			memberList = ms.separateMembers(c.getChanges(), c.getTypeOfChange());
		}
		EList<DeltaAction> deltaActions = d.getDeltaActions();
		for (int i = 0; i < d.getDeltaActions().size(); i++) {
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
					delta.append("adds { " + s.getDelta() + "\n");
				}
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
							mu.getModifiesSuperclass(), c));
				}
				for (ModifiesAction ma : mu.getModifiesClassMembers()) {
					// List of separated members is given to the delta action creator method

					delta.append(dac.createDeltaActionsForManyMembers(memberList, ma, c));
				}
				delta.append("}\n");
			} else {
				//					RemovesUnit ru = (RemovesUnit) da;
				//					for (RemovesAction ra : ru.)
			}
		}

		deltaString += delta.toString();
	}

	public void closeDeltaString() {
		if (deltaString.length() > 0) {
			deltaString += "}\n";
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
	public void write(String name, String path) {
		try {
			File f = new File(path + "\\" + name + ".deltaj");
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
	}

}
