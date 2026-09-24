serial_autologin_root:prepend() {
    inittab="${IMAGE_ROOTFS}/etc/inittab"
    autologin="${IMAGE_ROOTFS}${base_sbindir}/serial-autologin"

    if [ -e "${inittab}" ]; then
        if grep -qE '/agetty' "${inittab}"; then
            sed -i '/:respawn:/ s,/agetty,/agetty -a root ,g' "${inittab}"
        fi

        if grep -qE '/getty' "${inittab}"; then
            if [ ! -f "${autologin}" ]; then
                printf '%s\n' '#!/bin/sh' 'exec /bin/login -f root' > "${autologin}"
                chmod 0755 "${autologin}"
            fi

            sed -i "/:respawn:/ s,/getty,/getty -n -l ${base_sbindir}/serial-autologin,g" "${inittab}"
            return 0
        fi
    fi
}

