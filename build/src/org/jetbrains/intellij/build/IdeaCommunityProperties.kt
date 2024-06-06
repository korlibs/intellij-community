// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.intellij.build

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import org.jetbrains.intellij.build.BuildPaths.Companion.COMMUNITY_ROOT
import org.jetbrains.intellij.build.impl.BuildContextImpl
import org.jetbrains.intellij.build.io.copyDir
import org.jetbrains.intellij.build.io.copyFileToDir
import org.jetbrains.intellij.build.kotlin.KotlinBinaries
import java.nio.file.Path

internal suspend fun createCommunityBuildContext(
  options: BuildOptions = BuildOptions(),
  projectHome: Path = COMMUNITY_ROOT.communityRoot,
): BuildContext {
  return BuildContextImpl.createContext(projectHome = projectHome,
                                        productProperties = IdeaCommunityProperties(COMMUNITY_ROOT.communityRoot),
                                        setupTracer = true,
                                        options = options)
}

open class IdeaCommunityProperties(private val communityHomeDir: Path) : BaseIdeaProperties() {
  companion object {
    val MAVEN_ARTIFACTS_ADDITIONAL_MODULES = persistentListOf(
      "intellij.tools.jps.build.standalone",
      "intellij.devkit.runtimeModuleRepository.jps",
      "intellij.idea.community.build.tasks",
      "intellij.platform.debugger.testFramework",
      "intellij.platform.vcs.testFramework",
      "intellij.platform.externalSystem.testFramework",
      "intellij.maven.testFramework",
      "intellij.platform.reproducibleBuilds.diffTool",
    )
  }

  override val baseFileName: String get() = "korge"

  init {
    platformPrefix = "Idea"
    //platformPrefix = "Korge"
    applicationInfoModule = "intellij.idea.community.customization"
    additionalIDEPropertiesFilePaths = persistentListOf(communityHomeDir.resolve("build/conf/KorgeForge.properties"))
    toolsJarRequired = true
    scrambleMainJar = false
    useSplash = true
    buildCrossPlatformDistribution = true

    /* main module for JetBrains Client isn't available in the intellij-community project,
       so this property is set only when IDEA CE is built from the intellij-ultimate project. */
    embeddedJetBrainsClientMainModule = null

    productLayout.productImplementationModules = listOf(
      "intellij.platform.main",
      "intellij.idea.community.customization",
    )
    productLayout.bundledPluginModules = IDEA_BUNDLED_PLUGINS
      .addAll(listOf(
        "intellij.vcs.github.community",
        //"intellij.korgeforge",
      ))
      .toMutableList()

    productLayout.prepareCustomPluginRepositoryForPublishedPlugins = false
    productLayout.buildAllCompatiblePlugins = false
    productLayout.pluginLayouts = CommunityRepositoryModules.COMMUNITY_REPOSITORY_PLUGINS.addAll(listOf(
      JavaPluginLayout.javaPlugin(),
      CommunityRepositoryModules.groovyPlugin(),
      CommunityRepositoryModules.githubPlugin("intellij.vcs.github.community"),
    ))

    productLayout.addPlatformSpec { layout, _ ->
      layout.withModule("intellij.platform.duplicates.analysis")
      layout.withModule("intellij.platform.structuralSearch")
    }

    mavenArtifacts.forIdeModules = true
    mavenArtifacts.additionalModules = mavenArtifacts.additionalModules.addAll(MAVEN_ARTIFACTS_ADDITIONAL_MODULES)
    mavenArtifacts.squashedModules = mavenArtifacts.squashedModules.addAll(persistentListOf(
      "intellij.platform.util.base",
      "intellij.platform.util.zip",
    ))

    versionCheckerConfig = CE_CLASS_VERSIONS
    baseDownloadUrl = "https://download.jetbrains.com/idea/"
    //baseDownloadUrl = "https://forge.korge.org/download/"
    buildDocAuthoringAssets = true

    additionalVmOptions += "-Dide.show.tips.on.startup.default.value=false"
  }

  override suspend fun copyAdditionalFiles(context: BuildContext, targetDir: Path) {
    super.copyAdditionalFiles(context, targetDir)

    copyFileToDir(context.paths.communityHomeDir.resolve("LICENSE.txt"), targetDir)
    copyFileToDir(context.paths.communityHomeDir.resolve("NOTICE.txt"), targetDir)

    copyDir(
      sourceDir = context.paths.communityHomeDir.resolve("build/conf/KorgeForge/common/bin"),
      targetDir = targetDir.resolve("bin"),
    )
    bundleExternalPlugins(context, targetDir)
  }

  override fun getAdditionalPluginPaths(context: BuildContext): List<Path> {
    return listOf(
      //context.paths.projectHome.resolve("../korge-forge-plugin/build/distributions/KorgePlugin.zip")
      context.paths.projectHome.resolve("../korge-forge-plugin/build/distributions/KorgePlugin/KorgePlugin")
    )
  }

