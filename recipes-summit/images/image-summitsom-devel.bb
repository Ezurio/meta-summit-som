DESCRIPTION = "Summit SOM Devel Image"

inherit image-summitsom-gen image-summitsom-sd-gen

# Data partition size
WIC_ROOTFS_DATA_FIXED_SIZE = "5G"

IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-dvk \
    packagegroup-summit-diag \
    packagegroup-core-buildessential \
    "

include ${@'image-summitsom-demo-mcu.inc' if 'imx8mp-summitsom' in d.getVar('OVERRIDES').split(':') else ''}
