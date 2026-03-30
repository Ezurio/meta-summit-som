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
    ${KERNEL_IMAGETYPE} \
    prov_data.tar.zst_sign_enc.bin \
    "

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha512"
FIT_SIGN_ALG = "rsa4096"

UBOOT_SIGN_KEYDIR ?= "${SUMMIT_SOM_KEYS_DIR}/carbon"
UBOOT_SIGN_KEYNAME = "dev"

# Do not set these for k3r5
UBOOT_SIGN_ENABLE:k3 = "1"
KERNEL_IMAGETYPE:k3 = "fitImage"
KERNEL_IMAGETYPES:k3 = "fitImage"
IMAGE_BOOT_FILES:k3 = "${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} tiboot3.bin uboot.env ${KERNEL_IMAGETYPE}"

MACHINE_ESSENTIAL_EXTRA_RDEPENDS:remove = "kernel-image-image kernel-devicetree"

MACHINE_FEATURES = "usbgadget usbhost vfat ext2 alsa optee rtc screen wifi bluetooth" 

EMMC_DEVICE = "0"
SD_DEVICE = "1"

SERIAL_CONSOLES = "115200;ttyS2"
