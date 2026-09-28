#
# Assemble the kernel FIT image (kernel + dtb(s) + U-Boot boot script) as
# part of the image recipe's own task graph, instead of depending on a
# separate, shared linux-yocto-fitimage/ti-kernel-fitimage recipe. This keeps
# the fitImage tied to virtual/kernel:do_deploy and this image's own
# do_image_<verity-type> task, so it's rebuilt whenever the rootfs (and its
# embedded verity root hash) changes.
#
# NOTE: deliberately does NOT `inherit kernel-fit-image` - that class is for
# a standalone recipe (do_install/FILES:${PN} packaging, `inherit deploy`),
# and its do_deploy task collides with meta-freescale's
# image_populate_mfgtool.bbclass (do_populate_mfgtool depends on and runs
# before do_deploy of the same recipe - a hard cycle once one exists). So we
# only reuse the helper classes/oe.fitimage library and reimplement the
# do_compile() logic as custom do_compile_fit/do_deploy_fit tasks instead.
# do_deploy_fit writes straight to DEPLOY_DIR_IMAGE (not IMGDEPLOYDIR/
# do_image_complete's publish step) because do_image_wic (see __anonymous
# below) needs the real file and runs before do_image_complete would ever
# publish it.
inherit linux-kernel-base kernel-arch kernel-artifact-names uboot-config
inherit ${@oe.utils.ifelse( \
    d.getVar('HSM_FIT_KEY_ID'), \
    'fitimage-key-provider-sign', \
    '')}

# Real, PN-unique filenames so multiple images sharing a MACHINE don't
# overwrite each other's fitImage. do_deploy_fit also maintains plain
# "fitImage"/"kernel.itb"/"fit-image.its" symlinks to these for u-boot/
# swupdate/rootfs - same "last deploy wins" convention as
# kernel.bbclass's KERNEL_IMAGE_LINK_NAME.
FIT_DEPLOY_FITIMAGE_NAME ?= "fitImage-${PN}"
FIT_DEPLOY_KERNEL_ITB_NAME ?= "kernel-${PN}.itb"
FIT_DEPLOY_ITS_NAME ?= "fit-image-${PN}.its"

# Set to "0" to skip pulling in virtual/dtb (and its .dtbo overlays) even
# when PREFERRED_PROVIDER_virtual/dtb is set machine/distro-wide.
FIT_EXTERNAL_DTB_OVERLAYS ?= "1"

# Consume this image's own dm-verity boot script directly - no image name
# or opaque/fixed filename lookup needed, since this is the same recipe.
#
# Set before requiring image-fitimage.conf below (its `FIT_UBOOT_ENV ?= ""`
# would otherwise win the race). Points into IMGDEPLOYDIR, not
# DEPLOY_DIR_IMAGE, since that's where the verity conversion writes the
# .scr file - it's only copied to DEPLOY_DIR_IMAGE by do_image_complete,
# which runs after do_compile_fit.
FIT_UBOOT_ENV ?= "${IMGDEPLOYDIR}/${IMAGE_ROOTFS_VERITY_NAME}.scr"
FIT_SUPPORTED_INITRAMFS_FSTYPES ?= "squashfs-zst squashfs"

require conf/image-fitimage.conf

DEPENDS += "\
    u-boot-tools-native dtc-native \
    ${@'kernel-signing-keys-native' if d.getVar('FIT_GENERATE_KEYS') == '1' else ''} \
"

INITRAMFS_DEPLOY_DIR_IMAGE ?= "${DEPLOY_DIR_IMAGE}"
UBOOT_MKIMAGE_KERNEL_TYPE ?= "kernel"

# Matches the default in os-release.bbappend - kept here too since that
# bbappend's weak default only applies within the os-release recipe itself.
SUMMIT_VERSION ?= "0.0.0.0"

FIT_KERNEL_SIGN_ENABLE ?= "${UBOOT_SIGN_ENABLE}"
FIT_KERNEL_SIGN_KEYNAME ?= "${UBOOT_SIGN_KEYNAME}"
FIT_KERNEL_SIGN_KEYDIR ?= "${UBOOT_SIGN_KEYDIR}"

