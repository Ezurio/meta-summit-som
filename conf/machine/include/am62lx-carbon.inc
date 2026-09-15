#@TYPE: Machine
#@NAME: Carbon AM62Lx
#@SOC: AM62Lx
#@DESCRIPTION: Machine configuration for Carbon AM62Lx
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

MACHINEOVERRIDES =. "summitsom:"

# for better sstate cache reuse
DEFAULTTUNE = "armv8a-crc-crypto"

TFA_BOARD = "am62lx"

require conf/machine/include/u-boot-summit.inc
include ${@bb.utils.contains('BBFILE_COLLECTIONS', 'summit-radio', 'conf/machine/include/radio-stack.inc', '', d)}

MACHINE_FEATURES = "usbgadget usbhost alsa rtc optee screen touchscreen wifi bluetooth nand"

TI_PREFERRED_BSP = "ti-6_18"

SERIAL_CONSOLES = "115200;ttyS2"

MACHINE_ESSENTIAL_EXTRA_RDEPENDS = ""
EXTRA_IMAGEDEPENDS = "virtual/bootloader"

IMAGE_FSTYPES = ""

IMAGE_CLASSES += "dm-verity"

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha512"
FIT_SIGN_ALG = "rsa4096"

KERNEL_IMAGETYPE = "Image"
KERNEL_IMAGETYPES = "Image"
IMAGE_BOOT_FILES = "${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} tiboot3.bin uboot.env prov_data.tar.gz_sign_enc.bin"
SWUPDATE_IMAGES = "${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} tiboot3.bin uboot.env prov_data.tar.gz_sign_enc.bin"

CUSTOM_KEYS_DIR ?= ""

UBOOT_SIGN_ENABLE = "1"
UBOOT_SIGN_KEYNAME = "dev"
UBOOT_SIGN_KEYDIR = "${@oe.utils.conditional('CUSTOM_KEYS_DIR', '', '${SUMMIT_SOM_KEYS_DIR}/carbon', '${CUSTOM_KEYS_DIR}', d)}"

FIT_KERNEL_SIGN_ENABLE = "${UBOOT_SIGN_ENABLE}"
FIT_KERNEL_SIGN_KEYNAME = "${UBOOT_SIGN_KEYNAME}"
FIT_KERNEL_SIGN_KEYDIR = "${UBOOT_SIGN_KEYDIR}"

UBOOT_ENV_OFFSET = "3670016"
EMMC_SPL_OFFSET = "524288"
EMMC_BOOT_OFFSET = "2621440"
EMMC_DEVICE = "0"
SD_DEVICE = "1"

BT_SERIAL_PORT = "/dev/ttyS5"
