#!/usr/bin/env python

import glob
import os.path
import subprocess

patterns = """
*.svg
"""

scales = {
    'mdpi': 90, # 100%
    'hdpi': 135,
    'xhdpi': 180,
    'xxhdpi': 270,
#    'xxxhdpi': 360,
}

names = []
for each in patterns.strip().split():
    names += glob.glob(each)

for name in names:
    basename = os.path.splitext(os.path.split(name)[-1])[0]
    for density, dpi in scales.items():
        dest = "../app/src/main/res/mipmap-%s/%s.png" % (density, basename)
        command = "inkscape %s --without-gui --export-dpi=%d --export-png=%s" % (
            name, dpi, dest)
        print ">>>", command
        subprocess.call(command, shell=True)
        print
