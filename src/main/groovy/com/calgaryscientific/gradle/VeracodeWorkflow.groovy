/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017 Calgary Scientific Incorporated
 *
 * Copyright (c) 2013-2014 kctang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.calgaryscientific.gradle

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class VeracodeWorkflow {
    private static Logger log = LoggerFactory.getLogger(VeracodeWorkflow.class);

    static void appWorkflow(VeracodeAPI veracodeAPI,
                            String outputDir,
                            String app_id,
                            String build_version,
                            Set<File> fileSet,
                            Set<String> moduleWhitelist,
                            Integer maxTries,
                            Integer waitTime,
                            Boolean delete
    ) {
        // Work on the latest build
        String build_id = null
        // Save to variable to do the API call only once
        Node buildInfo
        String emptyAppRegex = "Could not find a build for application=\\S+"
        String buildStatus

        // This call to writeXmlWithErrorCheck might throw an error since Veracode errors out for empty Apps
        try {
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getFile(outputDir, app_id, build_id), veracodeAPI.getBuildInfo(build_id))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
        } catch (Exception e) {
            if (e.toString() =~ emptyAppRegex) {
                buildStatus = e.toString()
            } else {
                throw e
            }
        }

        log.info("buildStatus: " + buildStatus)

        // Previous Scan is complete (results are ready) or this is an newly created App that has no existing builds
        if (buildStatus == "Results Ready" || buildStatus =~ emptyAppRegex) {
            log.info("createBuild: " + build_version)
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getFile(outputDir, app_id, build_id), veracodeAPI.createBuild(build_version))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }

        // Clean build or with some uploaded files
        if (buildStatus == "Incomplete") {
            log.info("uploadFile: " + fileSet)
            VeracodeUploadFile.uploadFiles(veracodeAPI, VeracodeFileList.getFile(outputDir, app_id, build_id), fileSet, maxTries, waitTime, delete)
            log.info("beginPreScan")
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getFile(outputDir, app_id, build_id), veracodeAPI.beginPreScan())
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }

        // Pre-Scan completed
        if (buildStatus == "Pre-Scan Success") {
            log.info("beginScan")
            Node preScanResultsXML = XMLIO.writeXmlWithErrorCheck(VeracodePreScanResults.getFile(outputDir, app_id, build_id), veracodeAPI.getPreScanResults(build_id))
            VeracodePreScanResults.printModuleStatus(preScanResultsXML)
            Set<String> moduleIds = VeracodePreScanResults.extractWhitelistModuleIds(preScanResultsXML, moduleWhitelist)
            log.info("Module IDs: " + moduleIds.join(","))
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getFile(outputDir, app_id, build_id), veracodeAPI.beginScan(moduleIds))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }
    }

    static void sandboxWorkflow(VeracodeAPI veracodeAPI,
                                String outputDir,
                                String app_id,
                                String sandbox_id,
                                String build_version,
                                Set<File> fileSet,
                                Set<String> moduleWhitelist,
                                Integer maxTries,
                                Integer waitTime,
                                Boolean delete
    ) {
        // Work on the latest build
        String build_id = null
        // Save to variable to do the API call only once
        Node buildInfo
        String emptySandboxRegex = "Could not find a build for application=\\S+ and sandbox=\\S+"
        String buildStatus

        // This call to writeXmlWithErrorCheck might throw an error since Veracode errors out for empty Sandboxes
        try {
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getSandboxFile(outputDir, app_id, sandbox_id, build_id), veracodeAPI.getBuildInfoSandbox(build_id))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
        } catch (Exception e) {
            if (e.toString() =~ emptySandboxRegex) {
                buildStatus = e.toString()
            } else {
                throw e
            }
        }

        log.info("buildStatus: " + buildStatus)

        // Previous Scan is complete (results are ready) or this is an newly created Sandbox that has no existing builds
        if (buildStatus == "Results Ready" || buildStatus =~ emptySandboxRegex) {
            log.info("createBuild: " + build_version)
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getSandboxFile(outputDir, app_id, sandbox_id, build_id), veracodeAPI.createBuildSandbox(build_version))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }

        // Clean build or with some uploaded files
        if (buildStatus == "Incomplete") {
            log.info("uploadFile: " + fileSet)
            VeracodeUploadFile.uploadSandboxFiles(veracodeAPI, VeracodeFileList.getSandboxFile(outputDir, app_id, sandbox_id, build_id), fileSet, maxTries, waitTime, delete)
            log.info("beginPreScan")
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getSandboxFile(outputDir, app_id, sandbox_id, build_id), veracodeAPI.beginPreScanSandbox())
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }

        // Pre-Scan completed
        if (buildStatus == "Pre-Scan Success") {
            log.info("beginScan")
            Node preScanResultsXML = XMLIO.writeXmlWithErrorCheck(VeracodePreScanResults.getSandboxFile(outputDir, app_id, sandbox_id, build_id), veracodeAPI.getPreScanResultsSandbox(build_id))
            VeracodePreScanResults.printModuleStatus(preScanResultsXML)
            Set<String> moduleIds = VeracodePreScanResults.extractWhitelistModuleIds(preScanResultsXML, moduleWhitelist)
            log.info("Module IDs: " + moduleIds.join(","))
            buildInfo = XMLIO.writeXmlWithErrorCheck(VeracodeBuildInfo.getSandboxFile(outputDir, app_id, sandbox_id, build_id), veracodeAPI.beginScanSandbox(moduleIds))
            buildStatus = VeracodeBuildInfo.getBuildStatus(buildInfo)
            log.info("buildStatus: " + buildStatus)
        }
    }
}
