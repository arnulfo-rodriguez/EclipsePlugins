package arz.refactorings;

import java.util.List;
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
import org.eclipse.jdt.core.dom.Modifier;
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
import arz.jdt.AssignmentsFinder;
import arz.jdt.AstTools;
import arz.jdt.FieldDeclarationChanger;
import arz.jdt.FinalModifierAdder;

public class ToFinalRefactoring extends Refactoring {

	private static final String CANNOT_REFACTOR_FIELD = "This field is not initialized inline or has other assignments";
	private static final String NOT_A_FIELD = "The selection is not valid for this refactoring";
	private ICompilationUnit fCompilationUnit;
	private SourceField fField;
	private FieldDeclaration fFieldDeclaration;
	private VariableDeclarationFragment fFragment;
	private CompilationUnit fJavaAST;



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
			if (AstTools.fieldDeclarationCanBeFinal(fField)) {
				fJavaAST = AstTools.ParseToJavaAst(pm, fCompilationUnit);
				fFieldDeclaration = (FieldDeclaration) fField.findNode(fJavaAST);
				fFragment = AstTools.getDeclarationFragmentByName(fFieldDeclaration, fField.getElementName());
				boolean canVariableBeFinal = AssignmentsFinder.analyze(
						(IVariableBinding) fFragment.getName().resolveBinding(), fJavaAST).canVariableBeFinal();
				if (!canVariableBeFinal) {
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




	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		 return new FinalModifierAdder(fJavaAST.getAST(), fCompilationUnit, fFragment).addFinal();
	}





	@Override
	public String getName() {
		return "To Final";
	}

	public RefactoringStatus initialize(Map arguments) {
		// TODO: implement this when I find a use case.
		return null;
	}



	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.fCompilationUnit = compilationUnit;
	}

	public void setField(SourceField field) {
		this.fField = field;
	}



}