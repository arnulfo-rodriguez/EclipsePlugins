package arz.refactorings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

@SuppressWarnings("restriction")
public class ToFinalAction implements IWorkbenchWindowActionDelegate {

	SourceField fField;
	ICompilationUnit compilationUnit;

	
	private IWorkbenchWindow fWindow;
	public void dispose() {
		// Do nothing
	}
	
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (fField != null && compilationUnit != null) {
			ToFinalRefactoring refactoring= new ToFinalRefactoring();
			refactoring.setField((SourceField) fField);
			refactoring.setCompilationUnit(compilationUnit);
			run(new ToFinalWizard(refactoring), fWindow.getShell(), "String to final");
		}
	}
	
	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

    //I don't like having to use this method, i'd  rather make this work
	//like the built in refactorings.
	private ICompilationUnit CompilationUnitForCurrentEditor(){
		IEditorPart editorSite = fWindow.getActivePage().getActiveEditor();
		IEditorInput editorInput = editorSite.getEditorInput();
		IResource resource = (IResource)editorInput.getAdapter(IResource.class);
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom((IFile)resource);
		return icu;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		fField= null;
		compilationUnit = CompilationUnitForCurrentEditor();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection extended= (IStructuredSelection) selection;
			Object[] elements= extended.toArray();
			fField = getSelectedField(elements,compilationUnit);
		}else if (selection instanceof ITextSelection){
			try {
				IJavaElement[] elements = SelectionConverter.codeResolve((IJavaElement) compilationUnit,(ITextSelection)selection);
				 fField = getSelectedField(elements,compilationUnit); 
			} catch (JavaModelException e) {
			}
		}
		
		try {
			action.setEnabled(fField != null && 
					fField.exists() && 
					fField.isStructureKnown() && 
					!fField.getDeclaringType().isAnnotation() &&
					Flags.isPrivate(fField.getFlags()) &&
					!Flags.isFinal(fField.getFlags())
					);
		} catch (JavaModelException exception) {
			action.setEnabled(false);
		}
	}

	private SourceField getSelectedField(Object[] elements,ICompilationUnit compilationUnit) {
		SourceField vField = null;
		if (elements.length == 1 && elements[0] instanceof SourceField) {
			vField= (SourceField) elements[0];
		}
		return vField;
	}
	
	

}
