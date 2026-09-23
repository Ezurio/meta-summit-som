SUMMARY = "Summit USB Gadget daemon (Rust)"
HOMEPAGE = "https://github.com/rfpros/cp_linux-som-external"
LICENSE = "Ezurio-Clause"
NO_GENERIC_LICENSE[Ezurio-Clause] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit cargo pkgconfig systemd update-rc.d summit-platform-version

# The source tree ships a complete VENDOR/ directory.  Point the bitbake cargo
# vendoring mechanism at it so no network access is needed.
CARGO_VENDORING_DIRECTORY = "${S}/vendor"
CARGO_DISABLE_BITBAKE_VENDORING = "1"
CARGO_BUILD_FLAGS += "--offline --locked"
RUSTFLAGS += "-C panic=unwind"
# Regenerate swupdate-ipc bindings from the SWUpdate headers staged by the
# PACKAGECONFIG feature dependencies below.
export SWUPDATE_INCLUDE_DIR = "${STAGING_INCDIR}"

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/summit-usbgadget.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_apps-summit-usbgadget.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

SUMMIT_PLATFORM_VERSION = "lrd-14.25.0.x"

SRC_URI:append = " \
    file://summit-usbgadget.service \
    file://summit-usbgadget.init \
    file://summit-usbgadget.toml \
    file://shared-usb0.nmconnection \
    file://shared-usb1.nmconnection \
"

PACKAGECONFIG ??= "usb dfu fastboot-usb fastboot-tcp socket"
PACKAGECONFIG[usb] = "--features usb,,udev"
PACKAGECONFIG[dfu] = "--features dfu,,swupdate udev"
PACKAGECONFIG[fastboot-usb] = "--features fastboot-usb,,swupdate udev"
PACKAGECONFIG[fastboot-tcp] = "--features fastboot-tcp,,swupdate udev"
PACKAGECONFIG[socket] = "--features socket,,swupdate"
PACKAGECONFIG[socket-tls] = "--features socket-tls,,swupdate openssl"

CARGO_BUILD_FLAGS += "--no-default-features"

SYSTEMD_SERVICE:${PN} = "summit-usbgadget.service"
SYSTEMD_AUTO_ENABLE = "enable"

INITSCRIPT_NAME = "summit-usbgadget"
INITSCRIPT_PARAMS = "defaults 43"

INSANE_SKIP:${PN} = "already-stripped"

S = "${WORKDIR}/git"

do_configure:append() {
    cat <<- EOF >> "${CARGO_HOME}/config.toml"

[source.crates-io]
replace-with = "bitbake"
EOF
}

do_install:append() {
    install -D -m 0755 -t "${D}${bindir}/" \
        "${B}/target/${CARGO_TARGET_SUBDIR}/summit-usbgadget"

    install -D -m 0644 -t "${D}${sysconfdir}" \
        "${WORKDIR}/summit-usbgadget.toml"

    install -D -m 0755 "${WORKDIR}/summit-usbgadget.init" \
        "${D}${sysconfdir}/init.d/summit-usbgadget"

    install -D -m 0600 -t "${D}${libdir}/NetworkManager/system-connections/" \
        "${WORKDIR}/shared-usb0.nmconnection" \
        "${WORKDIR}/shared-usb1.nmconnection"

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 -t "${D}${systemd_system_unitdir}" \
            "${WORKDIR}/summit-usbgadget.service"
    fi
}

CONFFILES:${PN} += "${sysconfdir}/summit-usbgadget.toml"

FILES:${PN} += " \
    ${sysconfdir} \
    ${systemd_system_unitdir} \
    ${libdir}/NetworkManager/system-connections \
"

RDEPENDS:${PN} += " \
    summit-initdata \
"
