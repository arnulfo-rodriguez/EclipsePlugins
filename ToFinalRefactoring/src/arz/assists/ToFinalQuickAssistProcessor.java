/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

/**
 * @author arnulfo
 *
 */
public class ToFinalQuickAssistProcessor implements IQuickAssistProcessor {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.IQuickAssistProcessor#hasAssists(org.eclipse.jdt.ui.text.java.IInvocationContext)
	 */
	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.IQuickAssistProcessor#getAssists(org.eclipse.jdt.ui.text.java.IInvocationContext, org.eclipse.jdt.ui.text.java.IProblemLocation[])
	 */
	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
