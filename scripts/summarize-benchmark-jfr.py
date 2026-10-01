#!/usr/bin/env python3
"""Stream native JFR through the JDK consumer API with a bounded analysis heap."""
import os
import subprocess
import sys
from pathlib import Path
java=Path(os.environ.get('JAVA_HOME','/usr/lib/jvm/java-21-openjdk'))/'bin/java'
subprocess.run([str(java),'-Xmx256m',str(Path(__file__).with_name('BenchmarkJfrSummary.java')),sys.argv[1]],check=True)
