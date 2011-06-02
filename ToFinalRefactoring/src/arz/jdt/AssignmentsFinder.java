package arz.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;



@SuppressWarnings("restriction")
public class AssignmentsFinder   {
	
	public interface AssigmentsFinderResult{
		public boolean canVariableBeFinal();
		public VariableDeclarationFragment getDeclarationFragment();
	}
	
	private static class AssignmentsFinderVisitor extends ASTVisitor implements AssigmentsFinderResult {
		private boolean isAssigned = false;
		private IVariableBinding fVariable;
		private ASTNode finlineInitializationExpression = null;
		private VariableDeclarationFragment fFragment;

		public AssignmentsFinderVisitor(IVariableBinding binding) {
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

		@Override
		public boolean canVariableBeFinal() {
			return finlineInitializationExpression != null && !isAssigned;
		}

		// -- visit methods
		@Override
		public boolean visit(Assignment assignment) {
			if (isAssignmentToVariable(assignment)) {
				isAssigned = true;
			}
			return true;
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
					&& !prefixExpression.getOperator().equals(
							Operator.INCREMENT))
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
					&& fragment.getInitializer() != null) 
			{
				this.fFragment = fragment;
				finlineInitializationExpression = fragment.getInitializer();
			}
			return true;
		}

		@Override
		public VariableDeclarationFragment getDeclarationFragment() {
			// TODO Auto-generated method stub
			return fFragment;
		}
	}
	
	public static AssigmentsFinderResult analyze(IVariableBinding variableBinding,
			CompilationUnit javaAst) {
		AssignmentsFinderVisitor finder = new AssignmentsFinderVisitor(variableBinding);
		javaAst.accept(finder);
        return finder;
	}
}