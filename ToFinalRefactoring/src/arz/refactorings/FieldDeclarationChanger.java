package arz.refactorings;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.MultiTextEdit;

abstract class FieldDeclarationChanger {
	private FieldDeclaration fFieldDeclaration;
    private AST fAst;
	public FieldDeclarationChanger(FieldDeclaration fieldDeclaration,AST ast)
	{
		this.fFieldDeclaration = fieldDeclaration;
		this.fAst = ast;
	}
	public void applyEdition(ASTRewrite astRewrite, MultiTextEdit root)
			throws JavaModelException {
		FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode
				.copySubtree(fAst, fFieldDeclaration);
		editFieldDeclaration(newFieldDeclaration);
		astRewrite.remove(fFieldDeclaration, null);
		astRewrite.replace(fFieldDeclaration, newFieldDeclaration, null);
	}

	protected abstract void editFieldDeclaration(
			FieldDeclaration fieldDeclarationCopy);
}