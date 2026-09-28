#!/bin/sh

get_video_format() {
    CURRENT_FORMAT=$(v4l2-ctl -d "${CAM_DEV}" --get-fmt-video 2>/dev/null) || return 1
    fourcc=$(printf '%s\n' "${CURRENT_FORMAT}" | sed -nr "s/.*Pixel Format[[:space:]]*:[[:space:]]*'([^']+)'.*/\1/p")
    SIZE=$(printf '%s\n' "${CURRENT_FORMAT}" | sed -nr 's/.*Width\/Height[[:space:]]*:[[:space:]]*([0-9]+)\/([0-9]+).*/\1x\2/p')
    case "${fourcc}" in
        UYVY) format=UYVY ;;
        YUYV) format=YUY2 ;;
        *) return 1 ;;
    esac
    [ -n "${SIZE}" ] || return 1
    WIDTH=${SIZE%x*}
    HEIGHT=${SIZE#*x}
    FORMAT="${format} ${WIDTH} ${HEIGHT}"
}

DIRECT_VIDEO=0
case "$1" in
    /dev/video-*-cam*)
        CAM_DEV="$1"
        CAM_NUM=${CAM_DEV##*cam}
        ID="/dev/media${CAM_NUM}"
        ;;
    /dev/video*)
        CAM_DEV="$1"
        ;;
    /dev/media*)
        ID="$1"
        CAM_NUM=${ID#/dev/media}
        for dev in /dev/video-*-cam${CAM_NUM}; do
            [ -e "${dev}" ] || continue
            CAM_DEV=${dev}
            break
        done
        ;;
    *)
        for dev in /dev/video-*-cam*; do
            [ -e "${dev}" ] || continue
            CAM_DEV=${dev}
            CAM_NUM=${CAM_DEV##*cam}
            ID="/dev/media${CAM_NUM}"
            break
        done
        ;;
esac

ls -d /sys/class/drm/card* >/dev/null 2>&1 || {
    echo "No display device found"
    exit 1
}

[ -e "${CAM_DEV}" ] || {
    echo "No configured camera capture device found at ${CAM_DEV}"
    exit 1
}
get_video_format || {
    echo "No configured UYVY/YUYV capture format found on ${CAM_DEV}"
    exit 1
}

if [ -f /sys/devices/soc0/soc_id ]; then
    # Get the SoC ID
    read -r soc_id < /sys/devices/soc0/soc_id
elif [ -f /sys/devices/soc0/family ]; then
    # Get the SoC family
    read -r soc_id < /sys/devices/soc0/family
else
    soc_id="unknown"
fi

if pgrep weston >/dev/null 2>&1; then
    SINK=waylandsink
    formatstr=",format=${format}"

    case "${soc_id}" in
        i.MX93)
            if ! grep -q 'use-g2d=true' /etc/xdg/weston/weston.ini 2>/dev/null; then
                SINK="videoconvert ! waylandsink sync=false"
            fi
            ;;
    esac
else
    case "${soc_id}" in
        AM6*|J722S)
            if media-ctl -d "${ID}" -p | grep -q 'pivariety'; then
                SINK="kmssink can-scale=false"
            else
                SINK="kmssink"
            fi
            formatstr=",format=UYVY"
            ;;
        i.MX95)
            formatstr=",format=${format},framerate=5/1"
            SINK="videoconvert ! fbdevsink"
            ;;
        i.MX8MP)
            formatstr=""
            if media-ctl -d "${ID}" -p | grep -q 'pivariety'; then
                SINK="fbdevsink"
            else
                SINK="kmssink can-scale=false"
            fi
            ;;
        i.MX*)
            formatstr=",format=${format},framerate=5/1"
            SINK="videoconvert ! kmssink can-scale=false"
            ;;
        *)
            SINK="kmssink can-scale=false"
            formatstr=",format=${format}"
            ;;
    esac
fi

set -- ${FORMAT}

gst-launch-1.0 v4l2src device="${CAM_DEV}" ! video/x-raw,width="${2}",height="${3}""${formatstr}" ! ${SINK}
