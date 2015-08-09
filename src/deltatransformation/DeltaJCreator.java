package deltatransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.deltaj.deltaJ.AddsClassBodyMemberDeclaration;
import org.deltaj.deltaJ.AddsEnumConstant;
import org.deltaj.deltaJ.AddsImport;
import org.deltaj.deltaJ.AddsInterfacesList;
import org.deltaj.deltaJ.AddsMember;
import org.deltaj.deltaJ.AddsMemberDeclaration;
import org.deltaj.deltaJ.AddsSuperclass;
import org.deltaj.deltaJ.AddsUnit;
import org.deltaj.deltaJ.Delta;
import org.deltaj.deltaJ.DeltaAction;
import org.deltaj.deltaJ.DeltaJFactory;
import org.deltaj.deltaJ.DeltaJUnit;
import org.deltaj.deltaJ.JavaCompilationUnit;
import org.deltaj.deltaJ.ModifiesAction;
import org.deltaj.deltaJ.ModifiesUnit;
import org.deltaj.deltaJ.RemovesField;
import org.deltaj.deltaJ.RemovesImport;
import org.deltaj.deltaJ.RemovesInterfacesList;
import org.deltaj.deltaJ.RemovesMethod;
import org.deltaj.deltaJ.RemovesSuperclass;
import org.deltaj.deltaJ.RemovesUnit;
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffs.Change;

import com.max.jamoppinjection.ChangesValidator;

public class DeltaJCreator {

	/**
	 * Reference to the singleton instance of the DeltaJFactory. It is used to
	 * create AST nodes.
	 */
	private DeltaJFactory factory = DeltaJFactory.eINSTANCE;

	public DeltaJCreator() {
		
	}
	
	public DeltaJUnit createDeltaJUnit() {
		return factory.createDeltaJUnit();
	}
	
