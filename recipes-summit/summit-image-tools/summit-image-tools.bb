SUMMARY = "Summit Image Tools"

LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://mksdcard.sh \
    file://imx-ram.uuu \
    file://imx8mm-ram.uuu \
    file://imx95-ram.uuu \
    file://ti-ram.sh \
    file://70-ezurio-usb.rules \
    "

S = "${UNPACKDIR}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

addtask deploy before do_build after do_install

do_deploy () {
    install -D -m 0755 -t "${DEPLOYDIR}" "${S}/mksdcard.sh"
}

UUU_FILE:imx-generic-bsp = "${S}/imx-ram.uuu"
UUU_FILE:mx8mm-generic-bsp = "${S}/imx8mm-ram.uuu"
UUU_FILE:mx95-generic-bsp = "${S}/imx95-ram.uuu"

do_deploy:imx-generic-bsp:summitsom-initramfs () {
    install -D -m 0644 "${UUU_FILE}" "${DEPLOYDIR}/imx-ram.uuu"
}

do_deploy:ti-soc:summitsom-initramfs () {
    install -D -m 0755 -t "${DEPLOYDIR}" "${S}/ti-ram.sh"
    install -D -m 0644 -t "${DEPLOYDIR}" "${S}/70-ezurio-usb.rules"
}
