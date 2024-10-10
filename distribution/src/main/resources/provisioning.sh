#!/bin/bash

# Summary:
# This script provisions IoT devices by downloading necessary certificates, copying files, updating configurations,
# and starting services on target devices. It supports provisioning multiple devices simultaneously using a CSV file.
# The script constructs SSH connections to target devices, therefore the public key of the host system shall have been
# deployed on the target devices.
# The script logs the provisioning process for each device.

# Usage:
# ./provisioning.sh --config <config-file> --csv <csv-file>

# CSV file format: first row is the title row, then followed by the actual parameters.
# For example, to provision a RaspberryPI
#
# dev,path,env,thing
# pi@192.168.68.129,/home/pi/jamaica-ams,DEVELOP,raspberrypi

# Error Codes:
#  1 - Any error not listed below
#  2 - Unknown option, Config file not found, CSV file not found or missing parameters
#  3 - Required tool not installed
#  4 - Failed to invoke REST API of authentication server or backend server
#  5 - Failed to access target device via SSH
#  6 - Failed to configure OSGi runtime
#  7 - Failed to start OSGi runtime
#  8 - Device was not provisioned within the expected time

set -uo pipefail

cleanup() {
    local env=$1
    local thing=$2
    local pid=$3
    local osgi=$4
    log_info "Cleaning up ..."
    log_info "Stopping JamaicaAMS at $osgi/bin/jams"
    ssh $dev "pkill -f $osgi/bin/jams" 2>&1

    log_info "Stopping SSH session $pid to device $env $thing"
    kill -9 $pid

    log_debug "Restore GoGo Shell"
    ssh $dev find $osgi/ -name 'org.apache.felix.gogo.shell*.jar' -exec mv -t $osgi/bundle.3/ {} +
}

# Logging Configuration
readonly LOG_ERROR="1"
readonly LOG_INFO="2"
readonly LOG_DEBUG="3"
LOG_LEVEL="${LOG_DEBUG}"

log_debug() {
    if [ "$LOG_LEVEL" -ge "$LOG_DEBUG" ]; then
        echo -e "$(date): [DEBUG] $@"
    fi
}

log_error() {
    if [ "$LOG_LEVEL" -ge "$LOG_ERROR" ]; then
        echo -e "$(date): [ERROR] $@"
    fi
}

log_info() {
    if [ "$LOG_LEVEL" -ge "$LOG_INFO" ]; then
        echo "$(date): [INFO] $@"
    fi
}

check_tools() {
    log_info "Check tools ..."
    local required_tools=("jq" "curl" "scp" "ssh" "nohup")
    for tool in "${required_tools[@]}"; do
        if ! command -v $tool &>/dev/null; then
            log_error "$tool is not installed." >&2
            exit 3
        fi
    done
}

get_access_token() {
    log_info "Get access token from ${EDP_AUTH_SERVER}"
    local response
    response=$(curl -sS -X POST -H 'Content-Type: application/x-www-form-urlencoded' \
        -d "username=${EDP_USERNAME}" \
        -d "password=${EDP_PASSWORD}" \
        -d "grant_type=password" \
        -d "client_id=frontend" \
        -d "client_secret=not-used" \
        "${EDP_AUTH_SERVER}/auth/realms/${EDP_REALM}/protocol/openid-connect/token")
    log_debug "${EDP_AUTH_SERVER}/auth/realms/${EDP_REALM}/protocol/openid-connect/token Response: $response"
    TOKEN=$(echo "$response" | jq -r '.access_token')
    log_debug "JWT Token: $TOKEN"
    if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
        log_error "Failed to obtain access token"
        exit 4
    fi
}

query_provisioned_devices() {
    local thing=$1
    local env=$2
    curl -sS -X 'GET' \
        "${EDP_PROD_SERVER}/api/device/list?prefix=${thing}&environment=${env}" \
        -H 'accept: */*' \
        -H "Authorization: Bearer ${TOKEN}"
}

query_device_shadow() {
    local thing=$1
    local env=$2
    curl -sS -X 'GET' \
        "${EDP_PROD_SERVER}/api/device/shadow?thing-name=${thing}&env=${env}" \
        -H 'accept: */*' \
        -H "Authorization: Bearer ${TOKEN}"
}

