AUX=/home/cluster/mc715u31/zoo/zookeeper-3.4.5.jar:/home/cluster/mc715u31/zoo/lib/jline-0.9.94.jar:/home/cluster/mc715u31/zoo/lib/log4j-1.2.15.jar:/home/cluster/mc715u31/zoo/lib/netty-3.2.2.Final.jar::/home/cluster/mc715u31/zoo/lib/slf4j-api-1.6.1.jar:/home/cluster/mc715u31/zoo/lib/slf4j-log4j12-1.6.1.jar:/home/cluster/mc715u31/zoo/conf:.
CNT?=2
ID?=node01

all:
	javac -cp $(AUX) -Xlint *.java

run: 
	java -cp $(AUX) SyncPrimitive bTest $(ID):13569 $(CNT)

runfix: 
	java -cp $(AUX) SyncPrimitiveFixed bTest $(ID):13569 $(CNT)

rs:
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /b1
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /ready

proof:
	ssh node03 "cd ~/mc715/lab1; make run ID=node03 CNT=10"  &
	make run CNT=0   &

prooffix:
	ssh node03 "cd ~/mc715/lab1; make runfix ID=node03 CNT=10"  &
	make runfix CNT=0   &

ok:
	ssh node03 "cd ~/mc715/lab1; make run ID=node03 CNT=0"  &
	make run CNT=0   &

clean:
	rm -rf *.class zookeeper.out

