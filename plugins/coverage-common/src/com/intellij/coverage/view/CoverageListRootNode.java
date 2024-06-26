// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coverage.view;

import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public class CoverageListRootNode extends CoverageListNode {
  /**
   * @deprecated Use {@link CoverageListRootNode#CoverageListRootNode(Project, PsiNamedElement, CoverageSuitesBundle)}
   */
  @Deprecated
  public CoverageListRootNode(Project project, @NotNull PsiNamedElement classOrPackage,
                              CoverageSuitesBundle bundle,
                              @SuppressWarnings("unused") CoverageViewManager.StateBean stateBean) {
    this(project, classOrPackage, bundle);
  }

  public CoverageListRootNode(Project project, @NotNull PsiNamedElement classOrPackage, CoverageSuitesBundle bundle) {
    super(project, classOrPackage, bundle);
  }
}
