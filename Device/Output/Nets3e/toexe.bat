@echo off
pyinstaller -F main.py
mshta vbscript:msgbox("OK!",64,"Martin")(window.close)
pasue