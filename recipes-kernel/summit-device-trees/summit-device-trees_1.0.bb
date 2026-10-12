SUMMARY = "Summit SOM device tree blobs and overlays (virtual/dtb)"
DESCRIPTION = "Builds the Summit machine's default device tree and matching overlays from the dts set selected by the machine override"

inherit summit-device-tree-provider

# files/<machine override>/dts is found through FILESOVERRIDES
SRC_URI = "file://dts"
# The R5 SPL machines carry the summitsom override but have no device trees
SRC_URI:k3r5 = ""

COMPATIBLE_MACHINE = "summitsom"

DT_FILES_PATH = "${S}/dts"
