// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("LiftReturnOrAssignment", "ReplaceJavaStaticMethodWithKotlinAnalog")

package org.jetbrains.intellij.build

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.intellij.build.impl.*
import org.jetbrains.intellij.build.impl.PluginLayout.Companion.plugin
import org.jetbrains.intellij.build.impl.PluginLayout.Companion.pluginAuto
import org.jetbrains.intellij.build.impl.PluginLayout.Companion.pluginAutoWithDeprecatedCustomDirName
import org.jetbrains.intellij.build.io.copyDir
import org.jetbrains.intellij.build.kotlin.KotlinPluginBuilder
import org.jetbrains.intellij.build.python.PythonCommunityPluginModules

object CommunityRepositoryModules {
  /**
   * Specifies non-trivial layout for all plugins that sources are located in 'community' and 'contrib' repositories
   */
  val COMMUNITY_REPOSITORY_PLUGINS: PersistentList<PluginLayout> = persistentListOf(
    pluginAuto("intellij.yaml") { spec ->
      spec.withModule("intellij.yaml.editing", "yaml-editing.jar")
    },
    //plugin("intellij.ant") { spec ->
    //  spec.mainJarName = "antIntegration.jar"
    //  spec.withModule("intellij.ant.jps", "ant-jps.jar")
//
    //  spec.withGeneratedResources { dir, buildContext ->
    //    copyAnt(pluginDir = dir, context = buildContext)
    //  }
    //},
    plugin("intellij.laf.macos") { spec ->
      spec.bundlingRestrictions.supportedOs = persistentListOf(OsFamily.MACOS)
    },
    plugin("intellij.webp") { spec ->
      spec.withPlatformBin(OsFamily.WINDOWS, JvmArchitecture.x64, "plugins/webp/lib/libwebp/win", "lib/libwebp/win")
      spec.withPlatformBin(OsFamily.MACOS, JvmArchitecture.x64, "plugins/webp/lib/libwebp/mac", "lib/libwebp/mac")
      spec.withPlatformBin(OsFamily.MACOS, JvmArchitecture.aarch64, "plugins/webp/lib/libwebp/mac", "lib/libwebp/mac")
      spec.withPlatformBin(OsFamily.LINUX, JvmArchitecture.x64, "plugins/webp/lib/libwebp/linux", "lib/libwebp/linux")
    },
    plugin("intellij.webp") { spec ->
      spec.bundlingRestrictions.marketplace = true
      spec.withResource("lib/libwebp/linux", "lib/libwebp/linux")
      spec.withResource("lib/libwebp/mac", "lib/libwebp/mac")
      spec.withResource("lib/libwebp/win", "lib/libwebp/win")
    },
    plugin("intellij.laf.win10") { spec ->
      spec.bundlingRestrictions.supportedOs = persistentListOf(OsFamily.WINDOWS)
    },
    plugin("intellij.java.guiForms.designer") { spec ->
      spec.directoryName = "uiDesigner"
      spec.mainJarName = "uiDesigner.jar"
      spec.withModule("intellij.java.guiForms.jps", "jps/java-guiForms-jps.jar")
    },
    KotlinPluginBuilder.kotlinPlugin(KotlinPluginBuilder.KotlinUltimateSources.WITH_COMMUNITY_MODULES),
    pluginAuto(listOf("intellij.vcs.git")) { spec ->
      spec.withModule("intellij.vcs.git.rt", "git4idea-rt.jar")
    },
    pluginAuto(listOf("intellij.xpath")) { spec ->
      spec.withModule("intellij.xpath.rt", "rt/xslt-rt.jar")
    },
    pluginAuto(listOf("intellij.platform.langInjection", "intellij.java.langInjection", "intellij.xml.langInjection")) { spec ->
      spec.withModule("intellij.java.langInjection.jps")
    },
    pluginAutoWithDeprecatedCustomDirName("intellij.tasks.core") { spec ->
      spec.directoryName = "tasks"
      spec.withModule("intellij.tasks")
      spec.withModule("intellij.tasks.compatibility")
      spec.withModule("intellij.tasks.jira")
      spec.withModule("intellij.tasks.java")
    },
    pluginAuto(listOf("intellij.xslt.debugger")) { spec ->
      spec.withModule("intellij.xslt.debugger.rt", "xslt-debugger-rt.jar")
      spec.withModule("intellij.xslt.debugger.impl.rt", "rt/xslt-debugger-impl-rt.jar")
      spec.withModuleLibrary("Saxon-6.5.5", "intellij.xslt.debugger.impl.rt", "rt/saxon.jar")
      spec.withModuleLibrary("Saxon-9HE", "intellij.xslt.debugger.impl.rt", "rt/saxon9he.jar")
      spec.withModuleLibrary("Xalan-2.7.3", "intellij.xslt.debugger.impl.rt", "rt/xalan-2.7.3.jar")
      spec.withModuleLibrary("Serializer-2.7.3", "intellij.xslt.debugger.impl.rt", "rt/serializer-2.7.3.jar")
      spec.withModuleLibrary("RMI Stubs", "intellij.xslt.debugger.rt", "rmi-stubs.jar")
    },
    plugin("intellij.maven") { spec ->
      spec.withModule("intellij.maven.jps")
      spec.withModule("intellij.maven.server.m3.common", "maven3-server-common.jar")
      spec.withModule("intellij.maven.server.m3.impl", "maven3-server.jar")
      spec.withModule("intellij.maven.server.m36.impl", "maven36-server.jar")
      spec.withModule("intellij.maven.server.m40", "maven40-server.jar")
      spec.withModule("intellij.maven.server.telemetry", "maven-server-telemetry.jar")
      spec.withModule("intellij.maven.errorProne.compiler")
      spec.withModule("intellij.maven.server.indexer", "maven-server-indexer.jar")
      spec.withModuleLibrary(libraryName = "apache.maven.core:3.8.3", moduleName = "intellij.maven.server.indexer",
                             relativeOutputPath = "intellij.maven.server.indexer/lib")
      spec.withModuleLibrary(libraryName = "apache.maven.wagon.provider.api:3.5.2", moduleName = "intellij.maven.server.indexer",
                             relativeOutputPath = "intellij.maven.server.indexer/lib")
      spec.withModuleLibrary(libraryName = "apache.maven.archetype.common:3.2.1", moduleName = "intellij.maven.server.indexer",
                             relativeOutputPath = "intellij.maven.server.indexer/lib")

      spec.withModule("intellij.maven.artifactResolver.m31", "artifact-resolver-m31.jar")
      spec.withModule("intellij.maven.artifactResolver.common", "artifact-resolver-m31.jar")

      spec.withModule("intellij.maven.server.eventListener", relativeJarPath = "maven-event-listener.jar")

      spec.doNotCopyModuleLibrariesAutomatically(listOf(
        "intellij.maven.artifactResolver.common",
        "intellij.maven.artifactResolver.m31",
        "intellij.maven.server.m3.common",
        "intellij.maven.server.m3.impl",
        "intellij.maven.server.m36.impl",
        "intellij.maven.server.m40",
        "intellij.maven.server.indexer",
      ))
      spec.withGeneratedResources { targetDir, context ->
        val targetLib = targetDir.resolve("lib")

        val maven4Libs = BundledMavenDownloader.downloadMaven4Libs(context.paths.communityHomeDirRoot)
        copyDir(maven4Libs, targetLib.resolve("maven4-server-lib"))

        val maven3Libs = BundledMavenDownloader.downloadMaven3Libs(context.paths.communityHomeDirRoot)
        copyDir(maven3Libs, targetLib.resolve("maven3-server-lib"))

        val mavenTelemetryDependencies = BundledMavenDownloader.downloadMavenTelemetryDependencies(context.paths.communityHomeDirRoot)
        copyDir(mavenTelemetryDependencies, targetLib.resolve("maven-telemetry-lib"))

        val mavenDist = BundledMavenDownloader.downloadMavenDistribution(context.paths.communityHomeDirRoot)
        copyDir(mavenDist, targetLib.resolve("maven3"))
      }
    },
    pluginAuto(listOf(
      "intellij.gradle",
      "intellij.gradle.common",
      "intellij.gradle.toolingProxy",
    )) { spec ->
      spec.withModule("intellij.gradle.toolingExtension", "gradle-tooling-extension-api.jar")
      spec.withModule("intellij.gradle.toolingExtension.impl", "gradle-tooling-extension-impl.jar")
      spec.withProjectLibrary("Gradle", LibraryPackMode.STANDALONE_SEPARATE)
      spec.withProjectLibrary("Ant", "ant", LibraryPackMode.STANDALONE_SEPARATE)
    },
    pluginAuto(listOf("intellij.gradle.java", "intellij.gradle.jps")) {
      it.excludeProjectLibrary("Ant")
      it.excludeProjectLibrary("Gradle")
    },
    pluginAuto("intellij.junit") { spec ->
      spec.withModule("intellij.junit.rt", "junit-rt.jar")
      spec.withModule("intellij.junit.v5.rt", "junit5-rt.jar")
    },
    plugin("intellij.testng") { spec ->
      spec.mainJarName = "testng-plugin.jar"
      spec.withModule("intellij.testng.rt", "testng-rt.jar")
      spec.withProjectLibrary("TestNG")
    },
    pluginAuto(listOf("intellij.devkit")) { spec ->
      spec.withModule("intellij.devkit.jps")
      spec.withModule("intellij.devkit.runtimeModuleRepository.jps")

      spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_PUBLIC_BUILDS
    },
    pluginAuto(listOf("intellij.eclipse")) { spec ->
      spec.withModule("intellij.eclipse.jps", "eclipse-jps.jar")
      spec.withModule("intellij.eclipse.common", "eclipse-common.jar")
    },
    plugin("intellij.java.coverage") { spec ->
      spec.withModule("intellij.java.coverage.rt")
      // explicitly pack JaCoCo as a separate JAR
      spec.withModuleLibrary("JaCoCo", "intellij.java.coverage", "jacoco.jar")
    },
    plugin("intellij.java.decompiler") { spec ->
      spec.directoryName = "java-decompiler"
      spec.mainJarName = "java-decompiler.jar"
      spec.withModule("intellij.java.decompiler.engine", spec.mainJarName)
    },
    pluginAuto("intellij.terminal") { spec ->
      spec.withModule("intellij.terminal.completion")
      spec.withResource("resources/shell-integrations", "shell-integrations")
    },
    pluginAuto("intellij.emojipicker") { spec ->
      spec.bundlingRestrictions.supportedOs = persistentListOf(OsFamily.LINUX)
    },
    pluginAuto(listOf("intellij.textmate")) { spec ->
      spec.withResource("lib/bundles", "lib/bundles")
    },
    PythonCommunityPluginModules.pythonCommunityPluginLayout(),
    pluginAuto(listOf("intellij.completionMlRankingModels")) { spec ->
      spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_RELEASE
    },
    pluginAuto(listOf("intellij.statsCollector")) { spec ->
      spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_RELEASE
    },
    pluginAuto(listOf("intellij.lombok", "intellij.lombok.generated")),
    plugin("intellij.platform.testFramework.ui") { spec ->
      spec.withModuleLibrary("intellij.remoterobot.remote.fixtures", spec.mainModule, "")
      spec.withModuleLibrary("intellij.remoterobot.robot.server.core", spec.mainModule, "")
      spec.withProjectLibrary("okhttp")
    },
    pluginAuto(
      listOf(
        "intellij.performanceTesting",
        "intellij.tools.ide.starter.bus",
        "intellij.driver.model",
        "intellij.driver.impl",
        "intellij.driver.client"
      )
    ),
    pluginAuto(listOf("intellij.performanceTesting.ui"))
  )

