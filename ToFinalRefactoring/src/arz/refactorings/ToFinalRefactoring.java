package arz.refactorings;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;

public class ToFinalRefactoring extends Refactoring {

	private static final String CANNOT_REFACTOR_FIELD = "This field is not initialized inline or has other assignments";
	private static final String NOT_A_FIELD = "The selection is not valid for this refactoring";
	private ICompilationUnit fCompilationUnit;
	private SourceField fField;
	private FieldDeclaration fFieldDeclaration;
	private VariableDeclarationFragment fFragment;
	private CompilationUnit fJavaAST;

	private void addFinalModifierToDeclaration(ASTRewrite astRewrite)
			throws JavaModelException {
		new FieldDeclarationChanger(fFieldDeclaration, fJavaAST.getAST()) {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration newFieldDeclaration) {
				newFieldDeclaration.modifiers().add(
						fJavaAST.getAST().newModifier(
								ModifierKeyword.FINAL_KEYWORD));
			}
		}.applyEdition(astRewrite);

	}

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
			if (doesFieldMatchesInitialConditions()) {
				fJavaAST = AstTools.ParseToJavaAst(pm, fCompilationUnit);
				fFieldDeclaration = (FieldDeclaration) fField
						.findNode(fJavaAST);
				fFragment = getDeclaration(fFieldDeclaration);
				AssignmentsFinder finder = new AssignmentsFinder(
						(IVariableBinding) fFragment.getName().resolveBinding());
				fJavaAST.accept(finder);
				if (!finder.canVariableBeFinal()) {
					status.addFatalError(CANNOT_REFACTOR_FIELD);
				}
			} else {
				status.addFatalError(NOT_A_FIELD);
			}
		} catch (JavaModelException exception) {
			status.addFatalError(NOT_A_FIELD);
		} finally {
			pm.done();
		}
		return status;
	}

	private boolean doesFieldMatchesInitialConditions()
			throws JavaModelException {
		return fField != null && fField.exists() && fField.isStructureKnown()
				&& !fField.getDeclaringType().isAnnotation()
				&& Flags.isPrivate(fField.getFlags())
				&& !Flags.isFinal(fField.getFlags());
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		ASTRewrite astRewrite = ASTRewrite.create(fJavaAST.getAST());
		if (isThereOnlyOneDeclarationInTheLine()) {
			addFinalModifierToDeclaration(astRewrite);
		} else {
			splitMultipleDeclaration(astRewrite);
		}
		return createChangeFromAstRewrite(astRewrite);
	}

	private Change createChangeFromAstRewrite(ASTRewrite astRewrite)
			throws JavaModelException {
		MultiTextEdit edit = new MultiTextEdit();
		edit.addChild(astRewrite.rewriteAST());
		CompilationUnitChange compilationUnitChange = new CompilationUnitChange(
				"Make field Final", fCompilationUnit);
		compilationUnitChange.setEdit(edit);
		return compilationUnitChange;
	}

	private void createNewFieldDeclaration(ASTRewrite astRewrite) {
		VariableDeclarationFragment variableDeclarationFragmentCopy = (VariableDeclarationFragment) ASTNode
				.copySubtree(fJavaAST.getAST(), fFragment);
		FieldDeclaration newFieldDeclaration = fJavaAST.getAST()
				.newFieldDeclaration(variableDeclarationFragmentCopy);
		newFieldDeclaration.setType((Type) ASTNode.copySubtree(
				fJavaAST.getAST(), fFieldDeclaration.getType()));
		newFieldDeclaration.modifiers().addAll(
				ASTNode.copySubtrees(fJavaAST.getAST(),
						fFieldDeclaration.modifiers()));
		newFieldDeclaration.modifiers().add(
				fJavaAST.getAST().newModifier(ModifierKeyword.FINAL_KEYWORD));
		ListRewrite listRewrite = astRewrite.getListRewrite(
				fFieldDeclaration.getParent(),
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAfter(newFieldDeclaration, fFieldDeclaration, null);
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

	@Override
	public String getName() {
		return "To Final";
	}

	public RefactoringStatus initialize(Map arguments) {
		// TODO: implement this when I find a use case.
		return null;
	}

	private boolean isThereOnlyOneDeclarationInTheLine() {
		return fFieldDeclaration.fragments().size() == 1;
	}

	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.fCompilationUnit = compilationUnit;
	}

	public void setField(SourceField field) {
		this.fField = field;
	}

	private void splitMultipleDeclaration(ASTRewrite astRewrite)
			throws JavaModelException {
		createNewFieldDeclaration(astRewrite);
		new FieldDeclarationChanger(fFieldDeclaration, fJavaAST.getAST()) {
			@Override
			public void editFieldDeclaration(
					FieldDeclaration fieldDeclarationCopy) {
				int index = fFieldDeclaration.fragments().indexOf(fFragment);
				fieldDeclarationCopy.fragments().remove(index);
			}
		}.applyEdition(astRewrite);
	}

}