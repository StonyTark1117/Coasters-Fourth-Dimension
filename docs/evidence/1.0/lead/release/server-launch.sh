#!/bin/sh
cd /mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/release-install01/server
exec /usr/lib/jvm/java-21-openjdk/bin/java @user_jvm_args.txt @libraries/net/neoforged/neoforge/21.1.256/unix_args.txt nogui
