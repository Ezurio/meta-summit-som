DESCRIPTION = "Summit SOM Weston Image"

inherit image-summitsom-gen image-summitsom-sd-gen image-summitsom-swu-gen

IMAGE_FEATURES += "splash hwcodecs weston"

IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-dvk \
    packagegroup-summit-diag \
    "

include ${@'image-summitsom-demo-mcu.inc' if 'imx8mp-summitsom' in d.getVar('OVERRIDES').split(':') else ''}
