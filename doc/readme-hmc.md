# HMC Preparations

Ensure you have **correct date/time** and NTPd running to keep it accurate!

- Login to your HMC
- Navigate to *Console Settings*
    - Go to *Change Date and Time*
        - Set correct timezone, if not done already
        - Configure one or more NTP servers, if not done already
        - Enable the NTP client, if not done already
- Navigate to *Users and Security*
    - Create a new read-only/viewer **hmci** user, which will be used to connect to the HMC.
    - Click *Manage User Profiles and Access*, edit the newly created *hmci* user and click *User Properties*:
        - Set *Session timeout minutes* to **60**
        - Set *Verify timeout minutes* to **15**
        - Set *Idle timeout minutes* to **90**
        - Set *Minimum time in days between password changes* to **0**
        - **Enable** *Allow remote access via the web*
- Navigate to *HMC Management* and *Console Settings*
    - Click *Change Performance Monitoring Settings*:
        - Enable *Performance Monitoring Data Collection for Managed Servers*:  **All On**
        - Set *Performance Data Storage* to **1** day or preferable more

If you do not enable *Performance Monitoring Data Collection for Managed Servers*, you will see errors such as *Unexpected response: 403*.

Use the HMCi debug option (*--debug*) to get more details about what is going on.


## Configure date/time through CLI

Example showing how you configure related settings through the HMC CLI:

```shell
chhmc -c date -s modify --datetime MMDDhhmm           # Set current date/time: MMDDhhmm[[CC]YY][.ss]
chhmc -c date -s modify --timezone Europe/Copenhagen  # Configure your timezone
chhmc -c xntp -s enable                               # Enable the NTP service
chhmc -c xntp -s add -a IP_Addr                       # Add a remote NTP server
```
Remember to reboot your HMC after changing the timezone.
