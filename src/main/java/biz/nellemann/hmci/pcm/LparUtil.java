package biz.nellemann.hmci.pcm;

public class LparUtil {

    public Integer id;
    public String uuid;
    public String name;
    public String state;
    public String type;
    public String osType;
    public Number affinityScore;

    public LparMemory memory;
    public LparProcessor processor;
    public Network network = new Network();
    public Storage storage = new Storage();

}