python () {
    # Same INITRAMFS_IMAGE / virtual-dtb-provider / FIT_LOADABLES handling as
    # kernel-fit-image.bbclass, unmodified, just targeting our own
    # do_compile_fit task instead of do_compile.
    image = d.getVar('INITRAMFS_IMAGE')
    if image and d.getVar('INITRAMFS_IMAGE_BUNDLE') != '1':
        if d.getVar('INITRAMFS_MULTICONFIG'):
            mc = d.getVar('BB_CURRENT_MC')
            d.appendVarFlag('do_compile_fit', 'mcdepends', ' mc:' + mc + ':${INITRAMFS_MULTICONFIG}:${INITRAMFS_IMAGE}:do_image_complete')
        else:
            d.appendVarFlag('do_compile_fit', 'depends', ' ${INITRAMFS_IMAGE}:do_image_complete')

    providerdtb = d.getVar("PREFERRED_PROVIDER_virtual/dtb")
    if providerdtb and d.getVar('FIT_EXTERNAL_DTB_OVERLAYS') == '1':
        d.appendVarFlag('do_compile_fit', 'depends', ' virtual/dtb:do_populate_sysroot')
        d.setVar('EXTERNAL_KERNEL_DEVICETREE', "${RECIPE_SYSROOT}/boot/devicetree")

    loadables = d.getVar('FIT_LOADABLES')
    if loadables:
        for v in ['FIT_LOADABLE_ARCH', 'FIT_LOADABLE_COMPRESSION',
                  'FIT_LOADABLE_DESCRIPTION', 'FIT_LOADABLE_ENTRYPOINT',
                  'FIT_LOADABLE_FILENAME', 'FIT_LOADABLE_LOADADDRESS',
                  'FIT_LOADABLE_OS', 'FIT_LOADABLE_TYPE',
                  'FIT_LOADABLE_CORE_ID', 'FIT_LOADABLE_RECIPE']:
            if d.getVar(v):
                raise bb.parse.SkipRecipe("You cannot use %s as a variable, you can only set flags." % v)

            synt_value = " ? ".join([ d.getVarFlag(v, loadable) or "" for loadable in loadables.split() ])
            d.setVar(v, synt_value)

        # FIT_LOADABLES (a loadable's type defaults to "firmware" - see
        # fitimage_emit_section_loadable() in oe.fitimage) is how one or
        # more MCU firmware images get embedded as their own sections in
        # this fitImage - e.g. for a remoteproc'd Cortex-M core. This class
        # is shared across boards/images, so it has no idea which recipe(s)
        # actually build/deploy any given MCU firmware (that's whatever the
        # board/image configuration points FIT_LOADABLES at - a recipe like
        # summit-mcu-demos is just one example, not something to hardcode
        # here). The loadable's file only shows up in DEPLOY_DIR_IMAGE once
        # that recipe's own do_deploy has run, so wire that dependency up
        # generically via the optional FIT_LOADABLE_RECIPE[<name>] flag:
        #
        #   FIT_LOADABLES += "mcu-fw1 mcu-fw2"
        #   FIT_LOADABLE_FILENAME[mcu-fw1] = "some-mcu-firmware-1.bin"
        #   FIT_LOADABLE_LOADADDRESS[mcu-fw1] = "0x00000000"
        #   FIT_LOADABLE_RECIPE[mcu-fw1] = "some-mcu-firmware-1-recipe"
        #   FIT_LOADABLE_FILENAME[mcu-fw2] = "some-mcu-firmware-2.bin"
        #   FIT_LOADABLE_LOADADDRESS[mcu-fw2] = "0x00000000"
        #   FIT_LOADABLE_RECIPE[mcu-fw2] = "some-mcu-firmware-2-recipe"
        #
        # (FIT_LOADABLE_RECIPE is only consulted here for dependency
        # ordering; it plays no part in the .its content itself.)
        for loadable in loadables.split():
            recipe = d.getVarFlag('FIT_LOADABLE_RECIPE', loadable)
            if recipe:
                d.appendVarFlag('do_compile_fit', 'depends', ' %s:do_deploy' % recipe)
}

