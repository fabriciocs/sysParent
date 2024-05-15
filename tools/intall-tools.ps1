# Check if the script is running as Administrator
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator))
{
    # Relaunch the script with administrator rights
    $arguments = "& '" + $MyInvocation.MyCommand.Definition + "'"
    Start-Process powershell -Verb runAs -ArgumentList $arguments
    Exit
}


# Function to check and install Chocolatey
function Install-Chocolatey {
    if (!(Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-Host "Installing Chocolatey..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
    } else {
        Write-Host "Chocolatey is already installed."
    }
}

# Function to install ImageMagick
function Install-ImageMagick {
    if (!(Get-Command magick -ErrorAction SilentlyContinue)) {
        Write-Host "Installing ImageMagick..."
        choco install imagemagick -y
    } else {
        Write-Host "ImageMagick is already installed."
    }
}

# Function to install Potrace
function Install-Potrace {
    if (!(Get-Command potrace -ErrorAction SilentlyContinue)) {
        Write-Host "Installing Potrace..."
        choco install potrace -y
    } else {
        Write-Host "Potrace is already installed."
    }
}

# Main script execution
Install-Chocolatey
Install-ImageMagick
Install-Potrace
npm install -g @angular/cli