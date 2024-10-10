# Device Provisioning Using Bash Scripts

The EDP Client distribution provides Bash scripts for provisioning multiple IoT devices in parallel to facilitate device provisioning automation.

## Scripts Included

- `provisioning.sh`: Provisions IoT devices by downloading necessary certificates, copying files, updating configurations, and starting services on target devices. It supports provisioning multiple devices simultaneously using CSV files. The script logs the provisioning process for each device.

## Prerequisites

- The target device has JamaicaAMS deployed.
- The SSH server and FTP server are active on the remote target device.
- It is possible to establish a secure connection with the remote target device using a public certificate.
- Ensure the following tools are installed on your host system: `jq`, `curl`, `scp`, `ssh`, `nohup`, and target system: `ssh` and `nohup`.

## Usage

### Configuration File

Create a configuration file with the necessary authentication and server details. The template of the configuration file can be found at `provisioning.properties`. Protect this file by restricting its permissions:

```bash
chmod 600 provisioning.properties
```
### CSV File Format

The CSV file should have the following columns: hostname of target device, path of JamaicaAMS deployment, environment, thing name. The path of JamaicaAMS deployment shall contain the folders `bin`, `conf`, `lib` and `bundle.1` etc.

Example for localhost:

```csv
dev,path,env,thing
localhost,/home/user/jamaica-ams,DEVELOP,ubuntu-laptop
```

Example for a Raspberry PI:
```csv
dev,path,env,thing
pi@192.168.68.129,/home/pi/jamaica-ams,DEVELOP,raspberrypi
```
### Running the script

Run the `provisioning.sh` script by providing the paths to the configuration and CSV files:

```bash
./provisioning.sh --config provisioning.properties --csv devices.csv
```

#### Script Overview

The script performs the following actions for each device listed in the CSV file:

1. Obtains an access token from the authentication server.
2. Downloads the device certificate if it does not already exist.
3. Copies necessary files to the target device.
4. Updates the configuration properties on the target device.
5. Starts the necessary services on the target device.
6. Logs the provisioning process for each device.

#### Error Codes

1. Any error not listed below.
2. Unknown option, Config file not found, CSV file not found, or missing parameters.
3. Required tool not installed.
4. Failed to invoke REST API of the authentication server or backend server.
5. Failed to access the target device via SSH.
6. Failed to configure OSGi runtime.
7. Failed to start OSGi runtime.
8. Device was not provisioned within the expected time.
