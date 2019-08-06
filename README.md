Running:
1. Checkout the "refactor" branch from https://github.com/bgrozev/ice4j
```
git clone https://github.com/bgrozev/ice4j
cd ice4j
git checkout refactor
```

2. Install it in the local maven repo:
```
mvn install -DskipTests
```

3. Tweak the "configuration" (port numbers defined as constants) in Jijibg.java and PortManager.java

4. Build jijibg
```
mvn install -DskipTests
```

5. Run
```
./run.sh
```

