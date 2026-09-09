---
layout: page
title: "Developing a custom image with Yocto"
category: Summit SOM DVK
order: 3
product: Summit SOM
technology: soms
---

The Summit SOM (and DVK) support the creation of custom images via the [Yocto](https://www.yoctoproject.org) Project, and the steps below document how to setup a Yocto build environment for the Summit SOM (and DVK) and build an image and also outline some next steps for integrating the SOM into a custom board image.

## Prerequisites

### Yocto Project Prerequisites
If completely new to the Yocto Project, the [documentation](https://docs.yoctoproject.org) site provides a [Quick Build](https://docs.yoctoproject.org/brief-yoctoprojectqs/index.html) getting started guide focused on the Yocto Project reference distribution (Poky) and an emulated device (QEMU, or the Quick EMUlator); however, the steps below are tailored to the Summit SOM (and DVK) and assume that you have successfully setup a development PC for use with the Yocto Project (see the [system requirements](https://docs.yoctoproject.org/ref-manual/system-requirements.html#system-requirements) for more details).


### Summit SOM (and DVK) Prerequisites
In addition to the standard Yocto Project system requirements, the [`repo`](https://gerrit.googlesource.com/git-repo/) tool is needed to support cloning the necessary repositories at the proper revisions via Git, and the steps necessary to install the `repo` tool are listed in the ['Install'](https://gerrit.googlesource.com/git-repo/#install) section of the homepage.

## Build the Default Summit SOM DVK Image
1. Create a working directory for the Yocto build environment and change directories to it:

    ```bash
    mkdir -p <working-dir>
    cd <working-dir>
    ```

2. Pull in the necessary meta data sources using the `repo` tool optionally replacing `<branch>` with the target branch and `<manifest>` with the target manifest file:
    
    ```bash
    repo init -u git@github.com:Ezurio/Summit-SOM-Release-Packages.git [-b <branch>] [-m <manifest>]
    repo sync
    ```

3. Setup the Yocto build environment by sourcing the `setup-environment` script and passing the proper parameters.

    ```bash
    [MACHINE=<machine>] [DISTRO=<distro>] source setup-environment [-h] <build-dir>
    ```

    | Parameter     | Description |
    |---------------|-------------|
    | `<machine>`   | Machine name - there is currently only one supported machine name for the Summit SOM DVK (imx8mp-summitsom) |
    | `<distro>`    | Distro name - there are currently three supported distro names for the Summit SOM DVK:<ul><li>`summitsom-cmd`</li><li>`summitsom-wayland` (default)</li><li>`summitsom-xwayland`</li></ul> |
    | `-h`          | Show the script usage help info (optional) |
    | `<build-dir>` | Build directory |

    For example, to setup the build environment for the Summit SOM DVK with Wayland support and use a build directory of `build`, use the following command:

    ```bash
    MACHINE=imx8mp-summitsom DISTRO=summitsom-wayland source setup-environment build
    ```

    **Note:** The first time the `setup-environment` script is run, the NXP Yocto BSP EULA is shown and must be read and accepted before continuing with development. On subsequent runs of the script, you do not need to specify the `MACHINE` and `DISTRO` parameters or accept the EULA. The settings can be specified at any time in the `<build-dir>/conf/local.conf` file.

4. Build the image with `bitbake`. Presently, there are two supported images that can be built (and DVK):

    | Image Name            | Description                                                      |
    |-----------------------|------------------------------------------------------------------|
    | `image-summitsom-cmd` | Summit SOM command line image                            |

    To start the build, use the following command replacing `<image-name>` with the target image name:
    ```bash
    bitbake <image-name>
    ```

    For example, to build the Summit SOM command line image, use:

    ```bash
    bitbake image-summitsom-cmd
    ```

5. Wait for the build to complete and find the build artifacts. The build process can take a significant amount of time, and when complete, the build artifacts are available in the following directory:
    ```
    <build-dir>/tmp/deploy/images/<machine-name>
    ```

    Here, you will find a `.swu` file that can be used to securely update that uses the following naming pattern:
    ```
    <image-name>-<machine-name>-<date-time>.swu
    ```

    For example, the command line image would have a `.swu` file name like:
    ```
    image-summitsom-cmd-imx8mp-summitsom-20220324205149.swu
    ```

6. There are a number of ways to flash the onboard eMMC of the SOM:

    The standard Summit SOM DVK image utilizes an A/B update scheme when running from onboard eMMC which allows for built-in fallback support in the event of an update failure. To perform an image update, use one of the methods below once you have created a new `.swu` update file.

    * HTTP/FTP Server (Pull style)

        Start a HTTP or FTP server on a development machine that is connected to a network which can be accessed by the Summit SOM.

        Boot the Summit SOM DVK from the SD card and use the `fw_update` script (built around `swupdate`) to flash the image to both a/b sides:
        ```
        fw_update -m complete <url>
        poweroff
        ```

    * USB Flash Drive

        Copy the `.swu` file onto a USB flash drive.

        Boot the Summit SOM DVK from SD card and use the `fw_update` script (built around `swupdate`) to flash the image to both a/b sides:
        ```
        fw_update -m complete /media/.../update.swu
        poweroff
        ```

    * SD Card

        Copy the `.swu` file onto a premade DVK SD card:
        ```
        sudo cp image-summitsom-cmd-imx8mp-summitsom-20220324205149.swu /media/$USER/rootfs_data/upper/home/root/ && sync
        ```
        Boot the Summit SOM DVK from the SD card and use the `fw_update` script (built around `swupdate`) to flash the image to both a/b sides:
        ```
        fw_update </path/to/update.swu | url/to/swu>
        poweroff
        ```

    * Raw USB with direct cable using fastboot (Push style)
        ```
        fastboot flash update /path/to/update.swu
        ```

    * Raw USB with direct cable using dfu (Push style)
        ```
        sudo dfu-util -D /path/to/update.swu
        ```

    * Raw Network using fastboot (Push style)
        ```
        fastboot -s tcp:<moudule ip> flash update /path/to/update.swu
        ```

    * Raw Network using netcat (Push style)
        ```
        nc -N -W 5 "<module ip>" 9000 < "/path/to/update.swu"
        ```

## Updating the Image While Running From eMMC

    See item 6 above.

## Next Steps/Customizing the Image
See the links below from the Yocto Project documentation site for further information and in depth guides to customize your image and integrate the Summit SOM into your custom design:

* [What I wish I’d known about Yocto Project](https://docs.yoctoproject.org/what-i-wish-id-known.html)
* [Transitioning to a custom environment for systems development](https://docs.yoctoproject.org/transitioning-to-a-custom-environment.html)
* [Yocto Project Software Overview](https://www.yoctoproject.org/software-overview/)
* [Tips and Tricks Wiki](https://wiki.yoctoproject.org/wiki/TipsAndTricks)

## TI BSP special notes

TI BSP contains main DVK and overlay device trees. 
Overlay adds following options:
* LVDS port D to HDMI adapter (lvds-d-hdmi)
* LVDS port E to HDMI adapter (lvds-e-hdmi) (it can only mirror port D on AM62)
* RGB to HDMI adapter (rgb-hdmi)
* LVDS to LVDS adapter with BD 7" display (lvds-d-7)
* Wi-Fi on M.2 slot - LWBxx/IFxx radio (m2-ifx-sdio)
* Wi-Fi on M.2 slot - NX611 radio (m2-nx6xx-sdio)
* Wi-Fi on M.2 slot - TI351 radio (m2-ti3xx-sdio)
* Wi-Fi on M.2 slot - 60 radio (m2-60-sdio-uart)
* And many others

To enable overlay execute command as following:
```
set-mode lvds-d-hdmi
```

Building Regulatory Test and Manufacturing Test support images:

These images require regulatory tools packages that needs to be obtained 
from Ezurio and not available from public sites for direct download.
Packages will need to be placesd into "release" folder.

```
MACHINE=xxxx DISTRO=summit-mfg bitbake image-summitsom-mfg
```
