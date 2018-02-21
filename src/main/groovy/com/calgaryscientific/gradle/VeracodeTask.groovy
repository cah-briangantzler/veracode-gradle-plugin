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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
abstract class VeracodeTask extends DefaultTask {
    abstract static final String NAME
    String app_id
    String sandbox_id
    VeracodeAPI veracodeAPI
    VeracodeSetup veracodeSetup
    VeracodeAPIWrapperFactory veracodeAPIWrapperFactory
    static Logger log = LoggerFactory.getLogger(VeracodeTask.class);
    File outputFile
    List<String> requiredArguments = []
    List<String> optionalArguments = []
    final static Map<String, String> validArguments = [
            'app_id'                  : '123',
            'sandbox_id'              : '123',
            'build_id'                : '123',
            'flaw_id'                 : '123',
            'flaw_id_list'            : '123',
            'build_version'           : 'xxx',
            'sandbox_name'            : 'xxx',
            'file_id'                 : '123',
            'maxUploadAttempts'       : '123',
            'waitTimeBetweenAttempts' : '123',
            'deleteUploadedArtifacts' : 'true',
            'ignoreFailure'           : 'true',
            'action'                  : '[comment|fp|appdesign|osenv|netenv|rejected|accepted]',
            'comment'                 : 'xxx',
    ]

    VeracodeTask() {
        group = 'Veracode'
    }

    abstract void run()

    protected static String correctUsage(String taskName,
                                         List<String> requiredArguments,
                                         List<String> optionalArguments) {
        StringBuilder sb = new StringBuilder("Missing required arguments for task ${taskName}:\n")
        sb.append( "veracodeSetup {\n")
        requiredArguments.each() { arg ->
            sb.append("  ${arg}=${validArguments.get(arg)}\n")
        }
        optionalArguments.each() { arg ->
            sb.append(" [${arg}=${validArguments.get(arg)}]\n")
        }
        sb.append( "}\n")
        sb.toString()
    }

    protected void setupTask() {
        veracodeSetup = project.findProperty("veracodeSetup") as VeracodeSetup
        app_id = veracodeSetup.app_id
        sandbox_id = veracodeSetup.sandbox_id
        log.info("[SetupTask] name=${name} app_id=${app_id} sandbox_id=${sandbox_id}")
        veracodeAPIWrapperFactory = new VeracodeAPIWrapperFactory(veracodeSetup.username, veracodeSetup.password, veracodeSetup.key, veracodeSetup.id)
        veracodeAPI = new VeracodeAPI(veracodeAPIWrapperFactory, app_id, sandbox_id)
    }

    @TaskAction
    final def vExecute() {
        logging.level = LogLevel.INFO
        setupTask()
        run()
    }

    // === utility methods ===

    /**
     * Fail the task if any the given required objects is null
     * @param objs
     */
    protected void failIfNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) {
                fail(correctUsage(this.name, this.requiredArguments, this.optionalArguments))
            }
        }
    }

    protected fail(String msg) {
        throw new GradleException(msg)
    }
}
