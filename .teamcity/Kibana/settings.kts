/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package Kibana

import co.elastic.teamcity.common.TeamLevelProject
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

version = "2020.2"

val PRODUCTION_BRANCHES = listOf("master", "7.x")
val SANDBOX_BRANCHES = listOf("master_teamcity", "7.x_teamcity")

project(TeamLevelProject {
    id("Kibana")
    uuid = "4aef8f04-c7bc-464d-880d-b83545107160"
    name = "Kibana"

    PRODUCTION_BRANCHES.forEach { createKibanaSubProject(it, it) }
    SANDBOX_BRANCHES.forEach { createKibanaSubProject("$it sandbox", it) }
})

fun TeamLevelProject.createKibanaSubProject(projectName: String, branch: String) {
    val project = this

    subProject {
        id("${project.id.toString()}_${branch.replace('.', '_')}")
        name = projectName

        val kotlinDslRoot = createVcsRoot(project.id.toString(), this.name, branch)
        this.vcsRoot(kotlinDslRoot)

        features {
            versionedSettings {
                rootExtId = kotlinDslRoot.id.toString()
                mode = VersionedSettings.Mode.ENABLED
                buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
                settingsFormat = VersionedSettings.Format.KOTLIN
                storeSecureParamsOutsideOfVcs = true

                // Branch-specific projects use the "portable" DSL format
                param("useRelativeIds", "true")
                param("projectBranch", branch)
            }
        }
    }
}

fun createVcsRoot(projectId: String, projectName: String, branchName: String): GitVcsRoot {
    return GitVcsRoot {
        id("${projectId}_${branchName.replace('.', '_')}")

        name = "$projectName ($branchName)"
        url = "https://github.com/elastic/${projectName.toLowerCase()}.git"
        branch = "refs/heads/$branchName"
    }
}