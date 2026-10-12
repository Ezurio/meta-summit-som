DESCRIPTION = "Summit SOM Command Line Image"

inherit image-summitsom-gen ${@oe.utils.ifelse('mtk' in (d.getVar('OVERRIDES') or '').split(':'), '', 'image-summitsom-sd-gen')} image-summitsom-swu-gen

IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-dvk \
    packagegroup-summit-diag \
    "

include ${@'image-summitsom-demo-mcu.inc' if 'imx8mp-summitsom' in d.getVar('OVERRIDES').split(':') else ''}
