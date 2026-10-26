# Clear port 5173 script
$port = 5173

# Find processes using port 5173
$processes = netstat -ano | Select-String ":$port"

if ($processes) {
    Write-Host "Found processes using port $port:"
    $processes | ForEach-Object {
        $processInfo = $_ -split '\s+'
        $pid = $processInfo[-1]
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Process ID: $pid, Process Name: $($process.ProcessName)"
            # Terminate process
            try {
                Stop-Process -Id $pid -Force
                Write-Host "Terminated process $pid ($($process.ProcessName))"
            } catch {
                Write-Host "Failed to terminate process $pid: $($_.Exception.Message)"
            }
        }
    }
} else {
    Write-Host "Port $port is not in use"
}
