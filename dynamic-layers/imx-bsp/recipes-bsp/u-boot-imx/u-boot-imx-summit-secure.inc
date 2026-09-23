include ${@'u-boot-imx-summit-secure-ahab.inc' if 'ahab' in d.getVar('OVERRIDES').split(':') and d.getVar('SECURE_BOOT') == '1' else ''}
include ${@'u-boot-imx-summit-secure-hab4.inc' if 'hab4' in d.getVar('OVERRIDES').split(':') and d.getVar('SECURE_BOOT') == '1' else ''}
