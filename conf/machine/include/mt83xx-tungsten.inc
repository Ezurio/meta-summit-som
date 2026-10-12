#@TYPE: Machine
#@NAME: Tungsten 510 and 700
#@SOC: Genio 510, 700
#@DESCRIPTION: Machine configuration for Tungsten 510 and 700
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

require conf/machine/include/arm/arch-armv8a.inc
require conf/machine/include/soc-family.inc
require conf/machine/include/mediatek-common.inc

PREFERRED_VERSION_hwdata = "0.393"
PREFERRED_VERSION_hwdata-native = "0.393"
PREFERRED_VERSION_libdisplay-info = "0.2.0"
PREFERRED_VERSION_wayland = "1.24.0"
PREFERRED_VERSION_wayland-native = "1.24.0"
PREFERRED_VERSION_wayland-protocols = "1.47"
PREFERRED_VERSION_wayland-protocols-native = "1.47"
PREFERRED_VERSION_wayland-utils = "1.3.0"
PREFERRED_VERSION_weston = "15.0.1"

PREFERRED_PROVIDER_virtual/dtb:remove = "dtbo"

# for better sstate cache reuse
DEFAULTTUNE = "armv8a-crc-crypto"

SOC_SRAM_ADDR = "0x201000"

# Trusted Firmware-A
TFA_PLATFORM = "mt8188"

# OP-TEE
OPTEE_TARGET = "mediatek-mt8370"
# OP-TEE DRAM size (backup value only)
OPTEE_DRAM_SIZE = "0x100000000"

# LK
LK_PROJECT = "mt8390-evk"

MACHINE_FEATURES:summitsom = "usbgadget usbhost alsa rtc  screen touchscreen wifi bluetooth camera \
	${@bb.utils.contains('NDA_BUILD', '1', 'optee-efuse', '', d)}"

# Mali
require conf/machine/include/gpu-provider.inc
MALI_SOC = "mt8188"

MACHINE_EXTRA_RRECOMMENDS:append = " \
	linux-firmware-mt8188-scp \
	linux-firmware-mt8188-sof \
	kernel-module-mtk-vcu-driver-mt8365 \
	kernel-module-mtk-vcodec-driver-mt8365 \
	${@bb.utils.contains("MACHINE_FEATURES", "optee-efuse", "optee-ewriter", "", d)} \
"

MACHINEOVERRIDES =. "summitsom-genio:summitsom:"

require conf/machine/include/u-boot-summit.inc

PREFERRED_PROVIDER_u-boot = "u-boot-summit-mt83xx"
PREFERRED_PROVIDER_virtual/bootloader = "u-boot-summit-mt83xx"
PREFERRED_PROVIDER_u-boot:summitsom-usb-boot = "u-boot-summit-mt83xx-da"
PREFERRED_PROVIDER_virtual/bootloader:summitsom-usb-boot = "u-boot-summit-mt83xx-da"

include ${@bb.utils.contains('BBFILE_COLLECTIONS', 'summit-radio', 'conf/machine/include/radio-stack.inc', '', d)}

MACHINE_FEATURES = "usbgadget usbhost alsa rtc optee screen touchscreen wifi bluetooth camera"

# libdram
LIBDRAM_BOARD_NAME = "genio-700-evk"

# LK
LK_PROJECT = "mt8390-evk"
LK_BOARD_NAME = "${LK_PROJECT}"

SERIAL_CONSOLES = "115200;ttyS0"

MACHINE_ESSENTIAL_EXTRA_RDEPENDS = ""

EXTRA_IMAGEDEPENDS = "virtual/bl2:do_deploy virtual/bl3:do_deploy virtual/bootloader:do_deploy"

IMAGE_FSTYPES = ""

IMAGE_CLASSES += "dm-verity"

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha256"
FIT_SIGN_ALG = "rsa3072"

KERNEL_CLASSES = "kernel-fit-extra-artifacts"
KERNEL_IMAGETYPE = "Image"
KERNEL_IMAGETYPES = "Image"
IMAGE_BOOT_FILES = "bl2.img fip.bin uboot.env"
SWUPDATE_IMAGES = "bl2.img fip.bin uboot.env"

CUSTOM_KEYS_DIR ?= ""

UBOOT_SIGN_ENABLE = "1"
UBOOT_SIGN_KEYNAME = "dev"
UBOOT_SIGN_KEYDIR = "${@oe.utils.conditional('CUSTOM_KEYS_DIR', '', '${SUMMIT_SOM_KEYS_DIR}/tungsten', '${CUSTOM_KEYS_DIR}', d)}"
DEFAULT_EFUSE_KEY = "${SUMMIT_SOM_KEYS_DIR}/tungsten/dev.key"
SECURE_BOOT_ROT_KEY ?= "${SUMMIT_SOM_KEYS_DIR}/tungsten/dev.key"
BL2_SIGN_ENABLE = "${SECURE_BOOT}"
DA_SIGN_ENABLE = "${SECURE_BOOT}"

FIT_KERNEL_SIGN_ENABLE = "${UBOOT_SIGN_ENABLE}"
FIT_KERNEL_SIGN_KEYNAME = "${UBOOT_SIGN_KEYNAME}"
FIT_KERNEL_SIGN_KEYDIR = "${UBOOT_SIGN_KEYDIR}"

UBOOT_ENV_OFFSET = "4145152"
FIP_OFFSET = "409600"
EMMC_DEVICE = "0"
SD_DEVICE = "1"

SUMMIT_SOM_RADIO_TYPE ??= "mt320"
