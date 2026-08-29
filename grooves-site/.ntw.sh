#!/bin/bash
# inherit trap on ERR in shell functions, command substitutions, and commands executed in a subshell environment
set -E
# exit immediately if a command exits with a non-zero status
set -e
# exit immediately if a command tries to use an undefined variable
set -u
# the return value of a pipeline is the value of the last (rightmost) command to exit with a non-zero status, or zero if all commands in the pipeline exit successfully.
set -o pipefail

NTW_LOG_LEVEL=${NTW_LOG_LEVEL:-1}
NTW_LOG_FILE=${NTW_LOG_FILE:-"/tmp/ntw.log"}
PID=$$

COLOR_BLACK=0
COLOR_BLUE=4
COLOR_ORANGE=3
COLOR_RED=1

if [ "${USE_TPUT:-}" = "" ]; then
  set +e
  if tput sgr0 >/dev/null 2>/tmp/tput.txt; then
    if [ "$(wc -c /tmp/tput.txt | sed -E 's/^ +//g' | tr -s " " | cut -d " " -f 1)" -gt "3" ]; then
      USE_TPUT=0
    else
      USE_TPUT=1
    fi
  else
    USE_TPUT=0
  fi
  set -e
fi
log() {
  TS="$(date +'%Y-%m-%dT%H:%M:%S%z')"
  if [ "${NTW_LOG_LEVEL}" -ge "$1" ]; then
    if [ "${USE_TPUT}" = "1" ]; then
      COLOR_SET=$(tput setaf "$2")
      COLOR_RESET=$(tput sgr0)
    else
      COLOR_SET=""
      COLOR_RESET=""
    fi
    printf "%s ${COLOR_SET}%5s${COLOR_RESET} - %s\n" "$TS" "$3" "$4" >&2
  fi
  printf "{\"pid\":%d,\"ts\":\"%s\",\"level\":\"%s\",\"message\":\"%s\"}\n" "$PID" "$TS" "$3" "$4" >>"$NTW_LOG_FILE"
}
debug() {
  log 3 "${COLOR_BLACK}" "DEBUG" "$1"
}
info() {
  log 2 "${COLOR_BLUE}" "INFO" "$1"
}
warn() {
  log 1 "${COLOR_ORANGE}" "WARN" "$1"
}
error() {
  log 0 "${COLOR_RED}" "ERROR" "$1"
}
info "NTW_LOG_LEVEL: $NTW_LOG_LEVEL"

NTW_HOME=${NTW_HOME:-"${HOME:-/tmp}/.ntw"}
info "NTW_HOME: $NTW_HOME"
if [ ! -d "$NTW_HOME" ]; then
  mkdir -p "$NTW_HOME"
fi

NTW_NODE_DIST_URL=${NTW_NODE_DIST_URL:-"https://nodejs.org/dist"}
info "NTW_NODE_DIST_URL: $NTW_NODE_DIST_URL"

# Usage:
#   registryFromNpmrc
# Looks for a `registry = ...` line in the local .npmrc, falling back to
# ~/.npmrc if the local one doesn't set one. Echoes the found value, or
# nothing if neither file sets a registry.
registryFromNpmrc() {
  local npmrcFile
  for npmrcFile in .npmrc "${HOME:-}/.npmrc"; do
    if [ -n "$npmrcFile" ] && [ -f "$npmrcFile" ]; then
      local npmrcUrl
      npmrcUrl=$(cat "$npmrcFile" | grep -E "^registry *= *" | sed -e "s/ //g" | cut -d '=' -f 2)
      if [ -n "$npmrcUrl" ]; then
        debug "Found registry in $npmrcFile: $npmrcUrl"
        echo "$npmrcUrl"
        return 0
      fi
    fi
  done
}

if [ -z "${NTW_NPM_URL:-}" ]; then
  npmrcUrl=$(registryFromNpmrc)
  if [ -n "$npmrcUrl" ]; then
    NTW_NPM_URL=$npmrcUrl
  fi
fi
if [ -z "${NTW_NPM_URL:-}" ]; then
  NTW_NPM_URL="https://registry.npmjs.org/"
