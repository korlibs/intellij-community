/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.debugger.attach;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.xdebugger.attach.LocalAttachSettings;
import com.intellij.xdebugger.attach.XAttachGroup;
import icons.PythonIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class PyLocalAttachGroup implements XAttachGroup<LocalAttachSettings> {
  public static final PyLocalAttachGroup INSTANCE = new PyLocalAttachGroup();

  private PyLocalAttachGroup() {
  }

  @Override
  public int getOrder() {
    return XAttachGroup.DEFAULT.getOrder() - 10;
  }

  @NotNull
  @Override
  public String getGroupName() {
    return "Python";
  }

  @NotNull
  @Override
  public Icon getIcon(@NotNull Project project, @NotNull LocalAttachSettings info, @NotNull UserDataHolder dataHolder) {
    return PythonIcons.Python.Python;
  }

  @NotNull
  @Override
  public String getItemDisplayText(@NotNull Project project, @NotNull LocalAttachSettings settings, @NotNull UserDataHolder dataHolder) {
    return settings.getInfo().getArgs();
  }

  @Override
  public int compare(@NotNull Project project,
                     @NotNull LocalAttachSettings a,
                     @NotNull LocalAttachSettings b,
                     @NotNull UserDataHolder dataHolder) {
    return XAttachGroup.DEFAULT.compare(project, a, b, dataHolder);
  }
}
