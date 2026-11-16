#!/bin/sh
cd /mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/release-install01/client
exec /mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/tools/virtualgl/opt/VirtualGL/bin/vglrun -ld /mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/tools/virtualgl/usr/lib -c proxy -d /dev/dri/card1 /usr/lib/jvm/java-21-openjdk/bin/java -Dfourthdimension.validation=true @/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/release-install01/client/clean-client.args