fi
info "NTW_NPM_URL: $NTW_NPM_URL"

# Usage:
#   with_timeout <seconds> <command...>
# Kills <command...> if it hasn't finished after <seconds>. Needed because a
# blackholed connection (common behind corporate firewalls) can hang a plain
# `git clone`/`git pull` well past curl/npm's own timeouts.
with_timeout() {
  local seconds=$1
  shift
  "$@" &
  local cmd_pid=$!
  (
    sleep "$seconds"
    kill -TERM "$cmd_pid" 2>/dev/null
  ) &
  local watchdog_pid=$!
  local exit_code=0
  wait "$cmd_pid" 2>/dev/null || exit_code=$?
  kill "$watchdog_pid" 2>/dev/null
  wait "$watchdog_pid" 2>/dev/null
  return "$exit_code"
}

# Usage:
#   selectNode <Version>
# Examples:
#   selectNode v16.13.1
selectNode() {
  debug "selectNode $1"
  local version=$1

  if which node >/dev/null 2>&1; then
    local currentVersion
    currentVersion=$(node --version)
    if [ "${currentVersion}" = "${version}" ]; then
      info "System node is already at version ${version}. Using it instead of provisioning."
      return 0
    fi
    debug "System node is at version ${currentVersion}, not ${version}. Provisioning."
  fi

  debug "PWD: $(pwd)"
  local pwdmd5
  pwdmd5="$(pwd | md5sum | cut -d ' ' -f 1)"
  debug "PWDMD5: $pwdmd5"
  local tars="${NTW_HOME}/tars"
  local home_base="${NTW_HOME}/node/${pwdmd5}"

  local baseUrl=${NTW_NODE_DIST_URL}
  local os=${2:-$(uname -s | tr '[:upper:]' '[:lower:]')}
  local arch=${3:-$(uname -m | sed -e 's/^aarch64$/arm64/g' | sed -e 's/^x86_64/x64/g')}

  local filename="node-$version-$os-$arch.tar.gz"
  local node_url="${baseUrl}/${version}/node-${version}-${os}-${arch}.tar.gz"
  local sha_url="${baseUrl}/${version}/SHASUMS256.txt"
  local cache_location="${tars}/node-${version}-${os}-${arch}.tar.gz"
  local node_home="${home_base}/node-${version}-${os}-${arch}"

  mkdir -p "$tars"
  mkdir -p "$home_base"

  debug "sha_url: $sha_url"
  local expected_sha
  expected_sha=$(curl -s --connect-timeout 5 --max-time 30 "$sha_url" 2>/dev/null | grep "$filename" | cut -d " " -f 1)
  debug "expected_sha: $expected_sha"
  local actual_sha

  if [ -f "$cache_location" ]; then
    actual_sha=$(sha256sum "$cache_location" | cut -d " " -f 1)
    debug "actual_sha: $actual_sha"
    if [ "$actual_sha" != "$expected_sha" ]; then
      warn "Cache invalid. Downloading $node_url to $cache_location"
      curl -s --connect-timeout 5 --max-time 120 "$node_url" -o "$cache_location"
    else
      info "Using cached $filename from $cache_location"
    fi
  else
    info "Tar doesn't exist locally. Downloading $node_url to $cache_location"
    curl -s --connect-timeout 5 --max-time 120 "$node_url" -o "$cache_location"
  fi

  if [ ! -d "$node_home" ]; then
    info "Extracting tar into $node_home"
    tar xzf "$cache_location" --directory "$home_base"
  fi

  info "Setting NODE_HOME='$node_home'"
  export NODE_HOME="$node_home"
  export PATH="$NODE_HOME/bin:$PATH"
}

