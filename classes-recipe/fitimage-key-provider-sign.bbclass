# Cloud-HSM FIT signing for recipes using kernel-fitimage or custom-fit-gen.
# The FIT key in SIG_KEY_PATH is a PKCS#11 wrapper, never generated locally.

inherit uboot-key-provider-common

FIT_GENERATE_KEYS = "0"

do_kernel_generate_rsa_keys:prepend() {
    summit_key_provider_setup_env
}

do_assemble_fitimage:prepend() {
    summit_key_provider_setup_env
}

do_assemble_fitimage_initramfs:prepend() {
    summit_key_provider_setup_env
}

# For custom-fit-gen users (e.g. summit-mcu-demos) where signing happens in do_compile
do_compile:prepend() {
    summit_key_provider_setup_env
}

do_kernel_generate_rsa_keys[network] = "1"
do_assemble_fitimage[network] = "1"
do_assemble_fitimage_initramfs[network] = "1"
do_compile[network] = "1"
do_compile_fit[network] = "1"

do_compile_fit[prefuncs] += "summit_key_provider_export_env"
do_compile_fit[depends] += "${SUMMIT_KEY_PROVIDER_TASK_DEPS}"
