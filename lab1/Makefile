AUX=/home/cluster/mc715u31/zoo/zookeeper-3.4.5.jar:/home/cluster/mc715u31/zoo/lib/jline-0.9.94.jar:/home/cluster/mc715u31/zoo/lib/log4j-1.2.15.jar:/home/cluster/mc715u31/zoo/lib/netty-3.2.2.Final.jar::/home/cluster/mc715u31/zoo/lib/slf4j-api-1.6.1.jar:/home/cluster/mc715u31/zoo/lib/slf4j-log4j12-1.6.1.jar:/home/cluster/mc715u31/zoo/conf:.
CNT?=2
all:	SyncPrimitive.java
	javac -cp $(AUX) -Xlint *.java

run: 
	java -cp $(AUX) SyncPrimitive bTest node01:13569 $(CNT) 

rs:
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /b1

proof:
	ssh node03 "cd ~/mc715/lab1; make run CNT=20"  &
	make run CNT=0   &

ok:
	ssh node03 "cd ~/mc715/lab1; make run CNT=0"  &
	make run CNT=0   &

clean:
	rm -rf *.class zookeeper.out

