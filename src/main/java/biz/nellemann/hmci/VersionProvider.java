/*
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci;

import picocli.CommandLine;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws IOException {

        Manifest manifest = new Manifest(getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
        Attributes attrs = manifest.getMainAttributes();

        return new String[] { "${COMMAND-FULL-NAME} " + attrs.getValue("Build-Version") };
    }

}
