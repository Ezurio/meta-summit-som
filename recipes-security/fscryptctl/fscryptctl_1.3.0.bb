SUMMARY = "low-level tool handling Linux filesystem encryption"
DESCRIPTION = "fscryptctl is a low-level tool written in C that handles raw keys and manages \
policies for Linux filesystem encryption (https://lwn.net/Articles/639427). \
For a tool that presents a higher level interface and manages metadata, key \
generation, key wrapping, PAM integration, and passphrase hashing, see \
fscrypt (https://github.com/google/fscrypt)."
HOMEPAGE = "https://github.com/google/fscryptctl"
SECTION = "base"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "f1ec919877f6b5360c03fdb44b6ed8a47aa459e8"
SRC_URI = "\
    git://github.com/google/fscryptctl.git;branch=master;protocol=https \
    file://0001-add_key-support-key-id-option.patch \
    "

S = "${WORKDIR}/git"

do_compile:prepend() {
    sed -i 's/fscryptctl\.1//g' "${S}/Makefile"
    sed -i 's/install-man//g' "${S}/Makefile"
}

do_install() {
    oe_runmake DESTDIR="${D}" PREFIX="${prefix}" install
}
