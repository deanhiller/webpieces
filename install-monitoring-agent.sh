#!/bin/bash
# Copyright 2014-2017 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Install and start the Google Stackdriver monitoring agent.
#
# This script does the following:
#
#   1. Configures the required apt or yum repository.
#      The environment variable REPO_SUFFIX can be set to alter which
#      repository is used. A dash (-) will be inserted prior to the supplied
#      suffix. Example values are 'unstable' or '20151027-1'.
#   2. Installs the monitoring agent.
#   3. Starts the monitoring agent.
#

# TODO(lingshi): Add "set -e".

# Name of the monitoring agent.
AGENT_NAME='stackdriver-agent'

# Host that serves the repositories.
REPO_HOST='packages.cloud.google.com'

# URL for the monitoring agent documentation.
MONITORING_AGENT_DOCS_URL="https://cloud.google.com/monitoring/agent"

# URL documentation which lists supported platforms for running the monitoring agent.
MONITORING_AGENT_SUPPORTED_URL="${MONITORING_AGENT_DOCS_URL}/#supported_operating_systems"

# Recent systems provide /etc/os-release. The ID variable defined therein
# is particularly useful for identifying Amazon Linux.
if [[ -f /etc/os-release ]]; then
  . /etc/os-release
fi

preinstall() {
  cat <<EOM
==============================================================================
Starting installation of ${AGENT_NAME}
==============================================================================

EOM
}

postinstall() {
  exit_code=0
  service "${AGENT_NAME}" restart
  if [[ $? -eq 0 ]]; then
    cat <<EOM

==============================================================================
Installation of ${AGENT_NAME}-${VERSION} completed successfully.
EOM

    # Prints an additional banner if we appear to have a credentials issue.
    check_credentials
  else
    cat <<EOM

==============================================================================
Errors occurred while installing ${AGENT_NAME}-${VERSION}. See the log snippet
above or run:
  sudo service ${AGENT_NAME} status
EOM
    exit_code=1
  fi

  cat <<EOM

Please consult the documentation for troubleshooting advice:
  ${MONITORING_AGENT_DOCS_URL}

You can monitor the monitoring agent's logfile at:
  ${COLLECTD_LOG}
==============================================================================
EOM

  return "$exit_code"
}

install_for_debian() {
  lsb_release -v >/dev/null 2>&1 || { \
    apt-get update && apt-get -y install lsb-release; \
  }
  local CODENAME="$(lsb_release -sc)"
  local REPO_NAME="google-cloud-monitoring-${CODENAME}${REPO_SUFFIX+-${REPO_SUFFIX}}"
  cat > /etc/apt/sources.list.d/google-cloud-monitoring.list <<EOM
deb http://${REPO_HOST}/apt ${REPO_NAME} main
EOM
  curl --connect-timeout 5 -s -f "https://${REPO_HOST}/apt/doc/apt-key.gpg" | apt-key add -
  apt-get update || { \
    echo "Could not update apt repositories."; \
    echo "Please check your network connectivity and"; \
    echo "make sure you are running a supported Ubuntu/Debian distribution."; \
    echo "See ${MONITORING_AGENT_SUPPORTED_URL} for a list of supported platforms."; \
    exit 1; \
  }

  DEBIAN_FRONTEND=noninteractive apt-get -y install "${AGENT_NAME}"
  VERSION="$(dpkg -l "${AGENT_NAME}" | tail -n 1 |\
    sed -E 's/.*([0-9]+\.[0-9]+\.[0-9]+-[0-9]+).*/\1/')"
  COLLECTD_LOG="/var/log/syslog"
}

# Takes the repo codename as a parameter.
install_rpm() {
  lsb_release -v >/dev/null 2>&1 || yum -y install redhat-lsb-core
  local REPO_NAME="google-cloud-monitoring-${1}-\$basearch${REPO_SUFFIX+-${REPO_SUFFIX}}"
  cat > /etc/yum.repos.d/google-cloud-monitoring.repo <<EOM
[google-cloud-monitoring]
name=Google Cloud Monitoring Agent Repository
baseurl=https://${REPO_HOST}/yum/repos/${REPO_NAME}
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://${REPO_HOST}/yum/doc/yum-key.gpg
       https://${REPO_HOST}/yum/doc/rpm-package-key.gpg
EOM
  yum -y install "${AGENT_NAME}"
  VERSION="$(yum list "${AGENT_NAME}" | tail -n 1 |\
    sed -E 's/.*([0-9]+\.[0-9]+\.[0-9]+-[0-9]+).*/\1/')"
  COLLECTD_LOG="/var/log/messages"
}

