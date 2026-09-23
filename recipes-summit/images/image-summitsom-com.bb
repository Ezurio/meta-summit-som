DESCRIPTION = "Summit SOM Command Line Communication Module Image"

inherit image-summitsom-gen image-summitsom-sd-gen image-summitsom-swu-gen

IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-rcm \
    packagegroup-summit-diag \
    "
