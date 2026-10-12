#!/bin/sh
# SPDX-License-Identifier: LicenseRef-Ezurio-Clause
# Copyright (C) 2026 Ezurio

set -e

SCRIPT_DIR=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)
# Give both run-time and DFU-mode IDs so dfu-util does not fall back to
# matching any device in DFU mode.
ROM_DFU_DEVICE=0451:6165,0451:6165
GADGET_DFU_DEVICE=1fa3:0003,1fa3:0003

die() {
    printf 'ti-ram: %s\n' "$*" >&2
    exit 1
}

command -v dfu-util >/dev/null 2>&1 || die "dfu-util is required"
command -v fastboot >/dev/null 2>&1 || die "fastboot is required"
command -v sha256sum >/dev/null 2>&1 || die "sha256sum is required"

# Prepend the DFU options to the caller's arguments so every dfu-util call
# below receives them via "$@" without relying on word splitting.
FASTBOOT_SERIAL=
if [ -n "${USB_PATH:-}" ]; then
    set -- -p "${USB_PATH}" "$@"
    FASTBOOT_SERIAL="usb:${USB_PATH}"
elif [ -n "${USB_SERIAL:-}" ]; then
    set -- -S "${USB_SERIAL}" "$@"
    FASTBOOT_SERIAL="${USB_SERIAL}"
fi
set -- -w "$@"

fb() {
    if [ -n "${FASTBOOT_SERIAL}" ]; then
        fastboot -s "${FASTBOOT_SERIAL}" "$@"
    else
        fastboot "$@"
    fi
}

(cd "${SCRIPT_DIR}" && sha256sum -c manifest.sha256) || \
    die "rescue archive integrity check failed"

printf '%s\n' "Waiting for ROM DFU and downloading tiboot3.bin"
dfu-util "$@" -d "${ROM_DFU_DEVICE}" -a bootloader \
    -D "${SCRIPT_DIR}/tiboot3.bin" || true

printf '%s\n' "Waiting for R5 SPL DFU and downloading tispl.bin"
dfu-util "$@" -d "${GADGET_DFU_DEVICE}" -a tispl.bin \
    -D "${SCRIPT_DIR}/tispl.bin"
dfu-util "$@" -d "${GADGET_DFU_DEVICE}" -a tispl.bin -e || true

printf '%s\n' "Waiting for A53 SPL DFU and downloading u-boot.img"
dfu-util "$@" -d "${GADGET_DFU_DEVICE}" -a u-boot.img \
    -D "${SCRIPT_DIR}/u-boot.img"
dfu-util "$@" -d "${GADGET_DFU_DEVICE}" -a u-boot.img -e || true

# U-Boot proper runs 'fastboot -l ${loadaddr}', so the download lands at
# loadaddr. U-Boot commands can be sent before 'continue' with
# 'fastboot oem run:<command>', e.g. to set 'conf' as set-mode does.
printf '%s\n' "Waiting for U-Boot fastboot and downloading kernel.itb"
fb stage "${SCRIPT_DIR}/kernel.itb"

# 'continue' makes the U-Boot 'fastboot' command return; bootcmd then boots
# the downloaded FIT with bootm.
fb continue

printf '%s\n' "FIT transferred; target is booting"
