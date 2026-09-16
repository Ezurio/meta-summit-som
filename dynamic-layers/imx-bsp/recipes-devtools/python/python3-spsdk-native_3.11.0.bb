SUMMARY = "Open Source Secure Provisioning SDK for NXP MCU/MPU"
DESCRIPTION = "SPSDK provides tools for generating TrustZone, MasterBootImage, \
SecureBinary and AHAB images for NXP MCU/MPU devices."
HOMEPAGE = "https://github.com/nxp-mcuxpresso/spsdk"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=863e3c0c79e2589ac9d16c3918e115d1"

SRC_URI[sha256sum] = "3a601381ad6ac0195d5174b526840f749f8a56a4c6b8b64593e9092d58106023"
SRC_URI += " \
    file://0002-ignore-import-errors.patch \
"

# spsdk uses setuptools with setuptools_scm for version; the PyPI sdist includes
# a pre-generated spsdk/__version__.py so git is not needed at build time, but
# setuptools_scm must still be present in the native sysroot.
DEPENDS += "python3-setuptools-scm-native"

inherit pypi python_setuptools_build_meta native

# Build dependencies (libusbsio and libuuu are optional and are handled by
# patches;
# ruamel.yaml.clib is an optional C-accelerator omitted as ruamel.yaml falls back
# to its pure-Python implementation automatically;
# rich is listed in requirements.txt but is not imported anywhere in spsdk 3.11.0).
DEPENDS += "\
    python3-asn1crypto-native \
    python3-bincopy-native \
    python3-bitstring-native \
    python3-click-command-tree-native \
    python3-click-option-group-native \
    python3-click-native \
    python3-colorama-native \
    python3-crcmod-native \
    python3-cryptography-native \
    python3-deepmerge-native \
    python3-fastjsonschema-native \
    python3-filelock-native \
    python3-hexdump-native \
    python3-humanfriendly-native \
    python3-importlib-metadata-native \
    python3-jinja2-native \
    python3-oscrypto-native \
    python3-packaging-native \
    python3-platformdirs-native \
    python3-prettytable-native \
    python3-pyasn1-native \
    python3-pyserial-native \
    python3-pyyaml-native \
    python3-requests-native \
    python3-ruamel-yaml-native \
    python3-sly-native \
    python3-typing-extensions-native \
    python3-x690-native \
    "
