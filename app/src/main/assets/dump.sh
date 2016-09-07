#!/system/bin/sh

exec 2>&1

cd $1
export PATH=$1:$PATH

NAME=$(getprop ro.product.model | busybox tr ' ' '_')_$(getprop ro.build.version.release)_$(busybox date +%F | busybox sed s@-@@g)

mkdir -p /sdcard/Dumper/$NAME
rm -f /sdcard/Dumper/$NAME/$NAME.log

cat /system/build.prop > /sdcard/Dumper/$NAME/$NAME.log

echo "" >> /sdcard/Dumper/$NAME/$NAME.log
echo "" >> /sdcard/Dumper/$NAME/$NAME.log

VENDOR=0
VENDOR_PARTITION=
if busybox df | busybox grep -q "/vendor"; then
  echo "Separate /vendor partition!" >> /sdcard/Dumper/$NAME/$NAME.log
  VENDOR=1
  VENDOR_PARTITION=vendor
fi

TOTAL_SIZE=$(busybox df -B 1 | busybox grep "/system" | busybox tail -n 1 | busybox awk '{print $3}')
if [[ $VENDOR == "1" ]]; then
  TOTAL_SIZE=$(($TOTAL_SIZE + $(busybox df -B 1 | busybox grep "/vendor" | busybox tail -n 1 | busybox awk '{print $3}')))
fi
echo "TOTAL_SIZE : $TOTAL_SIZE" >> /sdcard/Dumper/$NAME/$NAME.log

RAMDISK_ITEMS=$(busybox ls -p / | busybox grep -v '/' | busybox tr '\n' ' ')
echo "RAMDISK_ITEMS : $RAMDISK_ITEMS" >> /sdcard/Dumper/$NAME/$NAME.log

cd /
busybox date >> /sdcard/Dumper/$NAME/$NAME.log
echo "executing : busybox tar -pcvf - system $VENDOR_PARTITION $RAMDISK_ITEMS | pv -i 0.1 -n -s ${TOTAL_SIZE} | pigz > /sdcard/Dumper/$NAME/$NAME.tar.gz" >> /sdcard/Dumper/$NAME/$NAME.log
echo "" >> /sdcard/Dumper/$NAME/$NAME.log
echo "" >> /sdcard/Dumper/$NAME/$NAME.log
( busybox tar -pcvf - system $VENDOR_PARTITION $RAMDISK_ITEMS | pv -i 0.1 -n -s ${TOTAL_SIZE} | pigz > /sdcard/Dumper/$NAME/$NAME.tar.gz ) 2>&1 | busybox tee -a /sdcard/Dumper/$NAME/$NAME.log
echo "done" >> /sdcard/Dumper/$NAME/$NAME.log
busybox date >> /sdcard/Dumper/$NAME/$NAME.log

sync
