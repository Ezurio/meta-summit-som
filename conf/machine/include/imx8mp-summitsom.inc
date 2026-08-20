#@TYPE: Machine
#@NAME: Summit SOM i.MX8M Plus
#@SOC: i.MX8
#@DESCRIPTION: Machine configuration for Summit SOM i.MX8M Plus
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

require conf/machine/include/imx8mp-nitrogen.inc

SUMMIT_SOM_RADIO_TYPE = "60-som8mp"
BT_SERIAL_PORT = "/dev/ttymxc2"

EMMC_DEVICE = "2"
SD_DEVICE = "0"
