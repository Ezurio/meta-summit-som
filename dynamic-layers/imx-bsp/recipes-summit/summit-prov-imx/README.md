# summit-prov-imx provisioning

## Purpose

`summit-prov-imx` provides a way to include secrets in an image without placing
the plaintext provisioning archive or its decryption key in the target root
filesystem. A product supplies certificates, private keys, and other data in a
`prov_data` directory. During the secured build, the recipe creates an archive
and encrypts it with a fleet AES-256 key. The target asks the i.MX93 EdgeLock
Secure Enclave (ELE) to unwrap that fleet key and perform the authenticated
decryption.

The design separates the following material:

- **Provisioning content**: files that must be secret on the target, supplied in
  `prov_data/` and encrypted before entering the image.
- **Fleet AES key**: encrypts the provisioning archive on the build host. It is
  wrapped for ELE and is never installed in plaintext.
- **Persistent ELE import master**: derived independently by SPSDK on the build
  host and by ELE on the target using ECDH and HKDF. Its plaintext value exists only in the
  temporary SPSDK workspace and is never installed on the target.
- **OEM signing key**: authenticates the signed ELE key-exchange request. Its
  certificate must belong to the OEM SRK table trusted by the device.
- **NXP production key-agreement key**: lets the build host and ELE derive the
  same import master. ELE holds the private key; the build uses the corresponding
  NXP public key.

The inputs included in this repository are provided only as a development
example and must not be used in production. You must generate and supply
your own product-specific provisioning content and secret cryptographic inputs,
and protect all associated private key material.

## Build and runtime flow

1. The recipe uses SPSDK and the secured build's AHAB signer to create a signed
   key-exchange message requesting persistent `OEM_IMPORT_MK_SK` key ID 1.
2. SPSDK derives the same master on the build host and uses it to create an ELE
   key-import TLV that wraps the fleet AES key.
3. The recipe archives `prov_data/` deterministically and encrypts it with the
   fleet key using AES-256-GCM and a fresh 12-byte nonce.
4. The image contains only the signed message, OEM public key, wrapped-key TLV,
   encrypted payload, and checksums. Temporary derived keys are deleted during
   the build.
5. The helper first attempts to import the volatile fleet key using persistent
  master ID 1. If the master is absent, it authenticates the signed message,
  performs one strict key-exchange operation to create the master, and retries
  the import. ELE persistence blobs are backed by `nvm_daemon` storage.
6. The helper uses the imported volatile key to authenticate and decrypt the
  provisioning payload. The volatile key disappears when its key store is
  closed or the device reboots.
7. On first boot, `summit-prov-imx.service` waits for `/data`, the ELE NVM
  daemon, and the TEE supplicant. Its wrapper verifies `SHA256SUMS`, decrypts
  the archive into a private temporary directory, initializes the OP-TEE
  PKCS#11 token, and imports the archive's certificates and private keys.
8. The wrapper copies optional `data/` content to `/data/prov`, removes all
  temporary plaintext, and creates `/data/.provisioned` only after every step
  succeeds. Later boots skip provisioning when that persistent marker exists.

The helper also accepts `--reuse-only`. This mode requires the key store and
persistent master ID 1 to exist, verifies the master's attributes, and never
attempts to create either one.

## Input layout

The `prov_data` and `summit-prov-imx` input directories must be direct
subdirectories of `SIG_DATA_PATH`. The recipe also uses the existing secured
AHAB build configuration, signer, and SRK certificates under that path. For
example, when `SIG_DATA_PATH` is `/path/to/keys/nitrogen/ahab`, it reads:

    /path/to/keys/nitrogen/ahab/
    ├── spsdk_ahab.yaml
    ├── keys/                     # OEM AHAB signer
    ├── crts/                     # OEM AHAB SRK certificates
    ├── prov_data/
    │   ├── keystore/
    │   └── data/                 # optional
    └── summit-prov-imx/
        ├── oem-ka-private.pem
        └── fleet-aes-256.bin

  The source tree provides development inputs in `keys/nitrogen/ahab`. Those
  credentials and provisioning files are exposed because they are committed to
  the repository and must not protect production data. Production builds must
  set `SIG_DATA_PATH` to a customer-controlled directory containing independently
  generated source keys, credentials, and provisioning content in the same
  layout.

The `prov_data` tree must contain a `keystore` directory and may also contain a
`data` directory. The keystore hierarchy follows the TI provisioning format:

    prov_data/
    ├── keystore/
    │   ├── 01/                  # PKCS#11 object ID
    │   │   ├── cert/
    │   │   │   └── device.der
    │   │   └── privkey/
    │   │       └── device.der
    │   └── 02/
    │       └── ...
    └── data/                    # optional; copied to /data/prov

Each immediate directory below `keystore/` supplies the PKCS#11 object ID.
Every DER certificate below its `cert/` directory is imported as a certificate
and its public key is imported as a public-key object. DER keys below
`privkey/` are imported as private-key objects. The filename stem becomes the
object label.

