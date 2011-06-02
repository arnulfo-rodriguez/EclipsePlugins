package arz.assists;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class ToFinalQuickAssistCompletionProposal extends CUCorrectionProposal {

	public ToFinalQuickAssistCompletionProposal(String name,
			ICompilationUnit cu, int relevance, Image image) {
		super(name, cu, relevance, image);
	}

	public ToFinalQuickAssistCompletionProposal(String name,
			ICompilationUnit cu, TextChange change, int relevance, Image image) {
		super(name, cu, change, relevance, image);
	}



}