download_certificate() {
    local thing=$1
    local env=$2
    if [[ ! -f device-certificates/$env/${thing}.p12 ]]; then
        log_info "Request and download certificate for thing ${thing}."
        curl -sS -X 'GET' \
            "${EDP_PROD_SERVER}/api/admin/${env}/provision/jitp/create-device-cert?thingName=${thing}" \
            -H 'accept: application/octet-stream' \
            -H "Authorization: Bearer ${TOKEN}" \
            -o "device-certificates/$env/${thing}.p12"
        if [[ $? -ne 0 ]]; then
            log_error "Failed to download the device certificate."
            exit 4
        fi
        log_info "Downloaded device certificate to device-certificates/$env/${thing}.p12"
    else
        log_info "Certificate already exists: ${thing}.p12"
    fi
}

provision_device() {
    local dev=$1
    local osgi=$2
    local env=$3
    local thing=$4

    log_info "Subprocess Provisioning thing $thing on device $dev"

    log_info "Getting access token."
    get_access_token

    log_info "Download device certificate."
    download_certificate "$thing" "$env"

    log_info "Define commands to run on the target device (or localhost)"
    commands=$(
        cat <<EOF
echo -e "$(date): [INFO] Update edp.client.iot-endpoint=${EDP_IOT_ENDPOINT}."
if grep -q "^edp.client.iot-endpoint=" $osgi/conf/system.properties; then
    sed -i "s|^edp.client.iot-endpoint=.*|edp.client.iot-endpoint=${EDP_IOT_ENDPOINT}|g" $osgi/conf/system.properties
else
    echo "edp.client.iot-endpoint=${EDP_IOT_ENDPOINT}" >> $osgi/conf/system.properties
fi

echo -e "$(date): [INFO] Update edp.client.keystore=./conf/${thing}.p12."
if grep -q "^edp.client.keystore=.*" $osgi/conf/system.properties; then
    sed -i "s|^edp.client.keystore=.*|edp.client.keystore=./conf/${thing}.p12|g" $osgi/conf/system.properties
else
    echo "edp.client.keystore=./conf/${thing}.p12" >> $osgi/conf/system.properties
fi

echo -e "$(date): [INFO] Update com.amazonaws.services.iot.client.level=WARNING."
if grep -q "^com.amazonaws.services.iot.client.level=.*" $osgi/conf/logging.properties; then
    sed -i "s|^com.amazonaws.services.iot.client.level=.*|com.amazonaws.services.iot.client.level=WARNING|g" $osgi/conf/logging.properties
else
    echo "com.amazonaws.services.iot.client.level=WARNING" >> $osgi/conf/logging.properties
fi

echo -e "$(date): [INFO] Update org.eclipse.paho.client.level=WARNING."
if grep -q "^org.eclipse.paho.client.level=.*" $osgi/conf/logging.properties; then
    sed -i "s|^org.eclipse.paho.client.level=.*|org.eclipse.paho.client.level=WARNING|g" $osgi/conf/logging.properties
else
    echo "org.eclipse.paho.client.level=WARNING" >> $osgi/conf/logging.properties
fi

find $osgi/bundle.3/ -name 'org.apache.felix.gogo.shell*.jar' -exec mv -t $osgi/ {} +

rm -rf $osgi/jamaica-ams-cache

cd $osgi; nohup $osgi/bin/jams 2>&1 &
EOF
    )
    log_debug "Commands to run on target device:"
    log_debug "$commands"

    log_info "Stopping processes of the command $osgi/bin/jams if exist"
    ssh $dev "pkill -e -n -f $osgi/bin/jams"
    sleep 3
    log_info "SCP copy certificate device-certificates/$env/${thing}.p12 to remote $osgi/conf/${thing}.p12"
    scp device-certificates/$env/${thing}.p12 ${dev}:$osgi/conf/${thing}.p12 || return 5
    log_info "SCP copy jar files of current folder to remote $osgi/bundle.2/"
    scp *.jar ${dev}:$osgi/bundle.2/ || return 5
    log_info "Execute commands on the target device"
    ssh $dev "$commands" 2>&1 &
    local pid=$!
    trap 'cleanup $dev $thing $pid $osgi' RETURN

    log_info "Refresh the access token before querying the provisioning status"
    get_access_token

    log_info "Query the server to get the status of the device using the REST API"
    for i in {1..12}; do
        provisioned_devices=$(query_provisioned_devices "$thing" "$env")
        log_debug "query_provisioned_devices Response: ${provisioned_devices}"
        is_device_provisioned=$(echo "$provisioned_devices" | jq -e --arg thing "$thing" '.devices[] | select(.thingName == $thing)' > /dev/null; echo $?)
        log_info "Device is provisioned: $([ ${is_device_provisioned} -eq 0 ] && echo true || echo false)"

        device_shadow=$(query_device_shadow "$thing" "$env")
        log_debug "query_device_shadow Response: ${device_shadow}"
        is_device_online=$(echo "$device_shadow" | jq -e '.state.reported.online == true' > /dev/null; echo $?)
        log_info "Device is online: $([ ${is_device_online} -eq 0 ] && echo true || echo false)"

        if [[ ${is_device_provisioned} -eq 0 && $is_device_online -eq 0 ]]; then
            log_info "Device $thing has been successfully provisioned."
            return 0
        fi

        sleep 5
    done

    log_error "Device $env $thing was not provisioned within the expected time." >&2
    return 8
}