python do_compile_fit() {
    import shutil
    import oe.fitimage

    itsfile = "fit-image.its"
    fitname = "fitImage"
    kernel_deploydir = d.getVar('DEPLOY_DIR_IMAGE')
    kernel_deploysubdir = d.getVar('KERNEL_DEPLOYSUBDIR')
    if kernel_deploysubdir:
        kernel_deploydir = os.path.join(kernel_deploydir, kernel_deploysubdir)

    # Collect all the its nodes before the its file is generated and mkimage gets executed
    root_node = oe.fitimage.ItsNodeRootKernel(
        d.getVar("FIT_DESC"), d.getVar("FIT_ADDRESS_CELLS"),
        d.getVar('HOST_PREFIX'), d.getVar('UBOOT_ARCH'), d.getVar("FIT_CONF_PREFIX"),
        oe.types.boolean(d.getVar('FIT_KERNEL_SIGN_ENABLE')), d.getVar("FIT_KERNEL_SIGN_KEYDIR"),
        d.getVar("UBOOT_MKIMAGE"), d.getVar("UBOOT_MKIMAGE_DTCOPTS"),
        d.getVar('FIT_MKIMAGE_EXTRA_OPTS'),
        d.getVar("UBOOT_MKIMAGE_SIGN"), d.getVar("UBOOT_MKIMAGE_SIGN_ARGS"),
        d.getVar('FIT_HASH_ALG'), d.getVar('FIT_SIGN_ALG'), d.getVar('FIT_PAD_ALG'),
        d.getVar('FIT_KERNEL_SIGN_KEYNAME'),
        oe.types.boolean(d.getVar('FIT_SIGN_INDIVIDUAL')), d.getVar('UBOOT_SIGN_IMG_KEYNAME')
    )

    # Prepare a kernel image section.
    shutil.copyfile(os.path.join(kernel_deploydir, "linux.bin"), "linux.bin")
    with open(os.path.join(kernel_deploydir, "linux_comp")) as linux_comp_f:
        linux_comp = linux_comp_f.read()
    root_node.fitimage_emit_section_kernel("kernel-1", "linux.bin", linux_comp,
        d.getVar('UBOOT_LOADADDRESS'), d.getVar('UBOOT_ENTRYPOINT'),
        d.getVar('UBOOT_MKIMAGE_KERNEL_TYPE'), d.getVar("UBOOT_ENTRYSYMBOL"))

    # Record the actual built kernel/image version for board-side version
    # checks. get_kernelversion_file() is provided by linux-kernel-base
    # (inherited above) and reads the real kernel ABI version out of the
    # kernel's shared build dir - the same mechanism module-base.bbclass
    # uses for KERNEL_VERSION.
    kernelversion = get_kernelversion_file(d.getVar('STAGING_KERNEL_BUILDDIR'))
    if kernelversion:
        root_node._kernel.add_property("summit-version",
            "Linux-%s-summit-%s" % (kernelversion, d.getVar('SUMMIT_VERSION')))
    else:
        bb.warn("Could not determine the kernel version from %s/kernel-abiversion - omitting summit-version from the FIT" % d.getVar('STAGING_KERNEL_BUILDDIR'))

    # Prepare a DTB image section
    #
    # overlay_confs collects the bare fdt-only configuration nodes for .dtbo
    # overlays (see below) - their creation is deferred until after
    # fitimage_emit_section_config() below, so the real DTB's own
    # configuration is still emitted first and remains the FIT 'default'.
    overlay_confs = []
    kernel_devicetree = d.getVar('KERNEL_DEVICETREE')
    external_kernel_devicetree = d.getVar("EXTERNAL_KERNEL_DEVICETREE")
    if kernel_devicetree:
        for dtb in kernel_devicetree.split():
            # In deploy_dir the DTBs are without sub-directories also with KERNEL_DTBVENDORED = "1"
            dtb_name = os.path.basename(dtb)

            # Skip DTB if it's also provided in EXTERNAL_KERNEL_DEVICETREE directory
            if external_kernel_devicetree:
                ext_dtb_path = os.path.join(external_kernel_devicetree, dtb_name)
                if os.path.exists(ext_dtb_path) and os.path.getsize(ext_dtb_path) > 0:
                    continue

            # Copy the dtb or dtbo file into the FIT image assembly directory
            shutil.copyfile(os.path.join(kernel_deploydir, dtb_name), dtb_name)
            root_node.fitimage_emit_section_dtb(dtb_name, dtb_name,
                d.getVar("UBOOT_DTB_LOADADDRESS"), d.getVar("UBOOT_DTBO_LOADADDRESS"))

    if external_kernel_devicetree:
        # iterate over all .dtb and .dtbo files in the external kernel devicetree directory
        # and copy them to the FIT image assembly directory
        for dtb_name in sorted(os.listdir(external_kernel_devicetree)):
            if dtb_name.endswith('.dtb') or dtb_name.endswith('.dtbo'):
                dtb_path = os.path.join(external_kernel_devicetree, dtb_name)
                is_overlay = dtb_name.endswith('.dtbo')

                # For symlinks, add a configuration node that refers to the DTB image node to which the symlink points
                symlink_target = oe.fitimage.symlink_points_below(dtb_name, external_kernel_devicetree)
                if symlink_target:
                    root_node.fitimage_emit_section_dtb_alias(dtb_name, symlink_target, True)
                    if is_overlay:
                        # A .dtbo overlay is applied on top of a base DTB's
                        # own configuration, not booted standalone - its
                        # configuration only needs a bare "fdt" reference,
                        # not the kernel/bootscr that
                        # fitimage_emit_section_config() would otherwise add
                        # to every entry in _dtb_alias.
                        alias_node = root_node._dtb_alias.pop()
                        overlay_confs.append((root_node._conf_prefix + alias_node.alias_name, alias_node.name))
                # For real DTB files add an image node and a configuration node
                else:
                    shutil.copyfile(dtb_path, dtb_name)
                    root_node.fitimage_emit_section_dtb(dtb_name, dtb_name,
                        d.getVar("UBOOT_DTB_LOADADDRESS"), d.getVar("UBOOT_DTBO_LOADADDRESS"), True)
                    if is_overlay:
                        # Same reasoning as the symlink case above.
                        dtb_node = root_node._dtbs.pop()
                        overlay_confs.append((root_node._conf_prefix + dtb_name, dtb_node.name))

    # Prepare a u-boot script section
    #
    # NOTE: not using fitimage_emit_section_boot_script() here - it hardcodes
    # the FIT configuration property name to "bootscr", but u-boot on this
    # board looks up "script" instead, so the image node and per-config
    # property are added directly (bootscr_node is referenced after
    # fitimage_emit_section_config() below, once the real DTB configs exist).
    fit_uboot_env = d.getVar("FIT_UBOOT_ENV")
    bootscr_node = None
    if fit_uboot_env:
        bootscr_node = root_node.its_add_node_image(
            "script", "Boot Script", "script", "none",
            {"data": '/incbin/("' + fit_uboot_env + '")', "arch": root_node._arch})

    # Prepare a setup section (For x86)
    setup_bin_path = os.path.join(kernel_deploydir, "setup.bin")
    if os.path.exists(setup_bin_path):
        shutil.copyfile(setup_bin_path, "setup.bin")
        root_node.fitimage_emit_section_setup("setup-1", "setup.bin")

    # Prepare a ramdisk section.
    initramfs_image = d.getVar('INITRAMFS_IMAGE')
    if initramfs_image and d.getVar("INITRAMFS_IMAGE_BUNDLE") != '1':
        # Find and use the first initramfs image archive type we find
        found = False
        for img in d.getVar("FIT_SUPPORTED_INITRAMFS_FSTYPES").split():
            initramfs_path = os.path.join(d.getVar("INITRAMFS_DEPLOY_DIR_IMAGE"), "%s.%s" % (d.getVar('INITRAMFS_IMAGE_NAME'), img))
            if os.path.exists(initramfs_path):
                bb.note("Found initramfs image: " + initramfs_path)
                found = True
                root_node.fitimage_emit_section_ramdisk("ramdisk-1", initramfs_path,
                    initramfs_image,
                    d.getVar("UBOOT_RD_LOADADDRESS"),
                    d.getVar("UBOOT_RD_ENTRYPOINT"))
                break
            else:
                bb.note("Did not find initramfs image: " + initramfs_path)

        if not found:
            bb.fatal("Could not find a valid initramfs type for %s, the supported types are: %s" % (d.getVar('INITRAMFS_IMAGE_NAME'), d.getVar('FIT_SUPPORTED_INITRAMFS_FSTYPES')))

    #
    # Prepare loadables sections
    #
    for loadable in d.getVar('FIT_LOADABLES').split():
        loadable_file = d.getVarFlag('FIT_LOADABLE_FILENAME', loadable)
        if not loadable_file:
            bb.fatal("File for loadable %s not specified through FIT_LOADABLE_FILENAME[%s]" % (loadable, loadable))

        loadable_loadaddress = d.getVarFlag('FIT_LOADABLE_LOADADDRESS', loadable)

        # Optional parameters
        loadable_arch = d.getVarFlag('FIT_LOADABLE_ARCH', loadable)
        loadable_compression = d.getVarFlag('FIT_LOADABLE_COMPRESSION', loadable)
        loadable_description = d.getVarFlag('FIT_LOADABLE_DESCRIPTION', loadable) or ("%s loadable" % loadable)
        loadable_entrypoint = d.getVarFlag('FIT_LOADABLE_ENTRYPOINT', loadable)
        loadable_os = d.getVarFlag('FIT_LOADABLE_OS', loadable)
        loadable_type = d.getVarFlag('FIT_LOADABLE_TYPE', loadable)
        loadable_core_id = d.getVarFlag('FIT_LOADABLE_CORE_ID', loadable)

        # Check if loadable artifact exists
        loadable_path = os.path.join(d.getVar("DEPLOY_DIR_IMAGE"), loadable_file)
        if not os.path.exists(loadable_path):
            bb.fatal("File for loadable %s not found at %s" % (loadable, loadable_path))

        root_node.fitimage_emit_section_loadable(loadable,
                                                 loadable_path, loadable_type,
                                                 loadable_description,
                                                 loadable_compression,
                                                 loadable_arch, loadable_os,
                                                 loadable_loadaddress,
                                                 loadable_entrypoint)
        if loadable_core_id:
            try:
                core_id = int(loadable_core_id, 0)
            except ValueError:
                bb.fatal("Core ID for loadable %s is not an integer: %s" %
                         (loadable, loadable_core_id))
            root_node._loadables[-1].add_property("core-id", core_id)

    # Generate the configuration section
    root_node.fitimage_emit_section_config(d.getVar("FIT_CONF_DEFAULT_DTB"), d.getVar("FIT_CONF_MAPPINGS"))

    # Reference the boot script from each real DTB's configuration (see the
    # u-boot script section above) - must run before the overlay configs are
    # added below, since those should NOT get a "script" property.
    #
    # fitimage_emit_section_config() only knows about "bootscr" (via
    # self._bootscr, which we deliberately never set - see above), so it
    # never added our "script" property to each config's signature
    # "sign-images" list. Add it here too, or the boot script would be
    # excluded from the configuration-level signature.
    if bootscr_node:
        for conf_node in root_node.configurations.sub_nodes:
            conf_node.add_property("script", bootscr_node.name)
            for sub_node in conf_node.sub_nodes:
                if sub_node.name == "signature-1":
                    sub_node.properties["sign-images"].append("script")

    # Now that the real DTB(s) have their configuration(s) emitted (and the
    # FIT 'default' is set), add the deferred bare fdt-only configurations
    # for .dtbo overlays collected above.
    for conf_name, fdt_name in overlay_confs:
        oe.fitimage.ItsNodeConfiguration(conf_name, root_node.configurations, "FDT blob",
            opt_props={"fdt": fdt_name})

    # Write the its file
    root_node.write_its_file(itsfile)

    # Assemble the FIT image
    root_node.run_mkimage_assemble(itsfile, fitname)

    # Sign the FIT image if required
    root_node.run_mkimage_sign(fitname)
}
do_compile_fit[dirs] = "${B}"
do_compile_fit[depends] += "virtual/kernel:do_deploy"

