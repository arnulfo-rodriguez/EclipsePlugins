package arz.jdt;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
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

public class MakeFieldFinalDetector {

	private static class AssignmentsFinderVisitor extends ASTVisitor {
		private boolean isAssigned = false;
		private IVariableBinding fVariable;
		private ASTNode finlineInitializationExpression = null;

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

		public boolean canFieldBeFinal() {
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
					&& fragment.getInitializer() != null) {
				finlineInitializationExpression = fragment.getInitializer();
			}
			return true;
		}
	}

	private static boolean checkDeclaration(IField field)
			throws JavaModelException {
		return field != null && field.exists() && field.isStructureKnown()
				&& !field.getDeclaringType().isAnnotation()
				&& Flags.isPrivate(field.getFlags())
				&& !Flags.isFinal(field.getFlags()) && !field.isBinary();
	}

	public static boolean detect(IVariableBinding variableBinding,
			CompilationUnit compilationUnit) throws JavaModelException {

		return checkDeclaration((IField) variableBinding.getJavaElement())
				&& checkAssignments(variableBinding, compilationUnit);

	}

	private static boolean checkAssignments(IVariableBinding variableBinding,
			CompilationUnit compilationUnit) {
		AssignmentsFinderVisitor finder = new AssignmentsFinderVisitor(
				variableBinding);
		compilationUnit.accept(finder);
		return finder.canFieldBeFinal();
	}
}