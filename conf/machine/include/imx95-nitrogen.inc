#@TYPE: Machine
#@NAME: Nitrogen i.MX95
#@SOC: i.MX95
#@DESCRIPTION: Machine configuration for Nitrogen i.MX95
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

MACHINEOVERRIDES =. "mx95:summitsom:ahab:"

require conf/machine/include/imx-base.inc
require conf/machine/include/arm/armv8-2a/tune-cortexa55.inc

# for better sstate cache reuse
DEFAULTTUNE:mx95-generic-bsp = "armv8a-crc-crypto"

require conf/machine/include/u-boot-summit.inc
include ${@bb.utils.contains('BBFILE_COLLECTIONS', 'summit-radio', 'conf/machine/include/radio-stack.inc', '', d)}

MACHINE_FEATURES = "pci usbgadget usbhost alsa rtc optee screen touchscreen wifi bluetooth camera"
MACHINE_FEATURES:append:use-nxp-bsp = " dpdk"

SOC_DEFAULT_IMAGE_FSTYPES = ""

IMAGE_BOOT_FILES = "uboot.env"
SWUPDATE_IMAGES = "flash.bin uboot.env"

UBOOT_PROVIDES_BOOT_CONTAINER = "1"
IMX_DEFAULT_BOOTLOADER:mx9-generic-bsp = "u-boot-imx-summit"
IMX_DEFAULT_UBOOTTOOLS:imx-generic-bsp = "u-boot-summit-tools"

DDR_FIRMWARE_VERSION = "202409"
DDR_FIRMWARE_NAME = " \
    lpddr5_dmem_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr5_dmem_qb_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr5_imem_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr5_imem_qb_v${DDR_FIRMWARE_VERSION}.bin \
"

SPL_BINARY = "spl/u-boot-spl.bin"

ATF_PLATFORM = "imx95"

OEI_CONFIGS = "ddr tcm"
OEI_CORE   = "m33"
OEI_SOC    = "mx95"
OEI_BOARD  = "mx95lp5"
OEI_DDR_CONFIG = "NITROGEN_IMX95_LPDDR5_6400MTS_timing"
OEI_DDRCONFIG = "${OEI_DDR_CONFIG}"
OEI_DDR_CONFIG_ECC = "NITROGEN_IMX95_LPDDR5_6400MTS_ecc_timing"

DDR_TYPE   = "lpddr5"

SYSTEM_MANAGER_CONFIG = "mx95evk"

SERIAL_CONSOLES = "115200;ttyLP0"

LOADADDR = ""
UBOOT_SUFFIX = "img"
UBOOT_BINARY = "flash.bin"
UBOOT_MAKE_TARGET = "all"
IMX_BOOT_SEEK = "32"

MACHINE_EXTRA_RDEPENDS += "${MACHINE_FIRMWARE}"
EXTRA_IMAGEDEPENDS += "virtual/bootloader"

SOC_HAS_VIVANTE_KERNEL_DRIVER_SUPPORT:imxgpu = "0"

IMX_DEFAULT_BSP = "nxp"

# Keep the SDK host variant on upstream QEMU; target QEMU remains i.MX-pinned.
PREFERRED_VERSION_nativesdk-qemu:mx95-nxp-bsp = "8.2%"

MACHINE_EXTRA_RRECOMMENDS:remove = "imx-m33-demos"  

KERNEL_CLASSES = "kernel-fit-extra-artifacts"
KERNEL_IMAGETYPES = "Image"

KERNEL_IMAGETYPE = "Image"
KERNEL_IMAGETYPE:mx9-generic-bsp = "Image"
IMAGE_CLASSES += "dm-verity"

UBOOT_LOADADDRESS = "0x92000000"
UBOOT_ENTRYPOINT:mx95-generic-bsp = "0x92000000"
UBOOT_DTB_LOADADDRESS = "0x95000000"
UBOOT_DTBO_LOADADDRESS = "0x95080000"
UBOOT_RD_LOADADDRESS = "0x96000000"

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha256"
FIT_SIGN_ALG = "rsa2048"

UBOOT_SIGN_KEYDIR = "${SIG_DATA_PATH}/keys"
SIG_DATA_PATH ?= "${SUMMIT_SOM_KEYS_DIR}/nitrogen/ahab"
UBOOT_SIGN_KEYNAME = "dev"
UBOOT_SIGN_ENABLE = "1"

UBOOT_ENV_OFFSET = "4128768"
EMMC_BOOT_OFFSET = "0"
EMMC_DEVICE = "0"
SD_DEVICE = "1"
