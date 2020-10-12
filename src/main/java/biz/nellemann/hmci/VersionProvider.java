package biz.nellemann.hmci;

import picocli.CommandLine;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

class VersionProvider implements CommandLine.IVersionProvider {

    public String[] getVersion() throws IOException {

        Manifest manifest = new Manifest(getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
        Attributes attrs = manifest.getMainAttributes();

        return new String[] { "${COMMAND-FULL-NAME} " + attrs.getValue("Build-Version") };
    }

}