install_for_redhat() {
  local VERSION_PRINTER='import platform; print(platform.dist()[1].split(".")[0])'
  local MAJOR_VERSION="$(python2 -c "${VERSION_PRINTER}")"
  install_rpm "el${MAJOR_VERSION}"
}

install_for_amazon_linux() {
  install_rpm "amzn"
}

install_for_suse() {
  SUSE_VERSION=${VERSION%%-*}
  local REPO_NAME="google-cloud-monitoring-sles${SUSE_VERSION}-\$basearch${REPO_SUFFIX+-${REPO_SUFFIX}}"
  # TODO: expand all short arguments in this script, for readability.
  zypper -n refresh || { \
    echo "Could not refresh zypper repositories."; \
    echo "This is not necessarily a fatal error; proceeding..."; \
  }
  zypper addrepo -g -t YUM "https://${REPO_HOST}/yum/repos/${REPO_NAME}" google-cloud-monitoring
  rpm --import "https://${REPO_HOST}/yum/doc/yum-key.gpg" "https://${REPO_HOST}/yum/doc/rpm-package-key.gpg"
  zypper -n refresh google-cloud-monitoring || { \
    echo "Could not refresh the Stackdriver agent zypper repositories."; \
    echo "Please check your network connectivity and"; \
    echo "make sure you are running a supported SUSE distribution."; \
    echo "See ${MONITORING_AGENT_SUPPORTED_URL} for a list of supported platforms."; \
    exit 1; \
  }
  zypper -n install -y "${AGENT_NAME}"
  systemctl daemon-reload

  VERSION="$(rpm -qa "${AGENT_NAME}" | tail -n 1 |\
    sed -E 's/.*([0-9]+\.[0-9]+\.[0-9]+-[0-9]+).*/\1/')"
  COLLECTD_LOG="/var/log/messages"
}

check_credentials() {
  # Check for GOOGLE_APPLICATION_CREDENTIALS, which might be set in
  # /etc/sysconfig or /etc/default.
  [[ -f "/etc/default/${AGENT_NAME}" ]] && source "/etc/default/${AGENT_NAME}"
  [[ -f "/etc/sysconfig/${AGENT_NAME}" ]] && source "/etc/sysconfig/${AGENT_NAME}"
  if [[ -n "$GOOGLE_APPLICATION_CREDENTIALS" && \
        -f "$GOOGLE_APPLICATION_CREDENTIALS" ]]; then
    return
  fi

  # Look at the user and system default paths.
  for path in /root/.config/gcloud/application_default_credentials.json \
      /etc/google/auth/application_default_credentials.json; do
    if [[ -f "$path" ]]; then
      return
    fi
  done

  if curl --connect-timeout 2 -s -i http://169.254.169.254 | \
      grep -i '^Metadata-Flavor: Google' > /dev/null 2>&1; then
    # Running on GCP; we can get credentials from the built-in service account.
    return
  fi

  cat <<EOM
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
WARNING: Google Compute Platform credentials are required for this platform
but were not found, thus the agent may have failed to start or initialize
properly.  Please consult the "Authorization" section of the documentation at
${MONITORING_AGENT_DOCS_URL} then restart the agent using:

  sudo service ${AGENT_NAME} restart
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
EOM
}

install() {
  case "${ID:-}" in
    amzn)
      echo 'Installing agent for Amazon Linux.'
      install_for_amazon_linux
      ;;
    debian|ubuntu)
      echo 'Installing agent for Debian or Ubuntu.'
      install_for_debian
      ;;
    rhel|centos)
      echo 'Installing agent for RHEL or CentOS.'
      install_for_redhat
      ;;
    sles)
      echo 'Installing agent for SLES.'
      install_for_suse
      ;;
    *)
      # Fallback for systems lacking /etc/os-release.
      if [[ -f /etc/debian_version ]]; then
        echo 'Installing agent for Debian.'
        install_for_debian
      elif [[ -f /etc/redhat-release ]]; then
        echo 'Installing agent for Red Hat.'
        install_for_redhat
      elif [[ -f /etc/SuSE-release ]]; then
        echo 'Installing agent for SLES.'
        install_for_suse
      else
        echo >&2 'Unidentifiable or unsupported platform.'
        echo >&2 "See ${MONITORING_AGENT_SUPPORTED_URL} for a list of supported platforms."
        exit 1
      fi
  esac
}

main() {
  preinstall

  if ! install; then
    echo >&2 'Installation failed.'
    exit 1
  fi

  postinstall
  return $?
}

main "$@"
