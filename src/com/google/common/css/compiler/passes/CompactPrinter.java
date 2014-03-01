/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css.compiler.passes;

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssAttributeSelectorNode;
import com.google.common.css.compiler.ast.CssBlockNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssClassSelectorNode;
import com.google.common.css.compiler.ast.CssCombinatorNode;
import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssConditionalBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationBlockNode;
import com.google.common.css.compiler.ast.CssDeclarationNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssFontFaceNode;
import com.google.common.css.compiler.ast.CssFunctionNode;
import com.google.common.css.compiler.ast.CssIdSelectorNode;
import com.google.common.css.compiler.ast.CssImportRuleNode;
import com.google.common.css.compiler.ast.CssKeyListNode;
import com.google.common.css.compiler.ast.CssKeyNode;
import com.google.common.css.compiler.ast.CssKeyframesNode;
import com.google.common.css.compiler.ast.CssMediaRuleNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssNodesListNode;
import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.CssPageRuleNode;
import com.google.common.css.compiler.ast.CssPageSelectorNode;
import com.google.common.css.compiler.ast.CssPriorityNode;
import com.google.common.css.compiler.ast.CssPropertyValueNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode;
import com.google.common.css.compiler.ast.CssPseudoClassNode.FunctionType;
import com.google.common.css.compiler.ast.CssPseudoElementNode;
import com.google.common.css.compiler.ast.CssRefinerNode;
import com.google.common.css.compiler.ast.CssSelectorListNode;
import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssStringNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.VisitController;

import java.util.logging.Logger;

/**
 * A compact-printer for {@link CssTree} instances.
 * TODO(oana): Change this pass to stop visiting when definitions are
 * encountered. The same goes for its test.
 *
 * @author oana@google.com (Oana Florescu)
 * @author fbenz@google.com (Florian Benz)
 */