Configure only the common parent path:

    SIG_DATA_PATH = "/secure/build-inputs/product/nitrogen/ahab"

## Recipe-provided platform input

### NXP production key-agreement public key

Each supported i.MX 9 family requires the NXP production key-agreement public
key corresponding to the private key available to ELE as key ID `0x70000000`.
This is public, NXP-owned key material rather than a customer secret, so the
recipe supplies the appropriate key for the target platform and decodes it only
in the temporary SPSDK workspace.

The keys supplied by the recipe are kept separate by processor family. For each
supported target, the recipe selects the NXP production key-agreement public key
qualified for that family. A key for one processor family must not be assumed
valid for another.

## Customer-provided cryptographic inputs

### `oem-ka-private.pem`

A stable P-256 OEM key-agreement private key. SPSDK uses it with the NXP
production key-agreement public key to derive the persistent ELE import master.
The recipe derives `oem-ka-public.bin` from this key during the build.

This is secret product key material. It must remain stable for every device that
shares this provisioning domain. Changing it derives a different master and
makes newly generated fleet-key TLVs incompatible with master ID 1 already
stored on deployed devices.

Generate a new product key with:

    openssl ecparam -name prime256v1 -genkey -noout \
        -out summit-prov-imx/oem-ka-private.pem

### `fleet-aes-256.bin`

The 32-byte AES-256 fleet key used to encrypt the provisioning archive. The
recipe also wraps this same key in the generated ELE import TLV.

This is secret build input and is never installed in the image. Generate a new
product fleet key with:

    openssl rand 32 > summit-prov-imx/fleet-aes-256.bin

This key may be rotated. Each build generates the encrypted payload and
key-import TLV together, ensuring that both use the supplied fleet key.

### Secured AHAB signing inputs

The recipe reuses `spsdk_ahab.yaml`, its selected OEM signer, and its SRK
certificate table from `SIG_DATA_PATH`. These are the same trust inputs used to
sign the secured AHAB boot image. The selected signer and SRK table must match
the SRK table hash provisioned on the target; otherwise ELE rejects the signed
key-exchange message.

Production private keys must be supplied through the product's approved secure
build and key-management process. The development keys included in this
repository are public examples and provide no production security.

## Build-generated artifacts

The following files are generated as part of `summit-prov-imx:do_install`; they
are not customer inputs:

### `key-exchange.bin`

A signed SPSDK message authorizing ELE to perform ECDH/HKDF key agreement and
create a persistent 256-bit `OEM_IMPORT_MK_SK` at explicit key ID 1. It binds
the key attributes, lifecycle, key-store ID, OEM ECDH public key, and NXP
production key-agreement key ID. The message is signed by the OEM SRK signer
selected by the secured AHAB build.

### `oem-ka-public.bin`

The raw 64-byte P-256 OEM key-agreement public key in `X || Y` format, derived
from `oem-ka-private.pem`. ELE uses it with its NXP production private key to
reproduce the import master derived by SPSDK on the build host.

### `key-import-tlv.bin`

An ELE key-import TLV containing the fleet AES key wrapped with RFC 3394 and
authenticated with CMAC under keys derived from `OEM_IMPORT_MK_SK` ID 1. It
requests a volatile AES-256 key permitted for GCM decryption in the current
lifecycle.

SPSDK temporarily produces the shared secret, import master, wrapping key, and
CMAC key while generating these artifacts. The recipe removes those derived
secrets after generating the TLV; they are not installed or added to the target
package.

## Recipe output

The recipe creates a deterministic `prov_data.tar.gz`, encrypts it with
AES-256-GCM using a fresh 12-byte (96-bit) nonce, and packages:

    nonce || ciphertext || 16-byte authentication tag

The unencrypted archive must not exceed 1 MiB.

The target package contains only:

- `key-exchange.bin`
- `oem-ka-public.bin`
- `key-import-tlv.bin`
- `prov-data.tar.gz.aes-gcm`
- a generated target-side `SHA256SUMS` covering the four files above

It also installs and enables `summit-prov-imx.service` and its orchestration
wrapper. The service is a systemd oneshot ordered after `mount_data.service`,
`nvm_daemon.service`, and `tee-supplicant@teepriv0.service`. Runtime progress
and errors are available in the systemd journal.

The target-side manifest supports detection of accidental artifact corruption;
it is not an authenticity mechanism. ELE authentication of the signed message,
wrapped key, and AES-GCM payload provides the cryptographic authenticity checks.

It does not contain the plaintext provisioning archive, fleet AES key, derived
master, shared secret, OEM key-agreement private key, or OEM signing private
keys.

The decrypted archive exists only in a mode-0700 temporary directory. An exit
trap removes that directory on success, failure, or termination. The persistent
completion marker is written only after PKCS#11 import and optional data copy
complete successfully.
