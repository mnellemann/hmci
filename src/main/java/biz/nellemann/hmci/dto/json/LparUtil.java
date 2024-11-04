package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LparUtil {

    public String id = "";
    public String uuid = "";
    public String name = "";
    public String state = "";
    public String type = "";
    public String osType = "";
    public double affinityScore = 0.0;

    public final LparMemory memory = new LparMemory();
    public final LparProcessor processor = new LparProcessor();
    public final Network network = new Network();
    public final Storage storage = new Storage();

}
