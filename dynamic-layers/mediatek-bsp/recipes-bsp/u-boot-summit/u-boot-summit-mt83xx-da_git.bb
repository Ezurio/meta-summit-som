SUMMARY = "Summit RAM Fastboot download agent for Tungsten MT83XX"

require u-boot-summit-mt83xx.inc

PROVIDES += "u-boot"

# The kconfig fragment merge ignores the '#include' in a .cfg, so use the board's DA defconfigs.
UBOOT_MACHINE:genio-700 = "tungsten_mt8390_ram_da_defconfig"
UBOOT_MACHINE:genio-510 = "tungsten_mt8370_ram_da_defconfig"

UBOOT_MAKE_TARGET = "all"
UBOOT_INITIAL_ENV = ""

DEPENDS += "${@oe.utils.conditional('DA_SIGN_ENABLE', '1', 'openssl-native mtk-secure-boot-config', '', d)}"

SIGN_ARGS = "sha384"

# The download agent consumes BL31 and the raw BL2 (its DDR loader).
do_compile[depends] += "virtual/bl3:do_deploy"
EXTRA_OEMAKE:append = " BL31=${DEPLOY_DIR_IMAGE}/bl31.bin"

do_compile:prepend() {
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bl2-raw.bin ${B}/ddr-loader.bin
}

# BootROM USB download payload: da-spl.bin stays raw (like lk.bin); signing is a detached da-spl.sign plus auth_sv5.auth.
do_compile:append() {
    if [ "${DA_SIGN_ENABLE}" = "1" ]; then
        openssl dgst -binary -${SIGN_ARGS} ${B}/da-spl.bin > ${B}/da-spl.digest
        openssl pkeyutl -sign -inkey ${RECIPE_SYSROOT}/${sysconfdir}/secure/da.pem -in ${B}/da-spl.digest \
            -out ${B}/da-spl.sign -pkeyopt digest:${SIGN_ARGS} -pkeyopt rsa_padding_mode:pss -pkeyopt rsa_pss_saltlen:32
    fi
}

do_install() {
    install -m 0644 -D -t ${D}/boot ${B}/da-spl.bin ${B}/da-fit.bin
    if [ "${DA_SIGN_ENABLE}" = "1" ]; then
        install -m 0644 ${B}/da-spl.sign ${RECIPE_SYSROOT}/${sysconfdir}/secure/auth_sv5.auth ${D}/boot
    fi
}

do_deploy() {
    install -m 0644 -D -t ${DEPLOYDIR} ${B}/da-spl.bin ${B}/da-fit.bin
    if [ "${DA_SIGN_ENABLE}" = "1" ]; then
        install -m 0644 ${B}/da-spl.sign ${RECIPE_SYSROOT}/${sysconfdir}/secure/auth_sv5.auth ${DEPLOYDIR}
    fi
}
