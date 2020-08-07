package biz.nellemann.hmci.pojo

class LogicalPartition {

    public String id
    public String name
    public String type

    protected List<String> pcmLinks

    LogicalPartition(String id) {
        this.id = id
    }

    String toString() {
        return "[${id}] ${name} (${type})"
    }

}
