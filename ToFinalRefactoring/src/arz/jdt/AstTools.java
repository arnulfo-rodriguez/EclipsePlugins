package arz.jdt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.SourceField;

public class AstTools {

	public static CompilationUnit ParseToJavaAst(IProgressMonitor monitor,
			ICompilationUnit compilationUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return (CompilationUnit) parser.createAST(monitor);
	}

	public static SourceField getSelectedField(Object[] elements,
			ICompilationUnit compilationUnit) {
		SourceField vField = null;
		if (elements.length == 1 && elements[0] instanceof SourceField) {
			vField = (SourceField) elements[0];
		}
		return vField;
	}

	public static VariableDeclarationFragment getDeclarationFragmentByName(
			FieldDeclaration fieldDeclaration, String elementName) {
		VariableDeclarationFragment result = null;
		for (Object decl : fieldDeclaration.fragments()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) decl;
			if (fragment.getName().resolveBinding().equals(elementName)) {
				result = fragment;
				break;
			}
		}
		return result;
	}

	public static boolean fieldDeclarationCanBeFinal(IField field)
			throws JavaModelException {
		return field != null && field.exists() && field.isStructureKnown()
				&& !field.getDeclaringType().isAnnotation()
				&& Flags.isPrivate(field.getFlags())
				&& !Flags.isFinal(field.getFlags());
	}

}
