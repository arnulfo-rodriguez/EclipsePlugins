package arz.refactorings;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

@SuppressWarnings("restriction")
public class AssignmentsFinder extends ASTVisitor {
	private boolean isAssigned = false;
	private IVariableBinding fVariable;
	private ASTNode finlineInitializationExpression = null;

	AssignmentsFinder(IVariableBinding binding) {
		fVariable = binding;
	}

	private boolean isNameReferenceToVariable(Name name) {
		return fVariable == name.resolveBinding();
	}

	private boolean isAssignmentToVariable(Assignment assignment) {
		if (fVariable == null)
			return false;

		if (!(assignment.getLeftHandSide() instanceof Name))
			return false;
		Name ref = (Name) assignment.getLeftHandSide();
		return isNameReferenceToVariable(ref);
	}

	public boolean canVariableBeFinal() {
		return finlineInitializationExpression != null && !isAssigned;
	}

	// -- visit methods
	@Override
	public boolean visit(Assignment assignment) {
		if (!isAssignmentToVariable(assignment))
			return true;

		isAssigned = true;
		return false;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		if (postfixExpression.getOperand() == null)
			return true;
		if (!(postfixExpression.getOperand() instanceof SimpleName))
			return true;
		SimpleName simpleName = (SimpleName) postfixExpression.getOperand();
		if (!isNameReferenceToVariable(simpleName))
			return true;

		isAssigned = true;
		return false;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (prefixExpression.getOperand() == null)
			return true;
		if (!(prefixExpression.getOperand() instanceof SimpleName))
			return true;
		if (!prefixExpression.getOperator().equals(Operator.DECREMENT)
				&& !prefixExpression.getOperator().equals(Operator.INCREMENT))
			return true;
		SimpleName simpleName = (SimpleName) prefixExpression.getOperand();
		if (!isNameReferenceToVariable(simpleName))
			return true;

		isAssigned = true;
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (isNameReferenceToVariable(fragment.getName())
				&& fragment.getInitializer() != null
				&& ASTNodes.isLiteral(fragment.getInitializer())) {
			finlineInitializationExpression = fragment.getInitializer();
		}
		return false;
	}
}
