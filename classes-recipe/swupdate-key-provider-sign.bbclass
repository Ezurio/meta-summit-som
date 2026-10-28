# SWUpdate CMS signing via a Cloud-HSM key provider. The update key in
# SIG_KEY_PATH is a PKCS#11 wrapper; the pinned certificate stays in
# SIG_SUPPORT_PATH and is checked against it by image-summitsom-swu-gen.

inherit summit-key-provider

DEPENDS:append = " ${SUMMIT_KEY_PROVIDER_DEPENDS}"

do_swuimage[prefuncs] += "summit_key_provider_export_env"
do_swuimage[depends] += "${SUMMIT_KEY_PROVIDER_TASK_DEPS}"
do_swuimage[network] = "1"
