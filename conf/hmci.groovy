/*
 Configuration for HMCi
 */

hmci.refresh = 60
hmci.rescan = 15

// InfluxDB to save metrics
influx {
    url = "http://10.32.64.29:8086"
    username = "root"
    password = ""
    database = "hmci"

}

// One or more HMC to query for data and metrics
hmc {

    // HMC on our primary site
    site1 {
        url = "https://10.32.64.39:12443"
        username = "hmci"
        password = "hmcihmci"
        unsafe = true
    }

    /*
    site2 {
        url = "https://10.32.64.39:12443"
        username = "viewer"
        password = "someSecret"
        unsafe = false
    }
    */

}
