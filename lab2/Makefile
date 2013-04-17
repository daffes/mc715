AUX=/home/cluster/mc715u31/zoo/zookeeper-3.4.5.jar:/home/cluster/mc715u31/zoo/lib/jline-0.9.94.jar:/home/cluster/mc715u31/zoo/lib/log4j-1.2.15.jar:/home/cluster/mc715u31/zoo/lib/netty-3.2.2.Final.jar::/home/cluster/mc715u31/zoo/lib/slf4j-api-1.6.1.jar:/home/cluster/mc715u31/zoo/lib/slf4j-log4j12-1.6.1.jar:/home/cluster/mc715u31/zoo/conf:/home/cluster/mc715u31/mc715/lab2/src/java/org/apache/zookeeper/recipes/barrier/.:.
ID?=node01

compile:
	javac -cp $(AUX) -Xlint src/java/org/apache/zookeeper/recipes/barrier/*.java

run: 
	java -cp $(AUX) DoubleBarrierTest $(ID):13569

rs:
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /b-prova
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /b-notas
	~/zoo/bin/zkCli.sh -server node01:13569 rmr /notas

clean:
	rm -rf *.class zookeeper.out

