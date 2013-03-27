AUX=/home/cluster/mc715u31/zoo/zookeeper-3.4.5.jar:/home/cluster/mc715u31/zoo/lib/jline-0.9.94.jar:/home/cluster/mc715u31/zoo/lib/log4j-1.2.15.jar:/home/cluster/mc715u31/zoo/lib/netty-3.2.2.Final.jar::/home/cluster/mc715u31/zoo/lib/slf4j-api-1.6.1.jar:/home/cluster/mc715u31/zoo/lib/slf4j-log4j12-1.6.1.jar:/home/cluster/mc715u31/zoo/conf:.

# AUX=~/zoo:~/zoo/lib:~/zoo/conf:.

all: Executor.java DataMonitor.java
	javac -cp $(AUX) -Xlint *.java

run:
	java -cp $(AUX) Executor node01:13569 /mc715 mc715 echo "hehehe" >> ~/test


