SUMMARY = "Lontium display bridge firmware"
DESCRIPTION = "Firmware for LT9611UXD and LT2611UXD display adapters"
LICENSE = "CLOSED"

SRC_URI = " \
    file://LT2611UXD_VESA_8Bit_2Port_V1.2_431487.bin \
    file://LT9611UXD_DSI_AUTO_V1.3_445651.bin \
    "

S = "${UNPACKDIR}"

inherit allarch

FIRMWARE_DIR = "${nonarch_base_libdir}/firmware"

do_install() {
    install -D -m 0644 -t "${D}${FIRMWARE_DIR}" "${S}/LT*.bin"
    ln -sf "LT2611UXD_*.bin" "${D}${FIRMWARE_DIR}/lt2611uxd_fw.bin"
    ln -sf "LT9611UXD_*.bin" "${D}${FIRMWARE_DIR}/lt9611c_fw.bin"
}

FILES:${PN} = "${FIRMWARE_DIR}"
