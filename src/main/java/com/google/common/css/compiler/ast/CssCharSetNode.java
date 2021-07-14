package com.google.common.css.compiler.ast;

import java.util.List;

public class CssCharSetNode extends CssAtRuleNode {

    public CssCharSetNode(List<CssCommentNode> comments) {
        super(CssAtRuleNode.Type.CHARSET, new CssLiteralNode("charset"), comments);
    }

    public CssCharSetNode(CssCharSetNode node) {
        super(node);
    }

    @Override
    public CssNode deepCopy() {
        return new CssCharSetNode(this);
    }
}
