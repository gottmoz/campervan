param(
    [Parameter(Mandatory=$true)][string]$HostName,
    [string]$User = "root",
    [string]$SshTarget = "",
    [switch]$InstallNginx
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($SshTarget)) {
    $SshTarget = "$User@$HostName"
}

$adminToken = $env:CAMPER_REMOTE_QUEUE_ADMIN_TOKEN
$commaToken = $env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3
$hikityToken = $env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_HIKITY_ANDROID
if ([string]::IsNullOrWhiteSpace($adminToken) -or [string]::IsNullOrWhiteSpace($commaToken) -or [string]::IsNullOrWhiteSpace($hikityToken)) {
    throw "Set CAMPER_REMOTE_QUEUE_ADMIN_TOKEN, CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3, and CAMPER_REMOTE_QUEUE_NODE_TOKEN_HIKITY_ANDROID."
}

$remoteTmp = "/tmp/camper-remote-queue"
ssh $SshTarget "rm -rf $remoteTmp && mkdir -p $remoteTmp"
scp "$root\tools\remote-queue\queue_server.py" "${SshTarget}:$remoteTmp/queue_server.py"
scp "$root\deploy\remote-queue\camper-remote-queue.service" "${SshTarget}:$remoteTmp/camper-remote-queue.service"
scp "$root\deploy\remote-queue\nginx-camper-remote-queue.conf" "${SshTarget}:$remoteTmp/nginx-camper-remote-queue.conf"

$installNginxFlag = if ($InstallNginx) { "1" } else { "0" }
$remoteScript = @"
set -euo pipefail
if ! id camper >/dev/null 2>&1; then
  useradd --system --home /opt/camper-agent --shell /usr/sbin/nologin camper
fi
mkdir -p /opt/camper-agent/remote-queue /var/lib/camper-agent/remote-queue /etc/camper-agent
cp $remoteTmp/queue_server.py /opt/camper-agent/remote-queue/queue_server.py
cp $remoteTmp/camper-remote-queue.service /etc/systemd/system/camper-remote-queue.service
cat > /etc/camper-agent/remote-queue.env <<'ENVEOF'
CAMPER_REMOTE_QUEUE_DATA=/var/lib/camper-agent/remote-queue
CAMPER_REMOTE_QUEUE_ADMIN_TOKEN=$adminToken
CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3=$commaToken
CAMPER_REMOTE_QUEUE_NODE_TOKEN_HIKITY_ANDROID=$hikityToken
ENVEOF
chmod 600 /etc/camper-agent/remote-queue.env
chown -R camper:camper /opt/camper-agent /var/lib/camper-agent
systemctl daemon-reload
systemctl enable --now camper-remote-queue
if [ "$installNginxFlag" = "1" ]; then
  apt-get update
  DEBIAN_FRONTEND=noninteractive apt-get install -y nginx
  cp $remoteTmp/nginx-camper-remote-queue.conf /etc/nginx/sites-available/camper-remote-queue
  ln -sfn /etc/nginx/sites-available/camper-remote-queue /etc/nginx/sites-enabled/camper-remote-queue
  nginx -t
  systemctl reload nginx
fi
systemctl --no-pager --full status camper-remote-queue
"@

$remoteScript | ssh $SshTarget "bash -s"
