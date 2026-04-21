LICENSE = "MIT"

inherit core-image extrausers

PASSWD = "\$5\$JJ/ksbVr4475qA49\$wzyEBumoH1YyHOG9OgKzjRKjwGVImxFDCu0m90hymoA"

EXTRA_USERS_PARAMS = "usermod -p '${PASSWD}' root;"

export IMAGE_BASENAME = "${PN}"
export IMAGE_BASENAME:summit-secure = "${PN}-secure"

IMAGE_ROOTFS_VERITY_TYPE = "squashfs-zst.verity"
IMAGE_ROOTFS_VERITY_NAME = "${IMAGE_LINK_NAME}.${IMAGE_ROOTFS_VERITY_TYPE}"
IMAGE_FSTYPES:append:summitsom = " ${IMAGE_ROOTFS_VERITY_TYPE}"

IMAGE_BOOT_FILES:append:summitsom = " ${IMGDEPLOYDIR}/${IMAGE_ROOTFS_VERITY_NAME}.scr.bin;fitImageVerity.bin"

#IMAGE_MACHINE_SUFFIX ?= ""
IMAGE_NAME_SUFFIX ?= ""

IMAGE_ROOTFS_EXTRA_SPACE = "0"

SUMMIT_CONSOLE_LOGGING ?= "quiet"

IMAGE_BOOTSTR = "bootside=\${bootside} ${SUMMIT_CONSOLE_LOGGING} init=/usr/sbin/pre-systemd-init.sh inittype=overlay"
IMAGE_BOOTSTR:remove:summit-secure = "inittype=overlay"

IMAGE_FEATURES = "\
    ssh-server-dropbear \
    allow-root-login \
    "

IMAGE_FEATURES:append:summit-secure = "\
    read-only-rootfs \
    "

ROOTFS_POSTPROCESS_COMMAND += "rootfs_os_release; "

rootfs_os_release() {
    ver=${IMAGE_VERSION_SUFFIX}
    ver=${ver#-}
    sed -i -e "s,^ID=.*,ID=${IMAGE_BASENAME},g" "${IMAGE_ROOTFS}${libdir}/os-release"
    sed -i -e "s,0.0.0.0,${ver},g" "${IMAGE_ROOTFS}${libdir}/os-release"
    printf 'Summit SOM %s %s %s \\n \l\n' "${MACHINE}" "${IMAGE_BASENAME}" "${ver}" > "${IMAGE_ROOTFS}${sysconfdir}/issue"
    echo "Summit SOM ${MACHINE} ${IMAGE_BASENAME} ${ver} %h" > "${IMAGE_ROOTFS}${sysconfdir}/issue.net"
}

ARCHIVE_NAME ?= "${IMAGE_BASENAME}-${MACHINE}-summit${IMAGE_VERSION_SUFFIX}"
ARCHIVE_WILDCARD ?= ""

addtask create_archive after do_image_complete before do_build

do_create_archive() {
    if echo "${IMAGE_VERSION_SUFFIX}" | grep -xEq '\-[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' ; then
        tar --transform='s,.*/,,' -cvjhf "${DEPLOY_DIR_IMAGE}/${ARCHIVE_NAME}.tar.bz2" ${ARCHIVE_WILDCARD}
    fi
}

do_backup_runtime () {
    BACKUP_SECRET_DIR=${IMAGE_ROOTFS}/usr/share/factory/etc/secret
    BACKUP_MISC_DIR=${IMAGE_ROOTFS}/usr/share/factory/etc/misc

    mkdir -p "${BACKUP_SECRET_DIR}"
    for BACKUP_TARGET in "modem" "stunnel" "chrony" "summit-rcm"; do
        if [ -d "${IMAGE_ROOTFS}/etc/${BACKUP_TARGET}" ]; then
            mv "${IMAGE_ROOTFS}/etc/${BACKUP_TARGET}" "${BACKUP_SECRET_DIR}"
            ln -sf /data/secret/${BACKUP_TARGET} "${IMAGE_ROOTFS}/etc/${BACKUP_TARGET}"
        fi
    done

    mkdir -p "${BACKUP_SECRET_DIR}/NetworkManager"
    for SM_SUB_DIR in "certs" "system-connections"; do
        if [ -d "${IMAGE_ROOTFS}/etc/NetworkManager/${SM_SUB_DIR}" ]; then
            mv "${IMAGE_ROOTFS}/etc/NetworkManager/${SM_SUB_DIR}" "${BACKUP_SECRET_DIR}/NetworkManager"
        else
            mkdir -p "${BACKUP_SECRET_DIR}/NetworkManager/${SM_SUB_DIR}"
        fi
        ln -sf /data/secret/NetworkManager/${SM_SUB_DIR} "${IMAGE_ROOTFS}/etc/NetworkManager/${SM_SUB_DIR}"
    done

    ln -sf /data/secret/NetworkManager.state "${IMAGE_ROOTFS}/etc/NetworkManager/NetworkManager.state"

    mkdir -p "${BACKUP_MISC_DIR}"
    [ -f "${IMAGE_ROOTFS}/etc/timezone" ] && \
    mv -t "${BACKUP_MISC_DIR}" \
        "${IMAGE_ROOTFS}/etc/timezone" \
        "${IMAGE_ROOTFS}/etc/localtime" \
        "${IMAGE_ROOTFS}/etc/adjtime"

    ln -sf /data/misc/timezone "${IMAGE_ROOTFS}/etc/timezone"
    ln -sf /data/misc/localtime "${IMAGE_ROOTFS}/etc/localtime"
    ln -sf /data/misc/adjtime "${IMAGE_ROOTFS}/etc/adjtime"

    # Needed to satisfy preset_all on some rebuilds
    rm -rf "${IMAGE_ROOTFS}/etc/machine-id"

    rm -rf "${IMAGE_ROOTFS}/media"
    ln -sf /run/media "${IMAGE_ROOTFS}/media"
}

ROOTFS_POSTPROCESS_COMMAND:append:summit-secure = " do_backup_runtime;"
