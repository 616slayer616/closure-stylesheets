/*
 * Copyright 2008 Google Inc.
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

package com.google.common.css.compiler.ast;

/**
 * A handler for at rules.
 */
public interface AtRuleHandler {
    /**
     * @return {@code true} if the contents of the rule should be visited,
     * false otherwise. {@link #leaveConditionalRule(CssConditionalRuleNode)}
     * will still be called.
     *
     * @param node node
     */
    boolean enterConditionalRule(CssConditionalRuleNode node);

    /**
     * Called after visiting a {@code CssConditionalRuleNode}'s sub trees
     *
     * @param node node
     */
    void leaveConditionalRule(CssConditionalRuleNode node);

    /**
     * Called before visiting a {@code CssImportRuleNode}'s sub trees
     *
     * @param node node
     */
    boolean enterImportRule(CssImportRuleNode node);

    /**
     * Called after visiting a {@code CssImportRuleNode}'s sub trees
     *
     * @param node node
     */
    void leaveImportRule(CssImportRuleNode node);

    /**
     * Called before visiting a {@code CssMediaRuleNode}'s sub trees
     *
     * @param node node
     */
    boolean enterMediaRule(CssMediaRuleNode node);

    /**
     * Called after visiting a {@code CssMediaRuleNode}'s sub trees
     *
     * @param node node
     */
    void leaveMediaRule(CssMediaRuleNode node);

    /**
     * Called before visiting a {@code CssPageRuleNode}'s sub trees
     *
     * @param node node
     */
    boolean enterPageRule(CssPageRuleNode node);

    /**
     * Called after visiting a {@code CssPageRuleNode}'s sub trees
     *
     * @param node node
     */
    void leavePageRule(CssPageRuleNode node);

    /**
     * Called before visiting a {@code CssPageSelectorNode}'s sub trees
     *
     * @param node node
     */
    boolean enterPageSelector(CssPageSelectorNode node);

    /**
     * Called after visiting a {@code CssPageSelectorNode}'s sub trees
     *
     * @param node node
     */
    void leavePageSelector(CssPageSelectorNode node);

    /**
     * Called before visiting a {@code CssFontFaceNode}'s sub trees
     *
     * @param node node
     */
    boolean enterFontFace(CssFontFaceNode node);

    /**
     * Called after visiting a {@code CssFontFaceNode}'s sub trees
     *
     * @param node node
     */
    void leaveFontFace(CssFontFaceNode node);

    /**
     * Called before visiting a {@code CssCharSetNode}'s sub trees
     *
     * @param node node
     */
    boolean enterCharSet(CssCharSetNode node);

    /**
     * Called after visiting a {@code CssCharSetNode}'s sub trees
     *
     * @param node node
     */
    void leaveCharSet(CssCharSetNode node);

    /**
     * @return {@code true} if the contents of the rule should be visited,
     * false otherwise. {@link #leaveDefinition(CssDefinitionNode)}
     * will still be called.
     *
     * @param node node
     */
    boolean enterDefinition(CssDefinitionNode node);

    /**
     * Called after visiting a {@code CssDefinitionNode}'s sub trees
     *
     * @param node node
     */
    void leaveDefinition(CssDefinitionNode node);

    /**
     * Called before visiting a {@code CssUnknownAtRuleNode}'s sub trees
     *
     * @param node node
     */
    boolean enterUnknownAtRule(CssUnknownAtRuleNode node);

    /**
     * Called after visiting a {@code CssUnknownAtRuleNode}'s sub trees
     */
    void leaveUnknownAtRule(CssUnknownAtRuleNode node);

    /**
     * Called between adjacent nodes in a media type list
     *
     * @param node node
     */
    boolean enterMediaTypeListDelimiter(CssNodesListNode<? extends CssNode> node);

    /**
     * Called between adjacent nodes in a media type list
     *
     * @param node node
     */
    void leaveMediaTypeListDelimiter(CssNodesListNode<? extends CssNode> node);

    /**
     * Called before visiting a {@code CssComponentNode}'s sub trees
     *
     * @param node node
     */
    boolean enterComponent(CssComponentNode node);

    /**
     * Called after visiting a {@code CssComponentNode}'s sub trees
     *
     * @param node node
     */
    void leaveComponent(CssComponentNode node);

    /**
     * Called before visiting a {@code CssKeyframesNode}'s sub trees
     *
     * @param node node
     */
    boolean enterKeyframesRule(CssKeyframesNode node);

    /**
     * Called after visiting a {@code CssKeyframesNode}'s sub trees
     *
     * @param node node
     */
    void leaveKeyframesRule(CssKeyframesNode node);

    /**
     * Called before visiting a {@code CssMixinDefinitionNode}'s sub trees
     *
     * @param node node
     */
    boolean enterMixinDefinition(CssMixinDefinitionNode node);

    /**
     * Called after visiting a {@code CssMixinDefinitionNode}'s sub trees
     *
     * @param node node
     */
    void leaveMixinDefinition(CssMixinDefinitionNode node);

    /**
     * Called before visiting a {@code CssMixinNode}'s sub trees
     *
     * @param node node
     */
    boolean enterMixin(CssMixinNode node);

    /**
     * Called after visiting a {@code CssMixinNode}'s sub trees
     *
     * @param node node
     */
    void leaveMixin(CssMixinNode node);

    /**
     * Called before visiting a {@code CssProvideNode}'s sub trees
     *
     * @param node node
     */
    boolean enterProvideNode(CssProvideNode node);

    /**
     * Called after visiting a {@code CssProvideNode}'s sub trees
     *
     * @param node node
     */
    void leaveProvideNode(CssProvideNode node);

    /**
     * Called before visiting a {@code CssRequireNode}'s sub trees
     *
     * @param node node
     */
    boolean enterRequireNode(CssRequireNode node);

    /**
     * Called after visiting a {@code CssRequireNode}'s sub trees
     *
     * @param node node
     */
    void leaveRequireNode(CssRequireNode node);


}
