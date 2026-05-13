@echo off
title AutoEcole SaaS – Stop All
PowerShell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-project.ps1"
