# GPG Key Setup Guide

This guide covers generating a GPG key for signing Maven Central releases and wiring it into GitHub Actions.

---

## 1. Generate the key

```bash
gpg --full-generate-key
```

When prompted:

| Prompt | Answer |
|---|---|
| Key type | `1` — RSA and RSA |
| Key size | `4096` |
| Expiry | `0` — does not expire |
| Real name | Your name (e.g. `Saumya Macwan`) |
| Email | The email you want on the key |
| Comment | Leave blank (press Enter) |
| Passphrase | Choose a strong passphrase — **remember it, you need it for the GitHub secret** |

---

## 2. Find your key ID

```bash
gpg --list-secret-keys --keyid-format SHORT
```

Example output:

```
sec   rsa4096/D3DA2F98 2026-06-20 [SC]
      5F08E9CB66EFDA9A8CE73A3A43447356D3DA2F98
uid         [ultimate] Saumya <you@email.com>
ssb   rsa4096/4C6D9D0D 2026-06-20 [E]
```

Your key ID is the 8-character hex after `rsa4096/` on the `sec` line — e.g. **`D3DA2F98`**.

---

## 3. Upload the public key to a keyserver

Maven Central verifies signatures against public keyservers.

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys D3DA2F98
```

Also upload to the OpenPGP keyserver (belt-and-suspenders):

```bash
gpg --keyserver keys.openpgp.org --send-keys D3DA2F98
```

---

## 4. Export the private key as base64

This is the value you store in the `SIGNING_SECRET_KEY_BASE64` GitHub secret.

```bash
# macOS — copies straight to clipboard
gpg --export-secret-keys D3DA2F98 | base64 | pbcopy

# Linux — prints to stdout, copy manually
gpg --export-secret-keys D3DA2F98 | base64
```

> If you get `Inappropriate ioctl for device`, your GPG agent is asking for a
> passphrase interactively. Run the export from a normal terminal (not a CI shell
> or headless session):
> ```bash
> export GPG_TTY=$(tty)
> gpg --export-secret-keys D3DA2F98 | base64 | pbcopy
> ```

---

## 5. Add GitHub Secrets

Go to: `github.com/sam829/simple-logger-overlay-android/settings/secrets/actions`

| Secret name | Value |
|---|---|
| `SIGNING_KEY_ID` | Last 8 chars of your key ID — e.g. `D3DA2F98` |
| `SIGNING_PASSWORD` | The passphrase you chose in step 1 |
| `SIGNING_SECRET_KEY_BASE64` | The base64 output from step 4 |
| `MAVEN_CENTRAL_USERNAME` | Sonatype Central Portal user token username |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype Central Portal user token password |

---

## 6. Get Sonatype user token credentials

1. Log in at [central.sonatype.com](https://central.sonatype.com)
2. Click your avatar → **View Account**
3. Under **User Token**, click **Generate User Token**
4. Copy the **username** → `MAVEN_CENTRAL_USERNAME`
5. Copy the **password** → `MAVEN_CENTRAL_PASSWORD`

> These are separate from your login credentials — always use the token pair, never your actual password.

---

## 7. Verify everything works

Merge any change to `main`. The `release.yml` workflow will:

1. Bump the patch version in `gradle.properties`
2. Commit with `[skip ci]`
3. Create and push a git tag (e.g. `v1.0.1`)
4. Publish to Maven Central
5. Create a GitHub Release with auto-generated notes
6. The `docs.yml` workflow will then deploy updated KDoc to GitHub Pages

---

## Key management tips

- **Back up your private key** in a secure location (password manager or encrypted drive):
  ```bash
  gpg --export-secret-keys D3DA2F98 > ~/backup/signing-key-D3DA2F98.gpg
  ```
- **If you lose the key**, generate a new one and update all 3 `SIGNING_*` secrets. Old releases remain verifiable via Maven Central's stored signatures.
- **If you change your passphrase**:
  ```bash
  gpg --edit-key D3DA2F98
  # then type: passwd → save
  ```
  Update `SIGNING_PASSWORD` in GitHub Secrets.

---

## Troubleshooting

| Error | Fix |
|---|---|
| `Inappropriate ioctl for device` | Run `export GPG_TTY=$(tty)` before the export command |
| `gpg: signing failed: No secret key` | Key ID mismatch — re-check `SIGNING_KEY_ID` |
| `401 Unauthorized` from Sonatype | Token expired — regenerate at central.sonatype.com and update secrets |
| `Namespace not found` | Sonatype namespace approval still pending |
