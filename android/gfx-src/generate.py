#!/usr/bin/env python

import glob
import os.path
import subprocess

patterns = """
#*.svg
ic_bt_reset_48px.svg
ic_bt_reset_disabled_48px.svg
"""

scales = {
    'mdpi': 90, # 100%
    'hdpi': 135,
    'xhdpi': 180,
    'xxhdpi': 270,
#    'xxxhdpi': 360,
}

names = []
for each in [ g for g in patterns.strip().split() if not g.startswith("#") ]:
    names += glob.glob(each)
print names

for name in names:
    basename = os.path.splitext(os.path.split(name)[-1])[0]
    for density, dpi in scales.items():
        dest = "../app/src/main/res/mipmap-%s/%s.png" % (density, basename)
        command = "inkscape %s --without-gui --export-dpi=%d --export-png=%s" % (
            name, dpi, dest)
        print ">>>", command
        subprocess.call(command, shell=True)
        print
