#@TYPE: Machine
#@NAME: Carbon AM6xx
#@SOC: AM62x, AM67x
#@DESCRIPTION: Machine configuration for Carbon AM6xx R5
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

MACHINEOVERRIDES =. "summitsom:"

require conf/machine/include/u-boot-summit.inc

TI_PREFERRED_BSP = "ti-6_18"

BSP_BOOTLOADER_PROVIDER:bsp-ti-6_18 = "u-boot-ti-summit"
BSP_BOOTLOADER_VERSION:bsp-ti-6_18 = "%"

SERIAL_CONSOLES = "115200;ttyS2"

IMAGE_FSTYPES = ""

IMAGE_CLASSES += "dm-verity"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha512"
FIT_SIGN_ALG = "rsa4096"

UBOOT_SIGN_ENABLE = "0"
UBOOT_SIGN_KEYNAME = "dev"
UBOOT_SIGN_KEYDIR = "${SUMMIT_SOM_KEYS_DIR}/carbon"

# It's needed to equalize hash for tclib-baremetal with tclib-glibc
# and as such have sstate cache reuse for native recipies
CXXFLAGS:append = " -fvisibility-inlines-hidden"
