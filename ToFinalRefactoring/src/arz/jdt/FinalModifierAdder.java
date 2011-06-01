package arz.jdt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextChange;

public class FinalModifierAdder {

	private AST fAst;
	private FieldDeclaration fFieldDeclaration;
	private VariableDeclarationFragment fFragment;
	private ICompilationUnit fCompilationUnit;
	
	
	public FinalModifierAdder(AST ast,ICompilationUnit compilationUnit,VariableDeclarationFragment fragment) {
		this.fAst = ast;
		this.fCompilationUnit = compilationUnit;
		this.fFragment = fragment;
		this.fFieldDeclaration = (FieldDeclaration) fFragment.getParent();
	}
	
	

	public TextChange addFinal() throws CoreException, OperationCanceledException {
		
		ASTRewrite astRewrite = ASTRewrite.create(fAst);
		if (isThereOnlyOneDeclarationInTheLine()) {
			addFinalModifierToDeclaration(astRewrite);
		} else {
			splitMultipleDeclaration(astRewrite);
		}
		return createChangeFromAstRewrite(astRewrite);
	}

	private void addFinalModifierToDeclaration(ASTRewrite astRewrite)
			throws JavaModelException {
		new FieldDeclarationChanger(fFieldDeclaration, fAst) {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration newFieldDeclaration) {
				newFieldDeclaration.modifiers().add(
						fAst.newModifier(ModifierKeyword.FINAL_KEYWORD));
			}
		}.applyEdition(astRewrite);
	}

	private boolean isThereOnlyOneDeclarationInTheLine() {
		return fFieldDeclaration.fragments().size() == 1;
	}

	private void splitMultipleDeclaration(ASTRewrite astRewrite)
			throws JavaModelException {
		createNewFieldDeclaration(astRewrite);
		new FieldDeclarationChanger(fFieldDeclaration, fAst) {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration fieldDeclarationCopy) {
				int index = fFieldDeclaration.fragments().indexOf(fFragment);
				fieldDeclarationCopy.fragments().remove(index);
			}
		}.applyEdition(astRewrite);
	}

	private void createNewFieldDeclaration(ASTRewrite astRewrite) {
		VariableDeclarationFragment variableDeclarationFragmentCopy = (VariableDeclarationFragment) ASTNode
				.copySubtree(fAst, fFragment);
		FieldDeclaration newFieldDeclaration = fAst
				.newFieldDeclaration(variableDeclarationFragmentCopy);
		newFieldDeclaration.setType((Type) ASTNode.copySubtree(fAst,
				fFieldDeclaration.getType()));
		newFieldDeclaration.modifiers().addAll(
				ASTNode.copySubtrees(fAst, fFieldDeclaration.modifiers()));
		newFieldDeclaration.modifiers().add(
				fAst.newModifier(ModifierKeyword.FINAL_KEYWORD));
		ListRewrite listRewrite = astRewrite.getListRewrite(
				fFieldDeclaration.getParent(),
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAfter(newFieldDeclaration, fFieldDeclaration, null);
	}

	private TextChange createChangeFromAstRewrite(ASTRewrite astRewrite)
			throws JavaModelException {
		CompilationUnitChange compilationUnitChange = new CompilationUnitChange(
				"Make field Final", fCompilationUnit);
		compilationUnitChange.setEdit(astRewrite.rewriteAST());
		return compilationUnitChange;
	}

}
