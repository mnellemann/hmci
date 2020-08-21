/*
    Copy this file to /etc/hmci.groovy and change it to suit your environment.
*/

// How often to query HMC's for data - in seconds
hmci.refresh = 30

// Rescan HMC's for new systems and partitions - every x refresh
hmci.rescan = 60

// InfluxDB to save metrics
influx {
    url = "http://localhost:8086"
    username = "root"
    password = ""
    database = "hmci"
}

// One or more HMC's to query for data and metrics
hmc {

    // HMC on our primary site
    site1 {
        url = "https://10.10.10.10:12443"
        username = "hmci"
        password = "hmcihmci"
        unsafe = true   // Ignore SSL cert. errors
    }

    /*
    site2 {
        url = "https://10.10.20.20:12443"
        username = "viewer"
        password = "someSecret"
        unsafe = false
    }
    */

}