# DEPENDS alone doesn't guarantee this: bitbake only wires DEPENDS into the
# standard do_configure/do_compile tasks' implicit sysroot dependency, and
# this class deliberately uses do_compile_fit instead (see NOTE above) - so
# without an explicit depends here, do_compile_fit can race ahead of
# u-boot-tools-native/dtc-native's own do_populate_sysroot and fail to find
# uboot-mkimage/dtc on PATH.
do_compile_fit[depends] += "u-boot-tools-native:do_populate_sysroot dtc-native:do_populate_sysroot"

# do_deploy alone isn't enough to guarantee STAGING_KERNEL_BUILDDIR (read
# above for the kernel-abiversion file) is actually populated: it's written
# by virtual/kernel's do_shared_workdir, which is deliberately not
# sstate-cacheable (do_shared_workdir_setscene always fails) so it always
# runs for real - but only when something actually depends on it. Depending
# on do_deploy alone risks do_deploy being restored from a shared sstate
# cache without do_shared_workdir ever running in this build tree (e.g. a
# fresh TMPDIR/work-shared sharing an existing SSTATE_DIR), leaving
# kernel-abiversion missing. Depend on it explicitly instead.
do_compile_fit[depends] += "virtual/kernel:do_shared_workdir"
addtask compile_fit before do_image_complete

