# Java I/O perf tester

## Usage:
```
cd <the directory containing many files>
java -jar target/io-perf-tester-1.0-SNAPSHOT.jar
```

To display debug logs for files that can't be read:
```
java -jar target/io-perf-tester-1.0-SNAPSHOT.jar --debug
```


## Tips:

On Linux, filesystem access are cached into the RAM. To start from a "clean" state, use:

```
# echo 3 > /proc/sys/vm/drop_caches
```
