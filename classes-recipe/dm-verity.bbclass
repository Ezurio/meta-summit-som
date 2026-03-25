inherit custom-fit-gen

verity_setup() {
    local type=${1}
    local input=${IMAGE_NAME}.${type}
    local output=${input}.verity
    local output_link=${IMAGE_LINK_NAME}.${type}.verity
    local size=$(stat --printf="%s" ${input})

    cp -a ${input} ${output} || return 1

    # Let's drop the first line of output (doesn't contain any useful info)
    # and feed the rest to another function.
    veritysetup --hash-offset=${size} format ${output} ${output} | \
        sed -r '1d; s/^([^:]+):\s+(.+)/\U\1=\E\2/; s/ /_/g' > ${output}.env

    ln -sf ${output}.env ${output_link}.env

    fallocate -d ${output}

    # Read values from file
    while read -r line; do eval ${line}; done < ${output}.env

    # Add partition size
    local HASH_BLOCK=$(expr ${DATA_BLOCKS} + 1)
    local DATA_SECT=$(expr ${DATA_BLOCKS} \* ${DATA_BLOCK_SIZE} / 512)

    printf "HASH_BLOCK=${HASH_BLOCK}\nDATA_SECT=${DATA_SECT}\n" >> ${output}.env

    {
        printf 'boot_dev=/dev/mmcblk${mmcdev}p${rootvol}\n'
        printf 'dm_table="vroot,%s,,ro,0 %s verity 1 ${boot_dev} ${boot_dev} %s %s %s %s %s %s %s"\n' \
            ${UUID} ${DATA_SECT} ${DATA_BLOCK_SIZE} ${HASH_BLOCK_SIZE} \
            ${DATA_BLOCKS} ${HASH_BLOCK} ${HASH_ALGORITHM} ${ROOT_HASH} ${SALT}
        printf 'setenv bootargs "${bootargs} ${IMAGE_BOOTSTR} dm-mod.create=\\"${dm_table}\\" '
        printf 'dm-mod.waitfor=${boot_dev} root=/dev/dm-0 rootwait rootfstype=%s ro"\n' \
            ${type%%-*}
    } > ${output}.scr

    fitimage_script ${output}.scr.its ${output}.scr ${output}.scr.bin

    ln -sf ${output}.scr.bin ${output_link}.scr.bin
}

IMAGE_TYPES += "verity"
CONVERSIONTYPES += "verity"
CONVERSION_CMD:verity = "verity_setup ${type}"
CONVERSION_DEPENDS_verity = "cryptsetup-native"

python __anonymous() {
    image_fstypes = d.getVar('IMAGE_FSTYPES')
    pn = d.getVar('PN')

    if "wic" not in image_fstypes:
        return

    fstypes_list = image_fstypes.split()

    # If we're using wic: we'll have to use partition images and not the rootfs
    # source plugin so add the appropriate dependency.
    for fst in fstypes_list:
        if ".verity" in fst:
            f = fst[:fst.index(".verity")]
            dep = ' %s:do_image_%s' % (pn, f.replace('-', '_'))
            d.appendVarFlag('do_image_wic', 'depends', dep)
}
