// Copyright 2000-2017 JetBrains s.r.o.
// Use of this source code is governed by the Apache 2.0 license that can be
// found in the LICENSE file.
package com.intellij.execution;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class WSLUtilTest extends TestCase {

  public void testWslToWinPath() throws Exception {
    assertWslPath("/usr/something/include", "%LOCALAPPDATA%\\lxss\\rootfs\\usr\\something\\include");
    assertWslPath("/usr/something/bin/gcc", "%LOCALAPPDATA%\\lxss\\rootfs\\usr\\something\\bin\\gcc");

    assertWslPath("/mnt/c", "c:");
    assertWslPath("/mnt/x/", "x:\\");

    assertWslPath("/mnt/c/temp/foo", "c:\\temp\\foo");
    assertWslPath("/mnt/c/temp/KeepCase", "c:\\temp\\KeepCase");
    assertWslPath("/mnt/c/name with spaces/another name with spaces", "c:\\name with spaces\\another name with spaces");
    assertWslPath("/mnt/c/юникод", "c:\\юникод");
  }

  public void testWinToWslPath() {
    assertWinPath("c:\\foo", "/mnt/c/foo");
    assertWinPath("c:\\temp\\KeepCase", "/mnt/c/temp/KeepCase");
  }

  public void testPaths() {
    final String originalWinPath = "c:\\usr\\something\\bin\\gcc";
    final String winPath = WSLUtil.getWindowsPath(WSLUtil.getWslPath(originalWinPath));
    assertEquals(originalWinPath, winPath);

    final String originalWslPath = "/usr/bin/gcc";
    final String wslPath = WSLUtil.getWslPath(WSLUtil.getWindowsPath(originalWslPath));
    assertEquals(originalWslPath, wslPath);
  }

  public void testVersion() {
    final String version = WSLUtil.getWslVersion();
    assertTrue(WSLUtil.hasWSL() ? version != null : version == null);
  }

  public void testResolveSymlink() throws Exception {
    if (!WSLUtil.hasWSL()) return;

    final File winFile = FileUtil.createTempFile("the_file.txt", null);
    final File winSymlink = new File(new File(FileUtil.getTempDirectory()), "sym_link");

    try {
      final String file = WSLUtil.getWslPath(winFile.getPath());
      final String symlink = WSLUtil.getWslPath(winSymlink.getPath());
      mkSymlink(file, symlink);

      final String resolved = WSLUtil.getWindowsPath(WSLUtil.resolveSymlink(symlink, 10_000));
      assertTrue(FileUtil.exists(resolved));
      assertTrue(winFile.getPath().equalsIgnoreCase(resolved));
    }
    finally {
      FileUtil.delete(winFile);
      FileUtil.delete(winSymlink);
    }
  }

  private static void assertWinPath(@NotNull String winPath, @NotNull String wslPath) {
    assertEquals(wslPath, WSLUtil.getWslPath(winPath));
  }

  private static void assertWslPath(@NotNull String wslPath, @NotNull String winPath) {
    assertEquals(prepare(winPath), WSLUtil.getWindowsPath(wslPath));
  }

  private static String prepare(@NotNull String path) {
    if (path.startsWith("%LOCALAPPDATA%")) {
      final String localappdata = System.getenv().get("LOCALAPPDATA");
      path = localappdata + path.substring("%LOCALAPPDATA%".length());
    }
    return path;
  }

  private static void mkSymlink(@NotNull String file, @NotNull String symlink) throws Exception {
    final GeneralCommandLine cl = new GeneralCommandLine();
    cl.setExePath("ln");
    cl.addParameters("-s", file, symlink);

    final GeneralCommandLine cmd = WSLUtil.patchCommandLine(cl, null, null, false);
    final CapturingProcessHandler process = new CapturingProcessHandler(cmd);
    final ProcessOutput output = process.runProcess(10_000);
    assertFalse(output.isTimeout());
    assertEquals(0, output.getExitCode());
  }
}