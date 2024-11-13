#!/bin/env pwsh
#requires -Version 7.0
param (
    [Parameter(Mandatory=$true)][string]$database
)

Get-Content -Raw "$PSScriptRoot/query.sql" | sqlite3 "$database" | Out-File "$PSScriptRoot/data.csv"