main() {
    if [[ $# -ne 4 ]]; then
        echo "Usage: $0 [--config <config-file>] [--csv <csv-file>]"
        exit 2
    fi

    local config_file=""
    local csv_file=""

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        key="$1"
        case $key in
        --config)
            config_file="$2"
            shift;shift
            ;;
        --csv)
            csv_file="$2"
            shift;shift
            ;;
        *)
            log_error "Unknown option: $key"
            exit 2
            ;;
        esac
    done

    if [[ -z "$config_file" ]]; then
        log_error "Config file must be provided with --config"
        exit 2
    elif [[ ! -f "$config_file" ]]; then
        log_error "Config file not found $config_file"
        exit 2
    else
        source "$config_file"
    fi

    if [[ -z "$csv_file" ]]; then
        log_error "CSV file must be provided with --csv"
        exit 2
    elif [[ ! -f "$csv_file" ]]; then
        log_error "CSV file not found $csv_file"
        exit 2
    fi

    check_tools

    local total_count=0
    local success_count=0
    local fail_count=0
    local successes=()
    local failures=()
    local subprocesses=()

    log_info "Provision devices in parallel."
    while IFS="," read -r dev osgi env thing; do
        if [[ -z "$dev" || -z "$osgi" || -z "$env" || -z "$thing" ]]; then
            log_error "CSV file has empty cells"
            exit 2
        fi
        ((total_count += 1))
        mkdir -p "device-certificates/$env"
        local log_file="device-certificates/$env/${thing}.log"
        log_info "Provisioning thing $thing on device $dev, and logging into $log_file" | tee "$log_file"
        provision_device "$dev" "$osgi" "$env" "$thing" >>"$log_file" 2>&1 &
        local pid=$!
        subprocesses+=("$pid:$env:$thing")
    done < <(tail -n +2 "$csv_file")

    for sp in "${subprocesses[@]}"; do
        IFS=":" read -r pid env thing <<<"$sp"
        wait "$pid"
        local exit_code=$?
        if [[ "${exit_code}" -eq 0 ]]; then
            log_info "Device ${env}:${thing} has been provisioned successfully."
            ((success_count += 1))
            successes+=("$env:$thing")
        else
            log_error "Failed to provision Device ${env}:${thing} with error code ${exit_code}."
            ((fail_count += 1))
            failures+=("$env:$thing")
        fi
    done

    log_info "========================================="
    log_info "Summary:"
    log_info "Total provisions: $total_count"
    log_info "Total successes:  $success_count"
    log_info "Total failures:   $fail_count"

    if [[ $success_count -ne 0 ]]; then
        log_info "Successful provisions:"
        printf '%s\n' "${successes[@]}" | sort | column -t
    fi

    if [[ $fail_count -ne 0 ]]; then
        log_info "Failed provisions:"
        printf '%s\n' "${failures[@]}" | sort | column -t
    fi
}

main "$@"
