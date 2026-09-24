python __anonymous() {
    if any(dt.endswith('.dtbo') for dt in (d.getVar('KERNEL_DEVICETREE') or '').split()):
        d.appendVar('DEPENDS', ' dtc-native')
}

do_check_dtbo_compatibility() {
	overlays=""
	base_dtbs=""
	for dtbf in ${KERNEL_DEVICETREE}; do
		dtb=$(normalize_dtb "$dtbf")
		case "$dtb" in
			*.dtbo) overlays="$overlays $dtb" ;;
			*.dtb) base_dtbs="$base_dtbs $dtb" ;;
		esac
	done

	if [ -z "$overlays" ]; then
		return 0
	fi
	bbnote "Starting kernel DTBO compatibility checks"
	if [ -z "$base_dtbs" ]; then
		bbfatal "KERNEL_DEVICETREE contains overlays but no base DTB to validate against"
	fi

	for overlay in $overlays; do
		overlay_path=$(get_real_dtb_path_in_kernel "$overlay")
		if [ ! -f "$overlay_path" ]; then
			bbfatal "DT overlay not found: $overlay_path"
		fi

		for base in $base_dtbs; do
			base_path=$(get_real_dtb_path_in_kernel "$base")
			if [ ! -f "$base_path" ]; then
				bbfatal "Base DTB not found: $base_path"
			fi

			bbnote "Checking overlay $overlay against $base"
			if ! fdtoverlay -i "$base_path" -o /dev/null "$overlay_path"; then
				bbfatal "Failed to apply DT overlay $overlay to base DTB $base"
			fi
		done
	done
}

addtask check_dtbo_compatibility after do_compile_kernelmodules before do_install