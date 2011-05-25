package arz.assists;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class ToFinalQuickAssistCompletionProposal implements
		IJavaCompletionProposal {

	
	private IVariableBinding fBinding;
	private IInvocationContext fContext;

	public ToFinalQuickAssistCompletionProposal(IVariableBinding binding,IInvocationContext context){
		this.fBinding = binding;
		this.fContext = context;
	}
	
	@Override
	public void apply(IDocument document) {
		// TODO Auto-generated method stub

	}

	@Override
	public Point getSelection(IDocument document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRelevance() {
		// TODO Auto-generated method stub
		return 0;
	}

}