  protected open fun bundleExternalPlugins(context: BuildContext, targetDirectory: Path) {
    //temporary unbundle VulnerabilitySearch
    //com.intellij.util.io.Decompressor.Zip(File(context.paths.projectHome.toFile(), "../korge-forge-plugin/build/distributions/KorgePlugin.zip")).extract(targetDirectory.resolve("plugins"))
    //ExternalPluginBundler.bundle('VulnerabilitySearch',
    //                             "$buildContext.paths.communityHome/build/dependencies",
    //                             buildContext, targetDirectory)
  }

  override fun createWindowsCustomizer(projectHome: String): WindowsDistributionCustomizer = CommunityWindowsDistributionCustomizer()
  override fun createLinuxCustomizer(projectHome: String): LinuxDistributionCustomizer = CommunityLinuxDistributionCustomizer()
  override fun createMacCustomizer(projectHome: String): MacDistributionCustomizer = CommunityMacDistributionCustomizer()

  protected open inner class CommunityWindowsDistributionCustomizer : WindowsDistributionCustomizer() {
    init {
      icoPath = "${communityHomeDir}/build/conf/KorgeForge/win/images/idea_CE.ico"
      icoPathForEAP = "${communityHomeDir}/build/conf/KorgeForge/win/images/idea_CE_EAP.ico"
      installerImagesPath = "${communityHomeDir}/build/conf/KorgeForge/win/images"
      fileAssociations = listOf("gradle", "kt", "kts")
    }

    override fun getFullNameIncludingEdition(appInfo: ApplicationInfoProperties) = "KorGE Forge"

    override fun getFullNameIncludingEditionAndVendor(appInfo: ApplicationInfoProperties) = "KorGE Forge"

    override fun getUninstallFeedbackPageUrl(appInfo: ApplicationInfoProperties): String {
      return "https://forge.korge.org/uninstall/?edition=IC-${appInfo.majorVersion}.${appInfo.minorVersion}"
    }
  }

  protected open inner class CommunityLinuxDistributionCustomizer : LinuxDistributionCustomizer() {
    init {
      iconPngPath = "${communityHomeDir}/build/conf/KorgeForge/linux/images/icon_CE_128.png"
      iconPngPathForEAP = "${communityHomeDir}/build/conf/KorgeForge/linux/images/icon_CE_EAP_128.png"
      snapName = "korge-forge"
      snapDescription = "KorGE Forge IDE to create VideoGames in Kotlin."
    }

    override fun getRootDirectoryName(appInfo: ApplicationInfoProperties, buildNumber: String) = "korge-$buildNumber"

    override fun generateExecutableFilesPatterns(context: BuildContext, includeRuntime: Boolean, arch: JvmArchitecture): List<String> {
      return super.generateExecutableFilesPatterns(context, includeRuntime, arch)
        .plus(KotlinBinaries.kotlinCompilerExecutables)
        .filterNot { it == "plugins/**/*.sh" }
    }
  }

  protected open inner class CommunityMacDistributionCustomizer : MacDistributionCustomizer() {
    init {
      icnsPath = "${communityHomeDir}/build/conf/KorgeForge/mac/images/idea.icns"
      icnsPathForEAP = "${communityHomeDir}/build/conf/KorgeForge/mac/images/communityEAP.icns"
      urlSchemes = listOf("korgeforge")
      associateIpr = true
      fileAssociations = FileAssociation.from("kt", "kts")
      bundleIdentifier = "org.korge.forge"
      dmgImagePath = "${communityHomeDir}/build/conf/KorgeForge/mac/images/dmg_background.tiff"
    }

    override fun getRootDirectoryName(appInfo: ApplicationInfoProperties, buildNumber: String): String {
      return if (appInfo.isEAP) {
        "KorGE Forge ${appInfo.majorVersion}.${appInfo.minorVersionMainPart} EAP.app"
      }
      else {
        "KorGE Forge.app"
      }
    }

    override fun generateExecutableFilesPatterns(context: BuildContext, includeRuntime: Boolean, arch: JvmArchitecture): List<String> {
      return super.generateExecutableFilesPatterns(context, includeRuntime, arch).asSequence()
        .plus(KotlinBinaries.kotlinCompilerExecutables)
        .filterNot { it == "plugins/**/*.sh" }
        .toList()
    }
  }

  override fun getSystemSelector(appInfo: ApplicationInfoProperties, buildNumber: String): String {
    return "Korge${appInfo.majorVersion}.${appInfo.minorVersionMainPart}"
  }

  override fun getBaseArtifactName(appInfo: ApplicationInfoProperties, buildNumber: String) = "korgeforge-$buildNumber"

  override fun getOutputDirectoryName(appInfo: ApplicationInfoProperties) = "korgeforge"
}
