// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("LiftReturnOrAssignment")

package org.jetbrains.intellij.build

import com.intellij.platform.diagnostic.telemetry.helpers.useWithScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.intellij.build.impl.*
import org.jetbrains.intellij.build.impl.PluginLayout.Companion.plugin
import org.jetbrains.intellij.build.impl.PluginLayout.Companion.pluginAuto
import org.jetbrains.intellij.build.impl.projectStructureMapping.DistributionFileEntry
import org.jetbrains.intellij.build.impl.projectStructureMapping.ProjectLibraryEntry
import org.jetbrains.intellij.build.io.copyDir
import org.jetbrains.intellij.build.io.copyFileToDir
import org.jetbrains.intellij.build.kotlin.KotlinPluginBuilder
import org.jetbrains.intellij.build.python.PythonCommunityPluginModules
import org.jetbrains.jps.model.library.JpsOrderRootType
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object CommunityRepositoryModules {
  /**
   * Specifies non-trivial layout for all plugins that sources are located in 'community' and 'contrib' repositories
   */
  val COMMUNITY_REPOSITORY_PLUGINS: PersistentList<PluginLayout> = persistentListOf(
    plugin("intellij.ant") { spec ->
      spec.mainJarName = "antIntegration.jar"
      spec.withModule("intellij.ant.jps", "ant-jps.jar")

      spec.withGeneratedResources { dir, buildContext ->
        copyAnt(pluginDir = dir, context = buildContext)
      }
    },
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
    plugin("intellij.properties") { spec ->
      spec.withModule("intellij.properties.psi", "properties.jar")
      spec.withModule("intellij.properties.psi.impl", "properties.jar")
    },
    plugin("intellij.vcs.git") { spec ->
      spec.withModule("intellij.vcs.git.rt", "git4idea-rt.jar")
    },
    plugin("intellij.vcs.svn") { spec ->
      spec.withProjectLibrary("sqlite")
    },
    plugin("intellij.jsonpath") { spec ->
      spec.withProjectLibrary("jsonpath")
    },
    plugin("intellij.xpath") { spec ->
      spec.withModule("intellij.xpath.rt", "rt/xslt-rt.jar")
    },
    plugin("intellij.platform.langInjection") { spec ->
      spec.withModule("intellij.java.langInjection", "IntelliLang.jar")
      spec.withModule("intellij.xml.langInjection", "IntelliLang.jar")
      spec.withModule("intellij.java.langInjection.jps")
    },
    plugin("intellij.tasks.core") { spec ->
      spec.directoryName = "tasks"
      spec.withModule("intellij.tasks")
      spec.withModule("intellij.tasks.compatibility")
      spec.withModule("intellij.tasks.jira")
      spec.withModule("intellij.tasks.java")
      spec.withProjectLibrary("XmlRPC")
      spec.withProjectLibrary("jsonpath")
    },
    plugin("intellij.xslt.debugger") { spec ->
      spec.withModule("intellij.xslt.debugger.rt", "xslt-debugger-rt.jar")
      spec.withModule("intellij.xslt.debugger.impl.rt", "rt/xslt-debugger-impl-rt.jar")
      spec.withModuleLibrary("Saxon-6.5.5", "intellij.xslt.debugger.impl.rt", "rt/saxon.jar")
      spec.withModuleLibrary("Saxon-9HE", "intellij.xslt.debugger.impl.rt", "rt/saxon9he.jar")
      spec.withModuleLibrary("Xalan-2.7.2", "intellij.xslt.debugger.impl.rt", "rt/xalan-2.7.2.jar")
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
    plugin(listOf(
      "intellij.gradle",
      "intellij.gradle.common",
      "intellij.gradle.toolingProxy",
    )) { spec ->
      spec.withModule("intellij.gradle.toolingExtension", "gradle-tooling-extension-api.jar")
      spec.withModule("intellij.gradle.toolingExtension.impl", "gradle-tooling-extension-impl.jar")
      spec.withProjectLibrary("Gradle", LibraryPackMode.STANDALONE_SEPARATE)
      spec.withProjectLibrary("Ant", "ant", LibraryPackMode.STANDALONE_SEPARATE)
    },
    pluginAuto(listOf(
      "intellij.packageSearch",
      "intellij.packageSearch.gradle",
      "intellij.packageSearch.maven",
      "intellij.packageSearch.kotlin",
    )) { spec ->
      spec.withModule("intellij.packageSearch.gradle.tooling", "pkgs-tooling-extension.jar")
    },
    plugin(listOf("intellij.gradle.java", "intellij.gradle.jps")),
    plugin("intellij.junit") { spec ->
      spec.mainJarName = "idea-junit.jar"
      spec.withModule("intellij.junit.rt", "junit-rt.jar")
      spec.withModule("intellij.junit.v5.rt", "junit5-rt.jar")
    },
    //plugin("intellij.korgeforge") { spec ->
    //  spec.directoryName = "KorgePlugin"
    //  //spec.mainJarName = "korge-forge-plugin.jar"
    //  spec.withModules(listOf())
    //  //spec.excludeFromModule("intellij.korgeforge", "META-INF/plugin.xml")
    //  // file://$MODULE_DIR$/../../korge-forge-plugin/build/distributions/KorgePlugin/KorgePlugin/lib
    //  //spec.withResource("../../korge-forge-plugin/build/distributions/KorgePlugin/KorgePlugin/lib", "lib")
    //  //spec.withResource("", "KorgePlugin/")
    //},
    plugin("intellij.java.byteCodeViewer") { spec ->
      spec.mainJarName = "byteCodeViewer.jar"
    },
    plugin("intellij.testng") { spec ->
      spec.mainJarName = "testng-plugin.jar"
      spec.withModule("intellij.testng.rt", "testng-rt.jar")
      spec.withProjectLibrary("TestNG")
    },
    plugin("intellij.dev") { spec ->
      spec.withModule("intellij.dev.psiViewer")
      spec.withModule("intellij.dev.codeInsight")
      spec.withModule("intellij.java.dev")
      spec.withModule("intellij.groovy.dev")
      spec.withModule("intellij.kotlin.dev")
      spec.withModule("intellij.platform.statistics.devkit")
    },
    //plugin("intellij.devkit") { spec ->
    //  spec.withModule("intellij.devkit.core")
    //  spec.withModule("intellij.devkit.git")
    //  spec.withModule("intellij.devkit.themes")
    //  spec.withModule("intellij.devkit.gradle")
    //  spec.withModule("intellij.devkit.i18n")
    //  spec.withModule("intellij.devkit.images")
    //  spec.withModule("intellij.devkit.intelliLang")
    //  spec.withModule("intellij.devkit.uiDesigner")
    //  spec.withModule("intellij.devkit.workspaceModel")
    //  spec.withModule("intellij.kotlin.devkit")
    //  spec.withModule("intellij.devkit.jps")
    //  spec.withModule("intellij.devkit.runtimeModuleRepository.jps")
    //
    //  spec.withProjectLibrary("workspace-model-codegen")
    //
    //  spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_PUBLIC_BUILDS
    //},
    plugin("intellij.eclipse") { spec ->
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
    plugin("intellij.terminal") { spec ->
      spec.withModule("intellij.terminal.completion")
      spec.withModule("intellij.terminal.sh")
      spec.withResource("resources/shell-integrations", "shell-integrations")
    },
    plugin("intellij.emojipicker") { spec ->
      spec.bundlingRestrictions.supportedOs = persistentListOf(OsFamily.LINUX)
    },
    plugin("intellij.textmate") { spec ->
      spec.withModule("intellij.textmate.core")
      spec.withResource("lib/bundles", "lib/bundles")
    },
    PythonCommunityPluginModules.pythonCommunityPluginLayout(),
    plugin("intellij.completionMlRankingModels") { spec ->
      spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_RELEASE
    },
    plugin("intellij.statsCollector") { spec ->
      spec.bundlingRestrictions.includeInDistribution = PluginDistribution.NOT_FOR_RELEASE
    },
    plugin(listOf("intellij.lombok", "intellij.lombok.generated")),
    plugin(listOf(
      "intellij.grazie",
      "intellij.grazie.core",
      "intellij.grazie.java",
      "intellij.grazie.json",
      "intellij.grazie.markdown",
      "intellij.grazie.properties",
      "intellij.grazie.xml",
      "intellij.grazie.yaml",
    )),
    plugin(listOf(
      "intellij.toml",
      "intellij.toml.core",
      "intellij.toml.json",
      "intellij.toml.grazie",
    )),
    plugin(listOf(
      "intellij.markdown",
      "intellij.markdown.fenceInjection",
      "intellij.markdown.frontmatter",
      "intellij.markdown.frontmatter.yaml",
      "intellij.markdown.frontmatter.toml",
      "intellij.markdown.images",
      "intellij.markdown.xml",
      "intellij.markdown.model",
      "intellij.markdown.spellchecker"
    )),
    plugin(listOf("intellij.settingsSync", "intellij.settingsSync.git")),
    plugin(listOf(
      "intellij.sh",
      "intellij.sh.core",
      "intellij.sh.terminal",
      "intellij.sh.copyright",
      "intellij.sh.python",
      "intellij.sh.markdown",
    )),
    plugin("intellij.featuresTrainer") { spec ->
      spec.withModule("intellij.vcs.git.featuresTrainer")
      spec.withProjectLibrary("assertJ")
      spec.withProjectLibrary("assertj-swing")
      spec.withProjectLibrary("git-learning-project")
    },
    plugin(listOf(
      "intellij.searchEverywhereMl",
      "intellij.searchEverywhereMl.ranking.ext",
      "intellij.searchEverywhereMl.ranking.core",
      "intellij.searchEverywhereMl.ranking.yaml",
      "intellij.searchEverywhereMl.ranking.vcs",
      "intellij.searchEverywhereMl.typos",
      "intellij.searchEverywhereMl.semantics"
    )) { spec ->
      spec.withModule("intellij.searchEverywhereMl.semantics.java")
      spec.withModule("intellij.searchEverywhereMl.semantics.kotlin")
      spec.withModule("intellij.searchEverywhereMl.semantics.testCommands")
    },
    plugin("intellij.platform.testFramework.ui") { spec ->
      spec.withModuleLibrary("intellij.remoterobot.remote.fixtures", spec.mainModule, "")
      spec.withModuleLibrary("intellij.remoterobot.robot.server.core", spec.mainModule, "")
      spec.withProjectLibrary("okhttp")
    },
    plugin("intellij.editorconfig") { spec ->
      spec.withProjectLibrary("ec4j-core")
    },
    plugin(
      "intellij.turboComplete",
    ) { spec ->
      spec.withModule("intellij.turboComplete.languages.kotlin")
    },
    pluginAuto(listOf("intellij.performanceTesting", "intellij.performanceTesting.remoteDriver")) { spec ->
      spec.withModule("intellij.driver.model")
      spec.withModule("intellij.driver.impl")
      spec.withModule("intellij.driver.client")
    }
  )

  val CONTRIB_REPOSITORY_PLUGINS: PersistentList<PluginLayout> = persistentListOf(
    plugin("intellij.errorProne") { spec ->
      spec.withModule("intellij.errorProne.jps", "jps/errorProne-jps.jar")
    },
    plugin("intellij.cucumber.java") { spec ->
      spec.withModule("intellij.cucumber.jvmFormatter", "cucumber-jvmFormatter.jar")
      spec.withModule("intellij.cucumber.jvmFormatter3", "cucumber-jvmFormatter3.jar")
      spec.withModule("intellij.cucumber.jvmFormatter4", "cucumber-jvmFormatter4.jar")
      spec.withModule("intellij.cucumber.jvmFormatter5", "cucumber-jvmFormatter5.jar")
    },
    plugin("intellij.protoeditor") { spec ->
      spec.withModule("intellij.protoeditor.core")
      spec.withModule("intellij.protoeditor.go")
      spec.withModule("intellij.protoeditor.jvm")
      spec.withModule("intellij.protoeditor.python")
    },
    plugin("intellij.serial.monitor") { spec ->
      spec.withProjectLibrary("io.github.java.native.jssc", LibraryPackMode.STANDALONE_SEPARATE)
    },
    plugin("intellij.dts") { spec ->
      spec.withModule("intellij.dts.pp")
    }
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


  @JvmStatic
  @JvmOverloads
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

private suspend fun copyAnt(pluginDir: Path, context: BuildContext): List<DistributionFileEntry> {
  val antDir = pluginDir.resolve("dist")
  return TraceManager.spanBuilder("copy Ant lib").setAttribute("antDir", antDir.toString()).useWithScope {
    val sources = ArrayList<ZipSource>()
    val libraryData = ProjectLibraryData(libraryName = "Ant", packMode = LibraryPackMode.MERGED, reason = "ant")
    copyDir(
      sourceDir = context.paths.communityHomeDir.resolve("lib/ant"),
      targetDir = antDir,
      dirFilter = { !it.endsWith("src") },
      fileFilter = { file ->
        if (file.toString().endsWith(".jar")) {
          sources.add(ZipSource(file = file, distributionFileEntryProducer = null))
          false
        }
        else {
          true
        }
      },
    )
    sources.sort()

    val antTargetFile = antDir.resolve("ant.jar")
    buildJar(targetFile = antTargetFile, sources = sources)

    sources.map { source ->
      ProjectLibraryEntry(
        path = antTargetFile,
        data = libraryData,
        libraryFile = source.file,
        hash = source.hash,
        size = source.size,
        relativeOutputFile = "dist/ant.jar",
      )
    }
  }
}
