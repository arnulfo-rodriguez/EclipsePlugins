/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import arz.jdt.AssignmentsFinder;

/**
 * @author arnulfo
 * 
 */
public class ToFinalQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode instanceof SimpleName) {
			SimpleName fName = (SimpleName) coveringNode;
			IBinding binding = fName.resolveBinding();
			if (binding instanceof IVariableBinding
					&& ((IVariableBinding) binding).isField()
					&& AssignmentsFinder.canVariableBeFinal(
							(IVariableBinding) binding, context.getASTRoot())) {
				return new IJavaCompletionProposal[] { (IJavaCompletionProposal) new ToFinalQuickAssistCompletionProposal(
						(IVariableBinding) binding, context) };
			}
		}
		return null;
	}

}