do_deploy_fit() {
    install -d "${DEPLOY_DIR_IMAGE}"
    install -m 0644 "${B}/fitImage" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}"
    ln -snf "${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_KERNEL_ITB_NAME}"
    install -m 0644 "${B}/fit-image.its" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_ITS_NAME}"

    # Generic "current" names, same convention kernel.bbclass uses for
    # KERNEL_IMAGE_LINK_NAME: whichever recipe deploys last wins the
    # symlink. u-boot/swupdate/rootfs consume these plain names and don't
    # care which image produced them.
    ln -snf "${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/fitImage"
    ln -snf "${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/kernel.itb"
    ln -snf "${FIT_DEPLOY_ITS_NAME}" "${DEPLOY_DIR_IMAGE}/fit-image.its"

    if [ "${INITRAMFS_IMAGE_BUNDLE}" != "1" ]; then
        ln -snf "${FIT_DEPLOY_ITS_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-its-${KERNEL_FIT_NAME}.its"
        if [ -n "${KERNEL_FIT_LINK_NAME}" ] ; then
            ln -snf "${FIT_DEPLOY_ITS_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-its-${KERNEL_FIT_LINK_NAME}"
        fi
    fi

    if [ -n "${INITRAMFS_IMAGE}" ]; then
        ln -snf "${FIT_DEPLOY_ITS_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-its-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_NAME}.its"
        if [ -n "${KERNEL_FIT_LINK_NAME}" ]; then
            ln -snf "${FIT_DEPLOY_ITS_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-its-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_LINK_NAME}"
        fi

        if [ "${INITRAMFS_IMAGE_BUNDLE}" != "1" ]; then
            ln -snf "${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_NAME}${KERNEL_FIT_BIN_EXT}"
            if [ -n "${KERNEL_FIT_LINK_NAME}" ] ; then
                ln -snf "${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_LINK_NAME}"
            fi
        fi
    fi
}
addtask deploy_fit after do_compile_fit before do_image_complete

