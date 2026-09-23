require conf/machine/include/u-boot-summit.inc
include ${@bb.utils.contains('BBFILE_COLLECTIONS', 'summit-radio', 'conf/machine/include/radio-stack.inc', '', d)}

DEFAULTTUNE:k3 ?= "armv8a-crc-crypto"

MACHINEOVERRIDES =. "summitsom:"

TI_PREFERRED_BSP = "ti-6_12"

BSP_BOOTLOADER_PROVIDER:bsp-ti-6_12 = "u-boot-ti-summit"
BSP_BOOTLOADER_VERSION:bsp-ti-6_12 = "%"

IMAGE_FSTYPES = ""

IMAGE_CLASSES += "dm-verity"

SWUPDATE_IMAGES = "\
    ${SPL_BINARYNAME} \
    u-boot.${UBOOT_SUFFIX} \
    tiboot3.bin \
    uboot.env \
    prov_data.tar.gz_sign_enc.bin \
    "

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha512"
FIT_SIGN_ALG = "rsa4096"

CUSTOM_KEYS_DIR ?= ""
UBOOT_SIGN_KEYDIR = "${@oe.utils.conditional('CUSTOM_KEYS_DIR', '', '${SUMMIT_SOM_KEYS_DIR}/carbon', '${CUSTOM_KEYS_DIR}', d)}"
UBOOT_SIGN_KEYNAME = "dev"

# Do not set these for k3r5
UBOOT_SIGN_ENABLE:k3 = "1"
KERNEL_CLASSES = "kernel-fit-extra-artifacts"
KERNEL_IMAGETYPE:k3 = "Image"
KERNEL_IMAGETYPES:k3 = "Image"
IMAGE_BOOT_FILES:k3 = "${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} tiboot3.bin uboot.env prov_data.tar.gz_sign_enc.bin"

MACHINE_ESSENTIAL_EXTRA_RDEPENDS:remove = "kernel-image-image kernel-devicetree"

MACHINE_FEATURES:k3 = "usbgadget usbhost vfat ext2 alsa optee rtc screen wifi bluetooth" 

MACHINE_FEATURES:k3r5 = ""

# It's needed to equalize hash for tclib-baremetal with tclib-glibc
# and as such have sstate cache reuse for native recipies
CXXFLAGS:append:k3r5 = " -fvisibility-inlines-hidden"

UBOOT_ENV_OFFSET = "3670016"
EMMC_SPL_OFFSET = "524288"
EMMC_BOOT_OFFSET = "2621440"

EMMC_DEVICE = "0"
SD_DEVICE = "1"

SERIAL_CONSOLES = "115200;ttyS2"
