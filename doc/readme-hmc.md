# IBM Power HMC

## Prepare Your HMC

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
        - Set *Session timeout minutes* to **120** (or at least 61 minutes)
        - Set *Verify timeout minutes* to **15**
        - Set *Idle timeout minutes* to **15**
        - Set *Minimum time in days between password changes* to **0**
        - **Enable** *Allow remote access via the web*
- Navigate to *HMC Management* and *Console Settings*
    - Click *Change Performance Monitoring Settings*:
        - Enable *Performance Monitoring Data Collection for Managed Servers*:  **All On**
        - Set *Performance Data Storage* to **1** day or preferable more

If you do not enable *Performance Monitoring Data Collection for Managed Servers*, you will see errors such as *Unexpected response: 403*.

Use the HMCi debug option (*--debug*) to get more details about what is going on.


### Configure date/time through CLI

Example showing how you configure related settings through the HMC CLI:

```shell
chhmc -c date -s modify --datetime MMDDhhmm           # Set current date/time: MMDDhhmm[[CC]YY][.ss]
chhmc -c date -s modify --timezone Europe/Copenhagen  # Configure your timezone
chhmc -c xntp -s enable                               # Enable the NTP service
chhmc -c xntp -s add -a IP_Addr                       # Add a remote NTP server
```
Remember to reboot your HMC after changing the timezone.


## Troubleshooting

### No errors, but still no data

- Double check timezone and time on your HMC and the host where the collector runs.
- Ensure NTP time sync. works both places.


### Test Login from CLI

Create a *login.xml* file with the following content (replace the user/password):

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<LogonRequest xmlns="http://www.ibm.com/xmlns/systems/power/firmware/web/mc/2012_10/" schemaVersion="V1_0">
    <UserID>hmci</UserID>
    <Password>hmcihmci</Password>
</LogonRequest>
```

Replace the hostname/IP address in the URL and try to connect:

```shell
curl -k -c cookies.txt -i -X PUT \
 -H "Content-Type: application/vnd.ibm.powervm.web+xml; type=LogonRequest" \
 -H "Accept: application/vnd.ibm.powervm.web+xml; type=LogonResponse" \
 -H "X-Audit-Memento: hmc_test" \
 -d @login.xml https://myhmc:12443/rest/api/web/Logon
```

A successful login should return a valid XML ```<LogonResponse>.....</LogonResponse>```.

In case of authentication errors, be sure to check you are not connecting through a proxy server.

#### Logon Response Example

```text
HTTP/2 200
date: Mon, 05 Aug 2024 13:00:01 GMT
server: Server Hardware Management Console
x-content-type-options: nosniff
strict-transport-security: max-age=31536000; includeSubDomains
referrer-policy: no-referrer
x-frame-options: SAMEORIGIN
x-transaction-id: XT11394547
content-type: application/vnd.ibm.powervm.web+xml; type=LogonResponse
set-cookie: JSESSIONID=C360496F8C817391E97DC769BC8B04E7; Path=/rest; Secure; HttpOnly
set-cookie: CCFWSESSION=91FAE2A13949CCC4EB545EB315175042; Path=/; Secure; HttpOnly
content-security-policy: default-src https: data:; script-src https: 'unsafe-inline' 'unsafe-eval'; style-src https: 'unsafe-inline' 'unsafe-eval'; connect-src wss: 'self'
x-xss-protection: 1; mode=block
vary: User-Agent

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<LogonResponse xmlns="http://www.ibm.com/xmlns/systems/power/firmware/web/mc/2012_10/" xmlns:ns2="http://www.w3.org/XML/1998/namespace/k2" schemaVersion="V1_0">
    <Metadata>
        <Atom/>
    </Metadata>
    <X-API-Session kxe="false" kb="ROR">tKVhm4YD0bS0qjaYuXI8b0BKckfJzchlBKUgOiaBAVvJkcNajy1Vk7uuFMyX4MzUpl_aRiDQzggqZH0SYMlqYLAzwV3kKgYqp6U4r_l5gjKNIHbjIyS5L1rUxa5crnfaJ87ApfR706sI7RrP1KpYKYWBg9eHxZFQKWj4McnPhRRk9vk6Jq8snGQH5n43ZXzayASZennqsr1lN3IGyNmAgm9_7xq-GN_J5tiE1zwSXvY=</X-API-Session>
</LogonResponse>
```
