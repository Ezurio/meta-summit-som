FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0001-camsys-camsv-fix-busy-s_fmt-and-runtime-resume-error.patch \
    file://0002-camsys-pool-unwind-working-buf-pool-init-on-failure.patch \
    file://0003-camsys-seninf-enable-sensor-and-camsv-links-by-default.patch \
    file://0004-camsys-ctrl-fit-sensor-timers-to-measured-sof-period.patch \
    file://0005-camsys-camsv-index-pipes-by-id-not-by-probed-count.patch \
    file://0006-camsys-camsv-fail-stream-on-when-top-camsv-is-missing.patch \
"
