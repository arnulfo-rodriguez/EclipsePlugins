package arz.refactorings;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ToFinalWizard extends RefactoringWizard {

   public ToFinalWizard(ToFinalRefactoring refactoring) {
      super(refactoring, WIZARD_BASED_USER_INTERFACE);
   }

   @Override
   protected void addUserInputPages() {

   }

}
