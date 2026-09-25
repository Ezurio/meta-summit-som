#!/bin/sh

ID=${1:-/dev/media0}

SENSOR=$(media-ctl -d "${ID}" -p | sed -rn 's/- entity [0-9]+: (.* [0-9]+-[0-9a-f]+) \(.*/\1/p')

# BD LCD panels are 1280x800, thus the format choice
case "${SENSOR}" in
	ov5640*|*pivariety*)
		WIDTH=1280
		HEIGHT=720
		;;
	ov5645*)
		WIDTH=1280
		HEIGHT=960
		;;
	*)
		exit 0
		;;
esac

OV564x_CAM_FMT="[fmt:UYVY/${WIDTH}x${HEIGHT} field:none colorspace:srgb]"

if [ -r /sys/devices/soc0/soc_id ]; then
    # Get the SoC ID
    read -r soc_id < /sys/devices/soc0/soc_id

	case "${soc_id}" in
		i.MX95)
			media-ctl -R "'crossbar' [2/0 -> 5/0 [1], 2/0 -> 6/0 [0], 2/0 -> 7/0 [0], 2/0 -> 8/0 [0], 2/0 -> 9/0 [0], 2/0 -> 10/0 [0], 2/0 -> 11/0 [0], 2/0 -> 12/0 [0]]"
			;;
	esac
fi

NODE_NEXT="${SENSOR}"
while [ -n "${NODE_NEXT}" ]; do
	NODE="${NODE_NEXT}"
	SUBDEV=$(media-ctl -d "${ID}" -p -e "${NODE}" | sed -rn 's/\s+device node name (\/dev\/v4l-subdev[0-9]+)/\1/p')
	if [ -n "${SUBDEV}" ]; then
		media-ctl -d "${ID}" --set-v4l2 "\"${NODE}\":0 ${OV564x_CAM_FMT}" 2>/dev/null
	fi

	NODE_NEXT=$(media-ctl -d "${ID}" -p -e "${NODE}" | sed -rn '/^\s*->/{s/^\s*-> "(.*)".*/\1/p;q}')
done

CAM_SUBDEV=$(media-ctl -d "${ID}" -p -e "${SENSOR}" | sed -rn 's/\s+device node name ([^ ]+)/\1/p')
CAM_DEV=$(media-ctl -d "${ID}" -p -e "${NODE}" | sed -rn 's/\s+device node name ([^ ]+)/\1/p')

# If final capture node is an ISI or video device, set format to YUYV
if [ -n "${CAM_DEV}" ] && [ -e "${CAM_DEV}" ]; then
	if v4l2-ctl -d "${CAM_DEV}" --list-formats | grep -q 'YUYV'; then
		v4l2-ctl -d "${CAM_DEV}" --set-fmt-video=width=${WIDTH},height=${HEIGHT},pixelformat=YUYV
	elif v4l2-ctl -d "${CAM_DEV}" --list-formats | grep -q 'UYVY'; then
		v4l2-ctl -d "${CAM_DEV}" --set-fmt-video=width=${WIDTH},height=${HEIGHT},pixelformat=UYVY
	fi
fi

num=${ID#/dev/media}
ln -snf "${CAM_DEV}" "/dev/video-${SENSOR%% *}-cam${num}"
ln -snf "${CAM_SUBDEV}" "/dev/v4l-${SENSOR%% *}-subdev${num}"
