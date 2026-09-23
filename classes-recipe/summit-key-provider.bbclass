# Provider-neutral interface for Cloud-HSM signing key materialization.
#
# Consumers use HSM_*_KEY_ID and summit_key_provider_* helpers without knowing
# which backend supplies the key. CLOUD_HSM_BACKEND selects the implementation;
# backend-specific key settings may select a default when it is not set explicitly.

CLOUD_HSM_CERT_DAYS ?= "3650"
# Optional FIT signature algorithm override for HSM-backed keys, e.g.
# "ecdsa256" for an ECC_NIST_P256 key. An empty value preserves the machine
# default.
CLOUD_HSM_FIT_SIGN_ALG ?= ""

CLOUD_HSM_SPSDK_PKCS11_OPTIONS ?= ""

def summit_key_provider_backend_class(d):
    backend = d.getVar('CLOUD_HSM_BACKEND') or ''
    supported = (d.getVar('CLOUD_HSM_BACKENDS') or '').split()
    if backend not in supported:
        bb.fatal(f"Unsupported CLOUD_HSM_BACKEND '{backend}' "
                 f"(supported: {', '.join(supported)})")
    return 'key-provider-backends/summit-key-provider-' + backend

def summit_key_provider_spsdk_signer(d, key_id):
    """Build an SPSDK PKCS#11 signer from the active provider interface."""
    if not key_id:
        return ''
    parts = [
        'type=pkcs11',
        'so_path=' + (d.getVar('HSM_PKCS11_LIBRARY') or ''),
        'token_label=' + summit_key_provider_token_label(key_id),
        'key_label=' + summit_key_provider_key_label(key_id),
        'user_pin=unused',
    ]
    options = d.getVar('CLOUD_HSM_SPSDK_PKCS11_OPTIONS')
    if options:
        parts.append(options)
    return ';'.join(parts)

inherit ${@summit_key_provider_backend_class(d)}

python () {
    if not d.getVar('CLOUD_HSM_BACKEND'):
        bb.fatal("summit-key-provider inherited without CLOUD_HSM_BACKEND")

    fit_sign_alg = d.getVar('CLOUD_HSM_FIT_SIGN_ALG')
    hsm_fit_key_id = d.getVar('HSM_FIT_KEY_ID')
    if fit_sign_alg and hsm_fit_key_id not in (None, '', 'None'):
        d.setVar('FIT_SIGN_ALG', fit_sign_alg)
        d.setVar('UBOOT_FIT_SIGN_ALG', fit_sign_alg)
}
