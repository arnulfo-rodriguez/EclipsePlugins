package arz.refactorings;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ToFinalDescriptor extends RefactoringDescriptor {

	public static final String REFACTORING_ID= "arz.refactorings.string.to.final";
    
	@SuppressWarnings("unchecked")
	private final Map fArguments;

	@SuppressWarnings("unchecked")
	public ToFinalDescriptor(String project, String description, String comment, Map arguments) {
		super(REFACTORING_ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		fArguments= arguments;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ToFinalRefactoring refactoring= new ToFinalRefactoring();
		status.merge(refactoring.initialize(fArguments));
		return refactoring;
	}

	@SuppressWarnings("unchecked")
	public Map getArguments() {
		return fArguments;
	}


}
