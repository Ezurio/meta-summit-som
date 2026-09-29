SUMMARY = "Summit SOM Rescue SD Card Boot Image"
DESCRIPTION = "Summit SOM Rescue Manufacturing Provisioning SD Card Boot Image"

inherit image-summitsom-gen image-summitsom-sd-gen image-summitsom-swu-gen
REQUIRED_DISTRO_FEATURES += "summitsom-rescue"


IMAGE_FEATURES = "\
    allow-empty-password \
    allow-root-login \
    empty-root-password \
    serial-autologin-root \
    "

# Minimal base packages
IMAGE_INSTALL += "\
    kernel-modules \
    ca-certificates \
    iproute2 \
    optee-client \
    dhcpcd \
    summit-update \
    summit-usbgadget \
    busybox-syslog \
    summit-initdata \
    "
