#!/bin/sh
#
# @author H. Schulz
# (c) H. Schulz, 2013
# 
# This programme is provided 'As-is', without any guarantee of any kind, implied or otherwise and is wholly unsupported.
# You may use and modify it as long as you state the above copyright.
#
python_script="multiconnect.py"
if [ $# -ge 2 ]
then
	which python > /dev/null
	if [ $? -eq 0 ] ; then
		xterm -geometry 50x8+0+0 -e "python $python_script $1 $2;sleep 3" &
		xterm -geometry 50x8+0+150 -e "python $python_script $1 $2;sleep 3" &
		xterm -geometry 50x8+0+300 -e "python $python_script $1 $2;sleep 3" &
		xterm -geometry 50x8+0+450 -e "python $python_script $1 $2;sleep 3" &
		xterm -geometry 50x8+0+600 -e "python $python_script $1 $2;sleep 3" &
		xterm -geometry 50x8+0+750 -e "python $python_script $1 $2;sleep 3" &
	else
		echo "Python not found in $PATH."
	fi

else
	echo "Usage: $0 <server-host> <server-port>"
fi