python __anonymous() {
    fstype = d.getVar("IMAGE_ROOTFS_VERITY_TYPE")
    if not fstype:
        return

    pn = d.getVar("PN")

    # image.bbclass keys do_image_<type> tasks by the base type - a
    # CONVERSION_CMD (verity is just one entry in CONVERSIONTYPES, like any
    # other conversion) is appended into its base type's task rather than
    # getting a separate task of its own, so strip the trailing conversion
    # suffix before deriving the task name.
    basetype = fstype
    for ctype in (d.getVar("CONVERSIONTYPES") or "").split():
        if basetype.endswith("." + ctype):
            basetype = basetype[:-(len(ctype) + 1)]
            break

    verity_task = "do_image_%s" % basetype.replace("-", "_").replace(".", "_")

    # Only (re)assemble the FIT after this image's own verity conversion has
    # produced a fresh boot script...
    d.appendVarFlag("do_compile_fit", "depends", " %s:%s" % (pn, verity_task))

    # ...and make sure swu (do_swuimage runs after do_image_complete, see
    # swupdate-image.bbclass) waits for the freshly deployed fitImage.
    d.appendVarFlag("do_image_complete", "depends", " %s:do_deploy_fit" % pn)

    # do_image_wic is a sibling of do_image_<verity-type> - it runs before
    # do_image_complete, not after - so it needs its own explicit dependency
    # on do_deploy_fit (mirrors dm-verity.bbclass's own do_image_wic
    # dependency on the verity task).
    #
    # Match dm-verity.bbclass's own "is wic in use" check here: a plain
    # substring test against the whole IMAGE_FSTYPES string, not exact-token
    # membership in its split() list - entries are typically
    # "wic.bz2"/"wic.bmap" (wic + a CONVERSIONTYPES suffix), never the bare
    # "wic" token, so a split()-based check never matched and this
    # dependency was silently never added.
    if "wic" in (d.getVar("IMAGE_FSTYPES") or ""):
        d.appendVarFlag("do_image_wic", "depends", " %s:do_deploy_fit" % pn)
}
