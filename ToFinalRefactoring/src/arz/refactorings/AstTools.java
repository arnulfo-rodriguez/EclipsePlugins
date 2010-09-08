package arz.refactorings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.SourceField;

public class AstTools {

   static CompilationUnit ParseToJavaAst(IProgressMonitor monitor,
         ICompilationUnit compilationUnit) {
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setSource(compilationUnit);
      parser.setResolveBindings(true);
      parser.setBindingsRecovery(true);
      return (CompilationUnit) parser.createAST(monitor);
   }

   static SourceField getSelectedField(Object[] elements,
         ICompilationUnit compilationUnit) {
      SourceField vField = null;
      if (elements.length == 1 && elements[0] instanceof SourceField) {
         vField = (SourceField) elements[0];
      }
      return vField;
   }

}
