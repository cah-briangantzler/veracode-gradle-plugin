/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017-2018 Calgary Scientific Incorporated
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

@CompileStatic
class VeracodeBuildList {
    static File getFile(String dir, String app_id) {
        return new File(dir, "buildlist-${app_id}-latest.xml")
    }

    static File getSandboxFile(String dir, String app_id, String sandbox_id) {
        return new File(dir, "buildlist-${app_id}-${sandbox_id}-latest.xml")
    }

    static void printBuildList(Node xml) {
        String app_id = xml.attribute('app_id') as String
        String sandbox_id = xml.attribute('sandbox_id') as String
        XMLIO.getNodeList(xml, 'build').each { build ->
            printf "app_id=%s ", app_id
            if (sandbox_id) {
                printf "sandbox_id=%s ", sandbox_id
            }
            printf "build_id=%-10s date=%-25s version=\"%s\"\n",
                    XMLIO.getNodeAttributes(build, 'build_id', 'policy_updated_date', 'version')
        }
    }

    static String getLatestBuildID(Node xml) {
        XMLIO.getNodeList(xml, 'build').last().attribute('build_id')
    }
}
