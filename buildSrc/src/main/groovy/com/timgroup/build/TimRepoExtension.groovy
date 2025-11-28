package com.timgroup.build

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

class TimRepoExtension {
    final Provider<String> nexusRepoUrl
    final Provider<String> nexusRepoUsername
    final Provider<String> nexusRepoPassword
    final Provider<String> codeartifactUrl
    final Provider<String> codeartifactToken

    @Inject
    TimRepoExtension(ProviderFactory providers) {
        nexusRepoUrl = providers.gradleProperty("repoUrl")
        nexusRepoUsername = providers.gradleProperty("repoUsername")
        nexusRepoPassword = providers.gradleProperty("repoPassword")
        codeartifactUrl = providers.environmentVariable("CODEARTIFACT_URL")
                .orElse(providers.gradleProperty("codeartifact.url"))
                .orElse("https://timgroup-148217964156.d.codeartifact.eu-west-1.amazonaws.com/maven/jars/")
        codeartifactToken = providers.environmentVariable("CODEARTIFACT_TOKEN")
                .orElse(providers.gradleProperty("codeartifact.token"))
    }
}
