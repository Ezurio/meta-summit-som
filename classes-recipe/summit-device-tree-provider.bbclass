inherit devicetree

FIT_EXTERNAL_DTB_OVERLAYS ?= "1"
SUMMIT_DEVICE_TREE_FILES[doc] = "Space-separated Summit DTS/DTSO sources selected by the machine."

DTS_FILES = "${@' '.join(__import__('fnmatch').filter((d.getVar('SUMMIT_DEVICE_TREE_FILES') or '').split(), '*.dts'))}"
DT_FILES = "${@bb.utils.contains('FIT_EXTERNAL_DTB_OVERLAYS', '1', '${SUMMIT_DEVICE_TREE_FILES}', '${DTS_FILES}', d)}"

# Outputs of a previous machine/distro/DT_FILES selection must not leak into install, deploy or the overlay check.
do_compile[cleandirs] += "${B}"

do_check_dtbo_compatibility() {
    cd "${B}"
    for ov in *.dtbo; do
        [ -e "$ov" ] || continue
        for base in *.dtb; do
            [ -e "$base" ] || bbfatal "Overlays were built but no base DTB to validate against"
            fdtoverlay -i "$base" -o /dev/null "$ov" \
                || bbfatal "Failed to apply DT overlay $ov to base DTB $base"
        done
    done
}
addtask check_dtbo_compatibility after do_compile before do_install