# Usage:
#   selectTool <toolName> <version>
selectTool() {
  debug "selectTool $1 $2"
  local toolName=$1
  local npmUrl=${NTW_NPM_URL}
  local npmrcUrl
  npmrcUrl=$(registryFromNpmrc)
  if [ -n "$npmrcUrl" ]; then
    npmUrl=$npmrcUrl
  fi
  debug "npmUrl: $npmUrl"
  local version=$2

  if which "${toolName}" >/dev/null 2>&1; then
    debug "Tool ${toolName} already installed. Checking version"
    currentVersion=$("${toolName}" --version)
    if [ "${currentVersion}" = "$version" ]; then
      info "Tool ${toolName} is already at version ${version}"
    else
      warn "Tool ${toolName} is at version ${currentVersion}. Installing ${version}"
      npm install "${toolName}@${version}" --registry="${npmUrl}" --global \
        --fetch-timeout=15000 --fetch-retries=1 --fetch-retry-mintimeout=2000 --fetch-retry-maxtimeout=5000
    fi
  else
    info "Installing ${toolName} from ${npmUrl}@${version}"
    npm install "${toolName}@${version}" --registry="${npmUrl}" --global \
      --fetch-timeout=15000 --fetch-retries=1 --fetch-retry-mintimeout=2000 --fetch-retry-maxtimeout=5000
  fi
}

update() {
  info "Updating ${BASH_SOURCE[0]} ..."
  exec cp "${NTW_HOME}/repo/.ntw.sh" "${BASH_SOURCE[0]}"
}

# Usage:
#   syncRepoCache
# Pulls (or clones) the cached copy of the node-tool-wrapper repo at
# ${NTW_HOME}/repo. Returns non-zero if the git operation failed.
syncRepoCache() {
  date +%s >"${NTW_HOME}/last-update-check"
  set +e
  if [ -d "${NTW_HOME}/repo" ]; then
    (
      cd "${NTW_HOME}/repo"
      GIT_TERMINAL_PROMPT=0 with_timeout 30 git pull
    )
    git_result=$?
  else
    GIT_TERMINAL_PROMPT=0 with_timeout 30 git clone https://github.com/rahulsom/node-tool-wrapper.git "${NTW_HOME}/repo"
    git_result=$?
  fi
  set -e

  if [ $git_result -ne 0 ]; then
    warn "Failed to update node-tool-wrapper repository. GitHub may be unavailable. Continuing with cached version."
    return 1
  fi
}

checkForUpdate() {
  debug "Checking for update..."
  local force=${1:-0}
  if [ "$force" -eq 1 ]; then
    debug "Force flag set. Setting do_update_cache to 1"
    do_update_cache=1
  elif [ ! -f "${NTW_HOME}/last-update-check" ]; then
    debug "No last-update-check file found. Setting do_update_cache to 1"
    echo 0 >"${NTW_HOME}/last-update-check"
    do_update_cache=1
  else
    debug "last-update-check file found. Reading"
    last_update_check=$(cat "${NTW_HOME}/last-update-check")
    LAST_UPDATED="$(date -r "$last_update_check" 2>/dev/null || date -d "@$last_update_check")"
    if [ $(($(date +%s) - last_update_check)) -gt 604800 ]; then
      debug "last-update-check ($LAST_UPDATED) is older than 7 days. Setting do_update_cache to 1"
      do_update_cache=1
    else
      debug "last-update-check ($LAST_UPDATED) is younger than 7 days. Setting do_update_cache to 0"
      do_update_cache=0
    fi
  fi

  if [ $do_update_cache -eq 1 ]; then
    syncRepoCache || return 0
  fi

  if [ -f "${NTW_HOME}/repo/.ntw.sh" ]; then
    cmp -s "${NTW_HOME}/repo/.ntw.sh" "${BASH_SOURCE[0]}" || warn "Update available for node-tool-wrapper. Run '${BASH_SOURCE[0]} update' to update"
  fi
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  debug "script ${BASH_SOURCE[0]} is top level ..."
  if [ "${1:-}" = "--force" ]; then
    checkForUpdate 1
  fi
  if [ "$1" = "update" ]; then
    update
  fi
else
  debug "script ${BASH_SOURCE[0]} is being sourced ..."
  if [ -n "${CI:-}" ]; then
    info "Running on CI. Skipping ntw update check."
  elif [ "${NTW_OFFLINE:-0}" = "1" ]; then
    info "NTW_OFFLINE=1. Skipping ntw update check."
  else
    checkForUpdate
  fi
fi
