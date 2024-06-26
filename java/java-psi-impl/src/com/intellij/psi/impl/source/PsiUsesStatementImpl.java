// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.PsiUsesStatementStub;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiUsesStatementImpl extends JavaStubPsiElement<PsiUsesStatementStub> implements PsiUsesStatement {
  public PsiUsesStatementImpl(@NotNull PsiUsesStatementStub stub) {
    super(stub, JavaStubElementTypes.USES_STATEMENT);
  }

  public PsiUsesStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable PsiJavaCodeReferenceElement getClassReference() {
    return PsiTreeUtil.getChildOfType(this, PsiJavaCodeReferenceElement.class);
  }

  @Override
  public @Nullable PsiClassType getClassType() {
    PsiUsesStatementStub stub = getStub();
    PsiJavaCodeReferenceElement ref =
      stub != null ? JavaPsiFacade.getElementFactory(getProject()).createReferenceFromText(stub.getClassName(), this) : getClassReference();
    return ref != null ? new PsiClassReferenceType(ref, null, PsiAnnotation.EMPTY_ARRAY) : null;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitUsesStatement(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public String toString() {
    return "PsiUsesStatement";
  }
}