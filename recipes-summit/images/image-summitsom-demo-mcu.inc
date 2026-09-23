IMAGE_INSTALL:append:imx8mp-summitsom = " summit-mcu-demos"

FIT_LOADABLES:append:imx8mp-summitsom = " mcu-firmware-0"
FIT_LOADABLE_FILENAME[mcu-firmware-0] = "summit-som8mplus-dvk-mcu-low-power-wakeup-demo-itcm.elf"
FIT_LOADABLE_CORE_ID[mcu-firmware-0] = "0"
FIT_LOADABLE_RECIPE[mcu-firmware-0] = "summit-mcu-demos"
