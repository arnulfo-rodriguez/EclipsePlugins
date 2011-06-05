/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.swt.graphics.Image;

import arz.jdt.MakeFieldFinalDetector;
import arz.jdt.MakeFieldFinalDetector.IAssigmentsFinderResult;
import arz.jdt.FinalModifierAdder;
/**
 * @author arnulfo
 * 
 */
public class ToFinalQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(final IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) coveringNode;
			IBinding binding = simpleName.resolveBinding();
			if (isFieldBinding(binding)) {

				IAssigmentsFinderResult result = MakeFieldFinalDetector.detect(
						(IVariableBinding) binding, 
						context.getASTRoot());

				if (result.canFieldBeFinal()) {
					return new IJavaCompletionProposal[] { new ToFinalQuickAssistCompletionProposal(
							"Make field final",
							context.getCompilationUnit(),
							new FinalModifierAdder(
									context.getASTRoot()
									.getAST(), context.getCompilationUnit(),
									result.getDeclarationFragment()).addFinal(),
							1, (Image) null) };
				}
			}
		}
		return null;
	}

	private boolean isFieldBinding(IBinding binding) {
		return binding instanceof IVariableBinding
				&& ((IVariableBinding) binding).isField();
	}

}
