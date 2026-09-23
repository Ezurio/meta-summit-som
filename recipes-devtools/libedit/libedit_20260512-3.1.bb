SUMMARY = "BSD replacement for libreadline"
DESCRIPTION = "Command line editor library providing generic line editing, \
history, and tokenization functions"
HOMEPAGE = "http://www.thrysoee.dk/editline/"
SECTION = "libs"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://COPYING;md5=1e4228d0c5a9093b01aeaaeae6641533"

DEPENDS = "ncurses"

inherit autotools

SRC_URI = "http://www.thrysoee.dk/editline/${BP}.tar.gz \
           file://stdc-predef.patch \
           file://1000-prompt-support-trailing-literal-escape-sequences.patch \
           file://1001-readline-add-rl_clear_visible_line-and-rl_reset_line_state.patch \
           file://1002-readline-fix-redisplay-avoid-pushing-reprint.patch \
           file://1003-readline-callback-mode-and-exit-cleanup.patch \
          "
SRC_URI[sha256sum] = "432d5e7ea8b0116dd39f2eca7bc11d0eed77faa6b77ea526ace89907c23ea4a0"

# configure hardcodes /usr/bin search path bypassing HOSTTOOLS
CACHED_CONFIGUREVARS += "ac_cv_path_NROFF=/bin/false"

BBCLASSEXTEND = "native nativesdk"
