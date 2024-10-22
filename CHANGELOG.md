# Changelog

All notable changes to this project will be documented in this file.

## 2.0.1 - 2024-11-xx
 - Prometheus support

## 1.4.10 - 2024-08-11
- Improve RPM and DEB package scripts

## 1.4.9 - 2024-08-06
- Updated 3rd party dependencies
- Update gradle minor version

## 1.4.8 - 2024-07-03
- Changes for running on containerized environment
- Updated 3rd party dependencies

## 1.4.7 - 2024-02-29
- Improve packaging and management of init-scripts

## 1.4.6 - 2024-02-07
- Do not throw error on HMC 500 responses
- Update links in documentation
- Update 3rd party dependencies

## 1.4.5 - 2023-11-13
- Adjust timeout to not have lingering sessions on HMC
- Update 3rd party dependencies

## 1.4.4 - 2023-05-20
- Support for InfluxDB v2, now requires InfluxDB 1.8 or later
- Increase influx writer buffer limit
- Various dashboard improvements

## 1.4.3 - 2023-03-21
- Fix and improve processor utilization dashboards.
- Minor code cleanup.

## 1.4.2 - 2023-01-05
- Fix error in SR-IOV port type being null.

## 1.4.1 - 2022-12-15
- Retrieve multiple PCM samples and keep track of processing.
- Rename VIOS metric 'vFC' (storage adapter) to 'virtual'.

## 1.4.0 - 2022-12-01
- Rewrite of toml+xml+json de-serialization code (uses jackson now).
- Changes to configuration file format - please look at [doc/hmci.toml](doc/hmci.toml) as example.
- Logging (write to file) JSON output from HMC is currently not possible.

## 1.3.3 - 2022-09-20
- Default configuration location on Windows platform.
- Process LPAR SR-IOV logical network ports data
- Update default dashboards
- Update documentation

## 1.3.0 - 2022-02-04
- Correct use of InfluxDB batch writing.

## 1.2.8 - 2022-02-28
- Sort measurement tags before writing to InfluxDB.
- Update 3rd party dependencies.


## 1.2.7 - 2022-02-24
- Options to include/exclude Managed Systems and/or Logical Partitions.