  fun githubPlugin(mainModuleName: String): PluginLayout {
    return plugin(mainModuleName) { spec ->
      spec.directoryName = "vcs-github"
      spec.mainJarName = "vcs-github.jar"
      spec.withModules(listOf(
        "intellij.vcs.github"
      ))
    }
  }

  fun groovyPlugin(additionalModules: List<String> = emptyList(), addition: ((PluginLayout.PluginLayoutSpec) -> Unit)? = null): PluginLayout {
    return plugin("intellij.groovy") { spec ->
      spec.directoryName = "Groovy"
      spec.mainJarName = "Groovy.jar"
      spec.withModules(listOf(
        "intellij.groovy.psi",
        "intellij.groovy.structuralSearch",
        "intellij.groovy.git",
      ))
      spec.withModule("intellij.groovy.jps", "groovy-jps.jar")
      spec.withModule("intellij.groovy.rt", "groovy-rt.jar")
      spec.withModule("intellij.groovy.spock.rt", "groovy-spock-rt.jar")
      spec.withModule("intellij.groovy.rt.classLoader", "groovy-rt-class-loader.jar")
      spec.withModule("intellij.groovy.constants.rt", "groovy-constants-rt.jar")
      spec.withModules(additionalModules)

      spec.excludeFromModule("intellij.groovy.psi", "standardDsls/**")
      spec.withResource("groovy-psi/resources/standardDsls", "lib/standardDsls")
      spec.withResource("hotswap/gragent.jar", "lib/agent")
      spec.withResource("groovy-psi/resources/conf", "lib")
      addition?.invoke(spec)
    }
  }
}
