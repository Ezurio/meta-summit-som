#!/usr/bin/env python3
"""Generate the matched ELE provisioning artifacts from secured-build inputs."""

import argparse
import base64
import os
from pathlib import Path
import shutil
import subprocess

from cryptography.hazmat.primitives.serialization import load_pem_private_key
import yaml


def absolute_input(path: str, directory: Path) -> str:
    """Return an absolute input path while preserving signer configurations."""

    if path.startswith("type=") or os.path.isabs(path):
        return path
    return str(directory / path)


def main() -> None:
    """Generate the signed ELE key-exchange message and wrapped-key TLV."""

    parser = argparse.ArgumentParser()
    parser.add_argument("--sig-data-path", required=True, type=Path)
    parser.add_argument("--crypto-dir", required=True, type=Path)
    parser.add_argument("--nxp-prod-ka-public", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--work-dir", required=True, type=Path)
    parser.add_argument("--family", required=True)
    parser.add_argument("--issue-date", required=True)
    args = parser.parse_args()

    sig_data_path = args.sig_data_path.resolve()
    crypto_dir = args.crypto_dir.resolve()
    nxp_prod_ka_public = args.nxp_prod_ka_public.resolve()
    output_dir = args.output_dir.resolve()
    work_dir = args.work_dir.resolve()
    assets_dir = work_dir / "assets"
    key_exchange_config = work_dir / "key-exchange.yaml"
    key_import_config = work_dir / "key-import.yaml"
    nxp_public_key = work_dir / "nxp-prod-ka-public.bin"
    oem_private_key = crypto_dir / "oem-ka-private.pem"
    fleet_key = crypto_dir / "fleet-aes-256.bin"

    output_dir.mkdir(parents=True, exist_ok=True)
    assets_dir.mkdir(parents=True, exist_ok=True)

    with (sig_data_path / "spsdk_ahab.yaml").open(encoding="utf-8") as stream:
        ahab = yaml.safe_load(stream)

    signer = str(ahab["signer"])
    if not signer.startswith("type="):
        signer = f"type=file;file_path={absolute_input(signer, sig_data_path / 'keys')}"
        password_file = sig_data_path / "keys" / "key_pass.txt"
        if password_file.is_file():
            signer += f";password={password_file}"

    srk_table = ahab["srk_table"]
    srk_table["srk_array"] = [
        absolute_input(str(certificate), sig_data_path / "crts")
        for certificate in srk_table["srk_array"]
    ]

    private_key = load_pem_private_key(oem_private_key.read_bytes(), password=None)
    public_numbers = private_key.public_key().public_numbers()
    (output_dir / "oem-ka-public.bin").write_bytes(
        public_numbers.x.to_bytes(32, "big")
        + public_numbers.y.to_bytes(32, "big")
    )
    nxp_public_key.write_bytes(
        base64.b64decode(
            nxp_prod_ka_public.read_bytes().strip(),
            validate=True,
        )
    )

    key_exchange = {
        "family": args.family,
        "revision": "latest",
        "output": str(output_dir / "key-exchange.bin"),
        "srk_set": ahab["srk_set"],
        "used_srk_id": ahab["used_srk_id"],
        "srk_revoke_mask": 0,
        "fuse_version": 0,
        "sw_version": 0,
        "signer": signer,
        "srk_table": srk_table,
        "message": {
            "cert_version": 0,
            "cert_permission": 0,
            "issue_date": args.issue_date,
            "command": {
                "KEY_EXCHANGE_REQ": {
                    "key_store_id": 0x454C4500,
                    "key_exchange_algorithm": "ECDH HKDF SHA256 KEY IMPORT",
                    "salt_flags": 0,
                    "derived_key_grp": 0,
                    "derived_key_size_bits": 256,
                    "derived_key_type": "OEM_IMPORT_MK_SK",
                    "derived_key_lifetime": "PERSISTENT",
                    "derived_key_usage": ["Derive"],
                    "derived_key_permitted_algorithm": "HKDF SHA256",
                    "derived_key_lifecycle": "CURRENT",
                    "derived_key_id": 1,
                    "private_key_id": 0x70000000,
                    "oem_private_key": str(oem_private_key),
                    "nxp_prod_ka_pub": str(nxp_public_key),
                }
            },
        },
    }
    key_exchange_config.write_text(yaml.safe_dump(key_exchange, sort_keys=False), encoding="utf-8")

    try:
        environment = os.environ.copy()
        environment["CRYPTOGRAPHY_OPENSSL_NO_LEGACY"] = "1"
        subprocess.run(
            [
                "nxpimage",
                "signed-msg",
                "export",
                "-c",
                str(key_exchange_config),
                "-w",
                str(assets_dir),
            ],
            check=True,
            env=environment,
        )

        key_import = {
            "output": str(output_dir / "key-import-tlv.bin"),
            "family": args.family,
            "revision": "latest",
            "command": {
                "KEY_IMPORT": {
                    "key_id": 0,
                    "permitted_algorithm": "GCM",
                    "key_usage": ["Decrypt"],
                    "key_type": "AES",
                    "key_size_bits": 256,
                    "key_lifetime": "ELE_KEY_IMPORT_VOLATILE",
                    "key_lifecycle": "CURRENT",
                    "oem_mk_sk_key_id": 1,
                    "key_wrapping_algorithm": "RFC3394",
                    "signing_algorithm": "CMAC",
                    "import_key": str(fleet_key),
                    "oem_import_mk_sk_key": str(assets_dir / "oem_import_mk_sk.bin"),
                }
            },
        }
        key_import_config.write_text(yaml.safe_dump(key_import, sort_keys=False), encoding="utf-8")
        subprocess.run(
            ["nxpimage", "signed-msg", "tlv", "export", "-c", str(key_import_config)],
            check=True,
            env=environment,
        )
    finally:
        shutil.rmtree(assets_dir, ignore_errors=True)
        for sensitive_file in (key_exchange_config, key_import_config, nxp_public_key):
            sensitive_file.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
