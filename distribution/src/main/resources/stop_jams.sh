#!/bin/bash

set -uo pipefail

main() {
    local csvFile=""

    while [[ $# -gt 0 ]]; do
        key="$1"
        case $key in
        --csv)
            csvFile="$2"
            shift
            shift
            ;;
        *)
            echo "Unknown option: $key"
            exit 2
            ;;
        esac
    done

    if [[ -n "$csvFile" ]]; then
        if [[ ! -f "$csvFile" ]]; then
            echo "CSV file not found: $csvFile"
            exit 2
        fi

        while IFS="," read -r dev osgi env thing; do
            if [[ -z "$dev" || -z "$osgi" || -z "$env" || -z "$thing" ]]; then
                echo "CSV file has empty cells"
                exit 2
            fi

            if [[ "$dev" == "localhost" ]]; then
                echo "localhost: $ pkill -f $osgi/bin/jams"
                eval "pkill -f $osgi/bin/jams" &
            else
                echo "$dev: $ pkill -f $osgi/bin/jams"
                ssh $dev -n "pkill -f $osgi/bin/jams" 2>&1 &
            fi
        done < <(tail -n +2 "$csvFile")
        wait
    else
        echo "Usage: $0 --csv <csv-file>"
        exit 2
    fi
}

main "$@"
