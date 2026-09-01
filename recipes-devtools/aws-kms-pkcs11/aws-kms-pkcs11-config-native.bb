SUMMARY = "Multi-slot PKCS#11 JSON config for aws-kms-pkcs11"
DESCRIPTION = "Generates the aws-kms-pkcs11 multi-slot JSON configuration \
    file from the AWS_KMS_*_ARN variables. Split out from aws-kms-pkcs11 \
    since it has no source and no real build step of its own."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit native

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[vardeps] += "\
    AWS_KMS_KEY_ARN \
    AWS_KMS_CSF_KEY_ARN \
    AWS_KMS_IMG_KEY_ARN \
    AWS_KMS_FIT_KEY_ARN \
    AWS_KMS_AHAB_KEY_ARN \
    "

python do_install() {
    import json

    arn_vars = (
        'AWS_KMS_KEY_ARN',
        'AWS_KMS_CSF_KEY_ARN',
        'AWS_KMS_IMG_KEY_ARN',
        'AWS_KMS_FIT_KEY_ARN',
        'AWS_KMS_AHAB_KEY_ARN',
    )
    arns = sorted(set(filter(None, (d.getVar(v) or '' for v in arn_vars))))
    if not arns:
        bb.fatal("At least one of %s must be set." % ', '.join(arn_vars))

    # Extract the key ID (UUID) from the full ARN (arn:aws:kms:<region>:<account>:key/<key-id>)
    #key_id = (d.getVar('AWS_KMS_KEY_ARN') or '').rsplit('/', 1)[-1]
    #d.setVar('AWS_KMS_KEY_ID', key_id)

    config = {'slots': [{'kms_key_arn': arn} for arn in arns]}

    outdir = d.expand('${D}${datadir}/aws-kms-pkcs11')
    bb.utils.mkdirhier(outdir)
    with open(outdir + '/aws-kms-pkcs11-config.json', 'w') as f:
        f.write(json.dumps(config, indent=2))
        f.write('\n')
}

FILES:${PN} += "${datadir}"
