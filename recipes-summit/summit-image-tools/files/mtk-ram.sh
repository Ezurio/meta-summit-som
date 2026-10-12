#!/bin/sh
# SPDX-License-Identifier: LicenseRef-Ezurio-Clause
# Copyright (C) 2026 Ezurio

set -e

SCRIPT_DIR=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)

die() {
    printf 'mtk-rescue: %s\n' "$*" >&2
    exit 1
}

BOOTROM=$(command -v genio-bootrom || command -v aiot-bootrom) || \
    die "genio-bootrom (genio-tools) is required"
command -v fastboot >/dev/null 2>&1 || die "fastboot is required"

# Select a specific device by USB path or serial when several are attached.
FASTBOOT_SERIAL=
if [ -n "${USB_PATH:-}" ]; then
    FASTBOOT_SERIAL="usb:${USB_PATH}"
elif [ -n "${USB_SERIAL:-}" ]; then
    FASTBOOT_SERIAL="${USB_SERIAL}"
fi

fb() {
    if [ -n "${FASTBOOT_SERIAL}" ]; then
        fastboot -s "${FASTBOOT_SERIAL}" "$@"
    else
        fastboot "$@"
    fi
}

printf '%s\n' "Waiting for BootROM (reset the board into download mode) and downloading da-spl.bin"
# Secure-boot parts also need the detached DA signature and authentication file.
if [ -f "${SCRIPT_DIR}/da-spl.sign" ] && [ -f "${SCRIPT_DIR}/auth_sv5.auth" ]; then
    "${BOOTROM}" -b "${SCRIPT_DIR}/da-spl.bin" -s "${SCRIPT_DIR}/da-spl.sign" \
        -t "${SCRIPT_DIR}/auth_sv5.auth"
else
    "${BOOTROM}" -b "${SCRIPT_DIR}/da-spl.bin"
fi

# SPL fastboot stages the FIT at CONFIG_SPL_LOAD_FIT_ADDRESS; 'continue'
# ends the session and SPL boots it.
printf '%s\n' "Waiting for SPL fastboot and downloading da-fit.bin"
fb stage "${SCRIPT_DIR}/da-fit.bin"
fb continue

# Let the SPL fastboot device disappear before waiting for U-Boot's.
sleep 1

# U-Boot proper runs 'fastboot -l ${loadaddr}', so the download lands at
# loadaddr. U-Boot commands can be sent before 'continue' with
# 'fastboot oem run:<command>'.
printf '%s\n' "Waiting for U-Boot fastboot and downloading kernel.itb"
fb stage "${SCRIPT_DIR}/kernel.itb"

# 'continue' makes the U-Boot 'fastboot' command return; bootcmd then boots
# the downloaded FIT with bootm.
fb continue

printf '%s\n' "FIT transferred; target is booting"
