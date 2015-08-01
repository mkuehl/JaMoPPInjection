package deltatransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.deltaj.deltaJ.AddsClassBodyMemberDeclaration;
import org.deltaj.deltaJ.AddsMember;
import org.deltaj.deltaJ.AddsUnit;
import org.deltaj.deltaJ.Delta;
import org.deltaj.deltaJ.DeltaAction;
import org.deltaj.deltaJ.DeltaJFactory;
import org.deltaj.deltaJ.DeltaJUnit;
import org.deltaj.deltaJ.JavaCompilationUnit;
import org.deltaj.deltaJ.ModifiesAction;
import org.deltaj.deltaJ.ModifiesPackage;
import org.deltaj.deltaJ.ModifiesUnit;
import org.deltaj.deltaJ.PackageDeclaration;
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

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
			byte addRem) {
		// Trigger transformation of JaMoPP to DeltaJ AST
		JavaCompilationUnit jcu = fancyJamoppToDeltaJTransformation(jamoppParsedClass);
		CompilationUnit cu = (CompilationUnit) jamoppParsedClass;
		for (ConcreteClassifier c : cu.getClassifiers()) {
			c.getFields();
		}
		if (addRem > 0) {
			// Creating AddsUnit node.
			addJavaAddsUnit(parent, jcu);
		} else if (addRem == 0) {
			 addJavaModifiesUnit(parent, packageName, className, jcu);
		} else {
			
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
	
	private void addJavaModifiesUnit(Delta parent, String packageName, String className, JavaCompilationUnit deltaCode) {
		ModifiesUnit mu = factory.createModifiesUnit();
		ModifiesAction decl = factory.createAddsClassBodyMemberDeclaration();
//		Sources ss = factory.createSources();
//		Source s = factory.createSource();
//		System.out.println(deltaCode.toString());
//		s.setDelta(deltaCode.toString());
//		ss.getSources().add(s);
		for (Source s : deltaCode.getSource().getSources()) {
			System.out.println(s.getDelta());
		}
//		decl.setSource(deltaCode.getSource());
//		AddsMember am = factory.createAddsMember();
//		am = decl;
//		ModifiesAction ma = factory.createModifiesAction();
//		ma = am;
		ModifiesPackage mp = factory.createModifiesPackage();
		PackageDeclaration md = factory.createPackageDeclaration();
		md.setName(packageName);
		mp.setPackage(md);
		mu.setModifiesPackage(mp);
		mu.setName(className);
		mu.getModifiesClassMembers().add(decl);
		parent.getDeltaActions().add(mu);
	}
	/*
	 *  TODO create methods for modification and removal of elements. Works entirely different than AddsUnit, because
	 *  no unit is set but smaller actions for modifications have to be set. Do not know yet, how to apply removals!!
	 */
	
//	private void addJavaModifiesUnit(Delta parent, JavaCompilationUnit deltaCode) {
//		ModifiesUnit mu = factory.createModifiesUnit();
//		
//		ModifiesAction ma = factory.createModifiesAction();
//		
//		mu.getModifiesClassMembers().add(ma);
//		// Adding the child node to the adds unit
////		mu.setUnit(deltaCode);
//
//		// Adding the adds unit node as child to the delta node
//		parent.getDeltaActions().add(mu);
//
//		// Nothing will be returned because we don't need to add anything here.
//	}
//	
//	private void addJavaRemovesUnit(Delta parent, JavaCompilationUnit deltaCode) {
//		RemovesUnit ru = factory.createRemovesUnit();
//		factory.createrem
//		// Adding the child node to the adds unit
//		ru.setUnit(deltaCode);
//
//		// Adding the adds unit node as child to the delta node
//		parent.getDeltaActions().add(ru);
//
//		// Nothing will be returned because we don't need to add anything here.
//	}
	
	private JavaCompilationUnit fancyJamoppToDeltaJTransformation(EObject jamoppAST) {
		if (jamoppAST == null) {
			System.out.println("...............AST IST NULL..................");
			return null;
		}
		System.out.println("Given AST: " + JavaResourceUtil.getText(jamoppAST) + " END Given AST");
		CompilationUnit cu = (CompilationUnit) jamoppAST;
		System.out.println("CU: " + cu.toString() + " END CU");
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
		s.setDelta(JavaResourceUtil.getText(jamoppAST).trim());
		ss.getSources().add(s);
		jcu.setSource(ss);
		for (Source ts : jcu.getSource().getSources()) {
			System.out.println("Delta: " + ts.getDelta());
		}
		
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
	public void createDeltaFile(String name, String path, Delta d) {
		try {
			File f = new File(path + "\\" + name + ".deltaj");
			// without getParentFile() the path is created with the designated file as directory.
			f.getParentFile().mkdirs();
//			System.setProperty("user.dir", path);
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			String change = "";
			out.append("delta " + d.getName() + " {\n");
			for (DeltaAction da : d.getDeltaActions()) {
				if (da instanceof AddsUnit) {
					out.write("adds {\n");
					AddsUnit au = (AddsUnit) da;
					for (Source s : au.getUnit().getSource().getSources()) {
						change = s.getDelta();
					}
				} else if (da instanceof ModifiesUnit) {
					out.write("modifies {\n");
					ModifiesUnit mu = (ModifiesUnit) da;
					for (ModifiesAction ma : mu.getModifiesClassMembers()) {
						change += ma.toString();
					}
				} else {
//					RemovesUnit ru = (RemovesUnit) da;
//					for (RemovesAction ra : ru.)
				}
				out.append(change);
			}
			out.append("}\n}\n");
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