public class CompactPrinter extends DefaultTreeVisitor
    implements CssCompilerPass {

  private String compactedPrintedString = null;
  private VisitController visitController;
  private CssNode subtree;
  private static final Logger logger = Logger.getLogger(
      CompactPrinter.class.getName());

  /**
   * The string builder used to keep the printout in progress while the
   * tree is visited.
   *
   * <p>Subclasses can use this builder to add delimiters when
   * appropriate before and after a node.
   */
  protected StringBuilder sb = null;

  public CompactPrinter(CssNode subtree) {
    this.subtree = subtree;
    this.visitController = this.subtree.getVisitController();
  }

  public CompactPrinter(CssTree tree) {
    this(tree.getRoot());
  }

  @Override
  public boolean enterDefinition(CssDefinitionNode node) {
    return false;
  }

  @Override
  public boolean enterImportRule(CssImportRuleNode node) {
    sb.append(node.getType().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(' ');
      // TODO(user): teach visit controllers to explore
      // CssImportRuleNode parameters rather than leaving it to each
      // pass to figure things out.
      if (param instanceof CssStringNode) {
        sb.append(param.toString());
      } else {
        sb.append(param.getValue());
      }
    }
    return true;
  }

  @Override
  public void leaveImportRule(CssImportRuleNode node) {
    sb.append(';');
  }

  @Override
  public boolean enterMediaRule(CssMediaRuleNode node) {
    sb.append(node.getType().toString());
    if (node.getParameters().size() > 0) {
      sb.append(" ");
    }
    return true;
  }

  /**
   * This is necessary because the parser transform '(' ident ')' into a
   * boolean expression node and only stores the identifier itself.
   * For example: {@code @media all and (color)}
   */
  private void appendMediaParameterWithParentheses(CssValueNode node) {
    // TODO(fbenz): Try to avoid the special handling of this case.
    sb.append("(");
    sb.append(node.getValue());
    sb.append(")");
  }

  @Override
  public void leaveMediaRule(CssMediaRuleNode node) {
    sb.append('}');
  }

  @Override
  public boolean enterPageRule(CssPageRuleNode node) {
    sb.append(node.getType().toString());
    sb.append(' ');
    // TODO(fbenz): There are only two parameters possible ('bla:left') that
    // come with no whitespace in between. So it would be better to have a
    // single node (maybe a selector).
    for (CssValueNode param : node.getParameters()) {
      sb.append(param.getValue());
    }
    deleteLastCharIfCharIs(' ');
    return true;
  }

  @Override
  public boolean enterPageSelector(CssPageSelectorNode node) {
    sb.append(node.getType().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(' ');
      sb.append(param.getValue());
    }
    return true;
  }

  @Override
  public boolean enterFontFace(CssFontFaceNode node) {
    sb.append(node.getType().toString());
    return true;
  }

  @Override
  public boolean enterSelector(CssSelectorNode selector) {
    String name = selector.getSelectorName();
    if (name != null) {
      sb.append(name);
    }
    return true;
  }

  @Override
  public void leaveSelector(CssSelectorNode selector) {
    sb.append(',');
  }

  @Override
  public boolean enterClassSelector(CssClassSelectorNode node) {
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterIdSelector(CssIdSelectorNode node) {
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterPseudoClass(CssPseudoClassNode node) {
    sb.append(node.getPrefix());
    sb.append(node.getRefinerName());
    switch (node.getFunctionType()) {
      case NTH:
        sb.append(node.getArgument().replace(" ", ""));
        sb.append(")");
        break;
      case LANG:
        sb.append(node.getArgument());
        sb.append(")");
        break;
    }
    return true;
  }

  @Override
  public void leavePseudoClass(CssPseudoClassNode node) {
    if (node.getFunctionType() == FunctionType.NOT) {
      deleteLastCharIfCharIs(',');
      sb.append(")");
    }
  }

  @Override
  public boolean enterPseudoElement(CssPseudoElementNode node) {
    appendRefiner(node);
    return true;
  }

  @Override
  public boolean enterAttributeSelector(CssAttributeSelectorNode node) {
    sb.append(node.getPrefix());
    sb.append(node.getAttributeName());
    sb.append(node.getMatchSymbol());
    sb.append(node.getValue());
    sb.append(node.getSuffix());
    return true;
  }

  /**
   * Appends the representation of a class selector, an id selector,
   * or a pseudo-element.
   */
  private void appendRefiner(CssRefinerNode node) {
    sb.append(node.getPrefix());
    sb.append(node.getRefinerName());
  }

  @Override
  public boolean enterCombinator(CssCombinatorNode combinator) {
    if (combinator != null) {
      sb.append(combinator.getCombinatorType().getCanonicalName());
    }
    return true;
  }

  @Override
  public void leaveCombinator(CssCombinatorNode combinator) {
    deleteLastCharIfCharIs(',');
  }

  @Override
  public void leaveSelectorBlock(CssSelectorListNode node) {
    deleteLastCharIfCharIs(',');
  }

  @Override
  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    sb.append('{');
    return true;
  }

  @Override
  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    deleteLastCharIfCharIs(';');
    sb.append('}');
  }

  @Override
  public boolean enterBlock(CssBlockNode block) {
    if (block.getParent() instanceof CssUnknownAtRuleNode
        || block.getParent() instanceof CssMediaRuleNode) {
      sb.append("{");
    }
    return true;
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    if (declaration.hasStarHack()) {
      sb.append('*');
    }
    sb.append(declaration.getPropertyName().getValue());
    sb.append(':');
    return true;
  }

  @Override
  public void leaveDeclaration(CssDeclarationNode declaration) {
    deleteLastCharIfCharIs(' ');
    sb.append(';');
  }

  @Override
  public void leaveCompositeValueNode(CssCompositeValueNode node) {
    deleteLastCharIfCharIs(' ');
    if (node.getParent() instanceof CssPropertyValueNode) {
      sb.append(' ');
    }
  }

  @Override
  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssPriorityNode) {
      deleteLastCharIfCharIs(' ');
    }
    appendValueNode(node);
    return true;
  }

  @Override
  public void leaveValueNode(CssValueNode node) {
    if (node.getParent() instanceof CssPropertyValueNode) {
      sb.append(' ');
    }
  }

  @Override
  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    deleteLastCharIfCharIs(' ');
    sb.append(parent.getOperator().getOperatorName());
    return true;
  }

  @Override
  public boolean enterFunctionNode(CssFunctionNode node) {
    sb.append(node.getFunctionName());
    sb.append('(');
    return true;
  }

  @Override
  public void leaveFunctionNode(CssFunctionNode node) {
    deleteLastCharIfCharIs(' ');
    sb.append(") ");
  }

  // We need to handle both standard function calls separated by
  // commas, and IE-specific calls, with = as a separator as in
  // alpha(opacity=70) or even with spaces as separators as in
  // rect(0 0 0 0). In all cases, the separators each appear explicitly
  // as arguments.
  private static final ImmutableSet<String> ARGUMENT_SEPARATORS =
      ImmutableSet.of(",", "=", " ");

  @Override
  public boolean enterArgumentNode(CssValueNode node) {
    if (ARGUMENT_SEPARATORS.contains(node.toString())) {
      // If the previous argument was a function node, then it has a
      // trailing space that needs to be removed.
      deleteLastCharIfCharIs(' ');
    }
    appendValueNode(node);
    return true;
  }

  @Override
  public boolean enterConditionalBlock(CssConditionalBlockNode node) {
    visitController.stopVisit();
    // TODO(oana): Collect these messages with a proper message collector.
    logger.warning("Conditional block should not be "
        + "present: " + node.toString()
        + ((node.getSourceCodeLocation() != null) ?
            "@" + node.getSourceCodeLocation().getLineNumber() : ""));
    return true;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    sb.append("@").append(node.getName().toString());
    if (node.getParameters().size() > 0) {
      sb.append(" ");
    }
    return true;
  }

  @Override
  public boolean enterMediaTypeListDelimiter(
      CssNodesListNode<? extends CssNode> node) {
    sb.append(' ');
    return true;
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.getType().hasBlock()) {
      if (!(node.getBlock() instanceof CssDeclarationBlockNode)) {
        sb.append('}');
      }
    } else {
      sb.append(';');
    }
  }

  @Override
  public boolean enterKeyframesRule(CssKeyframesNode node) {
    sb.append('@').append(node.getName().toString());
    for (CssValueNode param : node.getParameters()) {
      sb.append(' ');
      sb.append(param.getValue());
    }
    if (node.getType().hasBlock()) {
      sb.append('{');
    }
    return true;
  }

  @Override
  public void leaveKeyframesRule(CssKeyframesNode node) {
    if (node.getType().hasBlock()) {
      sb.append('}');
    } else {
      sb.append(';');
    }
  }

  @Override
  public boolean enterKey(CssKeyNode node) {
    String value = node.getKeyValue();
    if (value != null) {
      sb.append(value);
    }
    return true;
  }

  @Override
  public void leaveKey(CssKeyNode key) {
    sb.append(',');
  }

  @Override
  public void leaveKeyBlock(CssKeyListNode block) {
    deleteLastCharIfCharIs(',');
  }

  /**
   * Deletes the last character from the string builder if the
   * character is as given.
   *
   * <p>Subclasses can modify this method in order to delete more in
   * cases where they've added extra delimiters.
   *
   * @param ch the character to delete
   */
  protected void deleteLastCharIfCharIs(char ch) {
    if (sb.length() == 0) {
      return;
    }
    if (sb.charAt(sb.length() - 1) == ch) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * Returns the CSS compacted printed output.
   */
  public String getCompactPrintedString() {
    return compactedPrintedString;
  }

  @Override
  public void runPass() {
    sb = new StringBuilder();
    visitController.startVisit(this);
    compactedPrintedString = sb.toString();
  }

  /**
   * Appends the given value node to the buffer.
   *
   * <p>Subclasses can modify this to provide a different
   * serialization for particular types of value nodes.
   *
   * @param node the node to append
   */
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssCompositeValueNode) {
      return;
    }
    if (node instanceof CssBooleanExpressionNode &&
        node.getParent() instanceof CssMediaRuleNode) {
      appendMediaParameterWithParentheses(node);
      return;
    }
    if (node instanceof CssStringNode) {
      CssStringNode s = (CssStringNode) node;
      sb.append(s.toString(CssStringNode.HTML_ESCAPER));
      return;
    }
    if (node instanceof CssNumericNode) {
      // TODO(user): Consider introducing
      //   void CssNode.appendTo(Appendable sb)
      // or
      //   CharSequence CssNode.asCharSequence
      CssNumericNode n = (CssNumericNode) node;
      sb.append(n.getNumericPart());
      sb.append(n.getUnit());
      return;
    }
    sb.append(node.toString());
  }

  public static String printCompactly(CssNode n) {
    CompactPrinter p = new CompactPrinter(n);
    p.runPass();
    return p.getCompactPrintedString().trim();
  }

  public static String printCompactly(CssTree t) {
    return printCompactly(t.getRoot());
  }
}
