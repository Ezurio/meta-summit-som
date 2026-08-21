SUMMARY = "Summit Set Mode"

LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch

SRC_URI = " \
    file://LICENSE.ezurio \
    file://set-mode \
    "

S = "${UNPACKDIR}"

RDEPENDS:${PN} = "\
    libubootenv-bin \
    u-boot-dumpimage \
    "

do_install () {
    install -D -m 0755 -t "${D}${bindir}" "${S}/set-mode"
}
