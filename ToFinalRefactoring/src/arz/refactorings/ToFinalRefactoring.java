package arz.refactorings;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import arz.jdt.AstTools;
import arz.jdt.FinalModifierAdder;
import arz.jdt.MakeFieldFinalDetector;

public class ToFinalRefactoring extends Refactoring {

	private static final String CANNOT_REFACTOR_FIELD = "This field is not initialized inline or has other assignments";
	private static final String NOT_A_FIELD = "The selection is not valid for this refactoring";
	private ICompilationUnit fCompilationUnit;
	private SourceField fField;
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
			if (fField != null) {
				fJavaAST = AstTools.ParseToJavaAst(pm, fCompilationUnit);
				FieldDeclaration fFieldDeclaration = (FieldDeclaration) fField
						.findNode(fJavaAST);
				fFragment = AstTools.getDeclarationFragmentByName(
						fFieldDeclaration, fField.getElementName());

				if (!MakeFieldFinalDetector.detect((IVariableBinding) fFragment
						.getName().resolveBinding(), fJavaAST)) {
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
		return new FinalModifierAdder(fJavaAST.getAST(), fCompilationUnit,
				fFragment).addFinal();
	}

	@Override
	public String getName() {
		return "To Final";
	}

	public RefactoringStatus initialize(Map arguments) {
		return null;
	}

	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.fCompilationUnit = compilationUnit;
	}

	public void setField(SourceField field) {
		this.fField = field;
	}

}