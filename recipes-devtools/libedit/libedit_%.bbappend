FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://1000-prompt-support-trailing-literal-escape-sequences.patch \
    file://1001-readline-add-rl_clear_visible_line-and-rl_reset_line_state.patch \
    file://1002-readline-fix-redisplay-avoid-pushing-reprint.patch \
    file://1003-readline-callback-mode-and-exit-cleanup.patch \
"
