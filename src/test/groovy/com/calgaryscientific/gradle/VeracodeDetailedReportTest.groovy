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

class VeracodeDetailedReportTest extends TestCommonSetup {
    File detailedReportFile = getResource('detailedreport-1.5.xml')

    def 'Test veracodeDetailedReport Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeDetailedReport')
        task.veracodeSetup.build_id = '123'

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.detailedReport(_) >> {
            return new String(detailedReportFile.readBytes())
        }
        assert is.readLine() =~ 'report file: '
    }
}
