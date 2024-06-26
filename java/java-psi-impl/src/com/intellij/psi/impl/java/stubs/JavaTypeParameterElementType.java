// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.psi.impl.java.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.cache.RecordUtil;
import com.intellij.psi.impl.java.stubs.impl.PsiTypeParameterStubImpl;
import com.intellij.psi.impl.source.BasicJavaElementType;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.impl.source.tree.java.PsiTypeParameterImpl;
import com.intellij.psi.impl.source.tree.java.TypeParameterElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JavaTypeParameterElementType extends JavaStubElementType<PsiTypeParameterStub, PsiTypeParameter> {
  public JavaTypeParameterElementType() {
    super("TYPE_PARAMETER", BasicJavaElementType.BASIC_TYPE_PARAMETER);
  }

  @Override
  public @NotNull ASTNode createCompositeNode() {
    return new TypeParameterElement();
  }

  @Override
  public PsiTypeParameter createPsi(final @NotNull PsiTypeParameterStub stub) {
    return getPsiFactory(stub).createTypeParameter(stub);
  }

  @Override
  public PsiTypeParameter createPsi(final @NotNull ASTNode node) {
    return new PsiTypeParameterImpl(node);
  }

  @Override
  public @NotNull PsiTypeParameterStub createStub(final @NotNull LighterAST tree, final @NotNull LighterASTNode node, final @NotNull StubElement<?> parentStub) {
    final LighterASTNode id = LightTreeUtil.requiredChildOfType(tree, node, JavaTokenType.IDENTIFIER);
    final String name = RecordUtil.intern(tree.getCharTable(), id);
    return new PsiTypeParameterStubImpl(parentStub, name);
  }

  @Override
  public void serialize(final @NotNull PsiTypeParameterStub stub, final @NotNull StubOutputStream dataStream) throws IOException {
    String name = stub.getName();
    dataStream.writeName(name);
  }

  @Override
  public @NotNull PsiTypeParameterStub deserialize(final @NotNull StubInputStream dataStream, final StubElement parentStub) throws IOException {
    return new PsiTypeParameterStubImpl(parentStub, dataStream.readNameString());
  }

  @Override
  public void indexStub(final @NotNull PsiTypeParameterStub stub, final @NotNull IndexSink sink) {
  }
}
