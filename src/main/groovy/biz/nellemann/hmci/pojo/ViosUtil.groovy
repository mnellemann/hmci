package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class ViosUtil {

    String id
    String uuid
    String name
    String state
    Integer affinityScore

    Memory memory
    LparProcessor processor
    Network network
    Storage storage

    class Memory {
        List<BigDecimal> assignedMem
        List<BigDecimal> utilizedMem
    }

    /*
        "viosUtil": [
          {
            "id": 1,
            "uuid": "2F30379A-860B-4661-A24E-CD8E449C81AC",
            "name": "VIOS1",
            "state": "Running",
            "affinityScore": 100,
            "memory": {
              "assignedMem": [
                8192.000
              ],
              "utilizedMem": [
                2061.000
              ]
            },
            "processor": {
              "weight": 0,
              "mode": "share_idle_procs_active",
              "maxVirtualProcessors": [
                2.000
              ],
              "currentVirtualProcessors": [
                0.000
              ],
              "maxProcUnits": [
                2.000
              ],
              "entitledProcUnits": [
                1.000
              ],
              "utilizedProcUnits": [
                0.006
              ],
              "utilizedCappedProcUnits": [
                0.079
              ],
              "utilizedUncappedProcUnits": [
                0.000
              ],
              "idleProcUnits": [
                0.073
              ],
              "donatedProcUnits": [
                0.921
              ],
              "timeSpentWaitingForDispatch": [
                0.000
              ],
              "timePerInstructionExecution": [
                51.000
              ]
            },
            "network": {
              "clientLpars": [
                "62F4D488-C838-41E2-B83B-E68E004E3B63"
              ],
              "genericAdapters": [
                {
                  "id": "ent2",
                  "type": "physical",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C10-T3",
                  "receivedPackets": [
                    29.733
                  ],
                  "sentPackets": [
                    25.900
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    7508.933
                  ],
                  "receivedBytes": [
                    5676.000
                  ],
                  "transferredBytes": [
                    13184.933
                  ]
                },
                {
                  "id": "ent6",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C3-T1",
                  "receivedPackets": [
                    12.967
                  ],
                  "sentPackets": [
                    9.700
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    3348.667
                  ],
                  "receivedBytes": [
                    2401.733
                  ],
                  "transferredBytes": [
                    5750.400
                  ]
                },
                {
                  "id": "ent4",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C2-T1",
                  "receivedPackets": [
                    26.900
                  ],
                  "sentPackets": [
                    33.800
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    8853.333
                  ],
                  "receivedBytes": [
                    8214.933
                  ],
                  "transferredBytes": [
                    17068.266
                  ]
                }
              ],
              "sharedAdapters": [
                {
                  "id": "ent5",
                  "type": "sea",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C2-T1",
                  "receivedPackets": [
                    56.633
                  ],
                  "sentPackets": [
                    59.700
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    16362.267
                  ],
                  "receivedBytes": [
                    13890.933
                  ],
                  "transferredBytes": [
                    30253.200
                  ],
                  "bridgedAdapters": [
                    "ent2",
                    "ent4",
                    "ent4"
                  ]
                }
              ],
              "virtualEthernetAdapters": [
                {
                  "physicalLocation": "U8247.22L.213C1BA-V1-C2",
                  "vlanId": 1,
                  "vswitchId": 0,
                  "isPortVlanId": true,
                  "receivedPackets": [
                    10.467
                  ],
                  "sentPackets": [
                    14.667
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    1931.333
                  ],
                  "receivedBytes": [
                    3875.433
                  ],
                  "receivedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalPackets": [
                    0.000
                  ],
                  "droppedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalBytes": [
                    0.000
                  ],
                  "receivedPhysicalBytes": [
                    0.000
                  ],
                  "transferredBytes": [
                    5806.766
                  ],
                  "transferredPhysicalBytes": [
                    0.000
                  ]
                },
                {
                  "physicalLocation": "U8247.22L.213C1BA-V1-C3",
                  "vlanId": 1,
                  "vswitchId": 0,
                  "isPortVlanId": true,
                  "receivedPackets": [
                    6.100
                  ],
                  "sentPackets": [
                    1.700
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    1420.533
                  ],
                  "receivedBytes": [
                    575.100
                  ],
                  "receivedPhysicalPackets": [
                    6.100
                  ],
                  "sentPhysicalPackets": [
                    1.700
                  ],
                  "droppedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalBytes": [
                    1420.533
                  ],
                  "receivedPhysicalBytes": [
                    575.100
                  ],
                  "transferredBytes": [
                    1995.633
                  ],
                  "transferredPhysicalBytes": [
                    1995.633
                  ]
                }
              ]
            },
            "storage": {
              "clientLpars": [
                "62F4D488-C838-41E2-B83B-E68E004E3B63"
              ],
              "genericPhysicalAdapters": [
                {
                  "id": "sissas0",
                  "type": "sas",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C14-T1",
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    4.533
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    2321.067
                  ],
                  "transmittedBytes": [
                    2321.067
                  ]
                }
              ],
              "genericVirtualAdapters": [
                {
                  "id": "vhost1",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C6",
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.000
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    0.000
                  ],
                  "transmittedBytes": [
                    0.000
                  ]
                },
                {
                  "id": "vhost0",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C5",
                  "numOfReads": [
                    0.500
                  ],
                  "numOfWrites": [
                    0.500
                  ],
                  "readBytes": [
                    256.000
                  ],
                  "writeBytes": [
                    256.000
                  ],
                  "transmittedBytes": [
                    512.000
                  ]
                },
                {
                  "id": "vhost2",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V1-C7",
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.000
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    0.000
                  ],
                  "transmittedBytes": [
                    0.000
                  ]
                }
              ],
              "fiberChannelAdapters": [
                {
                  "id": "fcs0",
                  "wwpn": "10000090faba5108",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C12-T1",
                  "numOfPorts": 3,
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.467
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    30583.467
                  ],
                  "runningSpeed": [
                    8.000
                  ],
                  "transmittedBytes": [
                    30583.467
                  ]
                },
                {
                  "id": "fcs1",
                  "wwpn": "10000090faba5109",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C12-T2",
                  "numOfPorts": 0,
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.000
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    0.000
                  ],
                  "runningSpeed": [
                    0.000
                  ],
                  "transmittedBytes": [
                    0.000
                  ]
                }
              ]
            }
          },
          {
            "id": 2,
            "uuid": "2BA128CE-38E4-4522-B823-7471633C2717",
            "name": "VIOS2",
            "state": "Running",
            "affinityScore": 100,
            "memory": {
              "assignedMem": [
                8192.000
              ],
              "utilizedMem": [
                2090.000
              ]
            },
            "processor": {
              "weight": 0,
              "mode": "share_idle_procs_active",
              "maxVirtualProcessors": [
                2.000
              ],
              "currentVirtualProcessors": [
                0.000
              ],
              "maxProcUnits": [
                2.000
              ],
              "entitledProcUnits": [
                1.000
              ],
              "utilizedProcUnits": [
                0.005
              ],
              "utilizedCappedProcUnits": [
                0.042
              ],
              "utilizedUncappedProcUnits": [
                0.000
              ],
              "idleProcUnits": [
                0.037
              ],
              "donatedProcUnits": [
                0.958
              ],
              "timeSpentWaitingForDispatch": [
                0.000
              ],
              "timePerInstructionExecution": [
                52.000
              ]
            },
            "network": {
              "genericAdapters": [
                {
                  "id": "ent6",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V2-C3-T1",
                  "receivedPackets": [
                    12.233
                  ],
                  "sentPackets": [
                    9.000
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    3011.200
                  ],
                  "receivedBytes": [
                    2265.067
                  ],
                  "transferredBytes": [
                    5276.267
                  ]
                },
                {
                  "id": "ent4",
                  "type": "virtual",
                  "physicalLocation": "U8247.22L.213C1BA-V2-C2-T1",
                  "receivedPackets": [
                    4.600
                  ],
                  "sentPackets": [
                    1.000
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    706.000
                  ],
                  "receivedBytes": [
                    3247.600
                  ],
                  "transferredBytes": [
                    3953.600
                  ]
                },
                {
                  "id": "ent2",
                  "type": "physical",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C6-T3",
                  "receivedPackets": [
                    5.167
                  ],
                  "sentPackets": [
                    0.000
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    0.000
                  ],
                  "receivedBytes": [
                    380.333
                  ],
                  "transferredBytes": [
                    380.333
                  ]
                }
              ],
              "sharedAdapters": [
                {
                  "id": "ent5",
                  "type": "sea",
                  "physicalLocation": "U8247.22L.213C1BA-V2-C2-T1",
                  "receivedPackets": [
                    9.767
                  ],
                  "sentPackets": [
                    1.000
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    706.000
                  ],
                  "receivedBytes": [
                    3627.933
                  ],
                  "transferredBytes": [
                    4333.933
                  ],
                  "bridgedAdapters": [
                    "ent2",
                    "ent4",
                    "ent4"
                  ]
                }
              ],
              "virtualEthernetAdapters": [
                {
                  "physicalLocation": "U8247.22L.213C1BA-V2-C2",
                  "vlanId": 1,
                  "vswitchId": 0,
                  "isPortVlanId": true,
                  "receivedPackets": [
                    0.000
                  ],
                  "sentPackets": [
                    0.000
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    0.000
                  ],
                  "receivedBytes": [
                    0.000
                  ],
                  "receivedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalPackets": [
                    0.000
                  ],
                  "droppedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalBytes": [
                    0.000
                  ],
                  "receivedPhysicalBytes": [
                    0.000
                  ],
                  "transferredBytes": [
                    0.000
                  ],
                  "transferredPhysicalBytes": [
                    0.000
                  ]
                },
                {
                  "physicalLocation": "U8247.22L.213C1BA-V2-C3",
                  "vlanId": 1,
                  "vswitchId": 0,
                  "isPortVlanId": true,
                  "receivedPackets": [
                    5.867
                  ],
                  "sentPackets": [
                    1.567
                  ],
                  "droppedPackets": [
                    0.000
                  ],
                  "sentBytes": [
                    1306.633
                  ],
                  "receivedBytes": [
                    517.900
                  ],
                  "receivedPhysicalPackets": [
                    5.867
                  ],
                  "sentPhysicalPackets": [
                    1.567
                  ],
                  "droppedPhysicalPackets": [
                    0.000
                  ],
                  "sentPhysicalBytes": [
                    1306.633
                  ],
                  "receivedPhysicalBytes": [
                    517.900
                  ],
                  "transferredBytes": [
                    1824.533
                  ],
                  "transferredPhysicalBytes": [
                    1824.533
                  ]
                }
              ]
            },
            "storage": {
              "clientLpars": [
                "62F4D488-C838-41E2-B83B-E68E004E3B63"
              ],
              "genericPhysicalAdapters": [
                {
                  "id": "sissas1",
                  "type": "sas",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C15-T1",
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    6.400
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    3276.800
                  ],
                  "transmittedBytes": [
                    3276.800
                  ]
                }
              ],
              "fiberChannelAdapters": [
                {
                  "id": "fcs1",
                  "wwpn": "10000090fab674d7",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C2-T2",
                  "numOfPorts": 0,
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.000
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    0.000
                  ],
                  "runningSpeed": [
                    0.000
                  ],
                  "transmittedBytes": [
                    0.000
                  ]
                },
                {
                  "id": "fcs0",
                  "wwpn": "10000090fab674d6",
                  "physicalLocation": "U78CB.001.WZS0BYF-P1-C2-T1",
                  "numOfPorts": 3,
                  "numOfReads": [
                    0.000
                  ],
                  "numOfWrites": [
                    0.400
                  ],
                  "readBytes": [
                    0.000
                  ],
                  "writeBytes": [
                    26214.400
                  ],
                  "runningSpeed": [
                    8.000
                  ],
                  "transmittedBytes": [
                    26214.400
                  ]
                }
              ]
            }
          }
        ]
     */
}
