package biz.nellemann.hmci.dto.toml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HmcConfiguration {

    public String url;
    public String name;
    public String username;
    public String password;

    public Integer refresh = 60;
    public Integer discover = 120;

    public String trace;
    public Boolean energy = true;
    public Boolean trust = true;

    public List<String> excludeSystems = new ArrayList<>();
    public List<String> includeSystems = new ArrayList<>();
    public List<String> excludePartitions = new ArrayList<>();
    public List<String> includePartitions = new ArrayList<>();

}
