# 
# @author H. Schulz
# (c) H. Schulz, 2013
# 
# This programme is provided 'As-is', without any guarantee of any kind, implied or otherwise and is wholly unsupported.
# You may use and modify it as long as you state the above copyright.
#

# Test: Zahl der Verbindungen (-> Web-Server)
# Parameter: Serverhost Serverport
import socket
import time
import sys

N = 512
HOST = sys.argv[1]    # The remote host
PORT = int(sys.argv[2]) # The port used by the server


s=range(0,N)
for i in range(0,N):
        s[i] = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

for i in range(0,N):
        s[i].connect((HOST, PORT))
        print "Connected " + repr(i)
	time.sleep(0.1)

time.sleep(120)

#for i in range(0,N):
        #s[i].send("GET")
        #print "Send Data "+ repr(i)


for i in range(0,N):
        s[i].close()
	print "Closed " + repr(i)
