/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.swt.graphics.Image;

import arz.jdt.AssignmentsFinder;
import arz.jdt.AssignmentsFinder.AssigmentsFinderResult;
import arz.jdt.AstTools;
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
	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) coveringNode;
			IBinding binding = simpleName.resolveBinding();
			if (bindingIsField(binding)) {

				if (AstTools.fieldDeclarationCanBeFinal((IField) binding
						.getJavaElement())) {

					AssigmentsFinderResult result = AssignmentsFinder.analyze(
							(IVariableBinding) binding, context.getASTRoot());

					if (result.canVariableBeFinal()) {
						return new IJavaCompletionProposal[] { new ToFinalQuickAssistCompletionProposal(
								"Make field final",
								context.getCompilationUnit(),
								new FinalModifierAdder(context.getASTRoot()
										.getAST(),
										context.getCompilationUnit(), result
												.getDeclarationFragment())
										.addFinal(), 1, (Image) null) };
					}
				}
			}
		}
		return null;
	}

	private boolean bindingIsField(IBinding binding) {
		return binding instanceof IVariableBinding
				&& ((IVariableBinding) binding).isField();
	}

}
