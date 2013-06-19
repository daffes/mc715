SP=src/java
CP=/home/cluster/mc715u31/zoo/zookeeper-3.4.5.jar:/home/cluster/mc715u31/zoo/lib/jline-0.9.94.jar:/home/cluster/mc715u31/zoo/lib/log4j-1.2.15.jar:home/cluster/mc715u31/zoo/lib/netty-3.2.2.Final.jar::/home/cluster/mc715u31/zoo/lib/slf4j-api-1.6.1.jar:/home/cluster/mc715u31/zoo/lib/slf4j-log4j12-1.6.1.jar:/home/cluster/mc715u31/zoo/conf:src/java/org:src/java/org:.
ID?=node01
N?=2

compile:
	javac -sourcepath $(SP) -cp $(CP) src/java/org/apache/zookeeper/recipes/barrier/*.java
	cd src/java; javac -cp $(CP) *.java

run: 
	cd src/java;  java -cp $(CP) DoubleBarrierTest $(ID):13569 $(N) | grep -v INFO

clean:	
	@ find . -name '*.class' -print0 | xargs -0 rm || true
	@ rm zookeeper.out || true
	@ ~/zoo/bin/zkCli.sh -server node01:13569 rmr /b-prova
	@ ~/zoo/bin/zkCli.sh -server node01:13569 rmr /b-notas
	@ ~/zoo/bin/zkCli.sh -server node01:13569 rmr /notas