	/**
	 * Creating a new Delta. This delta is a node of the AST which represents a
	 * delta module in source code. The deltas name is set and the delta is to
	 * its corresponding parent node, a DeltaJUnit.
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
		// with is after it
		// is correctly added to the AST.
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
			byte addRem/* DeltaAction da*/) {
		// Trigger transformation of JaMoPP to DeltaJ AST
		JavaCompilationUnit jcu = fancyJamoppToDeltaJTransformation(jamoppParsedClass);
		CompilationUnit cu = (CompilationUnit) jamoppParsedClass;
		for (ConcreteClassifier c : cu.getClassifiers()) {
			c.getFields();
		}
//		if (da instanceof AddsUnit) {
//			// Creating AddsUnit node.
//			addJavaAddsUnit(parent, jcu);
//		} else if (da instanceof ModifiesUnit) {
//			for (ModifiesAction ma : ((ModifiesUnit) da).getModifiesClassMembers()) {
//				addJavaModifiesUnit(parent, packageName, className, ma, jcu);
//			}
//		} else {
//			
//		}
		if (addRem > 0) {
			addJavaAddsUnit(parent, jcu);
		} else if (addRem == 0) {
			addJavaModifiesUnit(parent, packageName, className, jcu);
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
	
	private void addJavaModifiesUnit(Delta parent, String packageName, String className, /*DeltaAction da, /*ModifiesAction ma, */JavaCompilationUnit deltaCode) {
		ModifiesUnit mu = factory.createModifiesUnit();
		AddsClassBodyMemberDeclaration decl = factory.createAddsClassBodyMemberDeclaration();
//		if (ma instanceof AddsClassBodyMemberDeclaration) {
////			AddsClassBodyMemberDeclaration decl = factory.createAddsClassBodyMemberDeclaration();
//			((AddsClassBodyMemberDeclaration) ma).setSource(deltaCode.getSource());
//		} else if (ma instanceof RemovesMethod) {
//			((RemovesMethod) ma).setName(getMethodNameFromCode(deltaCode.getSource().getSources().get(0).getDelta()));
//		}
//		decl.setSource(deltaCode.getSource());
		mu.getModifiesClassMembers().add(decl);
		parent.getDeltaActions().add(mu);
	}
	
//	/**
//	 * Returns the name of the method in the given code string. If no method can be found,
//	 * the empty string is returned.
//	 * @param code
//	 * @return
//	 */
//	private String getMethodNameFromCode(String code) {
//		if (code.contains("("))
//		if (code.startsWith("private")) {
//			code = code.substring(8);
//		} else if (code.startsWith("protected")) {
//			code = code.substring(10);
//		} else if (code.startsWith("public")) {
//			code = code.substring(7);
//		}
//		code.substring(code.indexOf(" ")+1, code.indexOf("("));
//		return code;
//	}
	/*
	 *  TODO create methods for modification and removal of elements. Works entirely different than AddsUnit, because
	 *  no unit is set but smaller actions for modifications have to be set. Do not know yet, how to apply removals!!
	 */
	
	private JavaCompilationUnit fancyJamoppToDeltaJTransformation(EObject jamoppAST) {
		if (jamoppAST == null) {
//			System.out.println("...............AST IST NULL..................");
			return null;
		}
		String modifiedCode = JavaResourceUtil.getText(jamoppAST).replace(ChangesValidator.getTempClassName(), "");
		modifiedCode = modifiedCode.substring(0, modifiedCode.length()-1);
//		System.out.println("Given AST: " + JavaResourceUtil.getText(jamoppAST) + " END Given AST");
//		CompilationUnit cu = (CompilationUnit) jamoppAST;
//		System.out.println("CU: " + cu.toString() + " END CU");
//		for (String ns : cu.getNamespaces()) {
//			System.out.println("Namespace: " + ns);
//		}
//		for (Import i : cu.getImports()) {
//			for (ConcreteClassifier c : i.getImportedClassifiers()) {
//				System.out.println(c.getName());
//			}
//		}
//		for (ConcreteClassifier c : cu.getClassifiers()) {
//			EList<Field> fields = c.getFields();
//			EList<Method> methods = c.getMethods();
//			for (Field f : fields) {
//				System.out.println("Field: " + f.getName());
//			}
//			for (Method m : methods) {
//				System.out.println("Method: " + m.getName());
//				for (Parameter p : m.getParameters()) {
//					System.out.println("Param: " + p.getClass().getSimpleName() + " " + p.getName());
//				}
//			}
//		}
		JavaCompilationUnit jcu = factory.createJavaCompilationUnit();
		Sources ss = factory.createSources();
		Source s = factory.createSource();
//		System.out.println("TRIMMED AST: ");
//		System.out.println(JavaResourceUtil.getText(jamoppAST).trim());
//		System.out.println("END OF TRIMMED AST");
		s.setDelta(JavaResourceUtil.getText(jamoppAST).trim());
		ss.getSources().add(s);
		jcu.setSource(ss);
//		for (Source ts : jcu.getSource().getSources()) {
//			System.out.println("Delta: " + ts.getDelta());
//		}
		
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
	public void createDeltaFile(String name, String path, Delta d, Change c) {
		try {
			File f = new File(path + "\\" + name + ".deltaj");
			// without getParentFile() the path is created with the designated file as directory.
			f.getParentFile().mkdirs();
//			System.setProperty("user.dir", path);
			StringBuilder affectedMembers = null;
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			String change = "";
			out.append("delta " + d.getName() + " {\n");
			for (DeltaAction da : d.getDeltaActions()) {
				if (da instanceof AddsUnit) {
//					out.write("adds {\n");
					AddsUnit au = (AddsUnit) da;
					for (Source s : au.getUnit().getSource().getSources()) {
						change = s.getDelta();
					}
				} else if (da instanceof ModifiesUnit) {
					out.write("modifies " + c.getQualifiedClassName() + " {\n");
					ModifiesUnit mu = (ModifiesUnit) da;
					for (ModifiesAction ma : mu.getModifiesClassMembers()) {
						affectedMembers = new StringBuilder();
						MemberSeparator ms = new MemberSeparator();
						switch (typeOfChange(ma)) {
						case 1:
							for (String s : ms.separateMembers(c.getChanges())) {
								affectedMembers.append("adds " + s.trim() + "\n");									
							}
							break;
						case 0:
							for (String s : ms.separateMembers(c.getChanges())) {
								affectedMembers.append("modifies " + s.trim() + "\n");									
							}
						case -1:
							for (String s : ms.separateMembers(c.getChanges())) {
								affectedMembers.append("removes " + s.trim() + "\n");									
							}
						}
						if (ma instanceof AddsClassBodyMemberDeclaration) {
//							out.write("adds ");
//							String changes = JavaResourceUtil.getText(eo);
//							if (changes.startsWith("class")) {
//								changes = JavaResourceUtil.getText(eo).replace(ChangesValidator.getTempClassName(), "");
//								changes = changes.substring(0, changes.length()-1);
//							}
//								System.out.println("EO : " + c.getChanges());
								
						} else if (ma instanceof RemovesField) {
							for (String s : ms.separateMembers(c.getChanges())) {
								affectedMembers.append("removes " + s + "\n");
							}
						}
					}
					if (affectedMembers != null) {
						out.write(affectedMembers.toString());
					}
				} else {
//					RemovesUnit ru = (RemovesUnit) da;
//					for (RemovesAction ra : ru.)
				}
				out.append(change);
				
			}
			out.append("\n}\n}\n");
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks type of change. If ma removes something, -1 is returned, if it adds something, 
	 * 1 is returned, otherwise 0 is returned (for modifies).
	 * @param ma
	 * @return
	 */
	private int typeOfChange(ModifiesAction ma) {
		if (ma instanceof RemovesField || ma instanceof RemovesImport ||
				ma instanceof RemovesInterfacesList || ma instanceof RemovesMethod ||
				ma instanceof RemovesSuperclass || ma instanceof RemovesUnit) {
			return -1;
		} else if (ma instanceof AddsClassBodyMemberDeclaration || ma instanceof AddsImport ||
				ma instanceof AddsInterfacesList || ma instanceof AddsMember ||
				ma instanceof AddsMemberDeclaration || ma instanceof AddsEnumConstant ||
				ma instanceof AddsSuperclass) {
			return 1;
		} else {
			return 0;
		}
	}
}
