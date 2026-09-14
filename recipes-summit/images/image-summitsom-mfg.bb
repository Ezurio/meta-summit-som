SUMMARY = "Summit SOM Manufacturing and Regulatory Compliance Testing support Image"

inherit image-summitsom-gen image-summitsom-sd-gen image-summitsom-swu-gen

REQUIRED_DISTRO_FEATURES += "summitsom-mfg"

IMAGE_FEATURES = "\
    allow-empty-password \
    allow-root-login \
    empty-root-password \
    serial-autologin-root \
    "

# Minimal base packages
IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-diag \
    summit-set-mode \
    busybox-syslog \
    ${@bb.utils.contains('COMBINED_FEATURES', 'wifi', 'packagegroup-summit-radio-stack-mfg', '', d)} \
    ${@bb.utils.contains('COMBINED_FEATURES', 'alsa', 'kernel-module-tac5x1x', '', d)} \
    "
