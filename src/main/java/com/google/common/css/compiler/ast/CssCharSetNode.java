package com.google.common.css.compiler.ast;

import java.util.List;

public class CssCharSetNode extends CssAtRuleNode {

    public CssCharSetNode(List<CssCommentNode> comments, CssDeclarationBlockNode block) {
        super(CssAtRuleNode.Type.CHARSET, new CssLiteralNode("charset"), block,
                comments);
    }

    public CssCharSetNode(CssCharSetNode node) {
        super(node);
    }

    @Override
    public CssNode deepCopy() {
        return new CssCharSetNode(this);
    }

    @Override
    public CssDeclarationBlockNode getBlock() {
        // The type is ensured by the constructor.
        return (CssDeclarationBlockNode) super.getBlock();
    }
}
