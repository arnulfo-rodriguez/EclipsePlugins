package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;

import arz.jdt.FinalModifierAdder;

public class ToFinalQuickAssistCompletionProposal extends CUCorrectionProposal {

	private ToFinalQuickAssistCompletionProposal(String name,
			ICompilationUnit cu, TextChange change, int relevance, Image image) {
		super(name, cu, change, relevance, image);
	}

	public ToFinalQuickAssistCompletionProposal(
			final IInvocationContext context, VariableDeclarationFragment declarationFragment) throws CoreException {

		this("Make field final",
				context.getCompilationUnit(), new FinalModifierAdder(context
						.getASTRoot().getAST(), context.getCompilationUnit(),
						declarationFragment).addFinal(), 1, (Image) null);
	}

}
