RPMB_FS_DEV_ID ?= "2"

EXTRA_OEMAKE:append:summitsom:mx8mm-generic-bsp = " \
    CFG_CORE_LARGE_PHYS_ADDR=y \
    CFG_CORE_ARM64_PA_BITS=36 \
    CFG_DDR_SIZE='UL(0x100000000)' \
    CFG_TZDRAM_START=0x56000000 \
"

EXTRA_OEMAKE:append:summitsom:mx8mp-generic-bsp = " \
    CFG_DDR_SIZE='UL(0x200000000)' \
"

EXTRA_OEMAKE:append:summitsom = " \
    CFG_TEE_CORE_LOG_LEVEL=1 \
    CFG_IN_TREE_EARLY_TAS='trusted_keys/f04a0fe7-1f5d-4b9b-abf7-619b85b4ce8c' \
    "

#EXTRA_OEMAKE:append:summit-secure = "\
#	CFG_RPMB_FS=y \
#	CFG_RPMB_FS_DEV_ID=${RPMB_FS_DEV_ID} \
#	CFG_REE_FS=n \
#"

#EXTRA_OEMAKE:append:summit-secure-provision = "\
#	CFG_RPMB_WRITE_KEY=y \
#"
