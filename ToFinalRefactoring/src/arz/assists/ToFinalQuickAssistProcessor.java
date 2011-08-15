/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import arz.jdt.AstTools;
import arz.jdt.MakeFieldFinalDetector;

/**
 * @author arnulfo
 * 
 */
@SuppressWarnings("restriction")
public class ToFinalQuickAssistProcessor implements IQuickAssistProcessor {

	private static final IJavaCompletionProposal[] NULL_COMPLETION_PROPOSAL = new IJavaCompletionProposal[] {};

	boolean hasToFinalQuickAssist(IBinding binding,
			CompilationUnit compilationUnit) throws JavaModelException {
		return (binding instanceof IVariableBinding && ((IVariableBinding) binding)
				.isField())
				&& MakeFieldFinalDetector.detect((IVariableBinding) binding,
						compilationUnit);
	}

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		return (coveringNode instanceof SimpleName)
				&& hasToFinalQuickAssist(
						((SimpleName) coveringNode).resolveBinding(),
						context.getASTRoot());
	}

	@Override
	public IJavaCompletionProposal[] getAssists(
			final IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();

		if (coveringNode instanceof SimpleName) {

			SimpleName simpleName = (SimpleName) coveringNode;
			IBinding binding = simpleName.resolveBinding();

			if (hasToFinalQuickAssist(binding, context.getASTRoot())) {
				SourceField sourceField = (SourceField) ((IVariableBinding) binding)
						.getJavaElement();
			
				FieldDeclaration fieldDeclaration = (FieldDeclaration) sourceField.findNode(context.getASTRoot());
				
				return new IJavaCompletionProposal[] {new ToFinalQuickAssistCompletionProposal
						(context, AstTools
										.getDeclarationFragmentByName(
												fieldDeclaration,
												simpleName.getIdentifier()))};
			}
		}
		return NULL_COMPLETION_PROPOSAL;
	}
}
