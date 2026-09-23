SUMMARY = "Summit Image Tools"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://mksdcard.sh \
    "

S = "${WORKDIR}"

inherit deploy

addtask deploy before do_build after do_install

do_deploy () {
    install -D -m 0755 -t "${DEPLOYDIR}" "${S}/mksdcard.sh"
}
