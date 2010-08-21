package arz.refactorings;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ToFinalRefactoring extends Refactoring {

	private ICompilationUnit compilationUnit;
	private SourceField fField;
	private VariableDeclarationFragment fFragment;
	private CompilationUnit javaAST;
	private FieldDeclaration fFieldDeclaration;

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			pm.beginTask("checkInititalConditions", 0);
			if (fField != null) {
				javaAST = ParseToJavaAst(pm);
				fFieldDeclaration = (FieldDeclaration) fField.findNode(javaAST);
				fFragment = getDeclaration(fFieldDeclaration);
				AssignmentsFinder finder = new AssignmentsFinder(
						(IVariableBinding) fFragment.getName().resolveBinding());
				javaAST.accept(finder);
				if (!finder.canVariableBeFinal()) {
					status.addFatalError("This field is not initialized inline or has other assignments");
				}
			}
		} finally {
			pm.done();
		}
		return status;
	}

	private VariableDeclarationFragment getDeclaration(
			FieldDeclaration fieldDeclaration) {
		VariableDeclarationFragment result = null;
		for (Object decl : fieldDeclaration.fragments()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) decl;
			if (fragment.getName().getIdentifier()
					.equals(fField.getElementName())) {
				result = fragment;
				break;
			}
		}
		return result;
	}

	private CompilationUnit ParseToJavaAst(IProgressMonitor monitor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return (CompilationUnit) parser.createAST(monitor);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		// TODO Auto-generated method stub
		ASTRewrite astRewrite = ASTRewrite.create(javaAST.getAST());
		CompilationUnitChange result = new CompilationUnitChange(
				"Make field Final",
				compilationUnit);
		MultiTextEdit root = new MultiTextEdit();
		result.setEdit(root);
		if (isThereOnlyOneDeclarationInTheLine()) {
			addFinalModifierToDeclaration(astRewrite, root);
		} else {
			splitMultipleDeclaration(astRewrite, root);
		}
		root.addChild(astRewrite.rewriteAST());
		return result;
	}

	abstract class FieldDeclarationChanger {
		public void applyEdition(ASTRewrite astRewrite, MultiTextEdit root)
				throws JavaModelException {
			FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode
					.copySubtree(javaAST.getAST(), fFieldDeclaration);
			editFieldDeclaration(newFieldDeclaration);
			astRewrite.remove(fFieldDeclaration, null);
			astRewrite.replace(fFieldDeclaration, newFieldDeclaration, null);
		}

		protected abstract void editFieldDeclaration(
				FieldDeclaration fieldDeclarationCopy);
	}

	private void addFinalModifierToDeclaration(ASTRewrite astRewrite,
			MultiTextEdit root) throws JavaModelException {
		new FieldDeclarationChanger() {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration newFieldDeclaration) {
				newFieldDeclaration.modifiers().add(
						javaAST.getAST().newModifier(
								ModifierKeyword.FINAL_KEYWORD));
			}
		}.applyEdition(astRewrite, root);

	}

	private void splitMultipleDeclaration(ASTRewrite astRewrite,
			MultiTextEdit root) throws JavaModelException {
		createNewFieldDeclaration(astRewrite);
		new FieldDeclarationChanger() {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration fieldDeclarationCopy) {
				int index = fFieldDeclaration.fragments().indexOf(fFragment);
				fieldDeclarationCopy.fragments().remove(index);
			}
		}.applyEdition(astRewrite, root);
	}

	private void createNewFieldDeclaration(ASTRewrite astRewrite) {
		VariableDeclarationFragment variableDeclarationFragmentCopy = (VariableDeclarationFragment) ASTNode
				.copySubtree(javaAST.getAST(), fFragment);
		FieldDeclaration newFieldDeclaration = javaAST.getAST()
				.newFieldDeclaration(variableDeclarationFragmentCopy);
		newFieldDeclaration.setType((Type) ASTNode.copySubtree(
				javaAST.getAST(), fFieldDeclaration.getType()));
		newFieldDeclaration.modifiers().addAll(
				ASTNode.copySubtrees(javaAST.getAST(),
						fFieldDeclaration.modifiers()));
		newFieldDeclaration.modifiers().add(
				javaAST.getAST().newModifier(ModifierKeyword.FINAL_KEYWORD));
		ListRewrite listRewrite = astRewrite.getListRewrite(
				fFieldDeclaration.getParent(),
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAfter(newFieldDeclaration, fFieldDeclaration, null);
	}

	private boolean isThereOnlyOneDeclarationInTheLine() {
		return fFieldDeclaration.fragments().size() == 1;
	}

	@Override
	public String getName() {
		return "To Final";
	}

	public void setField(SourceField field) {
		this.fField = field;
	}

	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public RefactoringStatus initialize(Map arguments) {
		return null;
	}

}