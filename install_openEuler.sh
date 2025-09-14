#!/bin/bash
# Quick deployment script for openEuler Linux
set -e

# Determine package manager (dnf or yum)
PKG_MGR=$(command -v dnf || command -v yum)
if [ -z "$PKG_MGR" ]; then
  echo "Error: neither dnf nor yum found. Please install dependencies manually." >&2
  exit 1
fi

# Install system dependencies
sudo $PKG_MGR install -y python3 python3-pip luajit || \
  { echo "Failed to install system packages" >&2; exit 1; }

# Install Python dependencies
pip3 install -r requirements.txt

echo "Installation complete. Run 'python3 app.py' to start the server."
