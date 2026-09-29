
SYSVINIT_ENABLED_GETTYS:summitsom = " "

do_install:append:summitsom() {
    if ${@oe.utils.conditional('VIRTUAL-RUNTIME_dev_manager', 'busybox-mdev', 'false', '[ -f "${D}${sysconfdir}/inittab" ]', d)}; then
        sed -i -E "/sysfs|devtmpfs/d" "${D}${sysconfdir}/inittab"
    fi
}
