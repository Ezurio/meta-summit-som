FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/files:"

SRC_URI:append:summitsom = "\
    file://0005-timedate-symlink.patch \
    file://95-network-generator.preset \
    "

PACKAGECONFIG:summitsom = " \
    ${@bb.utils.filter('DISTRO_FEATURES', 'acl audit efi ldconfig pam selinux smack usrmerge polkit seccomp', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wifi', 'rfkill', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'xkbcommon', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'sysvinit', 'link-udev-shared', d)} \
    backlight \
    binfmt \
    hibernate \
    hostnamed \
    idn \
    kmod \
    localed \
    logind \
    machined \
    myhostname \
    set-time-epoch \
    sysusers \
    timedated \
    utmp \
    vconsole \
    zstd \
    microhttpd \
"

do_install:append:summitsom() {
    rm -f "${D}${systemd_unitdir}/system-generators/systemd-gpt-auto-generator"
    install -D -m 644 -t "${D}${systemd_unitdir}/system/system-preset" \
        "${WORKDIR}/95-network-generator.preset"
}
