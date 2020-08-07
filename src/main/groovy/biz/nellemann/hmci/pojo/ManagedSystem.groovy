package biz.nellemann.hmci.pojo

class ManagedSystem {

    public String id
    public String name
    public String type
    public String model
    public String serialNumber

    protected List<String> pcmLinks = new ArrayList<>()
    public Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()

    ManagedSystem(String id) {
        this.id = id
    }

    String toString() {
        return "[${id}] ${name} (${type}-${model} ${serialNumber})"
    }

}
