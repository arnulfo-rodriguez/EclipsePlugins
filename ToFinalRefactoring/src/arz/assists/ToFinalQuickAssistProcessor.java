/**
 * 
 */
package arz.assists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.swt.graphics.Image;

import arz.jdt.AstTools;
import arz.jdt.FinalModifierAdder;
import arz.jdt.MakeFieldFinalDetector;

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
	public IJavaCompletionProposal[] getAssists(
			final IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) coveringNode;
			IBinding binding = simpleName.resolveBinding();
			if (binding instanceof IVariableBinding
					&& ((IVariableBinding) binding).isField()) {

				if (MakeFieldFinalDetector
						.detect((IVariableBinding) binding,
								context.getASTRoot())) {
					
					SourceField sourceField = (SourceField) binding.getJavaElement();
					
					VariableDeclarationFragment declarationFragment = AstTools.getDeclarationFragmentByName(
							(FieldDeclaration) sourceField
									.findNode(context.getASTRoot()),
							simpleName.getIdentifier());
					
					return new IJavaCompletionProposal[] { new ToFinalQuickAssistCompletionProposal(
							"Make field final", context.getCompilationUnit(),
							new FinalModifierAdder(context.getASTRoot()
									.getAST(), context.getCompilationUnit(),
									 declarationFragment)
									.addFinal(), 1, (Image) null) };
				}
			}
		}
		return null;
	}

}
